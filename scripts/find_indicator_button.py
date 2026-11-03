import sys
import re

sys.stdout.reconfigure(encoding='utf-8')

with open("app/src/main/assets/setuprush/assets/main-NVMoWWYe.js", "r", encoding="utf-8", errors="ignore") as fp:
    content = fp.read()

# Let's find where the indicator modal component is invoked or where the button with "fx" or "IND" or "Indicators" is
matches = re.finditer(r"Indicators|toggleIndicator|Pine editor|Open indicators", content, re.IGNORECASE)
for m in list(matches)[:10]:
    pos = m.start()
    print("--- Found match for indicators UI ---")
    print(content[max(0, pos-150):min(len(content), pos+200)])
