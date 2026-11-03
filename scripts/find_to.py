from pathlib import Path

text = Path("app/src/main/assets/setuprush/assets/main-NVMoWWYe.js").read_text(encoding="utf-8")
idx = text.find("var po=co(")
if idx != -1:
    print(text[max(0, idx - 400): idx])
