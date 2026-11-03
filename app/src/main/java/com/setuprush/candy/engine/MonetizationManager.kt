package com.setuprush.candy.engine

/**
 * Commercial Monetization & Google Play Store Compliance Manager.
 * Operates strictly as a competitive skill-based educational trading game.
 * 
 * Complies 100% with Google Play Financial Services & Real-Money Gambling Policies:
 * - NO real money deposits, withdrawals, or fiat conversion.
 * - ALL trading is 100% simulated paper trading using virtual credits.
 * - Monetization is restricted to cosmetic themes, Season Desk Pass, and Pro analytics.
 */
object MonetizationManager {

    /** Virtual in-game currency earned purely through trading skill & achievements */
    var deskCredits: Int = 2500
        private set

    var isProDeskSubscriber: Boolean = false
        private set

    var isSeasonPassUnlocked: Boolean = false
        private set

    data class PassRewardTier(
        val tier: Int,
        val xpRequired: Int,
        val freeRewardTitle: String,
        val freeRewardIcon: String,
        val premiumRewardTitle: String,
        val premiumRewardIcon: String,
        var isFreeClaimed: Boolean = false,
        var isPremiumClaimed: Boolean = false
    )

    data class StoreItem(
        val id: String,
        val title: String,
        val category: StoreCategory,
        val priceCredits: Int,
        val priceUsdEquivalent: String,
        val icon: String,
        val description: String,
        var isPurchased: Boolean = false
    )

    enum class StoreCategory(val label: String) {
        CHART_THEMES("Cockpit Themes"),
        CALLCARDS("Calling Cards & Banners"),
        SOUND_PACKS("Tactical Audio FX"),
        PASSES("Season Passes & Pro")
    }

    val SEASON_PASS_TIERS = (1..15).map { tier ->
        PassRewardTier(
            tier = tier,
            xpRequired = tier * 400,
            freeRewardTitle = when (tier % 4) {
                1 -> "+150 Desk Credits"
                2 -> "Common Calling Card"
                3 -> "+250 Desk Credits"
                else -> "Bronze Desk Decal"
            },
            freeRewardIcon = when (tier % 4) {
                1 -> "🪙"
                2 -> "🪪"
                3 -> "💰"
                else -> "🛡️"
            },
            premiumRewardTitle = when (tier % 4) {
                1 -> "Cyber Neon Cockpit Theme"
                2 -> "Animated Apex Predator Banner"
                3 -> "Mechanical Haptic Click Pack"
                else -> "Gold Bull Reticle HUD"
            },
            premiumRewardIcon = when (tier % 4) {
                1 -> "🎨"
                2 -> "👑"
                3 -> "🔊"
                else -> "🎯"
            }
        )
    }

    val STORE_CATALOG = mutableListOf(
        StoreItem(
            id = "skin_cyber_neon",
            title = "Cyber Neon Cockpit",
            category = StoreCategory.CHART_THEMES,
            priceCredits = 1200,
            priceUsdEquivalent = "\$1.99",
            icon = "⚡",
            description = "High-contrast glowing cyan & magenta neon candles with dark cybernetic grid."
        ),
        StoreItem(
            id = "skin_terminal_gold",
            title = "Apex Gold Terminal",
            category = StoreCategory.CHART_THEMES,
            priceCredits = 1500,
            priceUsdEquivalent = "\$2.49",
            icon = "🏆",
            description = "Executive institutional monochrome amber & gold aesthetic inspired by Wall Street trading terminals."
        ),
        StoreItem(
            id = "card_titan_apex",
            title = "Titan Apex Calling Card",
            category = StoreCategory.CALLCARDS,
            priceCredits = 800,
            priceUsdEquivalent = "\$0.99",
            icon = "🪪",
            description = "Holographic moving player card displaying your MMR rank and Win/Loss Profit Factor."
        ),
        StoreItem(
            id = "pass_season_1",
            title = "Season 1: Combine Pass",
            category = StoreCategory.PASSES,
            priceCredits = 2500,
            priceUsdEquivalent = "\$4.99",
            icon = "🎫",
            description = "Unlocks premium track for all 15 Desk Pass tiers, including exclusive themes and badges."
        ),
        StoreItem(
            id = "sub_pro_desk",
            title = "Pro Desk Combine Pass (Monthly)",
            category = StoreCategory.PASSES,
            priceCredits = 5000,
            priceUsdEquivalent = "\$9.99/mo",
            icon = "⭐",
            description = "Unlimited trading model slots, advanced institutional order flow telemetry, and priority ranked queues."
        )
    )

    fun awardCredits(amount: Int) {
        deskCredits += amount
    }

    fun addCredits(amount: Int) {
        awardCredits(amount)
    }

    fun purchaseItem(itemId: String): Boolean {
        val item = STORE_CATALOG.find { it.id == itemId } ?: return false
        if (item.isPurchased) return true
        if (deskCredits >= item.priceCredits) {
            deskCredits -= item.priceCredits
            item.isPurchased = true
            if (item.id == "pass_season_1") {
                isSeasonPassUnlocked = true
            }
            if (item.id == "sub_pro_desk") {
                isProDeskSubscriber = true
            }
            return true
        }
        return false
    }

    const val GOOGLE_PLAY_COMPLIANCE_DISCLAIMER =
        "BREAKOUT BLITZ IS A SIMULATED TRADING GAME DESIGNED FOR TRAINING AND ENTERTAINMENT. " +
        "NO REAL MONEY OR ASSETS ARE DEPOSITED, GAINED, OR RISKED. ALL CHARTS RUN ON SIMULATED OR HISTORICAL PAPER TAPES."
}
