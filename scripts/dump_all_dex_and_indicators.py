from pathlib import Path
import re

text = Path('app/src/main/assets/setuprush/assets/main-NVMoWWYe.js').read_text(encoding='utf-8')

# Search for F=[ (DEX items)
pos_f = text.find('F=[')
if pos_f != -1:
    # let's count brackets to get full F
    depth = 0
    end = pos_f
    for i in range(pos_f, len(text)):
        if text[i] == '[':
            depth += 1
        elif text[i] == ']':
            depth -= 1
            if depth == 0:
                end = i + 1
                break
    print("=== FULL DEX (F) ===")
    print(text[pos_f:min(pos_f+3000, end)])
    Path('scripts/dex_extracted.js').write_text(text[pos_f:end], encoding='utf-8')

# Search for vi=[ (Indicators)
pos_vi = text.find('vi=[')
if pos_vi != -1:
    depth = 0
    end = pos_vi
    for i in range(pos_vi, len(text)):
        if text[i] == '[':
            depth += 1
        elif text[i] == ']':
            depth -= 1
            if depth == 0:
                end = i + 1
                break
    print("\n=== FULL INDICATORS (vi) ===")
    print(text[pos_vi:min(pos_vi+2000, end)])
    Path('scripts/indicators_extracted.js').write_text(text[pos_vi:end], encoding='utf-8')

# Search for Ir=[ (Auto Strategies)
pos_ir = text.find('Ir=[')
if pos_ir != -1:
    depth = 0
    end = pos_ir
    for i in range(pos_ir, len(text)):
        if text[i] == '[':
            depth += 1
        elif text[i] == ']':
            depth -= 1
            if depth == 0:
                end = i + 1
                break
    print("\n=== FULL AUTO STRATEGIES (Ir) ===")
    print(text[pos_ir:end])
    Path('scripts/auto_strategies_extracted.js').write_text(text[pos_ir:end], encoding='utf-8')
