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
    "ChartWall": "ChartWall-x--1dyXO.js",
    "Shop": "Shop-B21SFhwa.js"
}

assets_dir = r"app/src/main/assets/setuprush/assets"

for name, fname in files_to_inspect.items():
    path = os.path.join(assets_dir, fname)
    if os.path.exists(path):
        with open(path, "r", encoding="utf-8", errors="ignore") as fp:
            c = fp.read()
            # find all backtick strings
            backticks = re.findall(r"`([^`]{4,80})`", c)
            # filter readable English text
            readable = [b.strip() for b in backticks if re.search(r"[a-zA-Z]{3,}\s+[a-zA-Z]{3,}", b) and not b.startswith("http") and not "<" in b]
            print(f"=== {name} ===")
            print(readable[:8])
            print()
