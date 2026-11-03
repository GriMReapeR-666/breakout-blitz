import sys
sys.stdout.reconfigure(encoding='utf-8')

with open('app/src/main/assets/setuprush/assets/landing-m10Niqv9.js', encoding='utf-8', errors='ignore') as f:
    content = f.read()

snippet = content[65000:75000]
print("Snippet from 65000 to 75000:\n")
print(snippet)
