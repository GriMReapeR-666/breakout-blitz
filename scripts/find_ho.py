from pathlib import Path

text = Path("app/src/main/assets/setuprush/assets/main-NVMoWWYe.js").read_text(encoding="utf-8")
idx = text.find("function ho(")
if idx != -1:
    print(text[idx: idx + 400])
else:
    idx2 = text.find("ho=()")
    if idx2 != -1:
        print(text[idx2: idx2 + 400])
    else:
        print("not found directly")
