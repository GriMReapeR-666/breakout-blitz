import re
import urllib.request
from concurrent.futures import ThreadPoolExecutor, as_completed
from pathlib import Path

BASE_URL = "https://setuprush.com/assets"
ROOT = Path("external_references/setuprush/assets")
HEADERS = {
    "User-Agent": "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Safari/537.36"
}

def scan_and_fetch():
    all_chunks = set()
    for p in Path("external_references/setuprush").rglob("*.js"):
        text = p.read_text(encoding="utf-8", errors="ignore")
        matches = re.findall(r'([a-zA-Z0-9_\-]+-[a-zA-Z0-9_\-]{8}\.js)', text)
        all_chunks.update(matches)

    print(f"Total unique chunk hashes found across all JS files: {len(all_chunks)}")
    missing = [c for c in all_chunks if not (ROOT / c).exists()]
    print(f"Missing chunks: {len(missing)}")

    def fetch_chunk(c):
        target = ROOT / c
        url = f"{BASE_URL}/{c}"
        try:
            req = urllib.request.Request(url, headers=HEADERS)
            with urllib.request.urlopen(req, timeout=6) as resp:
                data = resp.read()
                with open(target, "wb") as f:
                    f.write(data)
                return (c, len(data))
        except Exception:
            return None

    downloaded = 0
    with ThreadPoolExecutor(max_workers=16) as pool:
        futures = {pool.submit(fetch_chunk, c): c for c in missing}
        for fut in as_completed(futures):
            res = fut.result()
            if res:
                c, size = res
                print(f"[FETCHED] {c} ({size} bytes)", flush=True)
                downloaded += 1

    print(f"\nFinished! Downloaded {downloaded} new chunks.")

if __name__ == "__main__":
    scan_and_fetch()
