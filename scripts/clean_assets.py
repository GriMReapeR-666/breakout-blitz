import os
import re
import urllib.parse
import urllib.request
from pathlib import Path

BASE_URL = "https://setuprush.com"
ROOT = Path("external_references/setuprush")

HEADERS = {
    "User-Agent": "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Safari/537.36"
}

def find_asset_references():
    candidates = set()
    # Find all strings in html, js, css
    for p in ROOT.rglob("*.*"):
        if p.suffix in [".html", ".js", ".css", ".json", ".webmanifest"]:
            try:
                text = p.read_text(encoding="utf-8", errors="ignore")
                
                # CSS url(...)
                for m in re.finditer(r'''url\(\s*["']?([^"')]+)["']?\s*\)''', text):
                    raw = m.group(1).strip()
                    if not raw.startswith("data:") and not raw.startswith("#"):
                        candidates.add(raw)
                        
                # Look for ./assets/... or /assets/... or assets/...
                for m in re.finditer(r'''["'`]((?:\./|/)?assets/[a-zA-Z0-9_\-\.]+\.[a-zA-Z0-9]{2,5})["'`]''', text):
                    candidates.add(m.group(1))

                # Look for vendor/...
                for m in re.finditer(r'''["'`]((?:vendor/)?[a-zA-Z0-9_\-\.]+\.(?:js|css|json|png|svg|jpg))["'`]''', text):
                    candidates.add(m.group(1))

                # Look for image / font / media filenames:
                for m in re.finditer(r'''["'`]((?:[a-zA-Z0-9_\-\/]+)\.(?:png|jpg|jpeg|svg|webp|gif|woff2?|ttf|otf|mp3|wav|ogg|json|webmanifest))["'`]''', text):
                    candidates.add(m.group(1))
            except Exception as e:
                pass
    return candidates

def download_missing(candidates):
    print(f"Total candidates extracted: {len(candidates)}")
    downloaded = 0
    failed = 0
    skipped = 0

    for item in sorted(candidates):
        # Clean item
        item = item.strip().split("?")[0].split("#")[0]
        if not item or item.startswith("data:") or item.startswith("http://") or " " in item:
            continue
        if item.startswith("https://"):
            if not item.startswith(BASE_URL):
                continue
            item = item[len(BASE_URL):]

        item = item.lstrip("./").lstrip("/")
        
        # Avoid invalid paths
        if any(c in item for c in ['<', '>', ':', '"', '|', '?', '*', '`', '$', '{', '}', ';', '(', ')']):
            continue

        target_file = ROOT / item
        if target_file.exists():
            skipped += 1
            continue

        url = f"{BASE_URL}/{item}"
        try:
            req = urllib.request.Request(url, headers=HEADERS)
            with urllib.request.urlopen(req, timeout=5) as resp:
                data = resp.read()
                # Check if it was a 404 disguised as html error page for non-html assets
                ct = resp.headers.get_content_type()
                if "text/html" in ct and not item.endswith(".html"):
                    # Check if Vercel 404
                    if b"NOT_FOUND" in data or b"The page could not be found" in data:
                        # 404
                        continue
                
                target_file.parent.mkdir(parents=True, exist_ok=True)
                with open(target_file, "wb") as f:
                    f.write(data)
                print(f"[OK] {item} ({len(data)} bytes)")
                downloaded += 1
        except urllib.error.HTTPError as e:
            if e.code != 404:
                print(f"[HTTP {e.code}] {item}")
            failed += 1
        except Exception as e:
            failed += 1

    print(f"\nDone! Downloaded: {downloaded}, Skipped (already exist): {skipped}, Failed/404: {failed}")

if __name__ == "__main__":
    c = find_asset_references()
    download_missing(c)
