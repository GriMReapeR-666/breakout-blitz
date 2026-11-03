from pathlib import Path

text = Path("app/src/main/assets/setuprush/assets/main-NVMoWWYe.js").read_text(encoding="utf-8")
idx = text.find("Pastel and loud")
if idx != -1:
    print(text[max(0, idx - 500): min(len(text), idx + 800)])
