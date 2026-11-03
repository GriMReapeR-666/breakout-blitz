import os
import glob
import re

assets_dir = r"app/src/main/assets/setuprush/assets"
js_files = glob.glob(os.path.join(assets_dir, "*.js"))

domains = set()
ws_urls = set()
api_paths = set()

for f in js_files:
    with open(f, "r", encoding="utf-8", errors="ignore") as fp:
        c = fp.read()
        for m in re.finditer(r'https?://([a-zA-Z0-9_\-\.]+)(/[^\s"\'`]*)?', c):
            dom = m.group(1)
            if not dom.endswith(".w3.org") and not dom.endswith("schema.org"):
                domains.add(dom)
        for m in re.finditer(r'wss?://([a-zA-Z0-9_\-\.]+)(/[^\s"\'`]*)?', c):
            ws_urls.add(m.group(0))
        for m in re.finditer(r'["\'](/api/[^"\'`\s]+)["\']', c):
            api_paths.add(m.group(1))

print("=== External Domains Referenced in Production JS ===")
for d in sorted(domains):
    print(" -", d)

print("\n=== WebSocket Endpoints ===")
for w in sorted(ws_urls):
    print(" -", w)

print("\n=== API Routes ===")
for a in sorted(api_paths):
    print(" -", a)
