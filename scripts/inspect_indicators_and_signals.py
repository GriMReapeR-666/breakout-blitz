import os
import glob
import re

assets_dir = r"app/src/main/assets/setuprush/assets"
files = glob.glob(os.path.join(assets_dir, "*.js"))

print("Scanning JS files for indicator, strategy, and signal features...")

patterns = [
    r"AUTO_STRATEGIES",
    r"LIBRARY",
    r"PineScript",
    r"Supertrend",
    r"Smart Money",
    r"FVG",
    r"BOS",
    r"crossover",
    r"indicator",
]

for f in files:
    fname = os.path.basename(f)
    try:
        with open(f, "r", encoding="utf-8", errors="ignore") as fp:
            content = fp.read()
            found = []
            for p in patterns:
                m = re.findall(p, content, re.IGNORECASE)
                if m:
                    found.append((p, len(m)))
            if found:
                print(f"File {fname}: {found}")
    except Exception as e:
        print(f"Error {f}: {e}")
