import sys
import re

sys.stdout.reconfigure(encoding='utf-8')

with open("app/src/main/assets/setuprush/assets/main-NVMoWWYe.js", "r", encoding="utf-8", errors="ignore") as fp:
    content = fp.read()

# Let's search for "PATTERN LOCKED" or "HITS TARGET"
for kw in ["PATTERN LOCKED", "HITS TARGET", "Candle Vision", "Pattern Vision"]:
    for m in re.finditer(kw, content, re.IGNORECASE):
        pos = m.start()
        print(f"Match for '{kw}':")
        print(content[max(0, pos-100):min(len(content), pos+200)])
        print("-" * 50)
