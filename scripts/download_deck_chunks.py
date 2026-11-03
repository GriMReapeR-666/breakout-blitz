import re
import urllib.request
from concurrent.futures import ThreadPoolExecutor, as_completed
from pathlib import Path

BASE_URL = "https://setuprush.com/assets"
ROOT = Path("external_references/setuprush/assets")
HEADERS = {
    "User-Agent": "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Safari/537.36"
}

def extract_and_download():
    setup_card = ROOT / "SetupCard-PdywsOQW.js"
    if not setup_card.exists():
        print("SetupCard not found!")
        return

    text = setup_card.read_text(encoding="utf-8")
    # Extract all .js files mentioned
    # They look like: import(`./ascending-triangle-hit-2-DG45SgG0.js`)
    chunks = set(re.findall(r'([a-zA-Z0-9_\-]+-[a-zA-Z0-9_\-]{8}\.js)', text))
    print(f"Found {len(chunks)} chunk files referenced in SetupCard!")

    def download_chunk(c):
        target = ROOT / c
        if target.exists():
            return None
        url = f"{BASE_URL}/{c}"
        try:
            req = urllib.request.Request(url, headers=HEADERS)
            with urllib.request.urlopen(req, timeout=8) as resp:
                data = resp.read()
                with open(target, "wb") as f:
                    f.write(data)
                return (c, len(data))
        except Exception as e:
            return None

    count = 0
    with ThreadPoolExecutor(max_workers=16) as pool:
        futures = {pool.submit(download_chunk, c): c for c in chunks}
        for fut in as_completed(futures):
            res = fut.result()
            if res:
                c, size = res
                print(f"[OK] {c} ({size} bytes)", flush=True)
                count += 1

    print(f"\nDone! Downloaded {count} deck chunk files.")

if __name__ == "__main__":
    extract_and_download()
