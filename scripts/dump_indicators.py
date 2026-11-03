import sys
import re
import json

sys.stdout.reconfigure(encoding='utf-8')

with open("app/src/main/assets/setuprush/assets/main-NVMoWWYe.js", "r", encoding="utf-8", errors="ignore") as fp:
    content = fp.read()

# Let's extract the full vi array
start_idx = content.find("vi=[{id:")
if start_idx != -1:
    # find where vi ends. vi=[{...}];
    # We can match regex for all {id:`...`,name:`...`,short:`...`,pane:...}
    matches = re.findall(r"\{id:`([^`]+)`,name:`([^`]+)`,short:`([^`]+)`,pane:(![01]|true|false),tag:`([^`]+)`", content[start_idx:start_idx+50000])
    print(f"Found {len(matches)} indicators in vi:")
    for idx, m in enumerate(matches):
        print(f"{idx+1}. ID: {m[0]} | Name: {m[1]} | Short: {m[2]} | Pane: {m[3]} | Tag: {m[4]}")
