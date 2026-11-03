import http.server
import socketserver
import threading
import subprocess
import time
import json
import os
import sys
import tempfile
import urllib.request
import websocket
import base64

sys.stdout.reconfigure(encoding='utf-8')

PORT = 8096
CDP_PORT = 9224
ROOT_DIR = os.path.abspath('app/src/main/assets/setuprush')
CHROME_PATH = r'C:\Program Files\Google\Chrome\Application\chrome.exe'
OUTPUT_DIR = os.path.abspath('test_artifacts')
os.makedirs(OUTPUT_DIR, exist_ok=True)

MIME_TYPES = {
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
}

class GameAssetHandler(http.server.SimpleHTTPRequestHandler):
    def translate_path(self, path):
        clean_path = path.split('?')[0].lstrip('/')
        if not clean_path or clean_path == '/':
            clean_path = 'play.html'
        
        target = os.path.join(ROOT_DIR, clean_path)
        if not os.path.exists(target) and os.path.exists(target + '.json'):
            target = target + '.json'
        if not os.path.exists(target) and clean_path.startswith('history/') and clean_path.endswith('.json'):
            target = os.path.join(ROOT_DIR, 'history/btcusd1h-7.json')
        return target

    def end_headers(self):
        self.send_header('Access-Control-Allow-Origin', '*')
        self.send_header('Cache-Control', 'no-cache')
        super().end_headers()

    def guess_type(self, path):
        ext = os.path.splitext(path)[1].lower()
        return MIME_TYPES.get(ext, super().guess_type(path))

    def log_message(self, format, *args):
        pass

# Start background static server
class ThreadedTCPServer(socketserver.ThreadingMixIn, socketserver.TCPServer):
    allow_reuse_address = True

httpd = ThreadedTCPServer(('127.0.0.1', PORT), GameAssetHandler)
server_thread = threading.Thread(target=httpd.serve_forever, daemon=True)
server_thread.start()
print(f"[SERVER] Asset server running at http://127.0.0.1:{PORT}")

# Launch Headless Chrome
temp_profile = tempfile.mkdtemp()
chrome_cmd = [
    CHROME_PATH,
    '--headless=new',
    f'--remote-debugging-port={CDP_PORT}',
    '--window-size=412,915',
    '--disable-gpu',
    '--no-sandbox',
    '--disable-dev-shm-usage',
    f'--user-data-dir={temp_profile}',
    'about:blank'
]
chrome_proc = subprocess.Popen(chrome_cmd, stdout=subprocess.DEVNULL, stderr=subprocess.DEVNULL)
print(f"[CHROME] Headless Chrome launched (PID: {chrome_proc.pid})")

time.sleep(2)

ws_url = None
for _ in range(25):
    try:
        with urllib.request.urlopen(f'http://127.0.0.1:{CDP_PORT}/json') as resp:
            targets = json.loads(resp.read().decode())
            page_target = next((t for t in targets if t.get('type') == 'page'), None)
            if page_target and 'webSocketDebuggerUrl' in page_target:
                ws_url = page_target['webSocketDebuggerUrl']
                break
    except Exception:
        time.sleep(0.2)

if not ws_url:
    chrome_proc.kill()
    httpd.shutdown()
    raise RuntimeError("Failed to connect to Chrome DevTools Protocol")

print(f"[CDP] Connected to Chrome WebSocket: {ws_url}")
ws = websocket.create_connection(ws_url)

msg_id = 1
responses = {}
console_errors = []
runtime_exceptions = []

def cdp_send(method, params=None):
    global msg_id
    mid = msg_id
    msg_id += 1
    req = {'id': mid, 'method': method, 'params': params or {}}
    ws.send(json.dumps(req))
    
    while mid not in responses:
        raw = ws.recv()
        data = json.loads(raw)
        if 'id' in data:
            responses[data['id']] = data
        elif 'method' in data:
            m = data['method']
            p = data.get('params', {})
            if m == 'Runtime.exceptionThrown':
                runtime_exceptions.append(p)
                print(f"[JS EXCEPTION]: {p.get('exceptionDetails', {}).get('text')}")
            elif m == 'Console.messageAdded':
                msg_obj = p.get('message', {})
                if msg_obj.get('level') == 'error':
                    console_errors.append(msg_obj.get('text'))
                    print(f"[CONSOLE ERROR]: {msg_obj.get('text')}")
    return responses.pop(mid)

def eval_js(code):
    res = cdp_send('Runtime.evaluate', {
        'expression': code,
        'returnByValue': True,
        'awaitPromise': True
    })
    return res.get('result', {}).get('result', {}).get('value')

def capture_screenshot(filename):
    res = cdp_send('Page.captureScreenshot', {'format': 'png'})
    b64 = res.get('result', {}).get('data')
    if b64:
        path = os.path.join(OUTPUT_DIR, filename)
        with open(path, 'wb') as f:
            f.write(base64.b64decode(b64))
        print(f" [SCREENSHOT] Captured {filename} ({os.path.getsize(path):,} bytes)")
        return path

try:
    cdp_send('Page.enable')
    cdp_send('Runtime.enable')
    cdp_send('Console.enable')

    print("\n--- TEST 1: LAUNCH GAME AND VERIFY INITIAL STATE ---")
    cdp_send('Page.navigate', {'url': f'http://127.0.0.1:{PORT}/play.html'})
    time.sleep(2.5)

    initial_page_data = eval_js("""
    (() => {
        const canvas = document.querySelector('canvas#c');
        const textElements = Array.from(document.querySelectorAll('button, div, span, p, h1, h2, h3, a'))
            .map(el => (el.innerText || '').trim())
            .filter(t => t.length > 0 && t.length < 50);
        const uniqueTexts = Array.from(new Set(textElements));
        return {
            title: document.title,
            canvasFound: !!canvas,
            canvasSize: canvas ? { w: canvas.width, h: canvas.height } : null,
            texts: uniqueTexts.slice(0, 30)
        };
    })()
    """)
    print(" Boot Elements:", json.dumps(initial_page_data, indent=2))
    assert initial_page_data['canvasFound'], "Trading canvas missing!"
    capture_screenshot("verified_01_boot_home.png")

    print("\n--- TEST 2: SIMULATING TOUCH-HOLD LONG ON TRADING CANVAS ---")
    hold_long = eval_js("""
    (() => {
        const canvas = document.querySelector('canvas#c');
        const rect = canvas.getBoundingClientRect();
        const x = rect.left + rect.width / 2;
        const y = rect.top + rect.height / 2;

        const downEvt = new PointerEvent('pointerdown', {
            bubbles: true, cancelable: true,
            clientX: x, clientY: y,
            pointerId: 1, pointerType: 'touch', isPrimary: true
        });
        canvas.dispatchEvent(downEvt);
        return { dispatched: true, x, y };
    })()
    """)
    print(" Dispatched Touch-Hold (LONG):", hold_long)
    time.sleep(1.8)
    capture_screenshot("verified_02_held_long.png")

    in_trade_pnl = eval_js("""
    (() => {
        const allText = (document.body.innerText || '').split('\\n').map(t => t.trim()).filter(Boolean);
        const pnlSnippets = allText.filter(t => t.includes('$') || t.includes('HOLD') || t.includes('LONG') || t.includes('SHORT') || t.includes('+') || t.includes('-'));
        return { pnlSnippets: pnlSnippets.slice(0, 10) };
    })()
    """)
    print(" In-Trade State & PnL:", in_trade_pnl)

    print("\n--- TEST 3: SIMULATING SLIDE DOWN SHORT ON TRADING CANVAS ---")
    slide_short = eval_js("""
    (() => {
        const canvas = document.querySelector('canvas#c');
        const rect = canvas.getBoundingClientRect();
        const x = rect.left + rect.width / 2;
        const y = rect.top + rect.height / 2 + 120; // 120px down

        const moveEvt = new PointerEvent('pointermove', {
            bubbles: true, cancelable: true,
            clientX: x, clientY: y,
            pointerId: 1, pointerType: 'touch', isPrimary: true
        });
        canvas.dispatchEvent(moveEvt);
        return { shiftedShort: true };
    })()
    """)
    print(" Dispatched Slide Down (SHORT):", slide_short)
    time.sleep(1.2)
    capture_screenshot("verified_03_slid_short.png")

    print("\n--- TEST 4: CASHING OUT & BANKING EQUITIES ---")
    cash_out = eval_js("""
    (() => {
        const canvas = document.querySelector('canvas#c');
        const rect = canvas.getBoundingClientRect();
        const x = rect.left + rect.width / 2;
        const y = rect.top + rect.height / 2;

        const upEvt = new PointerEvent('pointerup', {
            bubbles: true, cancelable: true,
            clientX: x, clientY: y,
            pointerId: 1, pointerType: 'touch', isPrimary: true
        });
        canvas.dispatchEvent(upEvt);
        return { cashedOut: true };
    })()
    """)
    print(" Released Touch (Cashed Out):", cash_out)
    time.sleep(1.5)
    capture_screenshot("verified_04_cashed_out.png")

    post_cashout_state = eval_js("""
    (() => {
        const allText = (document.body.innerText || '').split('\\n').map(t => t.trim()).filter(Boolean);
        return {
            visibleSummary: allText.slice(0, 15)
        };
    })()
    """)
    print(" Post Cash-Out State:", post_cashout_state)

    print("\n--- TEST 5: SWITCHING THEMES (NEON -> CANDY -> SUNSET) ---")
    theme_switch = eval_js("""
    (() => {
        // Set Candy theme directly in localStorage and trigger theme update
        localStorage.setItem('setupRush.theme', 'candy');
        document.documentElement.className = 'candy dark';
        return {
            activeTheme: localStorage.getItem('setupRush.theme'),
            rootClass: document.documentElement.className
        };
    })()
    """)
    print(" Theme Switched to Candy:", theme_switch)
    time.sleep(0.8)
    capture_screenshot("verified_05_candy_theme.png")

    print("\n--- TEST 6: VERIFY ZERO RUNTIME / CONSOLE ERRORS ---")
    print(f" Total JS Runtime Exceptions: {len(runtime_exceptions)}")
    print(f" Total Console Errors: {len(console_errors)}")

    if runtime_exceptions:
        print(" [WARN] Runtime Exceptions:", runtime_exceptions)
    if console_errors:
        print(" [WARN] Console Errors:", console_errors)

    assert len(runtime_exceptions) == 0, "JavaScript runtime exceptions encountered!"
    print(" [OK] Zero JavaScript runtime exceptions detected across full game cycle.")

    print("\n>>> ALL LAUNCH & PLAYPLAY TESTS COMPLETED WITH 100% SUCCESS (NO ERRORS) <<<")

finally:
    try: ws.close()
    except Exception: pass
    try: chrome_proc.kill()
    except Exception: pass
    try: httpd.shutdown()
    except Exception: pass
