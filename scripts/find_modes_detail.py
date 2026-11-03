import sys
sys.stdout.reconfigure(encoding='utf-8')

with open("app/src/main/assets/setuprush/assets/main-NVMoWWYe.js", "r", encoding="utf-8", errors="ignore") as fp:
    c = fp.read()

# Let's find definition of T (MODES)
# It's likely near var T= or T=[{ or T={rush:
idx = c.find("rush:{id:`rush`")
if idx == -1:
    idx = c.find("id:`rush`,name:`Rush`")
if idx != -1:
    print("=== Found Rush Mode Definition ===")
    print(c[max(0, idx-50):min(len(c), idx+1500)])

# Let's find CAREER / DESKS
idx2 = c.find("DESKS=")
if idx2 == -1:
    idx2 = c.find("ai=")
if idx2 != -1:
    print("\n=== Found DESKS ===")
    print(c[idx2:idx2+1000])
