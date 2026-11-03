from pathlib import Path
import re

text = Path('app/src/main/assets/setuprush/assets/main-NVMoWWYe.js').read_text(encoding='utf-8')

print("=== SEARCHING TOGGLE INDICATOR & PINE ===")
p = text.find('toggleIndicator:')
if p != -1:
    print(text[p-50:p+300])

# Find function Xa
m = re.search(r'function\s+Xa\s*\([^)]*\)\s*\{[^}]+\}', text)
if m:
    print("\nFunction Xa:\n", m.group(0))

# Search for indicator modal / dialog
# Look for text like "INDICATORS" or "LIBRARY" or "ACTIVE (0/4)"
print("\n=== SEARCHING INDICATOR MODAL HEADINGS ===")
for m in re.finditer(r'[\'"`]([A-Za-z0-9\s/]+(?:Indicators|Active|Pine|Script)[A-Za-z0-9\s/]*)[\'"`]', text):
    s = m.group(1).strip()
    if len(s) > 3 and len(s) < 40:
        print(" -", s)
