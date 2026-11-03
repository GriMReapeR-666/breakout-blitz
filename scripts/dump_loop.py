import re
import sys

content = open('external_references/setuprush/assets/main-NVMoWWYe.js', encoding='utf-8').read()

pos = content.find('requestAnimationFrame(ts)')
if pos != -1:
    chunk = content[max(0, pos - 15000):min(len(content), pos + 5000)]
    with open('scripts/extracted_render_loop.js', 'w', encoding='utf-8') as f:
        f.write(chunk)
    print(f"Dumped {len(chunk)} characters around render loop to scripts/extracted_render_loop.js")
else:
    print("Not found")
