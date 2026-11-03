import json
import re
from pathlib import Path

p = Path("app/src/main/assets/setuprush/history/sets.json")
text = p.read_text(encoding="utf-8")
data = json.loads(text)
print("sets.json loaded. Length:", len(data))

# Extract all .json filenames referenced
matches = set(re.findall(r'([a-zA-Z0-9_\-]+\.json)', text))
print(f"Total .json filenames referenced: {len(matches)}")
for m in sorted(matches)[:15]:
    print(" -", m)
