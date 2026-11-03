import re
import json

with open('scripts/indicators_extracted.js', 'r', encoding='utf-8') as f:
    text = f.read()

# Split by "{id:`"
chunks = text.split('{id:`')[1:]
print(f"Total indicator chunks found: {len(chunks)}")

indicators = []
for idx, chunk in enumerate(chunks):
    parts = chunk.split('`')
    if len(parts) >= 8:
        # id is parts[0]
        id_ = parts[0]
        # name is parts[2]
        name = parts[2]
        # short is parts[4]
        short = parts[4]
        
        # pane is in chunk after short
        pane_match = re.search(r'pane:([^,]+),', chunk)
        pane = False
        if pane_match:
            p_val = pane_match.group(1).strip()
            pane = (p_val == '!0' or p_val == 'true')
            
        # tag is parts[6]
        tag = parts[6]
        
        # description / source summary
        src_match = re.search(r'src:`([^`]+)`', chunk)
        src = src_match.group(1) if src_match else ""
        
        # Extract title or description from src
        title_line = ""
        for line in src.splitlines():
            if 'indicator(' in line:
                title_line = line
                break
        
        indicators.append({
            'id': id_,
            'name': name,
            'short': short,
            'pane': pane,
            'tag': tag,
            'title_line': title_line
        })

print(f"Successfully structured {len(indicators)} indicators!")
with open('scripts/all_indicators_catalog.json', 'w', encoding='utf-8') as f:
    json.dump(indicators, f, indent=2)

for i, ind in enumerate(indicators[:15]):
    print(f"{i+1:2d}. [{ind['tag']:12s}] {ind['id']:16s} | {ind['name']:25s} | {ind['short']:6s} | Pane: {ind['pane']}")
