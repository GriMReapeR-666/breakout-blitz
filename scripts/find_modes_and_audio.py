with open("app/src/main/assets/setuprush/assets/main-NVMoWWYe.js", "r", encoding="utf-8", errors="ignore") as fp:
    c = fp.read()

import re

# Find MODES
idx = c.find("MODES:")
if idx == -1:
    idx = c.find("MODES=")
if idx != -1:
    print("=== MODES snippet ===")
    print(c[idx:idx+1200])

# Find Audio files / SFX list
matches = re.findall(r'["\']([^"\']+\.(?:mp3|wav|ogg|webm|m4a))["\']', c)
print("\n=== Audio / Media Files referenced in main ===")
for m in set(matches):
    print(" -", m)
