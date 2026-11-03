import os
import re
import urllib.parse
import urllib.request
from concurrent.futures import ThreadPoolExecutor, as_completed
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
                
                # Dynamic imports
                for m in re.finditer(r'''import\s*\(\s*[`'"](\.[^`'"]+)[`'"]\s*\)''', text):
                    resolved = resolve_ref(p, m.group(1))
                    if resolved:
                        refs.add(resolved)
                        
                # Static imports
                for m in re.finditer(r'''(?:from|import)\s*[`'"](\.[^`'"]+)[`'"]''', text):
                    resolved = resolve_ref(p, m.group(1))
                    if resolved:
                        refs.add(resolved)
                        
                # Hashes foo-XXXXXXXX.ext
                for m in re.finditer(r'''[`'"]([a-zA-Z0-9_\-]+-[a-zA-Z0-9_\-]{8}\.(?:js|css|png|jpg|jpeg|svg|webp|woff2?))[`'"]''', text):
                    val = m.group(1)
                    refs.add(f"assets/{val}")
                    
                # General media & data assets
                for m in re.finditer(r'''[`'"]((?:[a-zA-Z0-9_\-\/]+)\.(?:png|jpg|jpeg|svg|webp|gif|woff2?|ttf|otf|mp3|wav|ogg|webmanifest))[`'"]''', text):
                    raw = m.group(1).lstrip("/")
                    if not any(c in raw for c in ['<', '>', ':', '"', '|', '?', '*', '`', '$', '{', '}', ';', '(', ')', ',']):
                        refs.add(raw)
                        refs.add(f"assets/{raw}")

                # CSS url(...)
                for m in re.finditer(r'''url\(\s*["']?([^"')]+)["']?\s*\)''', text):
                    resolved = resolve_ref(p, m.group(1))
                    if resolved:
                        refs.add(resolved)
            except Exception:
                pass
    return refs

def fetch_one(item):
    target = ROOT / item
    if target.exists():
        return None
    url = f"{BASE_URL}/{item}"
    try:
        req = urllib.request.Request(url, headers=HEADERS)
        with urllib.request.urlopen(req, timeout=5) as resp:
            data = resp.read()
            ct = resp.headers.get_content_type()
            if "text/html" in ct and not item.endswith(".html"):
                return None
            if b"NOT_FOUND" in data or b"The page could not be found" in data:
                return None
            target.parent.mkdir(parents=True, exist_ok=True)
            with open(target, "wb") as f:
                f.write(data)
            return (item, len(data))
    except Exception:
        return None

def run_downloader():
    round_num = 1
    total_downloaded = 0
    
    while True:
        print(f"\n--- Scan Round {round_num} ---", flush=True)
        refs = find_all_references()
        candidates = [r for r in sorted(refs) if not (ROOT / r).exists()]
        print(f"Candidates to check: {len(candidates)}", flush=True)
        if not candidates:
            break
            
        round_downloaded = 0
        with ThreadPoolExecutor(max_workers=16) as pool:
            futures = {pool.submit(fetch_one, item): item for item in candidates}
            for fut in as_completed(futures):
                res = fut.result()
                if res:
                    item, size = res
                    print(f"[OK] {item} ({size} bytes)", flush=True)
                    round_downloaded += 1
                    
        print(f"Round {round_num} downloaded: {round_downloaded} files", flush=True)
        total_downloaded += round_downloaded
        if round_downloaded == 0:
            break
        round_num += 1
        
    print(f"\nAll done! Total downloaded: {total_downloaded}", flush=True)

if __name__ == "__main__":
    run_downloader()
