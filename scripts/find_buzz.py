from pathlib import Path

text = Path("app/src/main/assets/setuprush/assets/main-NVMoWWYe.js").read_text(encoding="utf-8")
idx = text.find("buzz:")
if idx != -1:
    print(text[max(0, idx - 200): min(len(text), idx + 400)])
