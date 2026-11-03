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

console.log('[STEP 1] Navigating to game...');
await send('Page.navigate', { url: `http://127.0.0.1:${PORT}/play.html` });
await new Promise(r => setTimeout(r, 2000));

console.log('[STEP 2] Opening Practice modal...');
await evalCode(`
(() => {
    const all = Array.from(document.querySelectorAll('*'));
    const btn = all.find(el => (el.innerText || '').trim() === 'Practice');
    if (btn) btn.click();
})()
`);
await new Promise(r => setTimeout(r, 1000));
await takeScreenshot('loop_01_practice_modal');

console.log('[STEP 3] Clicking START to launch live trading ride...');
const startClick = await evalCode(`
(() => {
    const all = Array.from(document.querySelectorAll('button'));
    const startBtn = all.find(b => (b.innerText || '').trim() === 'START');
    if (startBtn) {
        startBtn.click();
        return { started: true };
    }
    return { started: false };
})()
`);
console.log('[START CLICK RESULT]:', startClick);

// Wait for countdown / stage launch (e.g. 3, 2, 1, or immediate start)
await new Promise(r => setTimeout(r, 2500));
await takeScreenshot('loop_02_ride_active');

// Inspect active stage HUD and state
const rideState = await evalCode(`
(() => {
    const canvas = document.querySelector('canvas#c');
    const hud = Array.from(document.querySelectorAll('*')).filter(el => {
        const c = el.className || '';
        return (c.includes('hud') || c.includes('score') || c.includes('pnl') || c.includes('timer') || c.includes('hold'));
    }).map(el => el.innerText.trim()).filter(Boolean);

    const fullText = (document.body.innerText || '').split('\\n').map(t => t.trim()).filter(Boolean);

    return {
        canvasFound: !!canvas,
        canvasSize: canvas ? { w: canvas.width, h: canvas.height } : null,
        hudSnippets: hud.slice(0, 10),
        fullTextVisible: fullText.slice(0, 25)
    };
})()
`);
console.log('[RIDE STATE]:', JSON.stringify(rideState, null, 2));

console.log('[STEP 4] Simulating HOLD LONG (PointerDown on trading canvas)...');
const holdResult = await evalCode(`
(() => {
    const canvas = document.querySelector('canvas#c') || document.body;
    const rect = canvas.getBoundingClientRect();
    const x = rect.left + rect.width / 2;
    const y = rect.top + rect.height / 2;

    // Dispatch PointerDown & TouchStart
    const downEvt = new PointerEvent('pointerdown', {
        bubbles: true,
        cancelable: true,
        clientX: x,
        clientY: y,
        pointerId: 1,
        pointerType: 'touch',
        isPrimary: true
    });
    canvas.dispatchEvent(downEvt);

    return { dispatched: true, x, y };
})()
`);
console.log('[HOLD RESULT]:', holdResult);

// Hold for 1.5 seconds while candles stream
await new Promise(r => setTimeout(r, 1500));
await takeScreenshot('loop_03_position_held_long');

// Inspect in-trade HUD
const inTradeState = await evalCode(`
(() => {
    const textLines = (document.body.innerText || '').split('\\n').map(t => t.trim()).filter(Boolean);
    return {
        activeText: textLines.slice(0, 20)
    };
})()
`);
console.log('[IN-TRADE STATE]:', inTradeState);

console.log('[STEP 5] Simulating SLIDE SHORT (PointerMove downward)...');
await evalCode(`
(() => {
    const canvas = document.querySelector('canvas#c') || document.body;
    const rect = canvas.getBoundingClientRect();
    const x = rect.left + rect.width / 2;
    const y = rect.top + rect.height / 2 + 100; // Slide down 100px

    const moveEvt = new PointerEvent('pointermove', {
        bubbles: true,
        cancelable: true,
        clientX: x,
        clientY: y,
        pointerId: 1,
        pointerType: 'touch',
        isPrimary: true
    });
    canvas.dispatchEvent(moveEvt);
})()
`);
await new Promise(r => setTimeout(r, 1000));
await takeScreenshot('loop_04_position_slid_short');

console.log('[STEP 6] Simulating CASH OUT (PointerUp - release hold)...');
await evalCode(`
(() => {
    const canvas = document.querySelector('canvas#c') || document.body;
    const rect = canvas.getBoundingClientRect();
    const x = rect.left + rect.width / 2;
    const y = rect.top + rect.height / 2;

    const upEvt = new PointerEvent('pointerup', {
        bubbles: true,
        cancelable: true,
        clientX: x,
        clientY: y,
        pointerId: 1,
        pointerType: 'touch',
        isPrimary: true
    });
    canvas.dispatchEvent(upEvt);
})()
`);
await new Promise(r => setTimeout(r, 1200));
await takeScreenshot('loop_05_after_cash_out');

const afterCashOutState = await evalCode(`
(() => {
    const textLines = (document.body.innerText || '').split('\\n').map(t => t.trim()).filter(Boolean);
    return {
        activeText: textLines.slice(0, 20)
    };
})()
`);
console.log('[AFTER CASH OUT STATE]:', afterCashOutState);

process.exit(0);
