import sys, re
sys.stdout.reconfigure(encoding='utf-8')

path = 'app/src/main/assets/setuprush/assets/main-NVMoWWYe.js'
with open(path, encoding='utf-8', errors='ignore') as f:
    c = f.read()

print(f"main-NVMoWWYe.js length: {len(c)}")

# Search for mode definitions
mode_matches = [m.start() for m in re.finditer(r'rush|pro|blowup|callit|daily', c)]
print(f"Mode occurrences: {len(mode_matches)}")

# Search for ride recording in main
matches = [m.start() for m in re.finditer(r'toExecutions|annotate|firstLiq|ridePnl|verifyRun|byPlaybook', c)]
print(f"Journal/engine occurrences: {len(matches)}")
for idx in matches[:10]:
    print(f"--- snippet at {idx}: ---")
    print(c[max(0, idx-80):min(len(c), idx+160)])
    print()
