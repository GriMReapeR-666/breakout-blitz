import sys
sys.stdout.reconfigure(encoding='utf-8')

with open("app/src/main/assets/setuprush/engine.js", "r", encoding="utf-8") as fp:
    c = fp.read()

import re

# find SETUPS object
idx = c.find("const SETUPS = {")
end_idx = c.find("const SETUP_KEYS = Object.keys(SETUPS);")

setups_block = c[idx:end_idx]
print(f"Setups block length: {len(setups_block)}")

# Find all keys in SETUPS
keys = re.findall(r"(\w+):\s*\{[\s\n]*names:\s*\[([^\]]+)\]", setups_block)
print(f"Found {len(keys)} setup definitions:")
for k, names in keys:
    print(f"Key: {k:20s} | Names: {names.strip()}")
