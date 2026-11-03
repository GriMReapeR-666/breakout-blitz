from pathlib import Path

text = Path("app/src/main/assets/setuprush/assets/main-NVMoWWYe.js").read_text(encoding="utf-8")
idx = text.find("axis:`rgba(13,2,33,.94)`},amber:{")
if idx != -1:
    print(text[idx: idx + 1000])
