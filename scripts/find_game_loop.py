import sys, re
sys.stdout.reconfigure(encoding='utf-8')

for name in ['landing-m10Niqv9.js', 'main-NVMoWWYe.js']:
    path = f'app/src/main/assets/setuprush/assets/{name}'
    with open(path, encoding='utf-8', errors='ignore') as f:
        content = f.read()
    print(f"\n=================== {name} ({len(content)} bytes) ===================")
    
    # Search for press / release
    matches = [m.start() for m in re.finditer(r'press\s*\(|release\s*\(', content)]
    print(f"Matches for press/release: {len(matches)}")
    for idx in matches[:5]:
        snippet = content[max(0, idx-100):min(len(content), idx+200)]
        print(f"--- snippet at {idx}: ---\n{snippet}\n")
