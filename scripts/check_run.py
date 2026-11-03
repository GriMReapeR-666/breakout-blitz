import urllib.request
import json
import time
import os
import zipfile

repo = "GriMReapeR-666/breakout-blitz"
headers = {"User-Agent": "Mozilla/5.0"}

def check_and_wait():
    print(f"Monitoring GitHub Actions build for {repo}...", flush=True)
    for attempt in range(60):
        try:
            url = f"https://api.github.com/repos/{repo}/actions/runs"
            req = urllib.request.Request(url, headers=headers)
            with urllib.request.urlopen(req) as resp:
                data = json.loads(resp.read().decode())
                runs = data.get("workflow_runs", [])
                if not runs:
                    print("No runs found yet.", flush=True)
                    time.sleep(5)
                    continue
                latest = runs[0]
                run_id = latest["id"]
                status = latest["status"]
                conclusion = latest.get("conclusion")
                sha = latest.get("head_sha", "")[:7]
                print(f"[{attempt+1}/60] Run #{run_id} ({sha}): status={status}, conclusion={conclusion}", flush=True)
                
                if status == "completed":
                    if conclusion == "success":
                        print("Build SUCCEEDED! Fetching APK artifact...", flush=True)
                        download_artifact(run_id)
                        return True
                    else:
                        print(f"Build failed with conclusion: {conclusion}", flush=True)
                        return False
        except Exception as e:
            print(f"Error checking status: {e}", flush=True)
        time.sleep(10)
    return False

def download_artifact(run_id):
    try:
        url = f"https://api.github.com/repos/{repo}/actions/runs/{run_id}/artifacts"
        req = urllib.request.Request(url, headers=headers)
        with urllib.request.urlopen(req) as resp:
            data = json.loads(resp.read().decode())
            artifacts = data.get("artifacts", [])
            for art in artifacts:
                if "apk" in art["name"].lower():
                    art_id = art["id"]
                    dl_url = art["archive_download_url"]
                    print(f"Artifact found: {art['name']} ({art['size_in_bytes']} bytes), ID: {art_id}")
    except Exception as e:
        print(f"Artifact fetch error: {e}")

if __name__ == "__main__":
    check_and_wait()
