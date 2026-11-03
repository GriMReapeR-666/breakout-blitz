import json
import re

with open("app/src/main/assets/setuprush/assets/SetupCard-PdywsOQW.js", "r", encoding="utf-8", errors="ignore") as fp:
    c = fp.read()

# find all json-like objects with "name" and "lesson"
matches = re.findall(r'\{"id":"([^"]+)","name":"([^"]+)","key":"([^"]+)","family":"([^"]+)","dir":(-?1),"result":"(win|loss)","sym":"([^"]+)","tf":"([^"]+)","seen":([0-9]+),"rate":([0-9\.]+),"lesson":"([^"]+)"\}', c)

print(f"Found {len(matches)} setup replay cards in SetupCard:")
seen_names = {}
for m in matches:
    name = m[1]
    if name not in seen_names:
        seen_names[name] = m

for name, m in list(seen_names.items())[:15]:
    print(f"• {name:28s} | Key: {m[2]:15s} | Family: {m[3]:12s} | Dir: {'+1 (Long)' if m[4]=='1' else '-1 (Short)'} | WinRate: {float(m[9])*100:.0f}%")
    print(f"  Lesson: \"{m[10]}\"")
