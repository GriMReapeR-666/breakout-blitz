with open("app/src/main/assets/setuprush/assets/main-NVMoWWYe.js", "r", encoding="utf-8", errors="ignore") as fp:
    c = fp.read()

import re
for m in re.finditer(r"VITE_ARENA_ORIGIN", c):
    pos = m.start()
    print("VITE_ARENA_ORIGIN usage:", c[max(0, pos-50):min(len(c), pos+400)])
