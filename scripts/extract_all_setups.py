from pathlib import Path
import json
import re

engine_text = Path('app/src/main/assets/setuprush/engine.js').read_text(encoding='utf-8')

# Find SETUPS definition in engine.js
pos = engine_text.find('const SETUPS = {')
if pos == -1:
    pos = engine_text.find('SETUPS = {')

# Find all setup keys
# e.g., hammer: { ... }, engulfing: { ... }
setups = re.findall(r'(\w+):\s*\{\s*names:\s*\[([^\]]+)\],\s*kinds:\s*\[([^\]]+)\],\s*family:\s*[\'"]([^\'"]+)[\'"],\s*winRate:\s*([0-9.]+)', engine_text)
print(f"=== SETUPS IN ENGINE.JS ({len(setups)} setups) ===")
for s in setups:
    print(f"Key: {s[0]:15} | Names: {s[1]:35} | Kinds: {s[2]:35} | Family: {s[3]:12} | WinRate: {s[4]}")

# Also search for setups in candle-vision.js
cv_text = Path('app/src/main/assets/setuprush/vendor/candle-vision.js').read_text(encoding='utf-8')
# Find PATTERN_FAMILY_METADATA or CANDLE_VISION_PATTERN_CATALOG
families = re.findall(r'([A-Z_]+_PATTERN_DEFINITION)', cv_text)
print(f"\n=== CANDLE VISION PATTERNS ({len(set(families))} definitions) ===")
for f in sorted(set(families)):
    print(" -", f)
