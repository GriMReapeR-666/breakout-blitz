import urllib.request
from pathlib import Path

BASE = "https://setuprush.com"
HEADERS = {"User-Agent": "Mozilla/5.0"}

endpoints = [
    ("api/markets?venue=hl", "api/markets"),
    ("api/rush/board?day=20758", "api/rush/board")
]

dirs = [
    Path("external_references/setuprush"),
    Path("app/src/main/assets/setuprush")
]

for url_suffix, local_rel in endpoints:
    url = f"{BASE}/{url_suffix}"
    print(f"Fetching {url}...")
    req = urllib.request.Request(url, headers=HEADERS)
    with urllib.request.urlopen(req) as resp:
        data = resp.read()
        for d in dirs:
            target = d / local_rel
            target.parent.mkdir(parents=True, exist_ok=True)
            with open(target, "wb") as out:
                out.write(data)
            print(f" -> Saved to {target} ({len(data)} bytes)")

print("API snapshots saved successfully!")
