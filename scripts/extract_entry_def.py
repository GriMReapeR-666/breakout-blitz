import sys, re
sys.stdout.reconfigure(encoding='utf-8')

with open('app/src/main/assets/setuprush/assets/main-NVMoWWYe.js', encoding='utf-8', errors='ignore') as f:
    c = f.read()

snippet = c[225000:232000]
print("Snippet from 225000 to 232000:\n")
print(snippet)
