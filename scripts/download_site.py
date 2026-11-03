import os
import re
import urllib.parse
import urllib.request
from pathlib import Path

BASE_URL = "https://setuprush.com"
OUTPUT_DIR = Path("external_references/setuprush")

HEADERS = {
    "User-Agent": "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Safari/537.36"
}

visited_urls = set()
downloaded_files = set()
failed_urls = set()

def fetch_data(url):
    req = urllib.request.Request(url, headers=HEADERS)
    with urllib.request.urlopen(req, timeout=15) as resp:
        return resp.read(), resp.headers.get_content_type()

def save_file(rel_path, data):
    # Strip leading slash and query params
    rel_path = rel_path.lstrip("/").split("?")[0]
    out_path = OUTPUT_DIR / rel_path
    out_path.parent.mkdir(parents=True, exist_ok=True)
    with open(out_path, "wb") as f:
        f.write(data)
    print(f"[SAVED] {rel_path} ({len(data)} bytes)")
    downloaded_files.add(rel_path)

def extract_urls_from_text(text, base_url):
    urls = set()
    # Match href, src, url(...), import statements
    patterns = [
        r'''(?:href|src)=["']([^"']+)["']''',
        r'''url\((?:["']?)([^"')]+)(?:["']?)\)''',
        r'''from\s+["']([^"']+)["']''',
        r'''import\s+["']([^"']+)["']''',
        r'''["'](\./assets/[^"']+)["']''',
        r'''["'](/assets/[^"']+)["']''',
        r'''["'](assets/[^"']+)["']''',
        r'''["']([^"']+\.(?:png|jpg|jpeg|svg|webp|gif|woff|woff2|ttf|otf|mp3|wav|ogg|json|webmanifest|css|js))["']'''
    ]
    for pat in patterns:
        for match in re.finditer(pat, text, re.IGNORECASE):
            raw_url = match.group(1).strip()
            if raw_url.startswith("data:") or raw_url.startswith("#") or raw_url.startswith("mailto:"):
                continue
            resolved = urllib.parse.urljoin(base_url, raw_url)
            # Only stay within setuprush.com
            parsed = urllib.parse.urlparse(resolved)
            if parsed.netloc == "setuprush.com":
                urls.add(resolved)
    return urls

def process_queue():
    queue = [
        "https://setuprush.com/",
        "https://setuprush.com/play",
        "https://setuprush.com/manifest.webmanifest"
    ]
    
    while queue:
        current_url = queue.pop(0)
        # Normalize url
        parsed = urllib.parse.urlparse(current_url)
        clean_url = urllib.parse.urlunparse((parsed.scheme, parsed.netloc, parsed.path, '', '', ''))
        if clean_url in visited_urls:
            continue
        visited_urls.add(clean_url)

        try:
            print(f"[FETCH] {clean_url}")
            data, content_type = fetch_data(current_url)
            
            # Determine relative path
            path = parsed.path
            if not path or path == "/":
                rel_path = "index.html"
            elif path.endswith("/"):
                rel_path = path.lstrip("/") + "index.html"
            elif "." not in os.path.basename(path):
                # Probably a route like /play
                rel_path = path.lstrip("/") + ".html"
            else:
                rel_path = path.lstrip("/")
                
            save_file(rel_path, data)
            
            # If text, css, html, js, or json, scan for more assets
            is_text = False
            text_content = ""
            for enc in ["utf-8", "latin1"]:
                try:
                    text_content = data.decode(enc)
                    is_text = True
                    break
                except UnicodeDecodeError:
                    pass
            
            if is_text and (any(rel_path.endswith(ext) for ext in [".html", ".css", ".js", ".json", ".webmanifest"]) or "text" in content_type or "javascript" in content_type):
                discovered = extract_urls_from_text(text_content, current_url)
                for d in discovered:
                    d_parsed = urllib.parse.urlparse(d)
                    d_clean = urllib.parse.urlunparse((d_parsed.scheme, d_parsed.netloc, d_parsed.path, '', '', ''))
                    if d_clean not in visited_urls and d not in queue:
                        queue.append(d)
                        
        except Exception as e:
            print(f"[ERROR] Failed {clean_url}: {e}")
            failed_urls.add(clean_url)

if __name__ == "__main__":
    process_queue()
    print("\n--- Summary ---")
    print(f"Total downloaded: {len(downloaded_files)}")
    print(f"Total failed: {len(failed_urls)}")
