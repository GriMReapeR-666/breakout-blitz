from pathlib import Path
import re

t = Path('app/src/main/assets/setuprush/assets/main-NVMoWWYe.js').read_text(encoding='utf-8')
p = t.find('Ir=Object.freeze([')
if p != -1:
    snippet = t[p:p+4000]
    # find all strategy objects
    # {id: `...`, name: `...`, family: `...`, description: `...`}
    matches = re.finditer(r'\{id:\s*`([^`]+)`,\s*(?:version:\s*\d+,\s*)?name:\s*`([^`]+)`,\s*short:\s*`([^`]+)`,\s*family:\s*`([^`]+)`,\s*description:\s*`([^`]+)`', snippet)
    print("=== AUTOMATED STRATEGIES / BOTS ===")
    for m in matches:
        print(f"Strategy: {m.group(2)} ({m.group(3)})")
        print(f"  Family     : {m.group(4)}")
        print(f"  Description: {m.group(5)}")
        print()

# Check what Fr is (the default strategy)
p_fr = t.find('Fr=Object.freeze({')
if p_fr == -1:
    p_fr = t.find('Fr={')
if p_fr != -1:
    print("=== DEFAULT STRATEGY (Fr) ===")
    print(t[p_fr:p_fr+800])
