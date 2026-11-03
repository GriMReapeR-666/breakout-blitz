from pathlib import Path
import re

text = Path('scripts/indicators_extracted.js').read_text(encoding='utf-8')
matches = re.finditer(r'\{id:\s*`([^`]+)`,\s*name:\s*`([^`]+)`,\s*short:\s*`([^`]+)`,\s*pane:\s*([^,]+),\s*tag:\s*`([^`]+)`', text)
print("=== INDICATORS IN SETUP RUSH ===")
for m in matches:
    print(f"- {m.group(2)} ({m.group(3)}) | Tag: {m.group(5)} | Pane: {m.group(4)}")
