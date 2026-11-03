import http from 'http';
import fs from 'fs';
import path from 'path';
import { spawn } from 'child_process';

const PORT = 8089;
const CDP_PORT = 9225;
const ROOT = path.resolve('app/src/main/assets/setuprush');
const CHROME_PATH = 'C:\\Program Files\\Google\\Chrome\\Application\\chrome.exe';

const MIME_TYPES = {
    '.html': 'text/html',
    '.js': 'application/javascript',
    '.css': 'text/css',
    '.json': 'application/json',
    '.png': 'image/png',
    '.jpg': 'image/jpeg',
    '.jpeg': 'image/jpeg',
    '.svg': 'image/svg+xml',
    '.woff2': 'font/woff2',
    '.webmanifest': 'application/manifest+json'
};

const server = http.createServer((req, res) => {
    let reqUrl = req.url;
    let reqPath = decodeURI(reqUrl.split('?')[0]);
    if (reqPath === '/' || reqPath === '') reqPath = '/play.html';
    
    if (reqPath.startsWith('/api/markets')) reqPath = '/api/markets';
    if (reqPath.startsWith('/api/rush/board')) reqPath = '/api/rush/board';
    if (reqPath === '/history/sets.json') reqPath = '/history/sets.json';
    
    let filePath = path.join(ROOT, reqPath.replace(/^\//, ''));
    if (!fs.existsSync(filePath) && fs.existsSync(filePath + '.json')) {
        filePath = filePath + '.json';
    }
    if (!fs.existsSync(filePath) && reqPath.startsWith('/history/') && reqPath.endsWith('.json')) {
        filePath = path.join(ROOT, 'history/btcusd1h-7.json');
    }
    
    if (fs.existsSync(filePath) && fs.statSync(filePath).isFile()) {
        const ext = path.extname(filePath).toLowerCase();
        const contentType = MIME_TYPES[ext] || (reqPath.startsWith('/api/') ? 'application/json' : 'application/octet-stream');
        res.writeHead(200, {
            'Content-Type': contentType,
            'Access-Control-Allow-Origin': '*'
        });
        fs.createReadStream(filePath).pipe(res);
    } else {
        res.writeHead(404, { 'Content-Type': 'text/plain' });
        res.end('Not Found');
    }
});

await new Promise(resolve => server.listen(PORT, '127.0.0.1', resolve));
console.log(`[TEST SERVER] Running at http://127.0.0.1:${PORT}`);

const chrome = spawn(CHROME_PATH, [
    '--headless=new',
    `--remote-debugging-port=${CDP_PORT}`,
    '--window-size=412,915',
    '--disable-gpu',
    '--no-sandbox',
    '--disable-dev-shm-usage',
    '--user-data-dir=' + path.resolve('.chrome-test-profile'),
    'about:blank'
], { stdio: 'ignore' });

process.on('exit', () => {
    try { chrome.kill(); } catch (e) {}
    try { server.close(); } catch (e) {}
});

async function getWsUrl() {
    for (let i = 0; i < 30; i++) {
        try {
            const res = await fetch(`http://127.0.0.1:${CDP_PORT}/json`);
            const targets = await res.json();
            const page = targets.find(t => t.type === 'page');
            if (page && page.webSocketDebuggerUrl) return page.webSocketDebuggerUrl;
        } catch (e) {}
        await new Promise(r => setTimeout(r, 200));
    }
    throw new Error('CDP endpoint not ready');
}

const wsUrl = await getWsUrl();
const ws = new WebSocket(wsUrl);

let msgId = 1;
const pending = new Map();

ws.onmessage = (event) => {
    const msg = JSON.parse(event.data);
    if (msg.id && pending.has(msg.id)) {
        pending.get(msg.id)(msg);
        pending.delete(msg.id);
    }
};

function send(method, params = {}) {
    return new Promise((resolve) => {
        const id = msgId++;
        pending.set(id, resolve);
        ws.send(JSON.stringify({ id, method, params }));
    });
}

await new Promise(r => ws.onopen = r);
await send('Page.enable');
await send('Runtime.enable');
await send('DOM.enable');

async function evalCode(expr) {
    const res = await send('Runtime.evaluate', {
        expression: expr,
        returnByValue: true,
        awaitPromise: true
    });
    const remoteObj = res.result?.result;
    if (res.result?.exceptionDetails || remoteObj?.subtype === 'error') {
        console.error('Eval error:', res.result?.exceptionDetails || remoteObj);
    }
    return remoteObj?.value;
}

async function takeScreenshot(name) {
    const res = await send('Page.captureScreenshot', { format: 'png' });
    if (res.result?.data) {
        const outDir = path.resolve('test_artifacts');
        fs.mkdirSync(outDir, { recursive: true });
        const filePath = path.join(outDir, `${name}.png`);
        fs.writeFileSync(filePath, Buffer.from(res.result.data, 'base64'));
        console.log(`[SCREENSHOT] Saved: ${name}.png`);
        return filePath;
    }
}

console.log('[STAGE 1] Loading game in Mobile Portrait (412x915)...');
await send('Page.navigate', { url: `http://127.0.0.1:${PORT}/play.html` });
await new Promise(r => setTimeout(r, 2000));
await takeScreenshot('01_home_screen');

// Inspect Modes and Clickable cards
const screenInfo = await evalCode(`
(() => {
    const allButtons = Array.from(document.querySelectorAll('button, [role="button"], a')).map(el => ({
        tag: el.tagName,
        text: el.innerText.trim().replace(/\\s+/g, ' '),
        aria: el.getAttribute('aria-label'),
        class: el.className
    }));
    return allButtons;
})()
`);
console.log('[HOME BUTTONS & CARDS]:', screenInfo.filter(b => b.text.length > 0 && b.text.length < 50));

// Find and click the main PLAY/START button for the ride
const launchResult = await evalCode(`
(() => {
    // Look for Daily, Rush, or Play buttons
    const candidates = Array.from(document.querySelectorAll('button, [role="button"]'));
    // Try finding "PLAY", "RIDE", "START", or a card with "DAILY"
    const playBtn = candidates.find(b => {
        const t = (b.innerText || '').toUpperCase();
        return t.includes('PLAY') || t.includes('RIDE') || t.includes('DAILY') || t.includes('START');
    });
    
    if (playBtn) {
        playBtn.click();
        return { clicked: true, text: playBtn.innerText.trim() };
    }
    return { clicked: false };
})()
`);
console.log('[LAUNCH RESULT]:', launchResult);
await new Promise(r => setTimeout(r, 1500));
await takeScreenshot('02_after_click_play');

// Check what stage is active now
const currentStage = await evalCode(`
(() => {
    return {
        canvasMounted: !!document.querySelector('canvas#c'),
        hudElements: Array.from(document.querySelectorAll('[class*="hud"], [class*="pnl"], [class*="score"], [class*="price"]')).map(el => el.innerText.trim()),
        activeText: document.body.innerText.split('\\n').filter(t => t.trim().length > 0).slice(0, 15)
    };
})()
`);
console.log('[CURRENT STAGE STATE]:', currentStage);

// Let's inspect the game engine state directly from window
const engineStatus = await evalCode(`
(() => {
    return {
        hasSetupRush: typeof window.setupRush !== 'undefined',
        hasEngine: typeof window.Engine !== 'undefined' || typeof window.engine !== 'undefined',
        hasCandleVision: typeof window.CandleVision !== 'undefined' || typeof window.cv !== 'undefined',
        localStorageProfile: JSON.parse(localStorage.getItem('setupRush.v2') || '{}')
    };
})()
`);
console.log('[ENGINE STATUS]:', engineStatus);

process.exit(0);
