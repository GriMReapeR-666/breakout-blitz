import json

with open('scripts/all_indicators_catalog.json', 'r', encoding='utf-8') as f:
    inds = json.load(f)

by_tag = {}
for i in inds:
    t = i['tag']
    by_tag.setdefault(t, []).append(i)

print('=== TOTAL INDICATORS FROM SITE: 68 ===\n')
for t, lst in by_tag.items():
    print(f"=== CATEGORY: {t.upper()} ({len(lst)} indicators) ===")
    for ind in lst:
        pane_str = "[SUB-PANE]" if ind['pane'] else "[OVERLAY]"
        print(f"  • {ind['id']:16s} | {ind['name']:28s} | {ind['short']:6s} | {pane_str}")
    print()
