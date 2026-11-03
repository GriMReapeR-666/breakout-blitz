import re
import urllib.request
import urllib.parse
from pathlib import Path

ROOT = Path("external_references/setuprush")
BASE_URL = "https://setuprush.com"

discovered = set()
for p in ROOT.rglob("*"):
    if p.suffix in [".js", ".css", ".html", ".webmanifest"]:
        text = p.read_text(encoding="utf-8", errors="ignore")
        # search for media / json / fonts / apis
        for m in re.finditer(r'''["'`/]([a-zA-Z0-9_\-\.\/]+\.(?:png|jpg|jpeg|svg|webp|gif|woff2?|ttf|otf|mp3|wav|ogg|json))["'`]''', text):
            discovered.add(m.group(1).lstrip("/"))

print(f"Total potential asset paths found: {len(discovered)}")
for d in sorted(discovered):
    print(" -", d)

headers = {"User-Agent": "Mozilla/5.0"}
downloaded = 0
for d in sorted(discovered):
    # filter out obvious node_modules or invalid
    if any(k in d for k in ["node_modules", "http:", "https:"]):
        continue
    target = ROOT / d
    if target.exists():
        continue
    url = f"{BASE_URL}/{d}"
    try:
        req = urllib.request.Request(url, headers=headers)
        with urllib.request.urlopen(req, timeout=4) as resp:
            data = resp.read()
            ct = resp.headers.get_content_type()
            if "text/html" in ct and not d.endswith(".html"):
                continue
            target.parent.mkdir(parents=True, exist_ok=True)
            with open(target, "wb") as f:
                f.write(data)
            print(f"[FETCHED] {d} ({len(data)} bytes)")
            downloaded += 1
    except Exception:
        pass

print(f"Downloaded {downloaded} additional assets.")
