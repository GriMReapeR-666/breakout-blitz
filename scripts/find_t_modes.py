import sys
sys.stdout.reconfigure(encoding='utf-8')

with open("app/src/main/assets/setuprush/assets/main-NVMoWWYe.js", "r", encoding="utf-8", errors="ignore") as fp:
    c = fp.read()

import re

# find where T is assigned with mode definitions
for m in re.finditer(r"var\s+T\s*=\s*\{|const\s+T\s*=\s*\{|let\s+T\s*=\s*\{", c):
    pos = m.start()
    print("Match at pos:", pos)
    print(c[pos:pos+1500])
    print("-" * 50)
