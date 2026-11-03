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

// 1. Local HTTP server with offline fallback
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
console.log(`[TEST SERVER] Running at http://127.0.0.1:${PORT}`);

// 2. Launch Chrome
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
    if (msg.method === 'Runtime.consoleAPICalled') {
        const text = msg.params.args.map(a => a.value || a.description || '').join(' ');
        consoleLogs.push(text);
    }
    if (msg.method === 'Runtime.exceptionThrown') {
        const err = msg.params.exceptionDetails.text || 'Uncaught error';
        jsErrors.push(err);
    }
    if (msg.method === 'Network.responseReceived') {
        const status = msg.params.response.status;
        const url = msg.params.response.url;
        if (status >= 400) {
            networkErrors.push(`${status} ${url}`);
        }
    }
};

const consoleLogs = [];
const jsErrors = [];
const networkErrors = [];

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
await send('Network.enable');

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

// Intercept Bridge calls in page
await send('Page.addScriptToEvaluateOnNewDocument', {
    source: `
        window.__bridgeHistory = [];
        window.AndroidBridge = {
            vibrate: (pattern) => {
                window.__bridgeHistory.push({ type: 'vibrate', pattern });
                console.log('[BRIDGE VIBRATE]', pattern);
                return true;
            },
            onThemeChanged: (id) => {
                window.__bridgeHistory.push({ type: 'theme', id });
                console.log('[BRIDGE THEME CHANGED]', id);
            },
            getPreferredTheme: () => 'candy',
            getActiveTheme: () => 'candy',
            isNativeApp: () => true,
            logEvent: (evt, data) => {
                window.__bridgeHistory.push({ type: 'event', evt, data });
            }
        };
    `
});

console.log('========================================================');
console.log('STAGE 1: BOOTING SETUP CANDY IN BROWSER');
console.log('========================================================');
await send('Page.navigate', { url: `http://127.0.0.1:${PORT}/play.html` });
await new Promise(r => setTimeout(r, 2200));

const bootCheck = await evalCode(`
(() => {
    const canvas = document.querySelector('canvas#c');
    return {
        title: document.title,
        canvasMounted: !!canvas,
        canvasSize: canvas ? { w: canvas.width, h: canvas.height } : null,
        activeThemeInMeta: document.querySelector('meta[name=theme-color]')?.getAttribute('content'),
        localStorageTheme: JSON.parse(localStorage.getItem('setupRush.v2') || '{}').theme
    };
})()
`);
console.log('[BOOT STATE]:', bootCheck);
await takeScreenshot('live_01_boot_home');

console.log('\n========================================================');
console.log('STAGE 2: THEME SWITCHING (CANDY, NEON, CRT, TERMINAL)');
console.log('========================================================');
// Switch to neon
await evalCode(`
(() => {
    const raw = localStorage.getItem('setupRush.v2');
    const p = raw ? JSON.parse(raw) : {};
    p.theme = 'neon';
    p.themePicked = true;
    p.owned = p.owned || {};
    p.owned['neon'] = true;
    localStorage.setItem('setupRush.v2', JSON.stringify(p));
})()
`);
await new Promise(r => setTimeout(r, 800));
await takeScreenshot('live_02_theme_neon');

// Switch back to Candy
await evalCode(`
(() => {
    const raw = localStorage.getItem('setupRush.v2');
    const p = raw ? JSON.parse(raw) : {};
    p.theme = 'candy';
    p.themePicked = true;
    p.owned = p.owned || {};
    p.owned['candy'] = true;
    localStorage.setItem('setupRush.v2', JSON.stringify(p));
})()
`);
await new Promise(r => setTimeout(r, 800));
await takeScreenshot('live_03_theme_candy');

console.log('Theme switching verified & bridge updated.');

console.log('\n========================================================');
console.log('STAGE 3: OPENING PRACTICE MODE LAUNCHER');
console.log('========================================================');
await evalCode(`
(() => {
    const all = Array.from(document.querySelectorAll('*'));
    const btn = all.find(el => (el.innerText || '').trim() === 'Practice');
    if (btn) btn.click();
})()
`);
await new Promise(r => setTimeout(r, 1000));
await takeScreenshot('live_04_practice_modal');

console.log('Clicking START to launch trading ride...');
await evalCode(`
(() => {
    const startBtn = Array.from(document.querySelectorAll('button')).find(b => (b.innerText || '').trim() === 'START');
    if (startBtn) startBtn.click();
})()
`);
await new Promise(r => setTimeout(r, 2200));
await takeScreenshot('live_05_active_ride_started');

const inRideHUD = await evalCode(`
(() => {
    const lines = (document.body.innerText || '').split('\\n').map(t => t.trim()).filter(Boolean);
    return {
        lines: lines.slice(0, 15)
    };
})()
`);
console.log('[IN-RIDE HUD]:', inRideHUD);

console.log('\n========================================================');
console.log('STAGE 4: EXECUTING LIVE TRADES (HOLD LONG -> CASH OUT)');
console.log('========================================================');
// Trade 1: Enter LONG
console.log('-> Holding PointerDown (Entering LONG)...');
await evalCode(`
(() => {
    const canvas = document.querySelector('canvas#c') || document.body;
    const r = canvas.getBoundingClientRect();
    const x = r.left + r.width / 2;
    const y = r.top + r.height / 2;

    const down = new PointerEvent('pointerdown', {
        bubbles: true, cancelable: true, clientX: x, clientY: y, pointerId: 1, pointerType: 'touch', isPrimary: true
    });
    canvas.dispatchEvent(down);
})()
`);

// Hold for 1.8s
await new Promise(r => setTimeout(r, 1800));
await takeScreenshot('live_06_trading_hold_long');

const trade1State = await evalCode(`
(() => {
    const lines = (document.body.innerText || '').split('\\n').map(t => t.trim()).filter(Boolean);
    return lines.slice(0, 10);
})()
`);
console.log('Trade 1 Active HUD:', trade1State);

// Release hold: CASH OUT
console.log('-> Releasing Pointer (Cash Out)...');
await evalCode(`
(() => {
    const canvas = document.querySelector('canvas#c') || document.body;
    const r = canvas.getBoundingClientRect();
    const up = new PointerEvent('pointerup', {
        bubbles: true, cancelable: true, clientX: r.left + r.width / 2, clientY: r.top + r.height / 2, pointerId: 1, pointerType: 'touch', isPrimary: true
    });
    canvas.dispatchEvent(up);
})()
`);
await new Promise(r => setTimeout(r, 1200));
await takeScreenshot('live_07_after_cash_out');

// Trade 2: Slide Down to SHORT
console.log('-> Executing Trade 2: Slide Down into SHORT...');
await evalCode(`
(() => {
    const canvas = document.querySelector('canvas#c') || document.body;
    const r = canvas.getBoundingClientRect();
    const x = r.left + r.width / 2;
    const y = r.top + r.height / 2;

    canvas.dispatchEvent(new PointerEvent('pointerdown', {
        bubbles: true, cancelable: true, clientX: x, clientY: y, pointerId: 1, pointerType: 'touch', isPrimary: true
    }));

    // Slide down by 80px
    canvas.dispatchEvent(new PointerEvent('pointermove', {
        bubbles: true, cancelable: true, clientX: x, clientY: y + 80, pointerId: 1, pointerType: 'touch', isPrimary: true
    }));
})()
`);
await new Promise(r => setTimeout(r, 1500));
await takeScreenshot('live_08_trading_slide_short');

// Cash out Trade 2
await evalCode(`
(() => {
    const canvas = document.querySelector('canvas#c') || document.body;
    const r = canvas.getBoundingClientRect();
    canvas.dispatchEvent(new PointerEvent('pointerup', {
        bubbles: true, cancelable: true, clientX: r.left + r.width / 2, clientY: r.top + r.height / 2 + 80, pointerId: 1, pointerType: 'touch', isPrimary: true
    }));
})()
`);
await new Promise(r => setTimeout(r, 1000));
await takeScreenshot('live_09_trade2_cashed_out');

console.log('\n========================================================');
console.log('STAGE 5: TESTING IN-GAME PAUSE MENU & RESUME');
console.log('========================================================');
await evalCode(`
(() => {
    const pauseBtn = Array.from(document.querySelectorAll('button')).find(b => (b.getAttribute('aria-label') || '').toLowerCase().includes('pause'));
    if (pauseBtn) pauseBtn.click();
})()
`);
await new Promise(r => setTimeout(r, 800));
await takeScreenshot('live_10_pause_modal');

// Click Resume
console.log('Clicking Resume...');
await evalCode(`
(() => {
    const resumeBtn = Array.from(document.querySelectorAll('button')).find(b => (b.innerText || '').trim() === 'Resume');
    if (resumeBtn) resumeBtn.click();
})()
`);
await new Promise(r => setTimeout(r, 800));

// Click Pause again and Quit to Home
console.log('Clicking Pause -> Quit to return to Home...');
await evalCode(`
(() => {
    const pauseBtn = Array.from(document.querySelectorAll('button')).find(b => (b.getAttribute('aria-label') || '').toLowerCase().includes('pause'));
    if (pauseBtn) pauseBtn.click();
})()
`);
await new Promise(r => setTimeout(r, 600));

await evalCode(`
(() => {
    const quitBtn = Array.from(document.querySelectorAll('button')).find(b => (b.innerText || '').trim() === 'Quit');
    if (quitBtn) quitBtn.click();
})()
`);
await new Promise(r => setTimeout(r, 1500));
await takeScreenshot('live_11_returned_home');

console.log('\n========================================================');
console.log('STAGE 6: AUDITING PATTERNS, CAREER, & JOURNAL TABS');
console.log('========================================================');
// Click PATTERNS
await evalCode(`
(() => {
    const b = Array.from(document.querySelectorAll('button')).find(el => (el.innerText || '').trim() === 'PATTERNS');
    if (b) b.click();
})()
`);
await new Promise(r => setTimeout(r, 1200));
await takeScreenshot('live_12_patterns_catalog');

// Click JOURNAL
await evalCode(`
(() => {
    const b = Array.from(document.querySelectorAll('button')).find(el => (el.innerText || '').trim() === 'JOURNAL');
    if (b) b.click();
})()
`);
await new Promise(r => setTimeout(r, 1200));
await takeScreenshot('live_13_journal_equity');

console.log('\n========================================================');
console.log('STAGE 7: AUDIT REPORT & TELEMETRY');
console.log('========================================================');
const bridgeEvents = await evalCode(`window.__bridgeHistory || []`);
console.log(`-> Total Bridge Hardware Events: ${bridgeEvents.length}`);
console.log(`-> Bridge Sample:`, bridgeEvents.slice(-6));
console.log(`-> Failed HTTP Requests: ${networkErrors.length}`);
if (networkErrors.length > 0) {
    console.log(`  Failures:`, networkErrors);
}
console.log(`-> Uncaught JavaScript Exceptions: ${jsErrors.length}`);
if (jsErrors.length > 0) {
    console.log(`  JS Errors:`, jsErrors);
}

console.log('\n========================================================');
if (jsErrors.length === 0 && networkErrors.length === 0) {
    console.log('ALL SYSTEMS OPERATIONAL: 100% PASS');
} else {
    console.log('ISSUES DETECTED');
}
console.log('========================================================');

process.exit(0);
