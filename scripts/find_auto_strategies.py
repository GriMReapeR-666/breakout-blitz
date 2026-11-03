import sys
import re

sys.stdout.reconfigure(encoding='utf-8')

with open("app/src/main/assets/setuprush/assets/main-NVMoWWYe.js", "r", encoding="utf-8", errors="ignore") as fp:
    content = fp.read()

# Let's search for AUTO_STRATEGIES usages
for m in re.finditer(r"AUTO_STRATEGIES", content):
    pos = m.start()
    print("--- Found AUTO_STRATEGIES usage ---")
    print(content[max(0, pos-150):min(len(content), pos+300)])
