import sys
import re

sys.stdout.reconfigure(encoding='utf-8')

with open("app/src/main/assets/setuprush/assets/main-NVMoWWYe.js", "r", encoding="utf-8", errors="ignore") as fp:
    content = fp.read()

# Let's search for "toggleIndicator" calls across main
for m in re.finditer(r"toggleIndicator\(", content):
    pos = m.start()
    print("--- Found toggleIndicator call ---")
    print(content[max(0, pos-200):min(len(content), pos+300)])
