import os
import sys

sys.stdout.reconfigure(encoding='utf-8')

replacements = {
    "app/src/main/java/com/setuprush/candy/NativeGameActivity.kt": [
        ("60/120 FPS hardware acceleration", "60/120 Hz ultra-fluid hardware acceleration"),
        ("combat metrics (K/D, volume, precision, challenges)", "trading performance metrics (profit factor, volume, precision, challenges)"),
        ("// 60/120 FPS smooth continuous tape progression", "// Smooth continuous tape progression"),
        ("// Post-Match Performance Debrief Dialog (FPS Metrics Translated to Financials)", "// Post-Match Performance Debrief Dialog (Performance Telemetrics)")
    ],
    "app/src/main/java/com/setuprush/candy/engine/CharacterCustomizationManager.kt": [
        ("* Implements competitive FPS character presentation (tactical esports style):", "* Implements competitive trader operator presentation:")
    ],
    "app/src/main/java/com/setuprush/candy/engine/MatchPerformanceEngine.kt": [
        ('* - "K/D Ratio" -> Profit Factor (Gross Gains / Gross Losses) & Win/Loss Ratio', '* - Profit Factor (Gross Gains / Gross Losses) & Win/Loss Ratio'),
        ('* - "Headshot %" -> Precision Entry Efficiency (MAFE: within 5% of wick extreme)', '* - Precision Entry Efficiency (MAFE: within 5% of wick extreme)'),
        ('val precisionAccuracyPct: Double, // "Headshot %"', 'val precisionAccuracyPct: Double, // MAFE Entry Precision %')
    ],
    "app/src/main/java/com/setuprush/candy/engine/StrategyMasteryManager.kt": [
        ("* Implements an FPS-style 5-tier mastery tree for each of the 24+ setups:", "* Implements an institutional 5-tier mastery tree for each of the 24+ setups:"),
        ("/** Record trade outcome and award Strategy Weapon XP */", "/** Record trade outcome and award Strategy Mastery XP */")
    ],
    "app/src/main/java/com/setuprush/candy/ui/ChartCanvas.kt": [
        ("* Features 120 FPS hardware accelerated drawing, dynamic viewport scaling,", "* Features 120 Hz ultra-fluid hardware accelerated drawing, dynamic viewport scaling,")
    ],
    "app/src/main/java/com/setuprush/candy/engine/MonetizationManager.kt": [
        ("* Operates strictly as a competitive skill-based esports & educational trading game.", "* Operates strictly as a competitive skill-based educational trading game."),
        ('"PROP RUSH IS A SIMULATED TRADING ESPORTS GAME DESIGNED FOR TRAINING AND ENTERTAINMENT. "', '"SETUP CANDY IS A SIMULATED TRADING GAME DESIGNED FOR TRAINING AND ENTERTAINMENT. "')
    ],
    "app/src/main/java/com/setuprush/candy/ui/GameMenuHub.kt": [
        ("* AAA Esports Game Menu Hub & Navigation Suite for Prop Trading Combat.", "* Tactical Game Menu Hub & Navigation Suite for Prop Trading."),
        ("* Implements industry-leading FPS menu UX design principles:", "* Implements industry-leading tactical trading hub UX design principles:"),
        ('LOADOUT("LOADOUT", "🛠️", "Trading Model Armory"),', 'LOADOUT("LOADOUT", "🛠️", "Trading Model Studio"),'),
        ('BARRACKS("BARRACKS", "🪪", "Dossier & Combat Stats"),', 'BARRACKS("BARRACKS", "🪪", "Dossier & Performance Stats"),'),
        ('text = "TACTICAL COMBAT",', 'text = "TACTICAL TRADING",'),
        ('text = "TACTICAL TRADING ESPORTS",', 'text = "COMPETITIVE TRADING LEAGUE",'),
        ('text = "DEPLOY COMBAT DRILL · ${activeFormat.badge}",', 'text = "DEPLOY SESSION DRILL · ${activeFormat.badge}",'),
        ('text = "Equip or customize your primary setup weapon, confluence filter, and risk protocols",', 'text = "Equip or customize your primary setup model, confluence filter, and risk protocols",'),
        ('text = "CAREER COMBAT STATS (FINANCIAL K/D)",', 'text = "CAREER TRADING STATS (PROFIT FACTOR)",'),
        ('text = "DESK ARMORY & PAYMENT GATEWAYS",', 'text = "DESK VAULT & PAYMENT GATEWAYS",')
    ],
    "app/src/main/java/com/setuprush/candy/ui/PostMatchDebriefDialog.kt": [
        ('* - Profit Factor & Win/Loss Ratio ("K/D Ratio")', '* - Profit Factor & Win/Loss Ratio'),
        ('* - Precision MAFE Efficiency ("Headshot %")', '* - Precision MAFE Efficiency'),
        ("// ==================== 2. FINANCIAL PERFORMANCE METRICS (FPS TRANSLATED) ====================", "// ==================== 2. FINANCIAL PERFORMANCE TELEMETRICS ===================="),
        ('text = "1. TRADER COMBAT METRICS (FPS TRANSLATION)",', 'text = "1. TRADER EXECUTION METRICS",'),
        ("// 4 Primary KPI Cards (Kills, Deaths, K/D, Headshots)", "// 4 Primary KPI Cards (Wins, Losses, Profit Factor, Accuracy)"),
        ('subtitle = "K/D RATIO",', 'subtitle = "PROFIT FACTOR",'),
        ('subtitle = "HEADSHOT %",', 'subtitle = "MAFE ACCURACY",')
    ],
    "app/src/main/java/com/setuprush/candy/ui/PreMatchShowcaseDialog.kt": [
        ("* - Equipped Trading Model (Weapon Class, Leverage, Risk Armor)", "* - Equipped Trading Model (Style Class, Leverage, Risk Armor)"),
        ("* - Combat Stats: K/D Equivalent (Profit Factor), Combat Damage (Net R), Headshot % (MAFE Accuracy)", "* - Trader Stats: Profit Factor, Net R-Volume, MAFE Accuracy"),
        ('text = "COMBAT TELEMETRY"', 'text = "EXECUTION TELEMETRY"'),
        ('text = "COMBAT DAMAGE"', 'text = "NET R-VOLUME"'),
        ("// Equipped Trading Model Loadout Weapon", "// Equipped Trading Model Loadout"),
        ('text = "LOADOUT WEAPON: ${model.name.uppercase()}",', 'text = "ACTIVE MODEL: ${model.name.uppercase()}",'),
        ('text = "DEPLOY INTO COMBAT NOW",', 'text = "DEPLOY INTO TRADING NOW",')
    ],
    "scripts/generate_all_density_assets.py": [
        ('"TACTICAL TRADING COMBAT · 60/120 FPS NATIVE ENGINE"', '"TACTICAL TRADING SIMULATOR · ULTRA-FLUID NATIVE ENGINE"')
    ],
    "scripts/verify_operator_skins_sessions_and_payments.py": [
        ('"DEPLOY INTO COMBAT NOW"', '"DEPLOY INTO TRADING NOW"')
    ],
    "scripts/verify_google_play_architecture_and_completeness.py": [
        ('"DEPLOY INTO COMBAT NOW"', '"DEPLOY INTO TRADING NOW"')
    ]
}

def main():
    total_replaced = 0
    for file_path, pairs in replacements.items():
        if not os.path.exists(file_path):
            print(f"File not found: {file_path}")
            continue
        with open(file_path, "r", encoding="utf-8") as f:
            content = f.read()
        
        file_replaced = 0
        for old_txt, new_txt in pairs:
            if old_txt in content:
                content = content.replace(old_txt, new_txt)
                file_replaced += 1
                total_replaced += 1
            else:
                print(f"Warning: String not found in {file_path}: {repr(old_txt[:40])}")
        
        with open(file_path, "w", encoding="utf-8") as f:
            f.write(content)
        print(f"Updated {file_path}: {file_replaced}/{len(pairs)} replacements made.")

    print(f"\nTotal replacements executed across all files: {total_replaced}")

if __name__ == "__main__":
    main()
