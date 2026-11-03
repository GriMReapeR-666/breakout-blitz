with open("app/src/main/assets/setuprush/assets/main-NVMoWWYe.js", "r", encoding="utf-8", errors="ignore") as fp:
    c = fp.read()

idx = c.find("function _e(")
if idx == -1:
    idx = c.find("_e=")
if idx != -1:
    print("=== uiSfx / _e snippet ===")
    print(c[idx:idx+1500])
