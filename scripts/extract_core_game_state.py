import sys
sys.stdout.reconfigure(encoding='utf-8')

with open('app/src/main/assets/setuprush/assets/main-NVMoWWYe.js', encoding='utf-8', errors='ignore') as f:
    c = f.read()

snippet = c[235000:245000]
print("Snippet from 235000 to 245000:\n")
print(snippet)
