import os
import io
import subprocess
import urllib.request
import zipfile
import hashlib

def get_github_token():
    p = subprocess.Popen(
        ['git', 'credential', 'fill'],
        stdin=subprocess.PIPE,
        stdout=subprocess.PIPE,
        stderr=subprocess.PIPE,
        text=True
    )
    out, _ = p.communicate('protocol=https\nhost=github.com\n\n', timeout=5)
    for line in out.splitlines():
        if line.startswith('password='):
            return line.split('=', 1)[1]
    raise RuntimeError("Could not retrieve GitHub token from git credentials.")

class CleanRedirectHandler(urllib.request.HTTPRedirectHandler):
    """Avoid sending GitHub Authorization token to Azure Blob Storage."""
    def redirect_request(self, req, fp, code, msg, headers, newurl):
        r = super().redirect_request(req, fp, code, msg, headers, newurl)
        if r and 'blob.core.windows.net' in newurl:
            r.headers.pop('Authorization', None)
            r.headers.pop('authorization', None)
        return r

def main():
    token = get_github_token()
    artifact_id = 13211157500
    artifact_url = f"https://api.github.com/repos/GriMReapeR-666/breakout-blitz/actions/artifacts/{artifact_id}/zip"
    
    print(f"Connecting to GitHub Actions artifact endpoint for artifact #{artifact_id}...")
    opener = urllib.request.build_opener(CleanRedirectHandler)
    req = urllib.request.Request(
        artifact_url,
        headers={
            'Authorization': f'Bearer {token}',
            'Accept': 'application/vnd.github.v3+json',
            'User-Agent': 'BreakoutBlitz-Downloader'
        }
    )
    
    with opener.open(req) as resp:
        zip_bytes = resp.read()
    
    print(f"Downloaded ZIP archive: {len(zip_bytes):,} bytes ({len(zip_bytes)/(1024*1024):.2f} MB)")
    
    # Save the raw zip archive
    zip_path = "BreakoutBlitz-Debug-APK.zip"
    with open(zip_path, "wb") as f:
        f.write(zip_bytes)
    print(f"Saved archive to: {os.path.abspath(zip_path)}")
    
    # Extract the APK
    apk_extracted_path = None
    with zipfile.ZipFile(io.BytesIO(zip_bytes)) as z:
        print(f"Archive contents: {z.namelist()}")
        for name in z.namelist():
            if name.endswith(".apk"):
                apk_data = z.read(name)
                target_apk_name = "BreakoutBlitz-debug.apk"
                with open(target_apk_name, "wb") as out_apk:
                    out_apk.write(apk_data)
                apk_extracted_path = os.path.abspath(target_apk_name)
                sha256 = hashlib.sha256(apk_data).hexdigest()
                print(f"\n=======================================================")
                print(f"APK EXTRACTED SUCCESSFULLY!")
                print(f"File:      {apk_extracted_path}")
                print(f"Size:      {len(apk_data):,} bytes ({len(apk_data)/(1024*1024):.2f} MB)")
                print(f"SHA-256:   {sha256}")
                print(f"=======================================================\n")
                break

    if not apk_extracted_path:
        print("Warning: No .apk found in archive.")

if __name__ == "__main__":
    main()
