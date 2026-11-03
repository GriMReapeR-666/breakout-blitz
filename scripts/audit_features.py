import os
import glob
import re
import json

# Let's inspect main JS files and extract all route components, state objects, game modes, and network API endpoints
assets_dir = r"app/src/main/assets/setuprush/assets"
js_files = glob.glob(os.path.join(assets_dir, "*.js"))

print("=== Scanning JS Assets for Feature Modules & Routes ===")
for f in js_files:
    fname = os.path.basename(f)
    size_kb = os.path.getsize(f) / 1024
    print(f"{fname:30s} {size_kb:8.1f} KB")

# Let's search for API endpoints in the JS files: e.g. /api/... or wss:// or https://
api_endpoints = set()
ws_endpoints = set()

for f in js_files:
    with open(f, "r", encoding="utf-8", errors="ignore") as fp:
        c = fp.read()
        for m in re.finditer(r'["\'](/api/[a-zA-Z0-9_\-\/]+)["\']', c):
            api_endpoints.add(m.group(1))
        for m in re.finditer(r'["\'](wss?://[^\s"\']+)["\']', c):
            ws_endpoints.add(m.group(1))
        for m in re.finditer(r'["\'](https?://[a-zA-Z0-9_\-\.]+(?:/[^\s"\']*)?)["\']', c):
            url = m.group(1)
            if "schema.org" not in url and "w3.org" not in url:
                api_endpoints.add(url)

print("\n=== Discovered API / Network Endpoints ===")
for ep in sorted(api_endpoints):
    print("API:", ep)

print("\n=== Discovered WebSocket Endpoints ===")
for ws in sorted(ws_endpoints):
    print("WS:", ws)
