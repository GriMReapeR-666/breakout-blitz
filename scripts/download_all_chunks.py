import os
import re
import urllib.parse
import urllib.request
from pathlib import Path

BASE_URL = "https://setuprush.com"
ROOT = Path("external_references/setuprush")
HEADERS = {
    "User-Agent": "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Safari/537.36"
}

def resolve_ref(source_file, ref):
    ref = ref.strip().split("?")[0].split("#")[0]
    if ref.startswith("data:") or ref.startswith("http://") or ref.startswith("https://"):
        return None
    source_rel = source_file.relative_to(ROOT)
    source_dir = source_rel.parent
    resolved = (source_dir / ref).resolve()
    try:
        rel_to_root = resolved.relative_to(ROOT.resolve())
        return rel_to_root.as_posix()
    except ValueError:
        return None

def find_all_references():
    refs = set()
    for p in ROOT.rglob("*.*"):
        if p.suffix in [".js", ".css", ".html"]:
            try:
                text = p.read_text(encoding="utf-8", errors="ignore")
                
                # Dynamic imports import(`./foo.js`) or import("./foo.js")
                for m in re.finditer(r'''import\s*\(\s*[`'"](\.[^`'"]+)[`'"]\s*\)''', text):
                    resolved = resolve_ref(p, m.group(1))
                    if resolved:
                        refs.add(resolved)
                        
                # Static imports / from
                for m in re.finditer(r'''(?:from|import)\s*[`'"](\.[^`'"]+)[`'"]''', text):
                    resolved = resolve_ref(p, m.group(1))
                    if resolved:
                        refs.add(resolved)
                        
                # Direct string literals with hashes like foo-XXXXXXXX.js, foo-XXXXXXXX.css, foo-XXXXXXXX.jpg
                for m in re.finditer(r'''[`'"]([a-zA-Z0-9_\-]+-[a-zA-Z0-9_\-]{8}\.(?:js|css|png|jpg|jpeg|svg|webp|woff2?))[`'"]''', text):
                    val = m.group(1)
                    # Likely in assets/
                    refs.add(f"assets/{val}")
                    
                # Other assets: images, fonts, icons
                for m in re.finditer(r'''[`'"]((?:[a-zA-Z0-9_\-\/]+)\.(?:png|jpg|jpeg|svg|webp|gif|woff2?|ttf|otf|mp3|wav|ogg|json|webmanifest))[`'"]''', text):
                    raw = m.group(1).lstrip("/")
                    if not any(c in raw for c in ['<', '>', ':', '"', '|', '?', '*', '`', '$', '{', '}', ';', '(', ')']):
                        if (ROOT / raw).exists():
                            continue
                        # Try both root and assets/
                        refs.add(raw)
                        refs.add(f"assets/{raw}")

                # CSS url(...)
                for m in re.finditer(r'''url\(\s*["']?([^"')]+)["']?\s*\)''', text):
                    resolved = resolve_ref(p, m.group(1))
                    if resolved:
                        refs.add(resolved)
            except Exception as e:
                pass
    return refs

def download_batch():
    round_num = 1
    total_downloaded = 0
    
    while True:
        print(f"\n--- Scan Round {round_num} ---")
        refs = find_all_references()
        to_download = []
        for r in sorted(refs):
            target = ROOT / r
            if not target.exists():
                to_download.append(r)
                
        print(f"Found {len(to_download)} new candidate assets to fetch...")
        if not to_download:
            break
            
        round_downloaded = 0
        for item in to_download:
            target = ROOT / item
            url = f"{BASE_URL}/{item}"
            try:
                req = urllib.request.Request(url, headers=HEADERS)
                with urllib.request.urlopen(req, timeout=5) as resp:
                    data = resp.read()
                    ct = resp.headers.get_content_type()
                    # Filter out vercel html 404s
                    if "text/html" in ct and not item.endswith(".html"):
                        continue
                    if b"NOT_FOUND" in data or b"The page could not be found" in data:
                        continue
                    target.parent.mkdir(parents=True, exist_ok=True)
                    with open(target, "wb") as f:
                        f.write(data)
                    print(f"[OK] {item} ({len(data)} bytes)", flush=True)
                    round_downloaded += 1
            except urllib.error.HTTPError:
                pass
            except Exception:
                pass
                
        print(f"Round {round_num} downloaded: {round_downloaded} files")
        total_downloaded += round_downloaded
        if round_downloaded == 0:
            break
        round_num += 1
        
    print(f"\nCompleted! Total assets downloaded across rounds: {total_downloaded}")

if __name__ == "__main__":
    download_batch()
