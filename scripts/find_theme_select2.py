from pathlib import Path

text = Path("app/src/main/assets/setuprush/assets/main-NVMoWWYe.js").read_text(encoding="utf-8")
idx = text.find("desc:`Lavender and rose, after dark`")
if idx != -1:
    print(text[idx + 200: idx + 1400])
