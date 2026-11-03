import re
from pathlib import Path

keys = set()
for p in Path("app/src/main/assets/setuprush").rglob("*.js"):
    text = p.read_text(encoding="utf-8", errors="ignore")
    matches = re.findall(r'localStorage\.(?:getItem|setItem)\([`\'"]([^`\'"]+)[`\'"]', text)
    keys.update(matches)
    matches_sr = re.findall(r'[`\'"](sr\.[a-zA-Z0-9_\.]+)[\`\'\"]', text)
    keys.update(matches_sr)

print("All sr.* keys found in game:")
for k in sorted(keys):
    print(" -", k)
