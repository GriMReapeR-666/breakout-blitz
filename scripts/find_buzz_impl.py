from pathlib import Path

text = Path("app/src/main/assets/setuprush/assets/feature-carousel-CQV1ynzp.js").read_text(encoding="utf-8")
idx = text.find("buzz")
if idx != -1:
    print(text[max(0, idx - 100): min(len(text), idx + 300)])
