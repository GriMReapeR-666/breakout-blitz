with open("app/src/main/assets/setuprush/assets/main-NVMoWWYe.js", "r", encoding="utf-8", errors="ignore") as fp:
    c = fp.read()

import re

idx = c.find("MODES:")
print("Around MODES:", c[idx-300:idx+100])

# Let's search for "rush:" and "blowup:"
for m in re.finditer(r"rush:\{id:`rush`|rush:\{", c):
    pos = m.start()
    print("Found rush mode at pos:", pos)
    print(c[pos:pos+600])
