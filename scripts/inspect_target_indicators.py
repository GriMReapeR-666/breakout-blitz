import sys
import re

sys.stdout.reconfigure(encoding='utf-8')

with open("app/src/main/assets/setuprush/assets/main-NVMoWWYe.js", "r", encoding="utf-8", errors="ignore") as fp:
    content = fp.read()

target_ids = ['supertrend', 'structure', 'fvg', 'orderblocks', 'sweeps', 'emacross', 'macd', 'rsi', 'rsidiv', 'utbot']

for tid in target_ids:
    m = re.search(r"id:`" + tid + r"`,name:`([^`]+)`,short:`([^`]+)`,pane:(![01]|true|false),tag:`([^`]+)`,src:`([^`]+)`", content)
    if m:
        print(f"================== {m.group(1)} ({tid}) ==================")
        print(f"Short: {m.group(2)} | Pane: {m.group(3)} | Tag: {m.group(4)}")
        print("Source:")
        print(m.group(5).strip())
        print()
