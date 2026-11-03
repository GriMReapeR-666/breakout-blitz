package com.setuprush.candy.engine

/**
 * Strategy Mastery Progression Engine & Solo Campaign Command.
 * Implements:
 * 1. 7 Strategy Families with 5-Tier Trader Skill Benchmarks (Pure discipline, volume & execution precision).
 * 2. 35-Level Solo Player Campaign Missions (5 per strategy family) where the selected setup
 *    ACTUALLY OCCURS on the chart and must be caught by the player to achieve victory.
 * 3. 5-Trade Series Certification Protocol (Win rate >= 60%, Net R >= +3.0R, Profit Factor >= 2.0).
 * 4. Backward compatible with all existing StrategyMasteryManager APIs.
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
        val tierTitle: String
            get() = when (tier) {
                1 -> "NOVICE TRADER"
                2 -> "APPRENTICE"
                3 -> "PROP MASTER"
                4 -> "QUANT ANALYST"
                5 -> "APEX GRANDMASTER"
                else -> "MASTER TRADER"
            }
    }

    /** Real Trader Skill Benchmarks (Pure discipline & execution metrics, zero artificial RPG perks) */
    data class TraderSkillBenchmark(
        val title: String,
        val icon: String,
        val valueText: String,
        val description: String
    )

    fun getTraderSkillBenchmarks(key: String): List<TraderSkillBenchmark> {
        val prog = getProgress(key)
        val winRatePct = (prog.winRate * 100).toInt()
        return listOf(
            TraderSkillBenchmark("Execution Precision", "🎯", "$winRatePct% WIN RATE", "Authentic tape entry discipline and trigger reaction time."),
            TraderSkillBenchmark("Pattern Recognition", "👁️", "${prog.winCount} Verified Fills", "Real market setups caught and executed on live chart tape."),
            TraderSkillBenchmark("Capital Preservation", "🛡️", if (prog.lossCount == 0) "100% DISCIPLINE" else "${(prog.winRate * 85 + 15).toInt()}% STOP DISCIPLINE", "Consistency in strictly honoring planned invalidation stop loss."),
            TraderSkillBenchmark("Trader Rank", "👑", prog.tierTitle, "Earned trading rank based on verified volume, PnL, and discipline.")
        )
    }

    data class SoloCampaignMission(
        val levelId: Int, // 1 to 35
        val family: StrategyCatalog.StrategyFamily,
        val tierInFamily: Int, // 1 to 5
        val title: String,
        val callsign: String,
        val briefing: String,
        val targetSetupKey: String,
        val targetBias: String, // "BULLISH" or "BEARISH"
        val symbolId: String,
        val targetR: Double = 2.0,
        val maxDrawdownR: Double = 1.0,
        val tapeSpeed: Double = 1.0,
        val defaultLeverage: Double = 10.0,
        var starsEarned: Int = 0, // 0 to 3
        var isUnlocked: Boolean = false,
        val xpReward: Int = 250,
        val creditsReward: Int = 150
    )

    private val progressMap = mutableMapOf<String, StrategyProgress>()
    private val campaignMissions = mutableListOf<SoloCampaignMission>()

    var activeMission: SoloCampaignMission? = null

    init {
        // Initialize legacy setups
        StrategyCatalog.ALL_SETUPS.forEach { setup ->
            val isStarter = setup.key in listOf("engulfing", "smc_fvg", "flag", "pa_break_retest")
            progressMap[setup.key] = StrategyProgress(
                key = setup.key,
                tier = if (isStarter) 5 else 1,
                xp = if (isStarter) 2500 else 0,
                isCertifiedForMultiplayer = isStarter
            )
        }

        // Initialize 35 Solo Player Campaign Missions (5 per Strategy Family)
        val missionsDef = listOf(
            // Family 1: PRICE ACTION (1..5)
            SoloCampaignMission(1, StrategyCatalog.StrategyFamily.PRICE_ACTION, 1, "OPERATION BREAKOUT", "RETEST_ALPHA", "Identify the horizontal resistance breakout and buy the pullback retest floor.", "pa_break_retest", "BULLISH", "BTC", tapeSpeed = 0.85, isUnlocked = true),
            SoloCampaignMission(2, StrategyCatalog.StrategyFamily.PRICE_ACTION, 2, "TRENDLINE CRACK", "DIAGONAL_VIPER", "Catch the breakdown below ascending diagonal support and short the flip.", "pa_trendline_break", "BEARISH", "ETH", tapeSpeed = 0.90),
            SoloCampaignMission(3, StrategyCatalog.StrategyFamily.PRICE_ACTION, 3, "DOUBLE BOTTOM BASE", "TWIN_SHIELD", "Confirm the second trough rejection wick and enter long on the neckline break.", "dbottom", "BULLISH", "SOL", tapeSpeed = 0.95),
            SoloCampaignMission(4, StrategyCatalog.StrategyFamily.PRICE_ACTION, 4, "BULL FLAG CATAPULT", "FLAG_PENNANT", "Ride the upward breakout of a descending channel consolidation in an active bull trend.", "flag", "BULLISH", "GOLD", tapeSpeed = 1.05),
            SoloCampaignMission(5, StrategyCatalog.StrategyFamily.PRICE_ACTION, 5, "HEAD & SHOULDERS APEX", "APEX_REVERSAL", "Master trial: Spot the right shoulder exhaustion and short the neckline breakdown.", "pa_head_shoulders", "BEARISH", "BTC", tapeSpeed = 1.15),

            // Family 2: CANDLESTICK PATTERNS (6..10)
            SoloCampaignMission(6, StrategyCatalog.StrategyFamily.CANDLESTICK, 1, "HAMMER REBOUND", "IRON_HAMMER", "Spot the elongated lower shadow (2.5x body) at support and hold long.", "candle_hammer", "BULLISH", "BTC", tapeSpeed = 0.85, isUnlocked = true),
            SoloCampaignMission(7, StrategyCatalog.StrategyFamily.CANDLESTICK, 2, "SHOOTING STAR ROOF", "FALLING_STAR", "Enter short upon seeing high upper wick rejection at resistance ceiling.", "candle_shooting_star", "BEARISH", "ETH", tapeSpeed = 0.90),
            SoloCampaignMission(8, StrategyCatalog.StrategyFamily.CANDLESTICK, 3, "ENGULFING DOMINANCE", "BODY_SWALLOW", "Catch the high-volume green candle completely engulfing the preceding red candle.", "engulfing", "BULLISH", "SOL", tapeSpeed = 0.95),
            SoloCampaignMission(9, StrategyCatalog.StrategyFamily.CANDLESTICK, 4, "MORNING STAR DAWN", "TRIPLE_DAWN", "Identify the 3-candle bottom reversal: impulse down, star doji, impulse up.", "candle_morning_star", "BULLISH", "GOLD", tapeSpeed = 1.05),
            SoloCampaignMission(10, StrategyCatalog.StrategyFamily.CANDLESTICK, 5, "RAILROAD TRACKS", "TWIN_RAILS", "Master trial: Spot dual opposing bars of equal range signaling immediate institutional rejection.", "candle_railroad", "BEARISH", "DOGE", tapeSpeed = 1.20),

            // Family 3: SMART MONEY CONCEPTS (11..15)
            SoloCampaignMission(11, StrategyCatalog.StrategyFamily.SMC, 1, "FAIR VALUE GAP", "IMBALANCE_SWARM", "Enter long as price dips into the 3-candle fair value imbalance gap.", "smc_fvg", "BULLISH", "BTC", tapeSpeed = 0.90, isUnlocked = true),
            SoloCampaignMission(12, StrategyCatalog.StrategyFamily.SMC, 2, "ORDER BLOCK BASTION", "INSTITUTIONAL_FOOTPRINT", "Catch the mitigative retest of the last bearish candle before impulse rally.", "smc_order_block", "BULLISH", "ETH", tapeSpeed = 0.95),
            SoloCampaignMission(13, StrategyCatalog.StrategyFamily.SMC, 3, "LIQUIDITY SWEEP", "STOP_HUNTER", "Identify stop raid above equal highs followed by immediate displacement lower.", "smc_liquidity_sweep", "BEARISH", "SOL", tapeSpeed = 1.05),
            SoloCampaignMission(14, StrategyCatalog.StrategyFamily.SMC, 4, "STRUCTURE SHIFT", "CHoCH_DISPLACEMENT", "Trade the decisive Change of Character (CHoCH) swing high break with displacement.", "smc_structure_shift", "BULLISH", "GOLD", tapeSpeed = 1.10),
            SoloCampaignMission(15, StrategyCatalog.StrategyFamily.SMC, 5, "BREAKER BLOCK FLIP", "BREAKER_CRUSH", "Master trial: Prior invalid order block flipped into aggressive institutional resistance.", "smc_breaker", "BEARISH", "SUI", tapeSpeed = 1.25),

            // Family 4: CANDLE RANGE THEORY (16..20)
            SoloCampaignMission(16, StrategyCatalog.StrategyFamily.CRT, 1, "AMD POWER OF THREE", "JUDAS_SWEEP", "Identify London session judas sweep manipulation before NY session expansion.", "crt_amd", "BULLISH", "BTC", tapeSpeed = 0.90, isUnlocked = true),
            SoloCampaignMission(17, StrategyCatalog.StrategyFamily.CRT, 2, "OPENING RANGE RUSH", "ORB_ACCELERATOR", "Enter aggressive long on break of 15-minute Opening Range high.", "crt_orb", "BULLISH", "ETH", tapeSpeed = 1.00),
            SoloCampaignMission(18, StrategyCatalog.StrategyFamily.CRT, 3, "PREVIOUS DAY HIGH SWEEP", "TURTLE_SOUP", "Short the failed breakout above yesterday's high returning into value.", "crt_pdh_sweep", "BEARISH", "SOL", tapeSpeed = 1.05),
            SoloCampaignMission(19, StrategyCatalog.StrategyFamily.CRT, 4, "SESSION EXPANSION", "KILLZONE_SURGE", "Ride high-beta volatility surge during London/NY session overlap.", "crt_session_expansion", "BULLISH", "GOLD", tapeSpeed = 1.15),
            SoloCampaignMission(20, StrategyCatalog.StrategyFamily.CRT, 5, "HTF WICK RETRACE", "BODY_FILL", "Master trial: Higher timeframe rejection wick fill on 1m intraday tape.", "crt_wick_fill", "BEARISH", "DOGE", tapeSpeed = 1.25),

            // Family 5: TREND FOLLOWING (21..25)
            SoloCampaignMission(21, StrategyCatalog.StrategyFamily.TREND_FOLLOWING, 1, "SUPERTREND SURF", "GREEN_RIBBON", "Enter on green Supertrend flip and ride high-momentum trending tape.", "trend_supertrend", "BULLISH", "BTC", tapeSpeed = 0.90, isUnlocked = true),
            SoloCampaignMission(22, StrategyCatalog.StrategyFamily.TREND_FOLLOWING, 2, "EMA GOLDEN CROSS", "DUAL_VECTOR", "Ride the 20 EMA crossing above the 50 EMA with expanding bands.", "trend_ema_cross", "BULLISH", "ETH", tapeSpeed = 0.95),
            SoloCampaignMission(23, StrategyCatalog.StrategyFamily.TREND_FOLLOWING, 3, "MOVING AVERAGE PULLBACK", "DYNAMIC_SPRING", "Exercise patience: Wait for the orderly retest of the rising 20 EMA to enter long.", "trend_ma_pullback", "BULLISH", "SOL", tapeSpeed = 1.05),
            SoloCampaignMission(24, StrategyCatalog.StrategyFamily.TREND_FOLLOWING, 4, "DONCHIAN CHANNEL SPRINT", "TURTLE_LEGEND", "Ride the 20-candle highest high channel breakout without second-guessing.", "trend_donchian", "BULLISH", "GOLD", tapeSpeed = 1.15),
            SoloCampaignMission(25, StrategyCatalog.StrategyFamily.TREND_FOLLOWING, 5, "PARABOLIC SAR ACCEL", "RADAR_DOTS", "Master trial: Spot trend exhaustion and enter short upon SAR dot flip above price.", "trend_psar", "BEARISH", "SUI", tapeSpeed = 1.25),

            // Family 6: MEAN REVERSION & OSCILLATORS (26..30)
            SoloCampaignMission(26, StrategyCatalog.StrategyFamily.MEAN_REVERSION, 1, "RSI DIVERGENCE", "MOMENTUM_MISMATCH", "Price prints lower low while RSI prints higher low at oversold territory.", "revert_rsi_div", "BULLISH", "BTC", tapeSpeed = 0.90, isUnlocked = true),
            SoloCampaignMission(27, StrategyCatalog.StrategyFamily.MEAN_REVERSION, 2, "BOLLINGER BAND PINCH", "VOLATILITY_SQUEEZE", "Catch the explosive directional breakout following extreme band compression.", "revert_bb_pinch", "BULLISH", "ETH", tapeSpeed = 1.00),
            SoloCampaignMission(28, StrategyCatalog.StrategyFamily.MEAN_REVERSION, 3, "KELTNER EXTREME FADE", "RUBBER_BAND", "Fade the +3 ATR extension outside upper Keltner channel back toward baseline.", "revert_keltner", "BEARISH", "SOL", tapeSpeed = 1.05),
            SoloCampaignMission(29, StrategyCatalog.StrategyFamily.MEAN_REVERSION, 4, "STOCHASTIC POP", "OSCILLATOR_ROCKET", "Enter long upon sub-15 oversold bullish stochastic %K / %D crossover.", "revert_stoch_pop", "BULLISH", "GOLD", tapeSpeed = 1.15),
            SoloCampaignMission(30, StrategyCatalog.StrategyFamily.MEAN_REVERSION, 5, "VWAP MEAN SNAP", "CENTRAL_MAGNET", "Master trial: Exploit violent extension from Institutional VWAP and ride mean reversion.", "revert_vwap_snap", "BEARISH", "DOGE", tapeSpeed = 1.30),

            // Family 7: COMMUNITY TOP INDICATORS (31..35)
            SoloCampaignMission(31, StrategyCatalog.StrategyFamily.INDICATOR_TOP, 1, "SQUEEZE MOMENTUM", "CARTER_FIRE", "Enter long as black squeeze dots release into explosive light green histogram.", "comm_squeeze", "BULLISH", "BTC", tapeSpeed = 0.95, isUnlocked = true),
            SoloCampaignMission(32, StrategyCatalog.StrategyFamily.INDICATOR_TOP, 2, "WAVETREND GOLDEN DOT", "LAZYBEAR_CROSS", "Buy the oversold green WaveTrend line crossing up through red line with gold circle.", "comm_wavetrend", "BULLISH", "ETH", tapeSpeed = 1.00),
            SoloCampaignMission(33, StrategyCatalog.StrategyFamily.INDICATOR_TOP, 3, "UT BOT SHORT ALERT", "QUANT_SIGNAL", "Short the chart instantly when UT Bot prints red sell arrow with trailing stop.", "comm_utbot", "BEARISH", "SOL", tapeSpeed = 1.10),
            SoloCampaignMission(34, StrategyCatalog.StrategyFamily.INDICATOR_TOP, 4, "NADARAYA-WATSON BOUNCE", "KERNEL_BOUND", "Buy the non-parametric regression lower envelope contact bounce.", "comm_nadaraya", "BULLISH", "GOLD", tapeSpeed = 1.15),
            SoloCampaignMission(35, StrategyCatalog.StrategyFamily.INDICATOR_TOP, 5, "WADDAH ATTAR EXPLOSION", "DEADZONE_BREAKER", "Master trial: Buy when green MACD volume pillar punches through explosion line.", "comm_waddah", "BULLISH", "SUI", tapeSpeed = 1.35)
        )
        campaignMissions.addAll(missionsDef)
    }

    // ==================== CAMPAIGN MISSIONS APIS ====================

    fun getAllCampaignMissions(): List<SoloCampaignMission> = campaignMissions

    fun getMissionsForFamily(family: StrategyCatalog.StrategyFamily): List<SoloCampaignMission> {
        return campaignMissions.filter { it.family == family }
    }

    fun getMissionById(id: Int): SoloCampaignMission? {
        return campaignMissions.find { it.levelId == id }
    }

    /**
     * Marks a campaign mission as completed, calculates star rating (1 to 3),
     * awards XP, Credits, and unlocks the subsequent mission level.
     */
    fun completeMission(levelId: Int, stars: Int, pnl: Double): Boolean {
        val mission = campaignMissions.find { it.levelId == levelId } ?: return false
        val awardedStars = stars.coerceIn(1, 3)
        if (awardedStars > mission.starsEarned) {
            val deltaStars = awardedStars - mission.starsEarned
            mission.starsEarned = awardedStars
            MonetizationManager.addCredits(mission.creditsReward * deltaStars)
            PlayerProfileManager.recordMatch(
                isWin = true,
                pnl = pnl,
                rMultiple = 2.0,
                format = "campaign_mission_${mission.levelId}",
                symbol = mission.symbolId
            )
        }

        // Unlock next mission
        val nextMission = campaignMissions.find { it.levelId == levelId + 1 }
        if (nextMission != null) {
            nextMission.isUnlocked = true
        }
        return true
    }

    // ==================== LEGACY STRATEGY PROGRESS APIS ====================

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

    // ==================== 5-TRADE SERIES MASTERY EVALUATION PROTOCOL ====================

    data class MasteryTradeRecord(
        val tradeNumber: Int,
        val isWin: Boolean,
        val rMultiple: Double,
        val pnl: Double,
        val entryPrice: Double,
        val exitPrice: Double,
        val timingGrade: String
    )

    data class StrategyMasteryEvaluation(
        val strategyKey: String,
        val seriesTargetTrades: Int = 5,
        val trades: MutableList<MasteryTradeRecord> = mutableListOf(),
        var isPassed: Boolean = false,
        var awardedStars: Int = 0
    ) {
        val completedCount: Int get() = trades.size
        val winCount: Int get() = trades.count { it.isWin }
        val lossCount: Int get() = trades.count { !it.isWin }
        val winRate: Double get() = if (trades.isEmpty()) 0.0 else winCount.toDouble() / trades.size
        val netR: Double get() = trades.sumOf { it.rMultiple }
        val grossProfit: Double get() = trades.filter { it.isWin }.sumOf { it.pnl }
        val grossLoss: Double get() = trades.filter { !it.isWin }.sumOf { kotlin.math.abs(it.pnl) }
        val profitFactor: Double get() = if (grossLoss == 0.0) (if (grossProfit > 0) 9.99 else 0.0) else grossProfit / grossLoss

        val meetsCertification: Boolean
            get() = trades.size >= seriesTargetTrades && winRate >= 0.60 && netR >= 3.0 && profitFactor >= 2.0
    }

    var activeEvaluation: StrategyMasteryEvaluation? = null

    fun startEvaluation(strategyKey: String): StrategyMasteryEvaluation {
        val eval = StrategyMasteryEvaluation(strategyKey = strategyKey)
        activeEvaluation = eval
        return eval
    }

    fun recordEvaluationTrade(
        isWin: Boolean,
        rMultiple: Double,
        pnl: Double,
        entryPrice: Double,
        exitPrice: Double,
        timingGrade: String = "CLEAN"
    ): StrategyMasteryEvaluation? {
        val eval = activeEvaluation ?: return null
        val tradeNum = eval.trades.size + 1
        eval.trades.add(
            MasteryTradeRecord(
                tradeNumber = tradeNum,
                isWin = isWin,
                rMultiple = rMultiple,
                pnl = pnl,
                entryPrice = entryPrice,
                exitPrice = exitPrice,
                timingGrade = timingGrade
            )
        )
        if (eval.trades.size >= eval.seriesTargetTrades) {
            eval.isPassed = eval.meetsCertification
            eval.awardedStars = if (eval.winRate >= 0.8 && eval.netR >= 4.0) 3 else if (eval.winRate >= 0.6) 2 else 1
            if (eval.isPassed) {
                recordTrade(eval.strategyKey, isWin = true, rMultiple = 3.0)
                MonetizationManager.addCredits(300)
            }
        }
        return eval
    }

    fun resetEvaluation() {
        activeEvaluation = null
    }
}
