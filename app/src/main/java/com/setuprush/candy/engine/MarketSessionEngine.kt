package com.setuprush.candy.engine

import java.util.Calendar

/**
 * Institutional Market Session, Daily Shuffled Historical Database & Multi-Timeframe Engine.
 * Simulates real trading conditions across all global trading sessions,
 * daily-shuffled historical market regimes, and multi-timeframe candle resolutions.
 */
object MarketSessionEngine {

    enum class MarketSession(
        val sessionName: String,
        val tag: String,
        val utcHours: String,
        val volatilityMul: Double,
        val characteristic: String
    ) {
        ASIAN_ACCUMULATION(
            "Asian Range Accumulation",
            "ASIA",
            "00:00 - 06:00 UTC",
            0.65,
            "Sideways range consolidation; liquidity pools building at equal highs/lows."
        ),
        LONDON_EXPANSION(
            "London Open (Judas Swing)",
            "LONDON",
            "07:00 - 10:00 UTC",
            1.45,
            "False directional manipulation sweep before true macro trend expansion."
        ),
        NEW_YORK_AM_KILLZONE(
            "New York AM (Silver Bullet)",
            "NY_AM",
            "13:30 - 16:00 UTC",
            1.85,
            "Peak institutional volume, maximum momentum follow-through, and FVG mitigations."
        ),
        LONDON_CLOSE_REVERSION(
            "London Close Reversal",
            "LONDON_CLOSE",
            "15:00 - 17:00 UTC",
            1.15,
            "Institutional profit taking, mean reversion bounces, and exhaustion fades."
        )
    }

    enum class Timeframe(
        val displayName: String,
        val code: String,
        val tickIntervalSec: Double,
        val idealStyle: String
    ) {
        TF_1S("1-Second Turbo", "1S", 1.0, "Hyper-scalping & instant reflex instinct execution"),
        TF_5S("5-Second Blitz", "5S", 5.0, "Standard competitive duel combine speed"),
        TF_1M("1-Minute Tactical", "1M", 60.0, "Intraday pattern lock & candlestick confirmations"),
        TF_5M("5-Minute Structure", "5M", 300.0, "High-probability SMC Fair Value Gaps & Order Blocks"),
        TF_15M("15-Minute Macro", "15M", 900.0, "Macro bias alignment & institutional liquidity sweeps")
    }

    enum class HistoricalRegime(
        val regimeName: String,
        val historicalDate: String,
        val baselineTrend: String,
        val baseVolatility: Double,
        val description: String
    ) {
        ETF_BREAKOUT(
            "Bitcoin Institutional ETF Breakout",
            "FEB-MAR 2024",
            "SUPER_BULLISH",
            1.6,
            "Massive sustained impulse trend riding above 20 EMA with minimal pullbacks."
        ),
        LIQUIDITY_CASCADE(
            "Black Swan Liquidity Hunt",
            "MAR 12 2020",
            "CASCADING_CRASH",
            2.5,
            "Violent cascading margin calls, order book vacuums, and sharp mean-reversion wicks."
        ),
        WYCKOFF_SPRING(
            "Wyckoff Accumulation & Spring",
            "JULY 2021",
            "ACCUMULATION_REVERSAL",
            1.1,
            "Extended consolidation floor with final deceptive bear trap sweep before takeoff."
        ),
        RATE_DECISION_SHOCK(
            "Federal Reserve Interest Rate Shock",
            "NOV 2023",
            "HIGH_WHIPSAW",
            2.2,
            "Violent bi-directional wicks wiping out both breakout buyers and early short sellers."
        ),
        SUI_MAINNET_MOMENTUM(
            "Layer-1 Ecosystem Price Discovery",
            "OCT 2024",
            "ALGO_MOMENTUM",
            1.8,
            "Clear Smart Money Concept Fair Value Gaps respecting 0.50 equilibrium retracements."
        )
    }

    var activeTimeframe: Timeframe = Timeframe.TF_5S
    var activeSession: MarketSession = MarketSession.NEW_YORK_AM_KILLZONE

    /** Deterministic daily seed that shuffles the market database based on current calendar day */
    fun getDailyDatabaseSeed(): Long {
        val cal = Calendar.getInstance()
        val day = cal.get(Calendar.DAY_OF_YEAR)
        val year = cal.get(Calendar.YEAR)
        return ((day.toLong() * 31337L) xor (year.toLong() * 1337L)) and 0x7FFFFFFFL
    }

    /** Returns today's shuffled historical market regime */
    fun getDailyMarketRegime(): HistoricalRegime {
        val seed = getDailyDatabaseSeed()
        val index = (seed % HistoricalRegime.values().size).toInt()
        return HistoricalRegime.values()[index]
    }

    /** Real trading conditions: calculate dynamic spread in basis points */
    fun getLiveSpreadBps(symbol: MarketSymbolCatalog.MarketSymbol): Double {
        val baseSpread = when (symbol.volatilityTier) {
            "CALM" -> 2.0
            "NORMAL" -> 3.5
            "STORM" -> 6.0
            else -> 10.0 // VOLCANO
        }
        return baseSpread * activeSession.volatilityMul
    }
}
