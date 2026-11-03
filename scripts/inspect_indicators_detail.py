import re

with open("app/src/main/assets/setuprush/assets/main-NVMoWWYe.js", "r", encoding="utf-8", errors="ignore") as fp:
    content = fp.read()

# Look for AUTO_STRATEGIES
idx = content.find("AUTO_STRATEGIES")
if idx != -1:
    print("--- AUTO_STRATEGIES Snippet ---")
    print(content[max(0, idx - 100):min(len(content), idx + 1000)])

# Look for indicators library
idx2 = content.find("LIBRARY:")
if idx2 != -1:
    print("\n--- LIBRARY Snippet ---")
    print(content[max(0, idx2 - 50):min(len(content), idx2 + 1200)])

# Look for indicator menu/modal/selection UI
idx3 = content.find("INDICATORS")
if idx3 != -1:
    print("\n--- INDICATORS Snippet ---")
    print(content[max(0, idx3 - 100):min(len(content), idx3 + 500)])
