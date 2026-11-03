import sys
import json
import urllib.request
import subprocess

def get_github_token():
    p = subprocess.Popen(["git", "credential", "fill"], stdin=subprocess.PIPE, stdout=subprocess.PIPE, text=True)
    out, _ = p.communicate("protocol=https\nhost=github.com\n\n", timeout=5)
    for line in out.splitlines():
        if line.startswith("password="):
            return line.split("=", 1)[1]
    raise RuntimeError("No token")

token = get_github_token()
url = "https://api.github.com/repos/GriMReapeR-666/breakout-blitz/actions/runs"
req = urllib.request.Request(
    url,
    headers={
        "Authorization": f"Bearer {token}",
        "Accept": "application/vnd.github.v3+json",
        "User-Agent": "BreakoutBlitz-Monitor"
    }
)
with urllib.request.urlopen(req) as resp:
    data = json.loads(resp.read().decode("utf-8"))

runs = data.get("workflow_runs", [])
if runs:
    latest = runs[0]
    commit_msg = latest.get("head_commit", {}).get("message", "").splitlines()[0]
    print(f"Latest run ID: #{latest['id']}")
    print(f"Status:     {latest['status']}")
    print(f"Conclusion: {latest['conclusion']}")
    print(f"Commit:     {latest['head_sha'][:7]} - {commit_msg}")
    print(f"URL:        {latest['html_url']}")
else:
    print("No workflow runs found.")
