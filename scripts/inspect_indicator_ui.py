import sys
import re

sys.stdout.reconfigure(encoding='utf-8')

with open("app/src/main/assets/setuprush/assets/main-NVMoWWYe.js", "r", encoding="utf-8", errors="ignore") as fp:
    content = fp.read()

# Let's find toggleIndicator: Xa
idx = content.find("function Xa(")
if idx == -1:
    idx = content.find("Xa=")
if idx != -1:
    print("--- Xa definition ---")
    print(content[idx:idx+1500])

# Let's find where LIBRARY is rendered in the UI (e.g. indicator modal or drawer)
for m in re.finditer(r"LIBRARY\.map|LIBRARY\.filter|LIBRARY\[", content):
    pos = m.start()
    print("--- Found LIBRARY usage ---")
    print(content[max(0, pos-200):min(len(content), pos+400)])
