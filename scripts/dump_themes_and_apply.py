from pathlib import Path
import re
import json

text = Path('app/src/main/assets/setuprush/assets/main-NVMoWWYe.js').read_text(encoding='utf-8')

# Find where I={night: starts
pos = text.find('I={night:')
if pos != -1:
    # Find matching closing brace or end of theme definitions
    # It ends before the next function or declaration
    snippet = text[pos:pos+4000]
    # let's find the closing of I
    # Count braces
    brace_count = 0
    end_pos = pos
    started = False
    for i in range(pos, len(text)):
        if text[i] == '{':
            brace_count += 1
            started = True
        elif text[i] == '}':
            brace_count -= 1
            if started and brace_count == 0:
                end_pos = i + 1
                break
    full_I = text[pos:end_pos]
    print("=== FULL THEMES MAP (I) ===")
    print(full_I)
    
    # Save to a json / file for easy reading
    Path('scripts/extracted_themes_raw.js').write_text(full_I, encoding='utf-8')

# Find full Ni(e) function
pos_ni = text.find('function Ni(e){')
if pos_ni != -1:
    brace_count = 0
    end_ni = pos_ni
    started = False
    for i in range(pos_ni, len(text)):
        if text[i] == '{':
            brace_count += 1
            started = True
        elif text[i] == '}':
            brace_count -= 1
            if started and brace_count == 0:
                end_ni = i + 1
                break
    full_ni = text[pos_ni:end_ni]
    print("\n=== FULL applyTheme (Ni) FUNCTION ===")
    print(full_ni)
    Path('scripts/extracted_apply_theme_raw.js').write_text(full_ni, encoding='utf-8')
