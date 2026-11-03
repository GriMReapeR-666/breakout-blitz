import os
import glob
import re

for f in glob.glob("app/src/main/assets/setuprush/**/*.js", recursive=True):
    with open(f, "r", encoding="utf-8", errors="ignore") as fp:
        c = fp.read()
        m = re.findall(r"\bSETUPS\b", c)
        if m:
            print(f"File {f}: {len(m)} matches")
            pos = c.find("SETUPS")
            print(c[max(0, pos-50):min(len(c), pos+400)])
            print("="*40)
