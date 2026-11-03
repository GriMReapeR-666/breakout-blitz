with open("app/src/main/assets/setuprush/assets/main-NVMoWWYe.js", "r", encoding="utf-8", errors="ignore") as fp:
    c = fp.read()

idx = c.find("SETUPS:{")
if idx == -1:
    idx = c.find("SETUPS=")
if idx != -1:
    print("=== Found SETUPS ===")
    print(c[idx:idx+2500])
