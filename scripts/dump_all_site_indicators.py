import re
import sys

sys.stdout.reconfigure(encoding='utf-8')

with open('scripts/indicators_extracted.js', 'r', encoding='utf-8') as f:
    text = f.read()

# Pattern for indicators in indicators_extracted.js
# id:`supertrend`,name:`Supertrend`,short:`ST`,pane:!1,tag:`Trend`
pattern = r'id:`([^`]+)`\s*,\s*name:`([^`]+)`\s*,\s*short:`([^`]+)`\s*,\s*pane:([^,]+)\s*,\s*tag:`([^`]+)`'

matches = re.findall(pattern, text)
print(f"Total indicators found: {len(matches)}")
print("-" * 80)
for i, m in enumerate(matches):
    pane_bool = "Separate Pane" if m[3].strip() in ["!0", "true", "!1==false"] else "Main Overlay"
    print(f"{i+1:2d}. [{m[4]:12s}] {m[0]:18s} | {m[1]:28s} | Short: {m[2]:6s} | {pane_bool}")
