with open("app/src/main/assets/setuprush/assets/main-NVMoWWYe.js", "r", encoding="utf-8", errors="ignore") as fp:
    c = fp.read()

import re

# find id:`blind` or id:`survivor`
idx = c.find("id:`blind`")
if idx != -1:
    print("=== Found Career Level Stages ===")
    print(c[max(0, idx-200):min(len(c), idx+1800)])
