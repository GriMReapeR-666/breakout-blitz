import re
import urllib.request
from pathlib import Path

BASE_URL = "https://setuprush.com/assets"
HEADERS = {"User-Agent": "Mozilla/5.0"}

EXT_DIR = Path("external_references/setuprush/assets")
APP_DIR = Path("app/src/main/assets/setuprush/assets")

text = (EXT_DIR / "Shop-B21SFhwa.js").read_text(encoding="utf-8")
images = set(re.findall(r'([a-zA-Z0-9_\-]+-[a-zA-Z0-9_\-]{8}\.jpg)', text))

print(f"Total shop images found: {len(images)}")
downloaded = 0
for img in sorted(images):
    target1 = EXT_DIR / img
    target2 = APP_DIR / img
    if target1.exists() and target2.exists():
        continue
    url = f"{BASE_URL}/{img}"
    try:
        req = urllib.request.Request(url, headers=HEADERS)
        with urllib.request.urlopen(req, timeout=5) as resp:
            data = resp.read()
            with open(target1, "wb") as f:
                f.write(data)
            with open(target2, "wb") as f:
                f.write(data)
            print(f"[OK] {img} ({len(data)} bytes)")
            downloaded += 1
    except Exception as e:
        print(f"[FAILED] {img}: {e}")

print(f"Downloaded {downloaded} shop theme images.")
