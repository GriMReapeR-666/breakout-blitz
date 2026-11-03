import sys
import os
import glob
import re

sys.stdout.reconfigure(encoding='utf-8')

assets_dir = r"app/src/main/assets/setuprush/assets"
for f in glob.glob(os.path.join(assets_dir, "*.js")):
    with open(f, "r", encoding="utf-8", errors="ignore") as fp:
        content = fp.read()
        for kw in ["AUTO_STRATEGY", "AUTO_STRATEGIES", "strategyFor"]:
            for m in re.finditer(kw, content):
                pos = m.start()
                print(f"File {os.path.basename(f)} @ {pos}:")
                print(content[max(0, pos-100):min(len(content), pos+200)])
                print("-" * 50)
