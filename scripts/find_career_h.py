with open("app/src/main/assets/setuprush/assets/main-NVMoWWYe.js", "r", encoding="utf-8", errors="ignore") as fp:
    c = fp.read()

import re

# Find H = or CAREER definition
idx = c.find("H=[")
if idx == -1:
    idx = c.find("CAREER=[")
if idx != -1:
    print("=== Found CAREER H ===")
    print(c[idx:idx+2000])

# Find CHAL W
idx2 = c.find("W=[")
if idx2 != -1:
    print("\n=== Found CHAL W ===")
    print(c[idx2:idx2+1500])
