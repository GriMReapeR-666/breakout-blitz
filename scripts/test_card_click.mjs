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

await send('Page.navigate', { url: `http://127.0.0.1:${PORT}/play.html` });
await new Promise(r => setTimeout(r, 2000));

// Find mode cards (Sniper, Rush, Blowup, Pro, Daily)
const cards = await evalCode(`
(() => {
    // Find all clickable elements that look like cards
    const elements = Array.from(document.querySelectorAll('*')).filter(el => {
        const text = el.innerText || '';
        return (text.includes('SNIPER') || text.includes('RUSH') || text.includes('BLOWUP') || text.includes('DAILY'))
            && el.children.length > 0 && el.children.length < 8 && el.offsetWidth > 100 && el.offsetHeight > 50;
    }).map(el => ({
        tag: el.tagName,
        text: el.innerText.trim().replace(/\\s+/g, ' '),
        rect: { x: el.offsetLeft, y: el.offsetTop, w: el.offsetWidth, h: el.offsetHeight },
        classes: el.className
    }));
    return elements;
})()
`);
console.log('[CARDS FOUND]:', cards);

// Click the Daily or Rush card
const clickCard = await evalCode(`
(() => {
    // Find card with DAILY or RUSH
    const all = Array.from(document.querySelectorAll('*'));
    for (const el of all) {
        const t = (el.innerText || '').toUpperCase();
        if (t.includes('DAILY #') || t.includes('RUSH') || t.includes('SNIPER')) {
            // Find clickable parent or self
            let p = el;
            while (p && p.tagName !== 'BODY') {
                if (p.tagName === 'BUTTON' || p.onclick || p.getAttribute('role') === 'button' || p.className.includes('card') || p.className.includes('cursor-pointer')) {
                    p.click();
                    return { clicked: true, text: p.innerText.slice(0, 40) };
                }
                p = p.parentElement;
            }
        }
    }
    return { clicked: false };
})()
`);
console.log('[CLICK CARD RESULT]:', clickCard);

await new Promise(r => setTimeout(r, 1500));
await takeScreenshot('03_after_card_click');

// Check if run started or if modal opened
const afterClick = await evalCode(`
(() => {
    return {
        buttons: Array.from(document.querySelectorAll('button')).map(b => b.innerText.trim()).filter(Boolean),
        hasCanvas: !!document.querySelector('canvas#c'),
        headings: Array.from(document.querySelectorAll('h1, h2, h3, h4, h5')).map(h => h.innerText.trim())
    };
})()
`);
console.log('[AFTER CLICK STATE]:', afterClick);

process.exit(0);
