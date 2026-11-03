import json
import re
from pathlib import Path

raw = Path('scripts/extracted_themes_raw.js').read_text(encoding='utf-8')
# Clean raw JS object into valid JSON
# raw is: I={night:{name:`Terminal`,desc:`White and red on black`,price:0,...},...}

# Strip I=
s = raw.strip()
if s.startswith('I='):
    s = s[2:]

# Replace backticks with double quotes
s = re.sub(r'`([^`]*)`', r'"\1"', s)

# Replace unquoted keys with quoted keys
# keys like name:, desc:, price:, light:, bg:, text:, etc.
s = re.sub(r'([a-zA-Z0-9_$]+)\s*:', r'"\1":', s)

# Replace !0 with true, !1 with false, 1e3 with 1000
s = s.replace('!0', 'true').replace('!1', 'false').replace('1e3', '1000')

# Remove trailing commas before } or ]
s = re.sub(r',\s*([}\]])', r'\1', s)

try:
    themes_data = json.loads(s)
    print(f"Successfully parsed {len(themes_data)} themes:")
    for k, v in themes_data.items():
        print(f"  {k:12} : {v.get('name'):12} (price: {v.get('price'):4}, light: {v.get('light', False)}) - bg: {v.get('bg')}, bull: {v.get('bull')}, bear: {v.get('bear')}")
    
    Path('scripts/themes_catalog.json').write_text(json.dumps(themes_data, indent=2), encoding='utf-8')
    print("Saved to scripts/themes_catalog.json")
except Exception as e:
    print("Error parsing JSON:", e)
    # Let's inspect where it failed
    import traceback
    traceback.print_exc()
