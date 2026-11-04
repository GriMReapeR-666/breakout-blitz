import urllib.request
import json
import time
import subprocess
import sys

repo = "GriMReapeR-666/breakout-blitz"
headers = {"User-Agent": "Mozilla/5.0"}
target_sha = "a3c3ace"

print(f"Monitoring GitHub Actions run for commit {target_sha}...", flush=True)

for i in range(40):
    try:
        url = f"https://api.github.com/repos/{repo}/actions/runs"
        req = urllib.request.Request(url, headers=headers)
        with urllib.request.urlopen(req, timeout=10) as resp:
            data = json.loads(resp.read().decode())
            runs = data.get("workflow_runs", [])
            target_run = None
            for r in runs:
                if r["head_sha"].startswith(target_sha):
                    target_run = r
                    break
            
            if target_run:
                run_id = target_run["id"]
                status = target_run["status"]
                conclusion = target_run.get("conclusion")
                print(f"[{i+1}/40] Run #{run_id}: status={status}, conclusion={conclusion}", flush=True)
                if status == "completed":
                    if conclusion == "success":
                        print("Build SUCCESS! Downloading artifact now...", flush=True)
                        subprocess.run([sys.executable, "scripts/download_apk.py"], check=True)
                        sys.exit(0)
                    else:
                        print(f"Build finished with failure conclusion: {conclusion}", flush=True)
                        sys.exit(1)
            else:
                print(f"[{i+1}/40] Run for {target_sha} not found yet...", flush=True)
    except Exception as e:
        print(f"Error checking run: {e}", flush=True)
    time.sleep(10)

print("Timed out waiting for run.")
sys.exit(1)
