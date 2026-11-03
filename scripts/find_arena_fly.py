with open("app/src/main/assets/setuprush/assets/Arena-CO6oEvqE.js", "r", encoding="utf-8", errors="ignore") as fp:
    c = fp.read()
    idx = c.find("fly.dev")
    if idx != -1:
        print("Arena fly.dev snippet:")
        print(c[max(0, idx-100):min(len(c), idx+400)])
    else:
        print("fly.dev not found in Arena-CO6oEvqE.js")

with open("app/src/main/assets/setuprush/assets/main-NVMoWWYe.js", "r", encoding="utf-8", errors="ignore") as fp:
    c = fp.read()
    idx = c.find("fly.dev")
    if idx != -1:
        print("main fly.dev snippet:")
        print(c[max(0, idx-100):min(len(c), idx+400)])
