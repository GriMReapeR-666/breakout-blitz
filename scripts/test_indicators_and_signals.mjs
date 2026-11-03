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
    if (reqPath.startsWith('/api/reels')) reqPath = '/api/reels';
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
        res.writeHead(200, { 'Content-Type': contentType, 'Access-Control-Allow-Origin': '*' });
        fs.createReadStream(filePath).pipe(res);
    } else {
        res.writeHead(404, { 'Content-Type': 'text/plain' });
        res.end('Not Found');
    }
});

await new Promise(resolve => server.listen(PORT, '127.0.0.1', resolve));

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

async function evalCode(expr) {
    const res = await send('Runtime.evaluate', {
        expression: expr,
        returnByValue: true,
        awaitPromise: true
    });
    return res.result?.result?.value;
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

console.log('[STEP 1] Loading game...');
await send('Page.navigate', { url: `http://127.0.0.1:${PORT}/play.html` });
await new Promise(r => setTimeout(r, 2000));

// Step 2: Open Indicators Dialog from Home Screen
console.log('[STEP 2] Opening Indicators dialog...');
const openInd = await evalCode(`
(() => {
    // Look for button with text "Indicators" or aria-label="Indicators"
    const all = Array.from(document.querySelectorAll('*'));
    const btn = all.find(el => (el.innerText || '').trim().startsWith('Indicators') || el.getAttribute('aria-label') === 'Indicators');
    if (btn) {
        let p = btn;
        while (p && p.tagName !== 'BODY') {
            if (p.tagName === 'BUTTON' || p.onclick) {
                p.click();
                return { clicked: true, text: p.innerText.slice(0, 30) };
            }
            p = p.parentElement;
        }
        btn.click();
        return { clicked: true, text: btn.innerText.slice(0, 30) };
    }
    return { clicked: false };
})()
`);
console.log('Open Indicators:', openInd);
await new Promise(r => setTimeout(r, 1200));
await takeScreenshot('signals_01_indicators_library');

// Inspect categories and indicator items in modal
const libraryContent = await evalCode(`
(() => {
    const categories = Array.from(document.querySelectorAll('[class*="category"], [class*="tag"], [class*="tab"]')).map(el => el.innerText.trim()).filter(Boolean);
    const indicatorButtons = Array.from(document.querySelectorAll('button')).map(b => b.innerText.trim()).filter(Boolean);
    return {
        categories: Array.from(new Set(categories)).slice(0, 10),
        sampleIndicators: indicatorButtons.slice(-15)
    };
})()
`);
console.log('Library Content in Modal:', libraryContent);

// Step 3: Enable Supertrend (ST) and Market Structure (BOS) via profile / toggle
console.log('[STEP 3] Enabling Supertrend & Market Structure SMC indicators...');
const enableResult = await evalCode(`
(() => {
    const raw = localStorage.getItem('setupRush.v2');
    const p = raw ? JSON.parse(raw) : {};
    // Enable supertrend and structure
    p.ind = ['supertrend', 'structure', 'fvg'];
    localStorage.setItem('setupRush.v2', JSON.stringify(p));
    return { activeIndicators: p.ind };
})()
`);
console.log('Enabled Indicators in Profile:', enableResult);

// Step 4: Launch Practice Ride with Signals Active
console.log('[STEP 4] Launching trading ride with active indicator signals...');
// Close modal if open or click start
await evalCode(`
(() => {
    // Close modal if open
    const cancel = Array.from(document.querySelectorAll('button')).find(b => (b.innerText || '').trim() === 'Cancel' || (b.innerText || '').trim() === 'Done');
    if (cancel) cancel.click();
})()
`);
await new Promise(r => setTimeout(r, 800));

// Open Practice and Start
await evalCode(`
(() => {
    const btn = Array.from(document.querySelectorAll('*')).find(el => (el.innerText || '').trim() === 'Practice');
    if (btn) btn.click();
})()
`);
await new Promise(r => setTimeout(r, 800));

await evalCode(`
(() => {
    const start = Array.from(document.querySelectorAll('button')).find(b => (b.innerText || '').trim() === 'START');
    if (start) start.click();
})()
`);
await new Promise(r => setTimeout(r, 2200));
await takeScreenshot('signals_02_chart_with_signals');

// Inspect active signals, banner text, and pattern locking
const chartSignals = await evalCode(`
(() => {
    const lines = (document.body.innerText || '').split('\\n').map(t => t.trim()).filter(Boolean);
    const patternLock = lines.find(l => l.includes('PATTERN') || l.includes('SETUP') || l.includes('TARGET'));
    return {
        allHUDLines: lines.slice(0, 20),
        patternLock
    };
})()
`);
console.log('[ACTIVE SIGNALS ON CHART]:', JSON.stringify(chartSignals, null, 2));

process.exit(0);
