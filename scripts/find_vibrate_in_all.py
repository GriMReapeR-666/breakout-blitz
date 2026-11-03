from pathlib import Path

for p in Path("app/src/main/assets/setuprush/assets").glob("*.js"):
    text = p.read_text(encoding="utf-8", errors="ignore")
    if "buzz:" in text or "vibrate" in text:
        print(p.name, "contains buzz or vibrate")
