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

class CleanRedirectHandler(urllib.request.HTTPRedirectHandler):
    """Avoid sending GitHub Authorization token to Azure Blob Storage."""
    def redirect_request(self, req, fp, code, msg, headers, newurl):
        r = super().redirect_request(req, fp, code, msg, headers, newurl)
        if r and "blob.core.windows.net" in newurl:
            r.headers.pop("Authorization", None)
            r.headers.pop("authorization", None)
        return r

token = get_github_token()
# Get latest workflow run
runs_url = "https://api.github.com/repos/GriMReapeR-666/breakout-blitz/actions/runs"
req_runs = urllib.request.Request(
    runs_url,
    headers={
        "Authorization": f"Bearer {token}",
        "Accept": "application/vnd.github.v3+json",
        "User-Agent": "BreakoutBlitz-Monitor"
    }
)
with urllib.request.urlopen(req_runs) as resp:
    runs_data = json.loads(resp.read().decode("utf-8"))
latest_run = runs_data.get("workflow_runs", [])[0]
run_id = latest_run["id"]
print(f"Inspecting Latest Run #{run_id} ({latest_run.get('conclusion') or latest_run.get('status')}) for commit {latest_run.get('head_commit', {}).get('id')[:7]}")

url = f"https://api.github.com/repos/GriMReapeR-666/breakout-blitz/actions/runs/{run_id}/jobs"
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

job_id = None
for j in data.get("jobs", []):
    print(f"Job: {j['name']}, Status: {j['status']}, Conclusion: {j['conclusion']}")
    job_id = j['id']
    for s in j.get("steps", []):
        if s.get("conclusion") == "failure":
            print(f"  Failed step: {s.get('name')}")

if job_id:
    log_url = f"https://api.github.com/repos/GriMReapeR-666/breakout-blitz/actions/jobs/{job_id}/logs"
    opener = urllib.request.build_opener(CleanRedirectHandler)
    req_log = urllib.request.Request(
        log_url,
        headers={
            "Authorization": f"Bearer {token}",
            "User-Agent": "BreakoutBlitz-Monitor"
        }
    )
    try:
        with opener.open(req_log) as resp_log:
            lines = resp_log.read().decode("utf-8", errors="replace").splitlines()
        print(f"\n--- Total log lines: {len(lines)} ---")
        # Filter for ERROR or failure lines
        error_lines = [line for line in lines if "e: " in line or "error" in line.lower() or "failed" in line.lower() or "exception" in line.lower()]
        print("\n--- Key error lines ---")
        for line in error_lines[-30:]:
            print(line)
        print("\n--- Last 40 lines of job log ---")
        for line in lines[-40:]:
            print(line)
    except Exception as e:
        print("Could not fetch job log:", e)
