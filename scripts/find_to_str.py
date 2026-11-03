import re
from pathlib import Path

text = Path("app/src/main/assets/setuprush/assets/main-NVMoWWYe.js").read_text(encoding="utf-8")
matches = re.finditer(r'\bto\s*=\s*[`\'"][^`\'"]+[`\'"]', text)
for m in matches:
    print(m.group(0))
