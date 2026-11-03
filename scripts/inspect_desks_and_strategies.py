from pathlib import Path
import re

text = Path('app/src/main/assets/setuprush/assets/main-NVMoWWYe.js').read_text(encoding='utf-8')

# 1. Search for `ai=` or `const ai=` or `let ai=`
# Where ai is the DESKS array
pos_desks = text.find('DESKS:ai')
if pos_desks != -1:
    # let's find `ai=`
    matches = re.findall(r'(\bai\s*=\s*\[[^\]]+\])', text)
    if matches:
        print("=== DESKS (ai) ===")
        print(matches[0][:1500])

# 2. Search for DEX: F (Pattern encyclopedia / Dex)
pos_dex = text.find('DEX:F')
if pos_dex != -1:
    matches = re.findall(r'(\bF\s*=\s*\[[^\]]+\])', text)
    if matches:
        print("\n=== DEX (F) ===")
        print(matches[0][:1500])

# 3. Search for AUTO_STRATEGIES: Ir
matches = re.findall(r'(\bIr\s*=\s*\[[^\]]+\])', text)
if matches:
    print("\n=== AUTO_STRATEGIES (Ir) ===")
    print(matches[0][:1500])

# Search for LIBRARY (Indicators library)
matches = re.findall(r'(\bvi\s*=\s*\[[^\]]+\])', text)
if matches:
    print("\n=== INDICATORS LIBRARY (vi) ===")
    print(matches[0][:1500])
