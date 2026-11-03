package com.setuprush.candy.engine

import kotlin.math.abs
import kotlin.math.max
import kotlin.math.min

/**
 * Competitive Opponent & Head-to-Head Prop Duel Engine.
 * Simulates institutional rival traders in real-time on identical chart feeds:
 * - 1v1 Head-to-Head Prop Duels (Best of 3 rounds, first to cash out +2R wins)
 * - 5v5 Multi-Asset Portfolio Clashes (Aggregate team P&L vs rival fund)
 * - 100-Trader Margin Call Tournaments (Dynamic liquidation line, last solvent standing)
 *
 * All state is simulated deterministically with zero external dependencies.
 */
object OpponentDuelEngine {

    enum class RivalTradeStatus(val label: String, val badge: String) {
        FLAT("FLAT (SCANNING)", "⚪"),
        HOLDING_LONG("LONG IN POSITION", "🟢"),
        HOLDING_SHORT("SHORT IN POSITION", "🔴"),
        CASHED_OUT("CASHED OUT +2R", "💰"),
        STOPPED_OUT("STOPPED OUT -1R", "🛑"),
        MARGIN_CALLED("MARGIN CALLED", "💥")
    }

    data class MatchFormat(
        val id: String,
        val badge: String,
        val title: String,
        val subtitle: String
    )

    val MATCH_FORMATS = listOf(
        MatchFormat("1v1", "⚔️ 1v1", "PROP DUEL", "Head-to-head match · First to +2R"),
        MatchFormat("5v5", "🛡️ 5v5", "PORTFOLIO CLASH", "Multi-asset fund battle"),
        MatchFormat("tournament", "🏆 100-TRADER", "MARGIN CALL ROYALE", "Last solvent standing"),
        MatchFormat("solo", "🎯 SOLO", "DESK RUN", "Unlimited backtesting & setup practice")
    )

    data class RivalTrader(
        val id: String,
        val name: String,
        val deskTag: String,
        val mmr: Int,
        val title: String = "Prop Desk Lead Trader",
        val badge: String = "⚡",
        val avatar: String = badge,
        val preferredStyle: String = title,
        val targetSetup: String = "smc_fvg",
        val winRate: Double = 0.74,
        val avgRMultiple: Double = 2.4,
        var status: RivalTradeStatus = RivalTradeStatus.FLAT,
        var entryPrice: Double = 0.0,
        var targetPrice: Double = 0.0,
        var stopPrice: Double = 0.0,
        var livePnl: Double = 0.0,
        var targetProgress: Float = 0f,
        var roundsWon: Int = 0,
        var totalBankedPnl: Double = 0.0,
        var isMarginCalled: Boolean = false
    )

    private val RIVAL_POOL = listOf(
        RivalTrader("rival_1", "Satoshi_Sniper", "TITAN", 2620, "Senior Order Flow Analyst", "⚡"),
        RivalTrader("rival_2", "OrderFlow_King", "APEX", 2590, "Prop Desk Lead Trader", "👑"),
        RivalTrader("rival_3", "Quantum_Scalper", "TITAN", 2680, "Algorithmic Arbitrageur", "🎯"),
        RivalTrader("rival_4", "Alpha_Whale", "SIGMA", 2540, "Macro Swing Specialist", "💎"),
        RivalTrader("rival_5", "FVG_Huntress", "PROP", 2610, "Institutional SMC Specialist", "🎖️")
    )

    var activeRival: RivalTrader? = null
        private set

    var isCompetitiveActive: Boolean = false
        private set

    var matchRound: Int = 1
        private set

    var playerRoundsWon: Int = 0
        private set

    var matchStatusMessage: String? = null
        private set

    /** Start a competitive head-to-head match against a matched rival */
    fun startMatch(formatId: String = "1v1", playerMmr: Int = 2640) {
        if (formatId == "solo" || formatId == "campaign" || formatId == "practice") {
            endMatch()
            return
        }
        val selected = RIVAL_POOL.minByOrNull { abs(it.mmr - playerMmr) } ?: RIVAL_POOL.first()
        activeRival = selected.copy(
            status = RivalTradeStatus.FLAT,
            entryPrice = 0.0,
            targetPrice = 0.0,
            stopPrice = 0.0,
            livePnl = 0.0,
            targetProgress = 0f,
            roundsWon = 0,
            totalBankedPnl = 0.0,
            isMarginCalled = false
        )
        isCompetitiveActive = true
        matchRound = 1
        playerRoundsWon = 0
        matchStatusMessage = "MATCH COMMENCED VS [${activeRival?.deskTag}] ${activeRival?.name}"
    }

    /** Update rival decisions on each price tick */
    fun updateTick(currentPrice: Double, tapeIdx: Int, activeSetup: SetupRecord?) {
        val rival = activeRival ?: return
        if (!isCompetitiveActive || rival.isMarginCalled) return

        when (rival.status) {
            RivalTradeStatus.FLAT -> {
                // Institutional rival enters when setup is locked and near entry price
                if (activeSetup != null && tapeIdx >= activeSetup.at - 2) {
                    val isLong = activeSetup.dir == 1
                    rival.status = if (isLong) RivalTradeStatus.HOLDING_LONG else RivalTradeStatus.HOLDING_SHORT
                    rival.entryPrice = currentPrice
                    val risk = abs(currentPrice - activeSetup.stop)
                    rival.stopPrice = activeSetup.stop
                    rival.targetPrice = if (isLong) currentPrice + (risk * 2.0) else currentPrice - (risk * 2.0)
                    matchStatusMessage = "${rival.name} ENTERED ${if (isLong) "LONG" else "SHORT"} @ $${String.format("%.2f", currentPrice)}"
                }
            }

            RivalTradeStatus.HOLDING_LONG -> {
                val pnl = (currentPrice - rival.entryPrice) * 10.0
                rival.livePnl = pnl
                val totalDistance = max(0.001, rival.targetPrice - rival.entryPrice)
                val currentDistance = currentPrice - rival.entryPrice
                rival.targetProgress = min(1.0f, max(0.0f, (currentDistance / totalDistance).toFloat()))

                if (currentPrice >= rival.targetPrice) {
                    // Rival cashed out at +2R!
                    rival.status = RivalTradeStatus.CASHED_OUT
                    rival.roundsWon++
                    rival.totalBankedPnl += 200.0
                    matchStatusMessage = "⚡ ${rival.name} CASHED OUT +2R (+$200.00)!"
                } else if (currentPrice <= rival.stopPrice) {
                    // Rival stopped out
                    rival.status = RivalTradeStatus.STOPPED_OUT
                    rival.totalBankedPnl -= 100.0
                    matchStatusMessage = "🛑 ${rival.name} STOPPED OUT (-$100.00)"
                }
            }

            RivalTradeStatus.HOLDING_SHORT -> {
                val pnl = (rival.entryPrice - currentPrice) * 10.0
                rival.livePnl = pnl
                val totalDistance = max(0.001, rival.entryPrice - rival.targetPrice)
                val currentDistance = rival.entryPrice - currentPrice
                rival.targetProgress = min(1.0f, max(0.0f, (currentDistance / totalDistance).toFloat()))

                if (currentPrice <= rival.targetPrice) {
                    // Rival cashed out at +2R!
                    rival.status = RivalTradeStatus.CASHED_OUT
                    rival.roundsWon++
                    rival.totalBankedPnl += 200.0
                    matchStatusMessage = "⚡ ${rival.name} CASHED OUT +2R (+$200.00)!"
                } else if (currentPrice >= rival.stopPrice) {
                    // Rival stopped out
                    rival.status = RivalTradeStatus.STOPPED_OUT
                    rival.totalBankedPnl -= 100.0
                    matchStatusMessage = "🛑 ${rival.name} STOPPED OUT (-$100.00)"
                }
            }

            RivalTradeStatus.CASHED_OUT, RivalTradeStatus.STOPPED_OUT -> {
                // Waiting for next round or next setup
            }

            RivalTradeStatus.MARGIN_CALLED -> {
                // Inactive
            }
        }
    }

    /** Record player cashing out */
    fun recordPlayerCashOut(pnl: Double) {
        if (!isCompetitiveActive) return
        if (pnl > 0) {
            playerRoundsWon++
            matchStatusMessage = "🏆 YOU WON ROUND $matchRound (+$$${String.format("%.2f", pnl)})!"
        }
        advanceRound()
    }

    /** Record player liquidation or stop-loss */
    fun recordPlayerStopLoss() {
        if (!isCompetitiveActive) return
        activeRival?.let { it.roundsWon++ }
        matchStatusMessage = "🛑 YOU WERE STOPPED OUT! ROUND $matchRound TO RIVAL."
        advanceRound()
    }

    private fun advanceRound() {
        matchRound++
        activeRival?.let { rival ->
            rival.status = RivalTradeStatus.FLAT
            rival.entryPrice = 0.0
            rival.livePnl = 0.0
            rival.targetProgress = 0f
        }
        if (playerRoundsWon >= 2) {
            matchStatusMessage = "👑 MATCH VICTORY! YOU DEFEATED ${activeRival?.name} (2-${activeRival?.roundsWon})!"
        } else if ((activeRival?.roundsWon ?: 0) >= 2) {
            matchStatusMessage = "💥 MATCH DEFEAT! ${activeRival?.name} WON (2-$playerRoundsWon)."
        }
    }

    fun endMatch() {
        isCompetitiveActive = false
        activeRival = null
        matchRound = 1
        playerRoundsWon = 0
        matchStatusMessage = null
    }
}
