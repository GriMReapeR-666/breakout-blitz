import urllib.request
from pathlib import Path

BASE = "https://setuprush.com"
HEADERS = {"User-Agent": "Mozilla/5.0"}

files_to_fetch = [
    "history/sets.json",
    "history/v2/20261031/manifest.json"
]

dirs = [
    Path("external_references/setuprush"),
    Path("app/src/main/assets/setuprush")
]

for f in files_to_fetch:
    url = f"{BASE}/{f}"
    print(f"Fetching {url}...")
    req = urllib.request.Request(url, headers=HEADERS)
    with urllib.request.urlopen(req) as resp:
        data = resp.read()
        for d in dirs:
            target = d / f
            target.parent.mkdir(parents=True, exist_ok=True)
            with open(target, "wb") as out:
                out.write(data)
            print(f" -> Saved to {target} ({len(data)} bytes)")

print("History sets and manifests downloaded successfully!")
