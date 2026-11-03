with open("app/src/main/assets/setuprush/assets/main-NVMoWWYe.js", "r", encoding="utf-8", errors="ignore") as fp:
    c = fp.read()

import re

idx = c.find("MODES:")
if idx != -1:
    print("=== MODES snippet ===")
    print(c[idx:idx+2500])

# Find definitions of setups in modes or levels
idx2 = c.find("SETUPS:")
if idx2 == -1:
    idx2 = c.find("SETUPS=")
if idx2 != -1:
    print("\n=== SETUPS snippet ===")
    print(c[idx2:idx2+1500])
