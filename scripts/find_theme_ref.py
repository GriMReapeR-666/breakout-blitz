import re
from pathlib import Path

text = Path("app/src/main/assets/setuprush/assets/main-NVMoWWYe.js").read_text(encoding="utf-8")
matches = re.finditer(r'([a-zA-Z0-9_\$]+)\.theme\b', text)
for m in list(matches)[:15]:
    idx = m.start()
    print(text[max(0, idx - 50): min(len(text), idx + 100)])
