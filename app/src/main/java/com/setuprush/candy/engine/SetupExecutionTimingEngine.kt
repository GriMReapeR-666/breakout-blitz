package com.setuprush.candy.engine

/**
 * Real-Time Trade Execution Timing & Missed Setup Detection Engine.
 * Analyzes player reaction to indicator and setup entry signals:
 * - PERFECT_ENTRY: Entered within 0-2 candles of trigger (Max R:R, +100 Bonus XP)
 * - GOOD_ENTRY: Entered within 3 candles of trigger (+50 XP)
 * - LATE_ENTRY: Chased trade after price already expanded >1R (-20 XP penalty)
 * - FOMO_EARLY: Entered before signal confirmation (-30 XP)
 * - MISSED_SETUP: Setup triggered and hit +2R target while trader remained flat
 * - DISCIPLINED_PASS: Player stayed flat on a false/failing setup that hit stop loss
 */
object SetupExecutionTimingEngine {

    enum class ExecutionTimingGrade(val badge: String, val title: String, val xpDelta: Int, val description: String) {
        PERFECT_ENTRY("🎯", "SNIPER EXECUTION", +100, "Entered immediately upon signal confirmation with optimal R:R."),
        GOOD_ENTRY("✅", "SOLID ENTRY", +50, "Entered within tolerance window; favorable risk-reward intact."),
        LATE_ENTRY("⚠️", "CHASED ENTRY", -20, "Entered late after market expanded; unfavorable risk-reward ratio."),
        FOMO_EARLY("❌", "PREMATURE FOMO", -30, "Entered before trigger confirmation; high risk of getting trapped."),
        MISSED_SETUP("⌛", "MISSED SETUP", 0, "Signal triggered and hit +2R target, but you remained flat."),
        DISCIPLINED_PASS("🛡️", "DISCIPLINED PASS", +40, "Avoided a false setup that ultimately failed into stop loss.")
    }

    data class TimingFeedback(
        val grade: ExecutionTimingGrade,
        val setupName: String,
        val candleDelta: Int,
        val message: String,
        val leakTip: String
    )

    var currentFeedback: TimingFeedback? = null
        private set

    var missedSetupsCount: Int = 0
        private set

    var sniperEntriesCount: Int = 0
        private set

    var chasedEntriesCount: Int = 0
        private set

    /** Evaluates player entry timing when they press and hold on canvas */
    fun evaluateEntry(activeSetup: SetupRecord?, tapePosition: Double, isLong: Boolean): TimingFeedback {
        if (activeSetup == null) {
            val fb = TimingFeedback(
                grade = ExecutionTimingGrade.FOMO_EARLY,
                setupName = "Unconfirmed Setup",
                candleDelta = 0,
                message = "NO CONFIRMED SETUP: Trading without a valid setup pattern.",
                leakTip = "Wait for Pattern Lock or indicator signal before holding position."
            )
            currentFeedback = fb
            return fb
        }

        val setupIdx = activeSetup.at
        val currentIdx = tapePosition.toInt()
        val delta = currentIdx - setupIdx

        val isDirectionCorrect = (activeSetup.dir == 1 && isLong) || (activeSetup.dir == -1 && !isLong)

        val fb = if (!isDirectionCorrect) {
            TimingFeedback(
                grade = ExecutionTimingGrade.FOMO_EARLY,
                setupName = activeSetup.name,
                candleDelta = delta,
                message = "WRONG BIAS: Entered ${if (isLong) "LONG" else "SHORT"} on a ${if (activeSetup.dir == 1) "BULLISH" else "BEARISH"} setup!",
                leakTip = "Check Pattern Lock banner direction before executing."
            )
        } else if (delta < 0) {
            TimingFeedback(
                grade = ExecutionTimingGrade.FOMO_EARLY,
                setupName = activeSetup.name,
                candleDelta = delta,
                message = "EARLY FOMO: Triggered $delta candles before confirmation close.",
                leakTip = "Wait for candle close confirmation to avoid false breakout wicks."
            )
        } else if (delta <= 2) {
            sniperEntriesCount++
            TimingFeedback(
                grade = ExecutionTimingGrade.PERFECT_ENTRY,
                setupName = activeSetup.name,
                candleDelta = delta,
                message = "SNIPER ENTRY: Snapped within ${delta} bar(s) of trigger! (+100 XP)",
                leakTip = "Exceptional reaction time and discipline."
            )
        } else if (delta <= 4) {
            TimingFeedback(
                grade = ExecutionTimingGrade.GOOD_ENTRY,
                setupName = activeSetup.name,
                candleDelta = delta,
                message = "GOOD ENTRY: Executed with acceptable slippage.",
                leakTip = "Try to enter on the first retest tick for higher R:R."
            )
        } else {
            chasedEntriesCount++
            TimingFeedback(
                grade = ExecutionTimingGrade.LATE_ENTRY,
                setupName = activeSetup.name,
                candleDelta = delta,
                message = "CHASED LATE: Entered ${delta} candles after trigger! R:R degraded.",
                leakTip = "If price has already moved past 1R, let it go and wait for next setup."
            )
        }

        currentFeedback = fb
        return fb
    }

    /** Monitored on each candle tick to detect missed setups or disciplined passes */
    fun checkTapeProgression(activeSetup: SetupRecord?, currentPrice: Double, isPlayerHolding: Boolean): TimingFeedback? {
        if (activeSetup == null || isPlayerHolding) return null

        val isBull = activeSetup.dir == 1
        val target = if (isBull) activeSetup.entry + (activeSetup.risk * 2.0) else activeSetup.entry - (activeSetup.risk * 2.0)
        val stop = activeSetup.stop

        // Check if setup reached +2R target while player was FLAT
        val hitTarget = if (isBull) currentPrice >= target else currentPrice <= target
        val hitStop = if (isBull) currentPrice <= stop else currentPrice >= stop

        if (hitTarget) {
            missedSetupsCount++
            val fb = TimingFeedback(
                grade = ExecutionTimingGrade.MISSED_SETUP,
                setupName = activeSetup.name,
                candleDelta = 0,
                message = "MISSED SETUP: ${activeSetup.name.uppercase()} reached +2.0R target while you were flat!",
                leakTip = "Hesitation costs alpha. Trust your indicator confluence and execute."
            )
            currentFeedback = fb
            return fb
        } else if (hitStop) {
            val fb = TimingFeedback(
                grade = ExecutionTimingGrade.DISCIPLINED_PASS,
                setupName = activeSetup.name,
                candleDelta = 0,
                message = "DISCIPLINED PASS: Avoided a failed setup that hit invalidation stop! (+40 XP)",
                leakTip = "Preserving capital on low-conviction setups is an edge."
            )
            currentFeedback = fb
            return fb
        }
        return null
    }

    fun clearFeedback() {
        currentFeedback = null
    }

    fun resetStats() {
        currentFeedback = null
        missedSetupsCount = 0
        sniperEntriesCount = 0
        chasedEntriesCount = 0
    }
}
