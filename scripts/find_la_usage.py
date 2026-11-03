with open("app/src/main/assets/setuprush/assets/main-NVMoWWYe.js", "r", encoding="utf-8", errors="ignore") as fp:
    c = fp.read()

import re
for m in re.finditer(r"La\s*[\+\(]|fetch\(`\$\{La\}", c):
    pos = m.start()
    print("La usage:", c[max(0, pos-100):min(len(c), pos+300)])
