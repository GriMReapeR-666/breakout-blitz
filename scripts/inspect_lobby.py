import re
from pathlib import Path

ROOT = Path("app/src/main/assets/setuprush/assets")

files = [
    ROOT / "Arena-CO6oEvqE.js",
    ROOT / "arena-BMV4T59r.js",
    ROOT / "GhostRace-etG3Al2V.js",
    ROOT / "main-NVMoWWYe.js"
]

print("--- Inspecting Lobby / Arena / Room Mechanisms ---")
for f in files:
    if not f.exists():
        continue
    text = f.read_text(encoding="utf-8", errors="ignore")
    # Search for room, lobby, host, join, match
    matches = re.finditer(r'.{0,80}(?:createRoom|joinRoom|roomCode|lobby|host|ghost|seed|race).{0,80}', text, re.IGNORECASE)
    print(f"\nMatches in {f.name}:")
    count = 0
    for m in matches:
        print(" -", m.group(0).strip())
        count += 1
        if count >= 8:
            break
