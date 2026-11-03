import re
from pathlib import Path

for p in Path("app/src/main/assets/setuprush").rglob("*.js"):
    text = p.read_text(encoding="utf-8", errors="ignore")
    if "sr.account" in text:
        print("Found in", p.name)
        for m in re.finditer(r'.{0,100}sr\.account.{0,100}', text):
            print("  ", m.group(0))
