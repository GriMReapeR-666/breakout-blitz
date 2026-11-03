package com.setuprush.candy.engine

/**
 * Professional Trader Profile, Desk Accreditation & Performance Engine.
 * Manages trader identity, verified accreditation badges, performance metrics,
 * and trading desk dossiers for competitive matchmaking and leaderboards.
 */
object PlayerProfileManager {

    data class AccreditationBadge(
        val id: String,
        val title: String,
        val icon: String,
        val requirement: String,
        val isUnlocked: Boolean = false
    ) {
        val name: String get() = title
        val description: String get() = requirement
    }

    data class TradingPerformance(
        val totalTrades: Int = 342,
        val profitableTrades: Int = 247,
        val stoppedTrades: Int = 95,
        val winRate: Double = 0.722,
        val profitFactor: Double = 2.84,
        val netPnl: Double = 124800.0,
        val avgRMultiple: Double = 2.3,
        val bestStreak: Int = 18,
        val currentStreak: Int = 6
    )

    data class TraderProfile(
        val username: String = "ApexTrader_99",
        val deskTag: String = "PROP",
        val traderTitle: String = "Senior Liquidity Specialist",
        val bio: String = "Specializes in 5-minute Fair Value Gap mitigations and structural breakouts.",
        val themeId: String = "night",
        val mmrRating: Int = 2640,
        val rankingTier: String = "Grandmaster Trader",
        val deskLevel: Int = 42,
        val experiencePoints: Long = 18450L,
        val showcaseBadges: List<AccreditationBadge>,
        val stats: TradingPerformance = TradingPerformance(),
        val equippedOperatorId: String = "operator_apex_quant",
        val equippedSuitId: String = "wearable_tactical_pinstripe",
        val equippedWatchId: String = "wearable_tourbillon",
        val equippedEyewearId: String = "wearable_ar_goggles"
    ) {
        val avatarEmoji: String get() = "🧑‍💻"
        val rankTitle: String get() = rankingTier
        val careerWinRate: Double get() = stats.winRate
        val profitFactor: Double get() = stats.profitFactor
        val bestWinStreak: Int get() = stats.bestStreak
        val unlockedBadges: List<AccreditationBadge> get() = ALL_BADGES.filter { it.isUnlocked }
    }

    val ALL_BADGES = listOf(
        AccreditationBadge("flawless_streak", "Flawless 10-Streak", "🎖️", "10 consecutive +2R profitable trades without a stop out.", true),
        AccreditationBadge("smc_certified", "SMC Certified", "⚡", "Mastered all 4 Smart Money Concepts setups with a 65%+ win rate.", true),
        AccreditationBadge("margin_call_winner", "Margin Call Champion", "👑", "Won a 50+ trader competitive liquidation tournament.", true),
        AccreditationBadge("accuracy_master", "Precision Execution", "🎯", "Executed 3 consecutive sniper trades with 100% target accuracy.", true),
        AccreditationBadge("trend_master", "Trend Runner", "💎", "Captured a +5R or greater sustained trend expansion.", false),
        AccreditationBadge("strike_demon", "Three-Line Master", "🔥", "Completed 50 profitable trades using the Three-Line Strike setup.", false)
    )

    var currentProfile: TraderProfile = TraderProfile(
        showcaseBadges = ALL_BADGES.filter { it.isUnlocked }.take(3)
    )

    fun updateProfile(username: String, deskTag: String, title: String, bio: String) {
        currentProfile = currentProfile.copy(
            username = username,
            deskTag = deskTag,
            traderTitle = title,
            bio = bio
        )
    }

    fun setShowcaseBadges(badgeIds: List<String>) {
        val selected = ALL_BADGES.filter { it.id in badgeIds }
        currentProfile = currentProfile.copy(showcaseBadges = selected.take(3))
    }

    fun recordMatch(isWin: Boolean, pnl: Double, rMultiple: Double = 2.0, format: String = "duel", symbol: String = "BTC") {
        val s = currentProfile.stats
        val total = s.totalTrades + 1
        val wins = s.profitableTrades + (if (isWin) 1 else 0)
        val stops = s.stoppedTrades + (if (!isWin) 1 else 0)
        val net = s.netPnl + pnl
        val streak = if (isWin) s.currentStreak + 1 else 0
        val best = kotlin.math.max(s.bestStreak, streak)
        val winRate = if (total > 0) wins.toDouble() / total else 0.0

        currentProfile = currentProfile.copy(
            mmrRating = (currentProfile.mmrRating + (if (isWin) 25 else -15)).coerceAtLeast(1000),
            experiencePoints = currentProfile.experiencePoints + (if (isWin) 250L else 50L),
            stats = s.copy(
                totalTrades = total,
                profitableTrades = wins,
                stoppedTrades = stops,
                winRate = winRate,
                netPnl = net,
                bestStreak = best,
                currentStreak = streak
            )
        )
    }
}
