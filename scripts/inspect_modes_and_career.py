from pathlib import Path
import re
import json

text = Path('app/src/main/assets/setuprush/assets/main-NVMoWWYe.js').read_text(encoding='utf-8')

# 1. Inspect MODES: T
pos = text.find('MODES:T')
if pos == -1:
    pos = text.find('MODES:')
print("=== MODES AROUND DEFINITION ===")
pos_modes = text.find('T={rush:')
if pos_modes == -1:
    pos_modes = text.find('rush:{name:')
if pos_modes != -1:
    print(text[pos_modes:pos_modes+1200])
else:
    # search for mode descriptions
    for m in re.finditer(r'([a-zA-Z0-9_$]+\s*=\s*\{\s*rush\s*:\s*\{[^\}]+\})', text):
        print(m.group(1)[:500])

# 2. Inspect DESKS / CAREER
pos_career = text.find('CAREER:H')
if pos_career != -1:
    print("\n=== CAREER / DESKS ===")
    pos_h = text.find('H={')
    if pos_h != -1:
        print(text[pos_h:pos_h+2000])

# Let's search for DESKS
for m in re.finditer(r'([a-zA-Z0-9_$]+\s*=\s*\[\s*\{\s*id\s*:\s*[\'"][^\'"]+[\'"],\s*name\s*:[^]]+\])', text):
    if 'desk' in m.group(1).lower() or 'basics' in m.group(1).lower():
        print("\nFound desks array:\n", m.group(1)[:800])

# Let's search for lesson titles or desk titles
print("\n=== SEARCHING FOR LESSONS / DESKS ===")
for m in re.finditer(r'\{id:[\'"]([^\'"]+)[\'"],name:[\'"]([^\'"]+)[\'"],(?:desc|subtitle):[\'"]([^\'"]+)[\'"]', text):
    print(f"ID: {m.group(1):15} | Name: {m.group(2):25} | Desc: {m.group(3)}")
