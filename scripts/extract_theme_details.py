from pathlib import Path
import re
import json

root = Path('app/src/main/assets/setuprush')

# 1. Inspect Shop-B21SFhwa.js
shop_file = root / 'assets' / 'Shop-B21SFhwa.js'
if shop_file.exists():
    text = shop_file.read_text(encoding='utf-8')
    print("=== SHOP THEME LIST & LOGIC ===")
    # Look for list of themes in shop
    # often an array of objects: { id: 'candy', name: 'Candy', ... }
    matches = re.findall(r'(\b[a-zA-Z0-9_$]+\s*=\s*\[\s*\{[^]]*\"candy\"[^]]*\])', text)
    if not matches:
        matches = re.findall(r'(\[\s*\{[^{}]*\"candy\"[^{}]*\}[^]]*\])', text)
    for m in matches[:3]:
        print("Shop theme definition snippet:\n", m[:1000])

# 2. Inspect CANDLE_VISION_THEMES in candle-vision.js
cv_file = root / 'vendor' / 'candle-vision.js'
if cv_file.exists():
    text = cv_file.read_text(encoding='utf-8')
    pos = text.find('CANDLE_VISION_THEMES')
    if pos != -1:
        print("\n=== CANDLE_VISION_THEMES ===")
        print(text[pos:pos+1500])

# 3. Inspect main-NVMoWWYe.js for THEMES or theme switching
main_file = root / 'assets' / 'main-NVMoWWYe.js'
if main_file.exists():
    text = main_file.read_text(encoding='utf-8')
    # search for where theme is set on document or localStorage
    # e.g., document.documentElement.className, data-theme, profile.theme
    matches = re.findall(r'([a-zA-Z0-9_$]*theme[a-zA-Z0-9_$]*\s*:[^,;}]+)', text, re.IGNORECASE)
    print("\n=== THEME REFERENCES IN MAIN ===")
    print(matches[:10])
    
    # search for functions modifying theme
    for m in re.finditer(r'(function[^{]*\{[^{}]*(?:setTheme|data-theme|\.theme-|themePicked)[^{}]*\})', text):
        print("Found theme function:", m.group(1)[:300])
