from pathlib import Path
import re

root = Path('app/src/main/assets/setuprush')
js_files = list(root.rglob('*.js'))

search_terms = ['theme-candy-card', 'theme-amber', 'theme-blueprint', 'setTheme', 'THEMES']

for js in js_files:
    text = js.read_text(encoding='utf-8', errors='ignore')
    for term in search_terms:
        if term in text:
            print(f"Found '{term}' in {js.name} (size: {len(text)} bytes)")
            # print surrounding context
            pos = text.find(term)
            start = max(0, pos - 300)
            end = min(len(text), pos + 500)
            print(f"Context around {term}:\n{text[start:end]}\n" + "="*60)
            break
