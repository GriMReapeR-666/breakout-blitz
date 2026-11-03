import os
import re
import json

root = 'app/src/main/assets/setuprush'

# Check CSS
css_files = []
for dirpath, _, filenames in os.walk(root):
    for f in filenames:
        if f.endswith('.css'):
            css_files.append(os.path.join(dirpath, f))

print('=== CSS FILES WITH THEME RULES ===')
for cf in css_files:
    content = open(cf, 'r', encoding='utf-8', errors='ignore').read()
    themes = re.findall(r'(\.theme-[a-zA-Z0-9_-]+|\[data-theme=[^\]]+\])', content)
    if themes:
        print(cf, set(themes))
        # Find CSS variable blocks for themes
        for m in re.finditer(r'(\.theme-[a-zA-Z0-9_-]+|\[data-theme=[^\]]+\])\s*\{([^}]+)\}', content):
            print(f"  Selector: {m.group(1)}")
            print(f"  Rules: {m.group(2)[:300]}...")

# Check JS files
js_files = []
for dirpath, _, filenames in os.walk(root):
    for f in filenames:
        if f.endswith('.js'):
            js_files.append(os.path.join(dirpath, f))

print('\n=== JS FILES DEFINING THEMES ===')
for jf in js_files:
    content = open(jf, 'r', encoding='utf-8', errors='ignore').read()
    # Check for presence of multiple theme names
    names = ['candy', 'night', 'terminal', 'neon', 'paper', 'violet', 'mint', 'ocean']
    present = [n for n in names if f'"{n}"' in content or f"'{n}'" in content]
    if len(present) >= 3:
        print(f"\nFile: {jf} (contains: {present})")
        # Search for theme object definition or theme switching function
        for line in content.split(';'):
            if any(f'"{p}"' in line or f"'{p}'" in line for p in present) and ('bull' in line or 'bear' in line or 'bg' in line or 'card' in line or 'color' in line or 'palette' in line):
                print(f"  Snippet: {line[:250]}")
                break
