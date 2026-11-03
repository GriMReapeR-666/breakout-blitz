from pathlib import Path
import re

text = Path('app/src/main/assets/setuprush/assets/main-NVMoWWYe.js').read_text(encoding='utf-8')

# Find where THEMES: I is defined
# e.g., ... THEMES: I, ...
pos = text.find('THEMES:I')
if pos == -1:
    pos = text.find('THEMES:')
print(f"Position of THEMES: {pos}")
if pos != -1:
    start = max(0, pos - 200)
    end = min(len(text), pos + 300)
    print("Around THEMES:\n", text[start:end])

# Search for definition of `Ni`
# e.g., function Ni( or var Ni = or const Ni =
matches = re.finditer(r'(function\s+Ni\s*\([^)]*\)\s*\{[^}]+\})', text)
for m in matches:
    print("\nFunction Ni (applyTheme):\n", m.group(1))

# Also search for `var I=` or `const I=` or `let I=` where I is an object with themes
for m in re.finditer(r'([a-zA-Z0-9_$]+\s*=\s*\{\s*night\s*:\s*\{[^\}]+\})', text):
    print("\nTheme object match:\n", m.group(1)[:500])

# Let's search for "candy" inside object definition
for m in re.finditer(r'([a-zA-Z0-9_$]+\s*=\s*\{[^{}]*candy\s*:\s*\{[^{}]+\}[^}]*\})', text):
    print("\nFound themes map with candy:\n", m.group(1)[:800])
