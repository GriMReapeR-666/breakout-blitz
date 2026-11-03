package com.setuprush.candy.engine

import kotlin.math.abs
import kotlin.math.max
import kotlin.math.min
import kotlin.math.roundToInt

/**
 * Institutional Post-Match Performance & Debrief Engine.
 * Translates competitive multiplayer and solo metrics into rigorous financial performance:
 * - "Kills" -> Target Take-Profits (+2R / +3R hits)
 * - "Deaths" -> Stop-Outs / Liquidations (1R risk hits)
 * - Profit Factor (Gross Gains / Gross Losses) & Win/Loss Ratio
 * - "Damage Dealt" -> Captured R-Multiple Volume (Net R expansion)
 * - Precision Entry Efficiency (MAFE: within 5% of wick extreme)
 * - "Score" -> Net Banked P&L & Desk Reputation Points
 * - "MVP / Top Trader" -> Highest Sharpe / Net R contribution
 * - "Medals & Commendations" -> Flawless Execution, Sniper Invalidation, Alpha Runner
 */
object MatchPerformanceEngine {

    enum class MatchOutcome(val label: String, val badge: String) {
        VICTORY("DESK VICTORY", "👑"),
        DEFEAT("MATCH DEFEAT", "🛑"),
        SOLE_SURVIVOR("SOLE SOLVENT SURVIVOR", "🏆"),
        TOP_TIER_FINISH("TOP 5% APEX DESK", "🎖️"),
        QUALIFIED("COMBINE QUALIFIED", "✅")
    }

    enum class ChallengeRequirementType {
        TAKE_PROFITS_COUNT, // "Kills" count
        WIN_STREAK,
        NET_PNL_TARGET,
        R_MULTIPLES_CAPTURED,
        PRECISION_ACCURACY_PCT,
        SURVIVE_TOURNAMENT_ROUNDS
    }

    data class PerformanceMedal(
        val id: String,
        val title: String,
        val icon: String,
        val description: String,
        val xpBonus: Int
    )

    enum class MistakeCategory(val label: String, val badge: String) {
        NONE_FLAWLESS("FLAWLESS EXECUTION", "🎯"),
        CHASED_ENTRY("CHASED EXTENDED MOVE", "⚠️"),
        PREMATURE_EXIT("PREMATURE CASH-OUT", "⏳"),
        UNCONFIRMED_EARLY("PREMATURE FOMO", "❌"),
        STOP_LOSS_IGNORED("STOP-LOSS IGNORED", "🛑"),
        COUNTER_TREND("COUNTER-TREND BIAS", "🌪️"),
        DISCIPLINED_STOP("DISCIPLINED INVALIDATION", "🛡️")
    }

    data class TradeReviewRecord(
        val tradeNumber: Int,
        val setupName: String,
        val direction: Int, // 1 for LONG, -1 for SHORT
        val entryPrice: Double,
        val exitPrice: Double,
        val pnl: Double,
        val rMultiple: Double,
        val timingBadge: String,
        val mistakeCategory: MistakeCategory,
        val mistakeDiagnosis: String,
        val actionableSolution: String
    )

    data class TraderMatchStats(
        val traderId: String,
        val name: String,
        val deskTag: String,
        val isPlayer: Boolean = false,
        val assetAssigned: String = "BTC/USD",
        val takeProfits: Int, // "Kills"
        val stopOuts: Int, // "Deaths"
        val netPnl: Double,
        val grossProfit: Double,
        val grossLoss: Double,
        val rMultiplesCaptured: Double, // "Damage"
        val precisionAccuracyPct: Double, // MAFE Entry Precision %
        val winRatePct: Double,
        val profitFactor: Double,
        val bestStreak: Int,
        val placementRank: Int = 1
    ) {
        val kdEquivalent: Double
            get() = if (stopOuts == 0) takeProfits.toDouble() else String.format("%.2f", takeProfits.toDouble() / stopOuts).toDouble()
    }

    data class TradingChallenge(
        val id: String,
        val modeId: String, // "1v1", "5v5", "tournament", "ffa", "all"
        val modeTitle: String,
        val title: String,
        val description: String,
        val targetValue: Double,
        var currentValue: Double = 0.0,
        val xpReward: Int,
        val mmrReward: Int,
        var isCompleted: Boolean = false
    ) {
        val progressRatio: Float
            get() = if (targetValue <= 0) 1f else (currentValue / targetValue).toFloat().coerceIn(0f, 1f)
    }

    data class MatchDebrief(
        val matchId: String,
        val modeId: String,
        val modeTitle: String,
        val marketSymbol: String,
        val outcome: MatchOutcome,
        val playerStats: TraderMatchStats,
        val scoreboard: List<TraderMatchStats>, // All traders in match
        val medalsEarned: List<PerformanceMedal>,
        val mmrChange: Int,
        val xpEarned: Int,
        val performanceGrade: String, // A+, A, B, C, F
        val feedbackSummary: String,
        val tradeReviews: List<TradeReviewRecord> = emptyList()
    )

    // Pre-configured Daily & Mode-Specific Challenges
    val ACTIVE_CHALLENGES = mutableListOf(
        TradingChallenge(
            id = "chal_1v1_sniper",
            modeId = "1v1",
            modeTitle = "1v1 Prop Duel",
            title = "Precision Invalidation",
            description = "Secure 3 Take-Profits (+2R) in 1v1 Duels with >75% precision entry accuracy.",
            targetValue = 3.0,
            currentValue = 2.0,
            xpReward = 450,
            mmrReward = 35
        ),
        TradingChallenge(
            id = "chal_5v5_carry",
            modeId = "5v5",
            modeTitle = "5v5 Portfolio Clash",
            title = "Desk Anchor MVP",
            description = "Capture +6.0R net volume across team market allocations without taking a stop-out.",
            targetValue = 6.0,
            currentValue = 4.2,
            xpReward = 600,
            mmrReward = 50
        ),
        TradingChallenge(
            id = "chal_tournament_survival",
            modeId = "tournament",
            modeTitle = "Margin Call Tournament",
            title = "Apex Liquidity Survivor",
            description = "Survive past 70 liquidated rivals in the 100-trader Margin Call Battle Royale.",
            targetValue = 70.0,
            currentValue = 48.0,
            xpReward = 850,
            mmrReward = 75
        ),
        TradingChallenge(
            id = "chal_ffa_volume",
            modeId = "ffa",
            modeTitle = "Free-For-All Combine",
            title = "Floor Orderflow Demon",
            description = "Bank >$3,000 in net profit during an 8-trader FFA timed floor combine.",
            targetValue = 3000.0,
            currentValue = 1850.0,
            xpReward = 500,
            mmrReward = 40
        ),
        TradingChallenge(
            id = "chal_solo_streak",
            modeId = "solo",
            modeTitle = "Solo Desk Mastery",
            title = "Flawless Discipline",
            description = "Execute a 5-streak profitable trade run on Desk 3 (1.5x tape speed).",
            targetValue = 5.0,
            currentValue = 3.0,
            xpReward = 400,
            mmrReward = 25
        )
    )

    var lastDebrief: MatchDebrief? = null
        private set

    /**
     * Generates a comprehensive post-game debrief for any game mode:
     * - 1v1 Head-to-Head Prop Duel
     * - 5v5 Multi-Asset Portfolio Clash
     * - 100-Trader Margin Call Tournament (Battle Royale)
     * - Free-For-All Floor Combine (8 Traders)
     * - Solo Mastery Combine
     */
    fun generateDebrief(
        modeId: String,
        symbol: String,
        playerTakes: Int,
        playerStops: Int,
        playerPnl: Double,
        playerBestStreak: Int,
        precisionScore: Double = 0.82,
        tradeReviews: List<TradeReviewRecord> = emptyList()
    ): MatchDebrief {
        val totalTrades = max(1, playerTakes + playerStops)
        val winRate = (playerTakes.toDouble() / totalTrades.toDouble()) * 100.0
        val grossProfit = playerTakes * 200.0
        val grossLoss = playerStops * 100.0
        val profitFactor = if (grossLoss == 0.0) grossProfit / 1.0 else grossProfit / grossLoss
        val rCaptured = (playerTakes * 2.0) - (playerStops * 1.0)

        // Grade Assignment
        val grade = when {
            winRate >= 80.0 && profitFactor >= 3.0 -> "A+"
            winRate >= 65.0 && profitFactor >= 2.0 -> "A"
            winRate >= 50.0 && profitFactor >= 1.5 -> "B"
            winRate >= 40.0 -> "C"
            else -> "F"
        }

        // Medals Evaluation
        val medals = mutableListOf<PerformanceMedal>()
        if (playerTakes >= 3 && playerStops == 0) {
            medals.add(PerformanceMedal("flawless", "Flawless Execution", "🎖️", "Achieved 100% win rate with zero stop-outs.", 300))
        }
        if (precisionScore >= 0.80) {
            medals.add(PerformanceMedal("sniper", "Sniper Accuracy", "🎯", "Executed with >80% precision entry efficiency (MAFE).", 200))
        }
        if (rCaptured >= 4.0) {
            medals.add(PerformanceMedal("r_demon", "Alpha Momentum", "💎", "Harvested +4.0R or more in market expansion.", 250))
        }
        if (playerBestStreak >= 4) {
            medals.add(PerformanceMedal("streak_master", "Unshakeable Streak", "🔥", "Held a disciplined 4+ consecutive winning trade streak.", 150))
        }

        val playerStats = TraderMatchStats(
            traderId = "player_1",
            name = PlayerProfileManager.currentProfile.username,
            deskTag = PlayerProfileManager.currentProfile.deskTag,
            isPlayer = true,
            assetAssigned = symbol,
            takeProfits = playerTakes,
            stopOuts = playerStops,
            netPnl = playerPnl,
            grossProfit = grossProfit,
            grossLoss = grossLoss,
            rMultiplesCaptured = rCaptured,
            precisionAccuracyPct = precisionScore * 100.0,
            winRatePct = winRate,
            profitFactor = profitFactor,
            bestStreak = playerBestStreak,
            placementRank = 1
        )

        // Generate rival scoreboard depending on mode
        val (outcome, scoreboard, mmrDelta, xpDelta, summary) = when (modeId) {
            "1v1" -> build1v1Scoreboard(playerStats, playerTakes > playerStops)
            "5v5" -> build5v5Scoreboard(playerStats, playerPnl >= 0)
            "tournament" -> buildTournamentScoreboard(playerStats, playerTakes)
            "ffa" -> buildFfaScoreboard(playerStats)
            else -> buildSoloScoreboard(playerStats, grade)
        }

        val missed = SetupExecutionTimingEngine.missedSetupsCount
        val snipers = SetupExecutionTimingEngine.sniperEntriesCount
        val chased = SetupExecutionTimingEngine.chasedEntriesCount

        val timingNote = buildString {
            if (snipers > 0) append(" 🎯 $snipers Sniper Execution(s).")
            if (missed > 0) append(" ⌛ $missed setup(s) reached +2R while flat.")
            if (chased > 0) append(" ⚠️ $chased late chase entry(s).")
        }
        val enhancedSummary = if (timingNote.isNotEmpty()) "$summary$timingNote" else summary

        val debrief = MatchDebrief(
            matchId = "MATCH-${System.currentTimeMillis() % 100000}",
            modeId = modeId,
            modeTitle = when (modeId) {
                "1v1" -> "1v1 Head-to-Head Prop Duel"
                "5v5" -> "5v5 Multi-Asset Portfolio Clash"
                "tournament" -> "Margin Call: 100-Trader Battle Royale"
                "ffa" -> "8-Trader Free-For-All Floor Combine"
                else -> "Solo Trading Desk Combine"
            },
            marketSymbol = symbol,
            outcome = outcome,
            playerStats = playerStats,
            scoreboard = scoreboard,
            medalsEarned = medals,
            mmrChange = mmrDelta,
            xpEarned = xpDelta + medals.sumOf { it.xpBonus },
            performanceGrade = grade,
            feedbackSummary = enhancedSummary,
            tradeReviews = tradeReviews
        )

        lastDebrief = debrief
        updateChallenges(debrief)
        return debrief
    }

    private fun build1v1Scoreboard(player: TraderMatchStats, isWin: Boolean): ScoreboardResult {
        val rivalR = if (isWin) 1 else 2
        val rivalS = if (isWin) 2 else 1
        val rivalPnl = (rivalR * 200.0) - (rivalS * 100.0)
        val rival = TraderMatchStats(
            traderId = "rival_1",
            name = OpponentDuelEngine.activeRival?.name ?: "Satoshi_Sniper",
            deskTag = OpponentDuelEngine.activeRival?.deskTag ?: "TITAN",
            isPlayer = false,
            assetAssigned = player.assetAssigned,
            takeProfits = rivalR,
            stopOuts = rivalS,
            netPnl = rivalPnl,
            grossProfit = rivalR * 200.0,
            grossLoss = rivalS * 100.0,
            rMultiplesCaptured = (rivalR * 2.0) - (rivalS * 1.0),
            precisionAccuracyPct = 76.5,
            winRatePct = (rivalR.toDouble() / (rivalR + rivalS).toDouble()) * 100.0,
            profitFactor = (rivalR * 200.0) / (rivalS * 100.0),
            bestStreak = 2,
            placementRank = if (isWin) 2 else 1
        )

        val updatedPlayer = player.copy(placementRank = if (isWin) 1 else 2)
        val scoreboard = if (isWin) listOf(updatedPlayer, rival) else listOf(rival, updatedPlayer)
        val outcome = if (isWin) MatchOutcome.VICTORY else MatchOutcome.DEFEAT
        val mmr = if (isWin) +42 else -26
        val xp = if (isWin) 450 else 120
        val note = if (isWin) "Outperformed rival trader on identical chart feed with superior risk-reward execution."
        else "Rival trader captured take-profit targets with tighter entry discipline."

        return ScoreboardResult(outcome, scoreboard, mmr, xp, note)
    }

    private fun build5v5Scoreboard(player: TraderMatchStats, isWin: Boolean): ScoreboardResult {
        val assets = listOf("BTC/USD", "ETH/USD", "SOL/USD", "SUI/USD", "XAU/USD")
        val teamMembers = mutableListOf<TraderMatchStats>()
        teamMembers.add(player.copy(assetAssigned = assets[0]))

        val botNames = listOf("Quant_Trader", "Alpha_Whale", "Delta_Neutral", "Liquidity_Sponge")
        botNames.forEachIndexed { idx, name ->
            val takes = (1..3).random()
            val stops = (0..2).random()
            val pnl = (takes * 180.0) - (stops * 95.0)
            teamMembers.add(
                TraderMatchStats(
                    traderId = "team_bot_$idx",
                    name = name,
                    deskTag = player.deskTag,
                    isPlayer = false,
                    assetAssigned = assets[(idx + 1) % assets.size],
                    takeProfits = takes,
                    stopOuts = stops,
                    netPnl = pnl,
                    grossProfit = takes * 180.0,
                    grossLoss = stops * 95.0,
                    rMultiplesCaptured = (takes * 2.0) - stops,
                    precisionAccuracyPct = 68.0 + (idx * 4.0),
                    winRatePct = (takes.toDouble() / max(1, takes + stops).toDouble()) * 100.0,
                    profitFactor = (takes * 180.0) / max(1.0, stops * 95.0),
                    bestStreak = takes,
                    placementRank = idx + 2
                )
            )
        }

        // Sort by net PnL
        val sorted = teamMembers.sortedByDescending { it.netPnl }.mapIndexed { index, s -> s.copy(placementRank = index + 1) }
        val outcome = if (isWin) MatchOutcome.VICTORY else MatchOutcome.DEFEAT
        val mmr = if (isWin) +55 else -30
        val xp = if (isWin) 600 else 180
        val note = "Your team generated $${String.format("%,.2f", sorted.sumOf { it.netPnl })} in collective portfolio equity."

        return ScoreboardResult(outcome, sorted, mmr, xp, note)
    }

    private fun buildTournamentScoreboard(player: TraderMatchStats, playerTakes: Int): ScoreboardResult {
        val rank = when {
            playerTakes >= 4 -> 1
            playerTakes == 3 -> 3
            playerTakes == 2 -> 12
            else -> 46
        }
        val isFirst = rank == 1
        val updatedPlayer = player.copy(placementRank = rank)
        val leaders = mutableListOf(updatedPlayer)

        val rivals = listOf(
            TraderMatchStats("tourn_1", "Titan_Algo_4", "TITAN", false, "BTC/USD", 3, 1, 520.0, 600.0, 100.0, 5.0, 84.0, 75.0, 6.0, 3, if (isFirst) 2 else 1),
            TraderMatchStats("tourn_2", "SMC_Predator", "SIGMA", false, "SOL/USD", 3, 2, 380.0, 600.0, 200.0, 4.0, 78.0, 60.0, 3.0, 2, if (isFirst) 3 else 2),
            TraderMatchStats("tourn_3", "Wick_Harvester", "PROP", false, "ETH/USD", 2, 1, 290.0, 400.0, 100.0, 3.0, 71.0, 66.7, 4.0, 2, 4),
            TraderMatchStats("tourn_4", "Margin_Reaper", "APEX", false, "SUI/USD", 2, 2, 180.0, 400.0, 200.0, 2.0, 65.0, 50.0, 2.0, 1, 5)
        )
        leaders.addAll(rivals)
        val sorted = leaders.sortedBy { it.placementRank }

        val outcome = if (isFirst) MatchOutcome.SOLE_SURVIVOR else MatchOutcome.TOP_TIER_FINISH
        val mmr = if (isFirst) +85 else if (rank <= 10) +40 else -15
        val xp = if (isFirst) 950 else 350
        val note = "Finished Rank #$rank / 100 traders in the dynamic margin liquidation tournament."

        return ScoreboardResult(outcome, sorted, mmr, xp, note)
    }

    private fun buildFfaScoreboard(player: TraderMatchStats): ScoreboardResult {
        val ffaRivals = listOf(
            TraderMatchStats("ffa_1", "Apex_Scorcher", "SOLO", false, "SOL/USD", 4, 1, 710.0, 800.0, 100.0, 7.0, 81.0, 80.0, 8.0, 3, 1),
            TraderMatchStats("ffa_2", "Tokyo_Scalper", "ASIA", false, "ETH/USD", 3, 1, 490.0, 600.0, 100.0, 5.0, 77.0, 75.0, 6.0, 2, 2),
            TraderMatchStats("ffa_3", "Gold_Maverick", "COMMOD", false, "GOLD", 2, 1, 280.0, 400.0, 100.0, 3.0, 70.0, 66.7, 4.0, 2, 4),
            TraderMatchStats("ffa_4", "Doge_Rider", "MEME", false, "DOGE", 2, 2, 160.0, 400.0, 200.0, 2.0, 62.0, 50.0, 2.0, 1, 5)
        )
        val all = mutableListOf(player)
        all.addAll(ffaRivals)
        val sorted = all.sortedByDescending { it.netPnl }.mapIndexed { idx, s -> s.copy(placementRank = idx + 1) }
        val playerRank = sorted.find { it.isPlayer }?.placementRank ?: 1
        val outcome = if (playerRank <= 2) MatchOutcome.VICTORY else MatchOutcome.DEFEAT
        val mmr = if (playerRank == 1) +60 else if (playerRank <= 3) +25 else -20
        val xp = if (playerRank == 1) 650 else 220
        val note = "Secured Rank #$playerRank in the 8-trader Free-For-All Floor Combine."

        return ScoreboardResult(outcome, sorted, mmr, xp, note)
    }

    private fun buildSoloScoreboard(player: TraderMatchStats, grade: String): ScoreboardResult {
        val isPass = grade in listOf("A+", "A", "B")
        val outcome = if (isPass) MatchOutcome.QUALIFIED else MatchOutcome.DEFEAT
        val mmr = if (isPass) +30 else +5
        val xp = if (isPass) 350 else 100
        val note = "Discipline Rating: Grade $grade. Profit Factor: ${String.format("%.2f", player.profitFactor)}."
        return ScoreboardResult(outcome, listOf(player), mmr, xp, note)
    }

    private fun updateChallenges(debrief: MatchDebrief) {
        val p = debrief.playerStats
        ACTIVE_CHALLENGES.forEach { ch ->
            if (ch.modeId == debrief.modeId || ch.modeId == "all") {
                when (ch.id) {
                    "chal_1v1_sniper" -> {
                        if (p.precisionAccuracyPct >= 75.0) ch.currentValue = min(ch.targetValue, ch.currentValue + p.takeProfits)
                    }
                    "chal_5v5_carry" -> {
                        ch.currentValue = min(ch.targetValue, ch.currentValue + max(0.0, p.rMultiplesCaptured))
                    }
                    "chal_tournament_survival" -> {
                        ch.currentValue = min(ch.targetValue, ch.currentValue + (100 - p.placementRank))
                    }
                    "chal_ffa_volume" -> {
                        ch.currentValue = min(ch.targetValue, ch.currentValue + max(0.0, p.netPnl))
                    }
                    "chal_solo_streak" -> {
                        ch.currentValue = max(ch.currentValue, p.bestStreak.toDouble())
                    }
                }
                if (ch.currentValue >= ch.targetValue) {
                    ch.isCompleted = true
                }
            }
        }
    }

    private data class ScoreboardResult(
        val outcome: MatchOutcome,
        val scoreboard: List<TraderMatchStats>,
        val mmrDelta: Int,
        val xpDelta: Int,
        val summaryNote: String
    )
}
