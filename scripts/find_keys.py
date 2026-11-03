import re
from pathlib import Path

text = Path("app/src/main/assets/setuprush/assets/main-NVMoWWYe.js").read_text(encoding="utf-8")
matches = set(re.findall(r'localStorage\.(?:getItem|setItem)\([`\'"]([^`\'"]+)[`\'"]', text))
print("LocalStorage keys found in main bundle:")
for m in sorted(matches):
    print(" -", m)
