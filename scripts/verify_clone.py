import re
from pathlib import Path

ROOT = Path("external_references/setuprush")

for html_file in [ROOT / "index.html", ROOT / "play.html"]:
    text = html_file.read_text(encoding="utf-8")
    refs = re.findall(r'''(?:href|src)=["']([^"']+)["']''', text)
    print(f"Checking {html_file.name}: {len(refs)} link references")
    missing = []
    for r in refs:
        if r.startswith("http") or r.startswith("#") or r.startswith("data:"):
            continue
        # handle /play -> play.html
        if r == "/play":
            target = ROOT / "play.html"
        elif r.startswith("/"):
            target = ROOT / r.lstrip("/")
        else:
            target = ROOT / r.lstrip("./")
            
        if not target.exists():
            missing.append(r)
            
    if missing:
        print(f"  Missing in {html_file.name}: {missing}")
    else:
        print(f"  All links in {html_file.name} resolved successfully!")
