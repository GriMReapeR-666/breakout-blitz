# Script to verify StrategyCatalog coverage across all strategy groups
import sys
import re

sys.stdout.reconfigure(encoding='utf-8')

with open("app/src/main/java/com/setuprush/candy/engine/StrategyCatalog.kt", "r", encoding="utf-8") as fp:
    c = fp.read()

# Match SetupCardData entries
matches = re.findall(r'SetupCardData\(\s*key\s*=\s*"([^"]+)",\s*name\s*=\s*"([^"]+)",\s*family\s*=\s*StrategyFamily\.([A-Z_]+),\s*bias\s*=\s*"([^"]+)",\s*winRate\s*=\s*([0-9\.]+),\s*lesson\s*=\s*"([^"]+)"', c)

print(f"Verified {len(matches)} setup cards declared in StrategyCatalog.kt:")
families = {}
for m in matches:
    key, name, fam, bias, wr, lesson = m
    families.setdefault(fam, []).append((key, name, bias, float(wr), lesson))

for fam, items in families.items():
    print(f"\n=== STRATEGY GROUP: {fam} ({len(items)} Setups) ===")
    for key, name, bias, wr, lesson in items:
        print(f"  • {name:40s} | {bias:7s} | WinRate: {int(wr*100)}%")
        print(f"    Lesson: \"{lesson}\"")
