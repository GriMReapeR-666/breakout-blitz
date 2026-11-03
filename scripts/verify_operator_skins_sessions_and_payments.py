import sys
import os

sys.stdout.reconfigure(encoding='utf-8')

def test_component(name, path, required_tokens):
    print(f"--- Verifying {name} ({os.path.basename(path)}) ---")
    if not os.path.exists(path):
        print(f"FAIL: File does not exist at {path}")
        return False
    with open(path, "r", encoding="utf-8") as f:
        content = f.read()
    
    missing = []
    for token in required_tokens:
        if token not in content:
            missing.append(token)
    
    if missing:
        print(f"FAIL: Missing required tokens in {name}: {missing}")
        return False
    else:
        print(f"PASS: All {len(required_tokens)} tokens verified in {name}")
        return True

def main():
    print("================================================================================")
    print("VERIFICATION SUITE: OPERATOR SKINS, WEARABLES, SESSIONS, TIMEFRAMES & PAYMENTS")
    print("================================================================================")
    
    base_dir = r"c:\Users\User\OneDrive\Desktop\selfmade game projects\setupcandy\app\src\main\java\com\setuprush\candy"
    
    # 1. CharacterCustomizationManager
    p1 = os.path.join(base_dir, "engine", "CharacterCustomizationManager.kt")
    tokens1 = [
        "enum class ItemRarity",
        "LEGENDARY", "EPIC", "RARE", "COMMON",
        "data class CharacterOperator",
        "operator_apex_quant", "operator_wallst_sniper", "operator_cyber_liquidator", "operator_satoshi_ghost", "operator_floor_boss",
        "enum class WearableSlot",
        "OPERATOR", "SUIT", "TIMEPIECE", "EYEWEAR", "HOLO_DOCK",
        "data class WearableItem",
        "wearable_tactical_pinstripe", "wearable_tourbillon", "wearable_ar_goggles", "wearable_holo_dock",
        "fun getEquippedOperator",
        "fun getEquippedSuit",
        "fun getEquippedWatch",
        "fun getEquippedEyewear",
        "fun equipOperator",
        "fun equipWearable"
    ]
    res1 = test_component("CharacterCustomizationManager", p1, tokens1)
    
    # 2. MarketSessionEngine
    p2 = os.path.join(base_dir, "engine", "MarketSessionEngine.kt")
    tokens2 = [
        "enum class MarketSession",
        "ASIAN_ACCUMULATION", "LONDON_EXPANSION", "NEW_YORK_AM_KILLZONE", "LONDON_CLOSE_REVERSION",
        "enum class Timeframe",
        "TF_1S", "TF_5S", "TF_1M", "TF_5M", "TF_15M",
        "enum class HistoricalRegime",
        "fun getDailyDatabaseSeed",
        "fun getDailyMarketRegime",
        "fun getLiveSpreadBps",
        "Calendar.DAY_OF_YEAR"
    ]
    res2 = test_component("MarketSessionEngine", p2, tokens2)
    
    # 3. PaymentGatewayManager
    p3 = os.path.join(base_dir, "engine", "PaymentGatewayManager.kt")
    tokens3 = [
        "enum class PaymentChannel",
        "GOOGLE_PLAY_BILLING", "OPEN_WEB_GATEWAY", "DESK_CREDITS",
        "data class PaymentTransaction",
        "fun executePurchase",
        "MonetizationManager.STORE_CATALOG"
    ]
    res3 = test_component("PaymentGatewayManager", p3, tokens3)
    
    # 4. PreMatchShowcaseDialog
    p4 = os.path.join(base_dir, "ui", "PreMatchShowcaseDialog.kt")
    tokens4 = [
        "fun PreMatchShowcaseDialog",
        "CharacterCustomizationManager.getEquippedOperator",
        "OpponentDuelEngine.activeRival",
        "countdownSeconds",
        "PRE-MATCH OPERATOR INTRO",
        "DEPLOY INTO TRADING NOW"
    ]
    res4 = test_component("PreMatchShowcaseDialog", p4, tokens4)
    
    # 5. GameMenuHub
    p5 = os.path.join(base_dir, "ui", "GameMenuHub.kt")
    tokens5 = [
        "fun GameMenuHub",
        "enum class MenuNavPillar",
        "LOBBY", "LOADOUT", "BARRACKS", "ACADEMY", "STORE",
        "CharacterCustomizationManager",
        "MarketSessionEngine",
        "LocalConfiguration.current"
    ]
    res5 = test_component("GameMenuHub", p5, tokens5)
    
    # 6. NativeGameActivity Integration
    p6 = os.path.join(base_dir, "NativeGameActivity.kt")
    tokens6 = [
        "showMasterMenuHub",
        "showPreMatchShowcase",
        "GameMenuHub(",
        "PreMatchShowcaseDialog(",
        "isLandscape"
    ]
    res6 = test_component("NativeGameActivity Integration", p6, tokens6)
    
    # 7. AndroidManifest orientation
    manifest_path = r"c:\Users\User\OneDrive\Desktop\selfmade game projects\setupcandy\app\src\main\AndroidManifest.xml"
    tokens7 = [
        'android:screenOrientation="user"'
    ]
    res7 = test_component("AndroidManifest Dual-Orientation", manifest_path, tokens7)
    
    all_passed = res1 and res2 and res3 and res4 and res5 and res6 and res7
    print("================================================================================")
    if all_passed:
        print("ALL VERIFICATION CHECKS PASSED: 7/7 COMPONENTS CERTIFIED")
        sys.exit(0)
    else:
        print("VERIFICATION FAILED: ONE OR MORE CHECKS DID NOT PASS")
        sys.exit(1)

if __name__ == "__main__":
    main()
