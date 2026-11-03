package com.setuprush.candy.engine

typealias StrategyGroup = StrategyCatalog.StrategyFamily

/**
 * Master catalog of all trading strategies and setups across Setup Rush.
 * Encompasses Price Action, Candlesticks, Smart Money Concepts (SMC),
 * Candle Range Theory (CRT), Trend Following, and Mean Reversion.
 */
object StrategyCatalog {

    enum class StrategyFamily(val displayName: String, val badge: String, val description: String) {
        PRICE_ACTION("Price Action", "PA", "Horizontal support/resistance, breakouts, retests, and structural flips."),
        CANDLESTICK("Candlestick Patterns", "CANDLE", "High-probability Japanese single, double, and multi-bar formations."),
        SMC("Smart Money Concepts", "SMC", "Institutional order flow, Fair Value Gaps, liquidity sweeps, and order blocks."),
        CRT("Candle Range Theory", "CRT", "Range expansion, Power of 3 (AMD), Opening Range Breakouts, and session cycles."),
        TREND_FOLLOWING("Trend Following", "TREND", "Momentum continuation, Supertrend, moving average ribbons, and channel surfing."),
        MEAN_REVERSION("Mean Reversion & Oscillators", "REVERT", "RSI divergence, extreme oversold/overbought fading, and Bollinger mean bounces."),
        INDICATOR_TOP("Community Top Indicators", "COMM_TOP", "Famous community indicators: Squeeze Momentum, WaveTrend, UT Bot, Nadaraya-Watson, QQE, Waddah Attar.")
    }

    data class SetupCardData(
        val key: String,
        val name: String,
        val family: StrategyFamily,
        val bias: String, // "BULLISH", "BEARISH", "BOTH"
        val winRate: Double,
        val lesson: String,
        val identificationRule: String,
        val entryTrigger: String,
        val invalidationStop: String,
        val targetTakeProfit: String = "+2R (Double Risk)",
        val commonTrap: String
    ) {
        val anatomy: String get() = identificationRule
        val trigger: String get() = entryTrigger
        val invalidation: String get() = invalidationStop
        val target: String get() = targetTakeProfit
        val trap: String get() = commonTrap
        val group: StrategyFamily get() = family
    }

    val ALL_SETUPS: List<SetupCardData> = listOf(
        // ==================== 1. PRICE ACTION ====================
        SetupCardData(
            key = "pa_break_retest",
            name = "Break & Retest",
            family = StrategyFamily.PRICE_ACTION,
            bias = "BOTH",
            winRate = 0.66,
            lesson = "Prior ceiling becomes the new floor; buy the first bounce.",
            identificationRule = "Clean horizontal resistance level tested at least twice, followed by a decisive impulse breakout candle.",
            entryTrigger = "Touch of previous resistance level after pullback with rejection wick.",
            invalidationStop = "Candle close back inside the prior consolidation range.",
            commonTrap = "Entering before the retest occurs; chasing the initial breakout wick into resistance."
        ),
        SetupCardData(
            key = "pa_trendline_break",
            name = "Trendline S/R Flip",
            family = StrategyFamily.PRICE_ACTION,
            bias = "BOTH",
            winRate = 0.63,
            lesson = "When a dynamic trend rail breaks, momentum accelerates.",
            identificationRule = "Diagonal support or resistance connecting 3 or more swing touches.",
            entryTrigger = "First candle closing decisively through the trendline boundary.",
            invalidationStop = "Opposite swing pivot formed just before the break.",
            commonTrap = "Drawing trendlines through candle bodies instead of wicks, leading to premature false entries."
        ),
        SetupCardData(
            key = "pa_range_fade",
            name = "Range High / Low Fade",
            family = StrategyFamily.PRICE_ACTION,
            bias = "BOTH",
            winRate = 0.61,
            lesson = "80% of breakouts fail in chop; fade the boundary extremes.",
            identificationRule = "Well-defined sideways channel with established equal highs (range top) and equal lows (range bottom).",
            entryTrigger = "Rejection wick piercing the boundary and closing back inside the range.",
            invalidationStop = "1.5x ATR beyond the range boundary high/low.",
            commonTrap = "Fading ranges during major high-impact news releases or opening volume surges."
        ),

        // ==================== 2. CANDLESTICKS ====================
        SetupCardData(
            key = "engulfing",
            name = "Bullish & Bearish Engulfing",
            family = StrategyFamily.CANDLESTICK,
            bias = "BOTH",
            winRate = 0.63,
            lesson = "A new body that completely swallows the body before it.",
            identificationRule = "Bar 2's real body completely covers the high-to-low range of Bar 1 in the counter direction.",
            entryTrigger = "Open of the next candle following the completed engulfing close.",
            invalidationStop = "Extreme wick of the engulfing candle.",
            commonTrap = "Taking engulfing candles in the middle of a consolidating sideways range."
        ),
        SetupCardData(
            key = "hammer",
            name = "Hammer & Shooting Star",
            family = StrategyFamily.CANDLESTICK,
            bias = "BOTH",
            winRate = 0.56,
            lesson = "Long wick rejects price and snaps back before close.",
            identificationRule = "Small real body near one extreme with a shadow at least 2.5 times the body length.",
            entryTrigger = "Break of the hammer high (Long) or shooting star low (Short).",
            invalidationStop = "Beyond the tip of the rejection wick.",
            commonTrap = "Entering on hammers formed in mid-air without confluence from horizontal support or FVG."
        ),
        SetupCardData(
            key = "star",
            name = "Morning & Evening Star",
            family = StrategyFamily.CANDLESTICK,
            bias = "BOTH",
            winRate = 0.66,
            lesson = "Trend candle, hesitation star, then aggressive return.",
            identificationRule = "Bar 1: Wide trend body. Bar 2: Small gap star body. Bar 3: Powerful counter-candle closing past Bar 1's midpoint.",
            entryTrigger = "Candle close of Bar 3.",
            invalidationStop = "Lowest point of Bar 2 (Morning Star) or highest point (Evening Star).",
            commonTrap = "Third candle failing to close past the 50% equilibrium mark of Bar 1."
        ),
        SetupCardData(
            key = "threeLineStrike",
            name = "Three-Line Strike",
            family = StrategyFamily.CANDLESTICK,
            bias = "BOTH",
            winRate = 0.71,
            lesson = "Three stair-step candles wiped out by one massive strike bar.",
            identificationRule = "Three consecutive higher/lower closing candles followed by a fourth monster bar that engulfs all three.",
            entryTrigger = "Break of the fourth candle's extreme in the strike direction.",
            invalidationStop = "Opposite end of the fourth strike candle.",
            commonTrap = "Confusing a standard engulfing candle with a true 3-bar strike pattern."
        ),
        SetupCardData(
            key = "soldiers",
            name = "Three White Soldiers & Three Black Crows",
            family = StrategyFamily.CANDLESTICK,
            bias = "BOTH",
            winRate = 0.62,
            lesson = "Three consecutive power candles with tiny wicks.",
            identificationRule = "Three consecutive green or red candles opening inside previous body and closing near extremes.",
            entryTrigger = "Close of the third candle or pullback to third candle open.",
            invalidationStop = "Low of the second candle.",
            commonTrap = "Entering after an overextended 5+ candle run where momentum is already exhausted."
        ),
        SetupCardData(
            key = "marubozu",
            name = "Marubozu Momentum Drive",
            family = StrategyFamily.CANDLESTICK,
            bias = "BOTH",
            winRate = 0.64,
            lesson = "Full-body candle with zero wicks signals aggressive institutional urgency.",
            identificationRule = "Open equals low and close equals high (Bullish) or vice versa (Bearish).",
            entryTrigger = "Next bar open or shallow 20% retest of the marubozu body.",
            invalidationStop = "Midpoint (50%) of the marubozu candle body.",
            commonTrap = "Entering when the marubozu candle is directly slamming into macro higher-timeframe resistance."
        ),

        // ==================== 3. SMART MONEY CONCEPTS (SMC) ====================
        SetupCardData(
            key = "smc_fvg",
            name = "Fair Value Gap (FVG)",
            family = StrategyFamily.SMC,
            bias = "BOTH",
            winRate = 0.68,
            lesson = "Imbalance between Bar 1 and Bar 3 pulls price like a vacuum.",
            identificationRule = "Bullish: Candle 1 High < Candle 3 Low. Bearish: Candle 1 Low > Candle 3 High. A clean void exists.",
            entryTrigger = "First touch into the shaded Fair Value Gap area.",
            invalidationStop = "Opposite candle boundary defining the gap.",
            commonTrap = "Trading old FVGs that have already been completely filled/mitigated."
        ),
        SetupCardData(
            key = "smc_bos",
            name = "Market Structure (BOS & CHoCH)",
            family = StrategyFamily.SMC,
            bias = "BOTH",
            winRate = 0.65,
            lesson = "BOS continues the flow; CHoCH marks the reversal.",
            identificationRule = "BOS: Price creates higher high (Bull) or lower low (Bear). CHoCH: Price breaks previous swing low during uptrend.",
            entryTrigger = "Candle close breaking swing structural level.",
            invalidationStop = "Origin swing low/high that created the breakout.",
            commonTrap = "Trading wick sweeps as structural breaks; valid BOS requires a full candle body close."
        ),
        SetupCardData(
            key = "smc_sweeps",
            name = "Liquidity Sweeps (Turtle Soup)",
            family = StrategyFamily.SMC,
            bias = "BOTH",
            winRate = 0.62,
            lesson = "Stop hunts past obvious highs snap back violently.",
            identificationRule = "Price wicks above previous equal highs or swing lows, triggers liquidity, and immediately rejects back inside.",
            entryTrigger = "Close of the rejection candle following the liquidity run.",
            invalidationStop = "High/low of the sweep wick.",
            commonTrap = "Trying to fade an aggressive expansion impulse that isn't sweeping liquidity, but genuinely breaking out."
        ),
        SetupCardData(
            key = "smc_orderblock",
            name = "Institutional Order Block (OB)",
            family = StrategyFamily.SMC,
            bias = "BOTH",
            winRate = 0.64,
            lesson = "The last opposite candle before a displacement move holds unfilled orders.",
            identificationRule = "The last down-close candle before an impulsive rally that breaks structure.",
            entryTrigger = "First touch of the top of the order block candle.",
            invalidationStop = "Bottom wick of the order block.",
            commonTrap = "Using unconfirmed order blocks that failed to create an FVG or break structure."
        ),

        // ==================== 4. CANDLE RANGE THEORY (CRT) ====================
        SetupCardData(
            key = "crt_amd",
            name = "Power of 3: AMD Cycle",
            family = StrategyFamily.CRT,
            bias = "BOTH",
            winRate = 0.70,
            lesson = "Accumulate, Manipulate (Judith Swing), Distribute towards real target.",
            identificationRule = "Tight range accumulation $\to$ deceptive false breakout (manipulation wick) $\to$ real high-speed expansion.",
            entryTrigger = "Re-entry into the accumulation range after the false manipulation breakout.",
            invalidationStop = "Extreme peak of the manipulation wick.",
            commonTrap = "Entering during the initial accumulation phase before the manipulation shakeout occurs."
        ),
        SetupCardData(
            key = "crt_orb",
            name = "Opening Range Breakout (ORB)",
            family = StrategyFamily.CRT,
            bias = "BOTH",
            winRate = 0.65,
            lesson = "The first 15-minute high and low frame the entire day's volatility.",
            identificationRule = "Establish High and Low of initial session candles. Wait for explosive expansion candle.",
            entryTrigger = "First bar closing outside the Opening Range bracket.",
            invalidationStop = "Opening range midpoint (50% level).",
            commonTrap = "Trading opening range breaks on low volume without continuation follow-through."
        ),
        SetupCardData(
            key = "crt_range_expansion",
            name = "Candle Range Expansion (Wick-to-Body)",
            family = StrategyFamily.CRT,
            bias = "BOTH",
            winRate = 0.67,
            lesson = "Once the opening wick rejects, the daily candle expands to its opposite extreme.",
            identificationRule = "Candle opens, dips to form the bottom wick, rejects aggressively, and starts expanding body upward.",
            entryTrigger = "Break of the candle's opening price after bottom wick rejection confirmation.",
            invalidationStop = "Low of the opening wick.",
            commonTrap = "Entering before the opening wick has clearly finished forming."
        ),

        // ==================== 5. TREND FOLLOWING ====================
        SetupCardData(
            key = "trend_supertrend",
            name = "Supertrend Volatility Ride",
            family = StrategyFamily.TREND_FOLLOWING,
            bias = "BOTH",
            winRate = 0.60,
            lesson = "Ride the trend until ATR trailing line flips color.",
            identificationRule = "Price closes above red upper band, turning line blue (BUY) or below blue line turning red (SELL).",
            entryTrigger = "Supertrend BUY or SELL on-chart tag.",
            invalidationStop = "Dynamic Supertrend line level.",
            commonTrap = "Taking Supertrend flip signals inside tight sideways consolidation ranges."
        ),
        SetupCardData(
            key = "trend_ema_ribbon",
            name = "EMA 9/21/50 Ribbon Fan",
            family = StrategyFamily.TREND_FOLLOWING,
            bias = "BOTH",
            winRate = 0.63,
            lesson = "Parallel moving average fan indicates strong institutional accumulation.",
            identificationRule = "EMA 9 > EMA 21 > EMA 50 with expanding distance between lines and rising angle.",
            entryTrigger = "Pullback touch to the 21 EMA line with bounce confirmation.",
            invalidationStop = "Close below the 50 EMA line.",
            commonTrap = "Buying into an EMA ribbon that is contracting and crossing horizontally."
        ),
        SetupCardData(
            key = "flag",
            name = "Bull Flag & Bear Flag",
            family = StrategyFamily.TREND_FOLLOWING,
            bias = "BOTH",
            winRate = 0.69,
            lesson = "Impulse flagpole followed by shallow orderly counter-channel.",
            identificationRule = "Sharp vertical move ($>3\\times$ ATR) followed by compact rectangular drift channel.",
            entryTrigger = "Breakout through the upper descending boundary of the flag channel.",
            invalidationStop = "Lowest point of the flag consolidation channel.",
            commonTrap = "Trading flags that retrace deeper than 61.8% of the flagpole move (these turn into failed reversals)."
        ),
        SetupCardData(
            key = "wedge",
            name = "Falling Wedge & Rising Wedge",
            family = StrategyFamily.TREND_FOLLOWING,
            bias = "BOTH",
            winRate = 0.68,
            lesson = "Converging channel contracts volatility before an explosive eruption.",
            identificationRule = "Price bounces between two downward sloping converging trendlines with decaying volume.",
            entryTrigger = "Decisive candle close outside the upper converging trendline.",
            invalidationStop = "Most recent swing low inside the wedge.",
            commonTrap = "Entering before the apex breakout candle has officially printed."
        ),

        // ==================== 6. MEAN REVERSION & OSCILLATORS ====================
        SetupCardData(
            key = "revert_rsi_div",
            name = "RSI Divergence Exhaustion",
            family = StrategyFamily.MEAN_REVERSION,
            bias = "BOTH",
            winRate = 0.67,
            lesson = "Price pushes a fresh extreme, but internal momentum is dying.",
            identificationRule = "Price creates Lower Low while RSI oscillator creates Higher Low (Bullish Divergence).",
            entryTrigger = "On-chart BULL DIV or BEAR DIV tag.",
            invalidationStop = "Price extreme that formed the divergence.",
            commonTrap = "Fading strong momentum on a single divergence; wait for structural trigger before entering."
        ),
        SetupCardData(
            key = "revert_bollinger_bounce",
            name = "Bollinger Band 2.0σ Squeeze & Snap",
            family = StrategyFamily.MEAN_REVERSION,
            bias = "BOTH",
            winRate = 0.61,
            lesson = "Extreme wicks outside the 2.0σ envelope mean-revert to the 20 SMA.",
            identificationRule = "Full candle wick prints outside the outer Bollinger Band, followed by inside candle close.",
            entryTrigger = "Re-entry close inside the outer Bollinger Band.",
            invalidationStop = "Extreme wick high/low outside the band.",
            commonTrap = "Trying to fade 'walking the band' trends where bands are expanding aggressively."
        ),
        SetupCardData(
            key = "doubleBottom",
            name = "Double Bottom & Double Top (W / M)",
            family = StrategyFamily.MEAN_REVERSION,
            bias = "BOTH",
            winRate = 0.65,
            lesson = "Support holds twice; the neckline break unlocks the move.",
            identificationRule = "Two distinct troughs/peaks at similar horizontal price levels separated by a central neckline.",
            entryTrigger = "Neckline horizontal resistance/support break.",
            invalidationStop = "Midpoint between the bottom and the neckline.",
            commonTrap = "Entering on the second bottom before the neckline has actually broken."
        ),
        SetupCardData(
            key = "headShoulders",
            name = "Head & Shoulders (Standard & Inverse)",
            family = StrategyFamily.MEAN_REVERSION,
            bias = "BOTH",
            winRate = 0.70,
            lesson = "Tri-peak reversal structure marks the end of macro trends.",
            identificationRule = "Left shoulder peak $\\to$ higher head peak $\\to$ lower right shoulder peak.",
            entryTrigger = "Neckline breakdown with candle body close confirmation.",
            invalidationStop = "Peak of the right shoulder.",
            commonTrap = "Prematurely entering before the neckline is penetrated."
        ),

        // ==================== 7. COMMUNITY TOP INDICATORS ====================
        SetupCardData(
            key = "sqz_breakout",
            name = "Squeeze Momentum Breakout (LazyBear)",
            family = StrategyFamily.INDICATOR_TOP,
            bias = "BOTH",
            winRate = 0.72,
            lesson = "Bollinger Bands compress inside Keltner Channels; when black dots turn green/red, volatility explodes.",
            identificationRule = "BB inside KC indicates squeeze (black dot). First green or red dot marks volatility release with histogram expansion.",
            entryTrigger = "First candle closing on squeeze release dot change (black to gray/green) matching histogram slope.",
            invalidationStop = "Opposite side of 20 EMA or lowest low of the squeeze consolidation base.",
            targetTakeProfit = "+2R (Double Risk)",
            commonTrap = "Entering mid-squeeze before the breakout dot confirms volatility expansion."
        ),
        SetupCardData(
            key = "wt_divergence",
            name = "WaveTrend Reversal Cross (LazyBear)",
            family = StrategyFamily.INDICATOR_TOP,
            bias = "BOTH",
            winRate = 0.70,
            lesson = "Smoothed momentum oscillator reveals overbought/oversold exhaustion and bullish/bearish crosses.",
            identificationRule = "WaveTrend lines WT1 and WT2 reach extreme levels (< -60 or > +60) followed by bullish/bearish cross.",
            entryTrigger = "Bullish gold dot cross below -60 or Bearish red dot cross above +60.",
            invalidationStop = "Recent swing low/high prior to the cross point.",
            targetTakeProfit = "+2R (Double Risk)",
            commonTrap = "Trading crosses in the neutral zone (-20 to +20) where chop leads to whipsaws."
        ),
        SetupCardData(
            key = "utbot_trend",
            name = "UT Bot ATR Trailing Alerts (QuantNomad)",
            family = StrategyFamily.INDICATOR_TOP,
            bias = "BOTH",
            winRate = 0.68,
            lesson = "Key value trailing stop combined with ATR dynamically ratchets behind price without getting shaken out.",
            identificationRule = "Price crosses above/below the dynamic ATR trailing stop band (Key Value 1, ATR Period 10).",
            entryTrigger = "First print of the UT Bot 'BUY' or 'SELL' alert tag.",
            invalidationStop = "Trailing stop line value on signal candle.",
            targetTakeProfit = "+2R (Double Risk)",
            commonTrap = "Taking signals inside sideways chop; require trending EMA alignment."
        ),
        SetupCardData(
            key = "nwe_envelope",
            name = "Nadaraya-Watson Kernel Envelope (LuxAlgo)",
            family = StrategyFamily.INDICATOR_TOP,
            bias = "BOTH",
            winRate = 0.71,
            lesson = "Non-parametric kernel regression captures non-linear price trends and outer boundary reversion.",
            identificationRule = "Price pierces upper or lower kernel regression envelope band (h=8, mult=3.0) and prints reversal arrow.",
            entryTrigger = "Reversal arrow printed on candle closing back inside the envelope channel.",
            invalidationStop = "Outer wick extremity beyond the kernel envelope band.",
            targetTakeProfit = "+2R (Double Risk)",
            commonTrap = "Repainting sensitivity if entering on unclosed bars; only enter on closed bar confirmation."
        ),
        SetupCardData(
            key = "qqe_trend",
            name = "QQE Mod Dual RSI Volatility Filter (Mihkel00)",
            family = StrategyFamily.INDICATOR_TOP,
            bias = "BOTH",
            winRate = 0.69,
            lesson = "Dual qualitative quantitative estimation combines fast RSI ATR trailing bands with baseline trend filter.",
            identificationRule = "Primary QQE line crosses signal line while baseline histogram matches color (blue = bull, red = bear).",
            entryTrigger = "QQE line cross + bar color change above/below the 50 level baseline.",
            invalidationStop = "Trailing ATR stop line from QQE indicator.",
            targetTakeProfit = "+2R (Double Risk)",
            commonTrap = "Ignoring baseline filter; taking buy signals when the macro baseline is red."
        ),
        SetupCardData(
            key = "wae_explosion",
            name = "Waddah Attar Explosion (WaddahAttar)",
            family = StrategyFamily.INDICATOR_TOP,
            bias = "BOTH",
            winRate = 0.73,
            lesson = "MACD momentum histogram exploding above the Bollinger dead-zone band signals high-velocity thrust.",
            identificationRule = "Up/Down explosion histogram column rises above both the Explosion Line and the DeadZone line.",
            entryTrigger = "Histogram bar exceeds explosion threshold line with matching green/red candle.",
            invalidationStop = "Lowest low / highest high of the pre-explosion consolidation base.",
            targetTakeProfit = "+2R (Double Risk)",
            commonTrap = "Entering when histogram is declining even if still above the deadzone line."
        )
    )

    fun findSetup(key: String): SetupCardData? = ALL_SETUPS.find { it.key == key }
    fun findStrategy(key: String): SetupCardData? = findSetup(key)
    fun getByFamily(family: StrategyFamily): List<SetupCardData> = ALL_SETUPS.filter { it.family == family }
    fun getSetupsByGroup(group: StrategyFamily): List<SetupCardData> = getByFamily(group)
}
