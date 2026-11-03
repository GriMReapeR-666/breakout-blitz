import re
from pathlib import Path

text = Path("app/src/main/assets/setuprush/assets/main-NVMoWWYe.js").read_text(encoding="utf-8")
idx = text.find("sr.account.v1")
if idx != -1:
    print(text[idx + 800: idx + 2000])
