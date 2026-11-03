with open("app/src/main/assets/setuprush/assets/LiveMarkets-CvNblRZM.js", "r", encoding="utf-8", errors="ignore") as fp:
    c = fp.read()
    print("=== LiveMarkets ===")
    print(c[:1500])

with open("app/src/main/assets/setuprush/assets/MarketFlap-DEq1M3wH.js", "r", encoding="utf-8", errors="ignore") as fp:
    c = fp.read()
    print("=== MarketFlap ===")
    print(c[:1000])
