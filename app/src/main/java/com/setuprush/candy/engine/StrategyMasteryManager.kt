package com.setuprush.candy.engine

/**
 * Strategy Mastery Progression Engine.
 * Implements an institutional 5-tier mastery tree for each of the 24+ setups:
 * - Tier 1: Recruit (Visual Anatomy)
 * - Tier 2: Marksman (Precision Entry)
 * - Tier 3: Veteran (Trap & Volume Filter)
 * - Tier 4: Specialist (Turbo Speed & Drawdown Control)
 * - Tier 5: Grandmaster (Blind Chart Boss Trial -> Unlocks for Multiplayer)
 */
object StrategyMasteryManager {

    data class StrategyProgress(
        val key: String,
        val tier: Int = 1, // 1 to 5
        val xp: Int = 0,
        val xpRequired: Int = 500,
        val isCertifiedForMultiplayer: Boolean = false,
        val completedTrials: Int = 0,
        val winCount: Int = 0,
        val lossCount: Int = 0
    ) {
        val winRate: Double
            get() = if (winCount + lossCount == 0) 0.0 else winCount.toDouble() / (winCount + lossCount)
    }

    private val progressMap = mutableMapOf<String, StrategyProgress>()

    init {
        // Initialize all strategies from StrategyCatalog
        StrategyCatalog.ALL_SETUPS.forEach { setup ->
            // Pre-certify 3 starter setups so multiplayer is immediately playable
            val isStarter = setup.key in listOf("engulfing", "smc_fvg", "flag")
            progressMap[setup.key] = StrategyProgress(
                key = setup.key,
                tier = if (isStarter) 5 else 1,
                xp = if (isStarter) 2500 else 0,
                isCertifiedForMultiplayer = isStarter
            )
        }
    }

    fun getProgress(key: String): StrategyProgress {
        return progressMap[key] ?: StrategyProgress(key = key)
    }

    fun getAllProgress(): List<StrategyProgress> = progressMap.values.toList()

    fun getCertifiedStrategies(): List<StrategyProgress> {
        return progressMap.values.filter { it.isCertifiedForMultiplayer }
    }

    /** Record trade outcome and award Strategy Mastery XP */
    fun recordTrade(key: String, isWin: Boolean, rMultiple: Double = 2.0): StrategyProgress {
        val curr = getProgress(key)
        val addedXp = if (isWin) (150 * rMultiple).toInt() else 35
        val newXp = curr.xp + addedXp
        val newWins = curr.winCount + (if (isWin) 1 else 0)
        val newLosses = curr.lossCount + (if (!isWin) 1 else 0)

        // Tier advancement threshold (500 XP per tier up to Tier 5)
        var newTier = curr.tier
        var certified = curr.isCertifiedForMultiplayer
        if (newXp >= curr.xpRequired && newTier < 5) {
            newTier++
            if (newTier == 5) {
                certified = true
            }
        }

        val updated = curr.copy(
            tier = newTier,
            xp = newXp,
            xpRequired = newTier * 500,
            isCertifiedForMultiplayer = certified,
            completedTrials = curr.completedTrials + 1,
            winCount = newWins,
            lossCount = newLosses
        )
        progressMap[key] = updated
        return updated
    }
}
