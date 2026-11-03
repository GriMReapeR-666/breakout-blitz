with open("app/src/main/assets/setuprush/assets/main-NVMoWWYe.js", "r", encoding="utf-8", errors="ignore") as fp:
    c = fp.read()

idx = c.find("function Wa(e){")
if idx != -1:
    print("Arena Networking Logic:")
    print(c[idx:idx+2500])
