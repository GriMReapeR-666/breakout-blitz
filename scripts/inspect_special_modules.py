import os
import glob
import re

files_to_inspect = {
    "GhostRace": "GhostRace-etG3Al2V.js",
    "GestureLab": "GestureLab-DpmiCTLl.js",
    "ClockIn": "ClockIn-eQ2xK_oW.js",
    "DailyPot": "DailyPot-BoQG-z1-.js",
    "Arena": "Arena-CO6oEvqE.js",
    "LiveMarkets": "LiveMarkets-CvNblRZM.js",
    "Wallet": "Wallet-D1GN1615.js",
    "Career": "Career-BejjhFbI.js",
    "Reels": "Reels-D0w6DVjV.js",
    "ChartWall": "ChartWall-x--1dyXO.js"
}

assets_dir = r"app/src/main/assets/setuprush/assets"

for name, fname in files_to_inspect.items():
    path = os.path.join(assets_dir, fname)
    if os.path.exists(path):
        with open(path, "r", encoding="utf-8", errors="ignore") as fp:
            c = fp.read()
            print(f"=== {name} ({fname}, {len(c)} chars) ===")
            # Look for exported component or title or descriptions
            matches = re.findall(r'["\']([^"\']{15,100})["\']', c)
            # print first 10 interesting descriptive strings
            sample_strings = [s for s in matches if any(w in s.lower() for w in ["race", "ghost", "lab", "streak", "pot", "match", "wallet", "desk", "level", "solana", "hyperliquid", "arena", "clock", "trade", "sound", "chest", "boost"])]
            print("Key concepts:", sample_strings[:10])
            print()
