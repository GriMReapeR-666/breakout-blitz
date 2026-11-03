import re
import sys

sys.stdout.reconfigure(encoding='utf-8')
content = open('external_references/setuprush/assets/main-NVMoWWYe.js', encoding='utf-8').read()

print(f"Bundle size: {len(content):,} characters")

# Find canvas rendering loops
for needle in ['getContext("2d")', 'getContext(\'2d\')', 'requestAnimationFrame', 'devicePixelRatio']:
    hits = list(re.finditer(re.escape(needle), content))
    print(f"\n--- Needle: {needle} (Hits: {len(hits)}) ---")
    for h in hits[:3]:
        start = max(0, h.start() - 100)
        end = min(len(content), h.end() + 200)
        print("  Slice:", repr(content[start:end]))
