import sys

sys.stdout.reconfigure(encoding='utf-8')

with open("app/src/main/assets/setuprush/assets/main-NVMoWWYe.js", "r", encoding="utf-8", errors="ignore") as fp:
    content = fp.read()

# Let's find definition of Ir (AUTO_STRATEGIES)
idx = content.find("Ir=")
if idx != -1:
    print("--- Ir definition ---")
    print(content[idx:idx+1500])

# Let's find definition of vi (LIBRARY)
idx = content.find("vi=")
if idx != -1:
    print("\n--- vi definition ---")
    print(content[idx:idx+3500])
