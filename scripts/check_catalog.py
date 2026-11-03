import sys, re
sys.stdout.reconfigure(encoding='utf-8')

with open('app/src/main/java/com/setuprush/candy/engine/StrategyCatalog.kt', encoding='utf-8') as f:
    content = f.read()

setups = re.findall(r'key = "([^"]+)"', content)
print(f"Total setups found: {len(setups)}")
for i, s in enumerate(setups, 1):
    print(f"  {i:2d}. {s}")

families = re.findall(r'([A-Z_]+)\("[^"]+",\s*"[^"]+",', content)
print(f"\nTotal families: {len(families)}")
for f in families:
    print(f"  - {f}")
