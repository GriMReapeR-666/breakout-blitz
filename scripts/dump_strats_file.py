from pathlib import Path
t = Path('app/src/main/assets/setuprush/assets/main-NVMoWWYe.js').read_text(encoding='utf-8')
p = t.find('Ir=Object.freeze([')
# find closing ])
end = t.find(']);', p)
if end != -1:
    snippet = t[p:end+2]
else:
    snippet = t[p:p+2500]

Path('scripts/strategies_full.txt').write_text(snippet, encoding='utf-8')
print("Saved to scripts/strategies_full.txt, size:", len(snippet))
