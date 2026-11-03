package com.setuprush.candy.engine

/**
 * AAA Character Operator, Wearables & Skin Customization Engine.
 * Implements competitive trader operator presentation:
 * - Character Operators with unique animated intro quotes, entrance animations, and traits.
 * - Wearable slots: Suits/Armor, Luxury Timepieces, AR Eyewear/HUDs, Holo-Docks.
 * - Rarity tiers: LEGENDARY, EPIC, RARE, COMMON.
 * - Pre-match show-off asset intros before duels and tournaments.
 */
object CharacterCustomizationManager {

    enum class ItemRarity(val label: String, val colorHex: Long, val badge: String) {
        LEGENDARY("LEGENDARY", 0xFFFFD700, "👑"),
        EPIC("EPIC", 0xFF9C27B0, "🔮"),
        RARE("RARE", 0xFF2196F3, "💎"),
        COMMON("COMMON", 0xFF808080, "⚪")
    }

    enum class WearableSlot(val displayName: String, val icon: String) {
        OPERATOR("Operator Skin", "🧑‍🚀"),
        SUIT("Suit & Tactical Armor", "👔"),
        TIMEPIECE("Luxury Timepiece", "⌚"),
        EYEWEAR("AR Eyewear / HUD", "🕶️"),
        HOLO_DOCK("Holographic Trading Rig", "🛸")
    }

    data class CharacterOperator(
        val id: String,
        val codename: String,
        val title: String,
        val avatarEmoji: String,
        val introQuote: String,
        val entryAnimation: String, // "HOLOGRAPHIC_STOMP", "CHRONO_DIAL_SPIN", "CYBER_FLICK"
        val rarity: ItemRarity,
        val bio: String,
        val unlocked: Boolean = false
    )

    data class WearableItem(
        val id: String,
        val name: String,
        val slot: WearableSlot,
        val rarity: ItemRarity,
        val icon: String,
        val statBuffDescription: String,
        val unlocked: Boolean = false
    )

    val ALL_OPERATORS = listOf(
        CharacterOperator(
            id = "operator_apex_quant",
            codename = "APEX QUANT",
            title = "Order Flow Architect",
            avatarEmoji = "🧑‍💻",
            introQuote = "Price is just liquidity seeking equilibrium. Prepare for extraction.",
            entryAnimation = "HOLOGRAPHIC_STOMP",
            rarity = ItemRarity.LEGENDARY,
            bio = "Ex-Tier-1 institutional high-frequency algorithmic trader who reads volume deltas at the speed of light.",
            unlocked = true
        ),
        CharacterOperator(
            id = "operator_wallst_sniper",
            codename = "STREET SNIPER",
            title = "Macro Invalidation Specialist",
            avatarEmoji = "🎯",
            introQuote = "One candle, one trigger. When the wick touches, the trade closes.",
            entryAnimation = "CHRONO_DIAL_SPIN",
            rarity = ItemRarity.EPIC,
            bio = "Veteran prop desk survivor with an unshakeable 78% win rate on Fair Value Gap mitigations.",
            unlocked = true
        ),
        CharacterOperator(
            id = "operator_cyber_liquidator",
            codename = "LIQUIDATOR",
            title = "Margin Hunt Operative",
            avatarEmoji = "🤖",
            introQuote = "Your stop losses are my target liquidity pool.",
            entryAnimation = "CYBER_FLICK",
            rarity = ItemRarity.LEGENDARY,
            bio = "Specializes in cascading liquidation runs and high-volatility flash breakouts.",
            unlocked = false
        ),
        CharacterOperator(
            id = "operator_satoshi_ghost",
            codename = "GHOST RUNNER",
            title = "Sovereign Node Vanguard",
            avatarEmoji = "👻",
            introQuote = "Untracked, unhedged, unstoppable. The genesis block remembers.",
            entryAnimation = "HOLOGRAPHIC_STOMP",
            rarity = ItemRarity.EPIC,
            bio = "Stealth trading operative executing sub-second orders across decentralized book depths.",
            unlocked = false
        ),
        CharacterOperator(
            id = "operator_floor_boss",
            codename = "PIT BOSS",
            title = "Exchange Ringmaster",
            avatarEmoji = "🧔",
            introQuote = "Welcome to the trading floor. The spread belongs to me.",
            entryAnimation = "CHRONO_DIAL_SPIN",
            rarity = ItemRarity.RARE,
            bio = "Old-school Chicago pit trader who trades market psychology and panic wicks.",
            unlocked = true
        )
    )

    val ALL_WEARABLES = listOf(
        WearableItem(
            id = "wearable_tactical_pinstripe",
            name = "Kevlar Pinstripe Double-Breasted",
            slot = WearableSlot.SUIT,
            rarity = ItemRarity.LEGENDARY,
            icon = "👔",
            statBuffDescription = "+10% Faster Invalidation Warning Visual Cues",
            unlocked = true
        ),
        WearableItem(
            id = "wearable_cyber_trench",
            name = "Neon Cyberpunk Longcoat",
            slot = WearableSlot.SUIT,
            rarity = ItemRarity.EPIC,
            icon = "🧥",
            statBuffDescription = "+5% Candle Trail Luminescence",
            unlocked = false
        ),
        WearableItem(
            id = "wearable_tourbillon",
            name = "Skeleton Tourbillon 18k Rose Gold",
            slot = WearableSlot.TIMEPIECE,
            rarity = ItemRarity.LEGENDARY,
            icon = "⌚",
            statBuffDescription = "Visual Tick Chrono Precision Dial (+50 Combo XP on Sniper Entry)",
            unlocked = true
        ),
        WearableItem(
            id = "wearable_ar_goggles",
            name = "Omni-Terminal AR HUD",
            slot = WearableSlot.EYEWEAR,
            rarity = ItemRarity.LEGENDARY,
            icon = "🕶️",
            statBuffDescription = "Projects Real-Time SMC Liquidity Range Bands directly onto candle chart",
            unlocked = true
        ),
        WearableItem(
            id = "wearable_holo_dock",
            name = "Triple-Monitor Anti-Gravity Rig",
            slot = WearableSlot.HOLO_DOCK,
            rarity = ItemRarity.EPIC,
            icon = "🛸",
            statBuffDescription = "Pre-match 3D Floating Rig Entrance Pedestal Display",
            unlocked = true
        )
    )

    fun getEquippedOperator(): CharacterOperator {
        val id = PlayerProfileManager.currentProfile.equippedOperatorId
        return ALL_OPERATORS.find { it.id == id } ?: ALL_OPERATORS[0]
    }

    fun getEquippedSuit(): WearableItem {
        val id = PlayerProfileManager.currentProfile.equippedSuitId
        return ALL_WEARABLES.find { it.id == id } ?: ALL_WEARABLES[0]
    }

    fun getEquippedWatch(): WearableItem {
        val id = PlayerProfileManager.currentProfile.equippedWatchId
        return ALL_WEARABLES.find { it.id == id } ?: ALL_WEARABLES[2]
    }

    fun getEquippedEyewear(): WearableItem {
        val id = PlayerProfileManager.currentProfile.equippedEyewearId
        return ALL_WEARABLES.find { it.id == id } ?: ALL_WEARABLES[3]
    }

    fun equipOperator(id: String) {
        val op = ALL_OPERATORS.find { it.id == id } ?: return
        PlayerProfileManager.currentProfile = PlayerProfileManager.currentProfile.copy(equippedOperatorId = op.id)
    }

    fun equipWearable(id: String) {
        val item = ALL_WEARABLES.find { it.id == id } ?: return
        when (item.slot) {
            WearableSlot.SUIT -> PlayerProfileManager.currentProfile = PlayerProfileManager.currentProfile.copy(equippedSuitId = item.id)
            WearableSlot.TIMEPIECE -> PlayerProfileManager.currentProfile = PlayerProfileManager.currentProfile.copy(equippedWatchId = item.id)
            WearableSlot.EYEWEAR -> PlayerProfileManager.currentProfile = PlayerProfileManager.currentProfile.copy(equippedEyewearId = item.id)
            else -> {}
        }
    }
}
