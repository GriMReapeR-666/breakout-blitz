import re
import urllib.request
from pathlib import Path

BASE_URL = "https://setuprush.com/assets"
ROOT = Path("external_references/setuprush/assets")
HEADERS = {"User-Agent": "Mozilla/5.0"}

text = (ROOT / "feature-carousel-BvU8oPuK.css").read_text(encoding="utf-8")
raw_urls = re.findall(r'url\(([^)]+)\)', text)

fonts_to_download = []
for u in raw_urls:
    clean = u.strip('"\'')
    if clean.endswith(".woff2"):
        fname = clean.lstrip("./")
        target = ROOT / fname
        if not target.exists():
            fonts_to_download.append(fname)
        else:
            print(f"[ALREADY EXISTS] {fname} ({target.stat().st_size} bytes)")

for f in fonts_to_download:
    url = f"{BASE_URL}/{f}"
    req = urllib.request.Request(url, headers=HEADERS)
    with urllib.request.urlopen(req) as resp:
        data = resp.read()
        target = ROOT / f
        with open(target, "wb") as out:
            out.write(data)
        print(f"[DOWNLOADED] {f} ({len(data)} bytes)")

print("All fonts verified!")
