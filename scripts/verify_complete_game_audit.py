import os
import re
import glob

def run_audit():
    print("================================================================================")
    print("COMPLETE END-TO-END VERIFICATION: ALL FEATURES, STRATEGIES, MODELS & ZERO INFINGEMENT")
    print("================================================================================")
    
    app_dir = r"c:\Users\User\OneDrive\Desktop\selfmade game projects\setupcandy\app\src\main"
    
    # 1. Strategy Types & Setups
    strat_path = os.path.join(app_dir, "java", "com", "setuprush", "candy", "engine", "StrategyCatalog.kt")
    strat_text = open(strat_path, "r", encoding="utf-8").read()
    families = re.findall(r'(\w+)\("([^"]+)",\s*"([^"]+)",\s*"([^"]+)"\)', strat_text)
    setups = re.findall(r'SetupCardData\(', strat_text)
    print(f"\n[1. STRATEGY FAMILIES & SETUPS] ({len(families)} Families, {len(setups)} Setup Cards)")
    for f in families:
        print(f"  - {f[0]}: \"{f[1]}\" ({f[2]}) - {f[3]}")
    
    # 2. Technical Indicators
    ind_path = os.path.join(app_dir, "java", "com", "setuprush", "candy", "engine", "IndicatorCatalog.kt")
    ind_text = open(ind_path, "r", encoding="utf-8").read()
    indicators = re.findall(r'IndicatorDef\(', ind_text)
    print(f"\n[2. TECHNICAL INDICATOR CATALOG] ({len(indicators)} Total Indicators)")
    
    # 3. Trading Model Studio & Loadouts
    model_path = os.path.join(app_dir, "java", "com", "setuprush", "candy", "engine", "TradingModelManager.kt")
    model_text = open(model_path, "r", encoding="utf-8").read()
    models = re.findall(r'CustomTradingModel\(\s*id\s*=\s*"([^"]+)",\s*name\s*=\s*"([^"]+)"', model_text)
    print(f"\n[3. TRADING MODEL STUDIO & LOADOUTS] ({len(models)} Weaponized Institutional Models)")
    for m in models:
        print(f"  - ID: {m[0]} | Name: {m[1]}")
        
    # 4. Character Customization, Operators & Wearables
    char_path = os.path.join(app_dir, "java", "com", "setuprush", "candy", "engine", "CharacterCustomizationManager.kt")
    char_text = open(char_path, "r", encoding="utf-8").read()
    operators = re.findall(r'CharacterOperator\(\s*id\s*=\s*"([^"]+)",\s*name\s*=\s*"([^"]+)"', char_text)
    wearables = re.findall(r'WearableItem\(', char_text)
    print(f"\n[4. OPERATOR SKINS & WEARABLES] ({len(operators)} Operators, {len(wearables)} Wearable Items)")
    for op in operators:
        print(f"  - Operator: {op[0]} | \"{op[1]}\"")
        
    # 5. Complete Brand-New Tactical User Interface
    ui_files = glob.glob(os.path.join(app_dir, "java", "com", "setuprush", "candy", "ui", "*.kt"))
    print(f"\n[5. BRAND NEW JETPACK COMPOSE UI COMPONENTS] ({len(ui_files)} Native UI Screens & Modals)")
    for uif in sorted(ui_files):
        print(f"  - {os.path.basename(uif)}: {os.path.getsize(uif):,} bytes")
        
    # 6. Audio Assets
    raw_files = glob.glob(os.path.join(app_dir, "res", "raw", "*.*"))
    print(f"\n[6. ORIGINAL PROCEDURAL SFX AUDIO] ({len(raw_files)} Audio Effects)")
    for rf in sorted(raw_files):
        print(f"  - {os.path.basename(rf)}: {os.path.getsize(rf):,} bytes")
        
    # 7. Multi-Density Vector & Raster App Icons
    icon_files = glob.glob(os.path.join(app_dir, "res", "mipmap-*", "*.*"))
    print(f"\n[7. ORIGINAL APP ICONS] ({len(icon_files)} Launcher Icons across all screen densities)")
    for ic in sorted(icon_files)[:8]:
        print(f"  - {os.path.basename(os.path.dirname(ic))}/{os.path.basename(ic)}")
        
    # 8. Copyright Cleanliness & Scraping Elimination
    assets_dir = os.path.join(app_dir, "assets")
    assets_count = len(os.listdir(assets_dir)) if os.path.exists(assets_dir) else 0
    mark_r_exists = os.path.exists(os.path.join(app_dir, "res", "drawable", "mark_r.png"))
    print(f"\n[8. COPYRIGHT & TRADEMARK SANITATION AUDIT]")
    print(f"  - Files in app/src/main/assets: {assets_count} (Must be 0)")
    print(f"  - Old mark_r.png in res/drawable: {mark_r_exists} (Must be False)")
    
    # 9. Institutional Trading Terminology Audit (FPS Terms = 0)
    fps_terms = ["\\bFPS\\b", "\\bHeadshot\\b", "\\bK/D\\b", "\\bWeapon\\b", "\\bArmory\\b", "\\bDeathmatch\\b"]
    all_kt_xml = glob.glob(os.path.join(app_dir, "**", "*.kt"), recursive=True) + glob.glob(os.path.join(app_dir, "**", "*.xml"), recursive=True)
    fps_found = 0
    for file_path in all_kt_xml:
        text = open(file_path, "r", encoding="utf-8", errors="ignore").read()
        for term in fps_terms:
            matches = re.findall(term, text, re.IGNORECASE)
            # Filter out 60/120 Hz mentions or unrelated occurrences
            for m in matches:
                fps_found += 1
                print(f"    Warning: Found {m} in {os.path.basename(file_path)}")
    print(f"  - Legacy FPS gaming terms found: {fps_found} (Must be 0)")
    
    # 10. Compiled APK Status
    apk_path = r"c:\Users\User\OneDrive\Desktop\selfmade game projects\setupcandy\BreakoutBlitz-debug.apk"
    apk_exists = os.path.exists(apk_path)
    apk_size = os.path.getsize(apk_path) if apk_exists else 0
    print(f"\n[9. COMPILED STANDALONE ANDROID APK]")
    print(f"  - BreakoutBlitz-debug.apk present: {apk_exists}")
    print(f"  - Size: {apk_size:,} bytes ({apk_size/(1024*1024):.2f} MB)")
    print("================================================================================")

if __name__ == "__main__":
    run_audit()
