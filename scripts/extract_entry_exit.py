import sys, re
sys.stdout.reconfigure(encoding='utf-8')

with open('app/src/main/assets/setuprush/assets/main-NVMoWWYe.js', encoding='utf-8', errors='ignore') as f:
    c = f.read()

snippet = c[230000:237000]
print("Snippet from 230000 to 237000:\n")
print(snippet)
