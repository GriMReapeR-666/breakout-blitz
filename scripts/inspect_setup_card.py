with open("app/src/main/assets/setuprush/assets/SetupCard-PdywsOQW.js", "r", encoding="utf-8", errors="ignore") as fp:
    c = fp.read()

import re

# Look for card structure, keys, properties
print(f"SetupCard file length: {len(c)}")
# Find some interesting UI strings or exported objects
matches = re.findall(r'["\']([^"\']{15,80})["\']', c)
sample = [s for s in matches if any(w in s.lower() for w in ["win", "rule", "setup", "entry", "target", "stop", "pattern", "candle", "card", "practice"])]
print("Sample card concepts:", sample[:15])

# Find where setup data is mapped
for m in re.finditer(r"names|winRate|family|trigger", c):
    pos = m.start()
    print("Match at pos:", pos)
    print(c[max(0, pos-50):min(len(c), pos+200)])
    print("-" * 40)
    break
