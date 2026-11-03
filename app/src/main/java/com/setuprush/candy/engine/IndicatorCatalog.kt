package com.setuprush.candy.engine

/**
 * Complete Catalog of all 68 Technical Indicators from Setup Rush.
 * Extracted directly from Candle Vision Pine Script v6 library:
 * - 15 Trend Indicators
 * - 6 Smart Money Concepts (SMC)
 * - 9 Volume Indicators
 * - 5 S/R & Channel Levels
 * - 8 Volatility Indicators
 * - 23 Momentum & Oscillators
 * - 2 Look & Styling Modes
 */
object IndicatorCatalog {

    enum class IndicatorCategory(val displayName: String, val badge: String) {
        ALL("All Indicators", "ALL"),
        TREND("Trend Following", "TREND"),
        SMART_MONEY("Smart Money Concepts", "SMC"),
        VOLUME("Volume & Liquidity", "VOL"),
        LEVELS("Levels & Channels", "LVL"),
        VOLATILITY("Volatility Bands", "VLT"),
        MOMENTUM("Momentum & Oscillators", "MOM"),
        LOOK("Look & Bar Styling", "LOOK")
    }

    data class IndicatorDef(
        val id: String,
        val name: String,
        val short: String,
        val category: IndicatorCategory,
        val isPane: Boolean, // true = separate lower sub-pane, false = overlay on candle canvas
        val description: String,
        val defaultActive: Boolean = false
    )

    val ALL_68_INDICATORS: List<IndicatorDef> = listOf(
        IndicatorDef(
            id = "supertrend",
            name = "Supertrend",
            short = "ST",
            category = IndicatorCategory.TREND,
            isPane = false,
            description = "ATR trailing stop bands with BUY & SELL entry tags.",
            defaultActive = true
        ),
        IndicatorDef(
            id = "structure",
            name = "Market Structure",
            short = "BOS",
            category = IndicatorCategory.SMART_MONEY,
            isPane = false,
            description = "Market Structure BOS (Break of Structure) & CHoCH (Change of Character) signals.",
            defaultActive = true
        ),
        IndicatorDef(
            id = "fvg",
            name = "Fair Value Gaps",
            short = "FVG",
            category = IndicatorCategory.SMART_MONEY,
            isPane = false,
            description = "3-candle imbalance gap boxes with active mitigation zones.",
            defaultActive = true
        ),
        IndicatorDef(
            id = "orderblocks",
            name = "Order Blocks",
            short = "OB",
            category = IndicatorCategory.SMART_MONEY,
            isPane = false,
            description = "Institutional order block zones formed before displacement moves.",
            defaultActive = false
        ),
        IndicatorDef(
            id = "volprofile",
            name = "Volume Profile",
            short = "VP",
            category = IndicatorCategory.VOLUME,
            isPane = false,
            description = "Horizontal traded volume distribution profile showing Point of Control (POC).",
            defaultActive = false
        ),
        IndicatorDef(
            id = "levels",
            name = "Support & Resistance",
            short = "S/R",
            category = IndicatorCategory.LEVELS,
            isPane = false,
            description = "Horizontal algorithmic support and resistance reaction levels.",
            defaultActive = false
        ),
        IndicatorDef(
            id = "bbands",
            name = "Bollinger Bands",
            short = "BB",
            category = IndicatorCategory.VOLATILITY,
            isPane = false,
            description = "Bollinger Bands (20 SMA, 2.0 std dev) envelope with squeeze detection.",
            defaultActive = false
        ),
        IndicatorDef(
            id = "ichimoku",
            name = "Ichimoku Cloud",
            short = "ICH",
            category = IndicatorCategory.TREND,
            isPane = false,
            description = "Ichimoku Kinko Hyo Tenkan, Kijun, and Senkou Span cloud bands.",
            defaultActive = false
        ),
        IndicatorDef(
            id = "vwap",
            name = "VWAP",
            short = "VWAP",
            category = IndicatorCategory.VOLUME,
            isPane = false,
            description = "Volume-Weighted Average Price intraday benchmark anchor.",
            defaultActive = false
        ),
        IndicatorDef(
            id = "ribbon",
            name = "EMA Ribbon",
            short = "EMA",
            category = IndicatorCategory.TREND,
            isPane = false,
            description = "Exponential Moving Average multi-ribbon fan (20, 50, 100, 200).",
            defaultActive = false
        ),
        IndicatorDef(
            id = "heikin",
            name = "Heikin Ashi",
            short = "HA",
            category = IndicatorCategory.LOOK,
            isPane = false,
            description = "Heikin-Ashi smoothed noise-reduction candlesticks.",
            defaultActive = false
        ),
        IndicatorDef(
            id = "rsi",
            name = "RSI",
            short = "RSI",
            category = IndicatorCategory.MOMENTUM,
            isPane = true,
            description = "Relative Strength Index (14) oscillator with 70/30 overbought/oversold bands.",
            defaultActive = false
        ),
        IndicatorDef(
            id = "macd",
            name = "MACD",
            short = "MACD",
            category = IndicatorCategory.MOMENTUM,
            isPane = true,
            description = "Moving Average Convergence Divergence (12, 26, 9) line and histogram.",
            defaultActive = false
        ),
        IndicatorDef(
            id = "squeeze",
            name = "Squeeze Momentum",
            short = "SQZ",
            category = IndicatorCategory.MOMENTUM,
            isPane = true,
            description = "John Carter Squeeze Momentum indicator identifying volatility compression breakouts.",
            defaultActive = false
        ),
        IndicatorDef(
            id = "wavetrend",
            name = "WaveTrend",
            short = "WT",
            category = IndicatorCategory.MOMENTUM,
            isPane = true,
            description = "WaveTrend oscillator by LazyBear detecting cycle turning points.",
            defaultActive = false
        ),
        IndicatorDef(
            id = "emacross",
            name = "EMA 50/200 Cross",
            short = "GX",
            category = IndicatorCategory.TREND,
            isPane = false,
            description = "Fast/Slow EMA 50/200 cross tracking Golden and Death crosses.",
            defaultActive = true
        ),
        IndicatorDef(
            id = "hull",
            name = "Hull MA",
            short = "HMA",
            category = IndicatorCategory.TREND,
            isPane = false,
            description = "Hull Moving Average with zero lag and directional color changes.",
            defaultActive = false
        ),
        IndicatorDef(
            id = "psar",
            name = "Parabolic SAR",
            short = "SAR",
            category = IndicatorCategory.TREND,
            isPane = false,
            description = "Parabolic Stop and Reverse trailing acceleration dots.",
            defaultActive = false
        ),
        IndicatorDef(
            id = "keltner",
            name = "Keltner Channels",
            short = "KC",
            category = IndicatorCategory.VOLATILITY,
            isPane = false,
            description = "Keltner Channels based on EMA and Average True Range bands.",
            defaultActive = false
        ),
        IndicatorDef(
            id = "donchian",
            name = "Donchian Channels",
            short = "DC",
            category = IndicatorCategory.VOLATILITY,
            isPane = false,
            description = "Donchian Channels plotting 20-period highest high and lowest low.",
            defaultActive = false
        ),
        IndicatorDef(
            id = "chandelier",
            name = "Chandelier Exit",
            short = "CE",
            category = IndicatorCategory.TREND,
            isPane = false,
            description = "Chandelier Exit trailing stop based on ATR from highest high.",
            defaultActive = false
        ),
        IndicatorDef(
            id = "alligator",
            name = "Williams Alligator",
            short = "ALLI",
            category = IndicatorCategory.TREND,
            isPane = false,
            description = "Bill Williams Alligator Jaw (13), Teeth (8), and Lips (5) moving averages.",
            defaultActive = false
        ),
        IndicatorDef(
            id = "tema",
            name = "TEMA / DEMA",
            short = "TEMA",
            category = IndicatorCategory.TREND,
            isPane = false,
            description = "Triple Exponential Moving Average smoothing noise with reduced lag.",
            defaultActive = false
        ),
        IndicatorDef(
            id = "linreg",
            name = "LinReg Channel",
            short = "LRC",
            category = IndicatorCategory.LEVELS,
            isPane = false,
            description = "Linear Regression trend channel with standard error bands.",
            defaultActive = false
        ),
        IndicatorDef(
            id = "envelope",
            name = "MA Envelope",
            short = "ENV",
            category = IndicatorCategory.VOLATILITY,
            isPane = false,
            description = "Moving Average Envelope with percentage bands above and below.",
            defaultActive = false
        ),
        IndicatorDef(
            id = "pivots",
            name = "Pivot Points",
            short = "PP",
            category = IndicatorCategory.LEVELS,
            isPane = false,
            description = "Classic Floor Trader Pivot Points (PP, R1, R2, S1, S2).",
            defaultActive = false
        ),
        IndicatorDef(
            id = "fib",
            name = "Auto Fibonacci",
            short = "FIB",
            category = IndicatorCategory.LEVELS,
            isPane = false,
            description = "Auto Fibonacci Retracement levels (0.236, 0.382, 0.5, 0.618, 0.786).",
            defaultActive = false
        ),
        IndicatorDef(
            id = "zigzag",
            name = "ZigZag",
            short = "ZZ",
            category = IndicatorCategory.LEVELS,
            isPane = false,
            description = "ZigZag pivot swing filter connecting significant highs and lows.",
            defaultActive = false
        ),
        IndicatorDef(
            id = "stoch",
            name = "Stochastic",
            short = "STO",
            category = IndicatorCategory.MOMENTUM,
            isPane = true,
            description = "Stochastic Oscillator (%K 14, %D 3) with 80/20 threshold zones.",
            defaultActive = false
        ),
        IndicatorDef(
            id = "stochrsi",
            name = "Stochastic RSI",
            short = "SRSI",
            category = IndicatorCategory.MOMENTUM,
            isPane = true,
            description = "Stochastic RSI combining Stoch formula applied to RSI values.",
            defaultActive = false
        ),
        IndicatorDef(
            id = "cci",
            name = "CCI",
            short = "CCI",
            category = IndicatorCategory.MOMENTUM,
            isPane = true,
            description = "Commodity Channel Index measuring deviation from statistical mean.",
            defaultActive = false
        ),
        IndicatorDef(
            id = "williams",
            name = "Williams %R",
            short = "%R",
            category = IndicatorCategory.MOMENTUM,
            isPane = true,
            description = "Williams %R oscillator identifying extreme overbought/oversold levels.",
            defaultActive = false
        ),
        IndicatorDef(
            id = "adx",
            name = "ADX / DMI",
            short = "ADX",
            category = IndicatorCategory.MOMENTUM,
            isPane = true,
            description = "Average Directional Index with +DI and -DI measuring trend strength.",
            defaultActive = false
        ),
        IndicatorDef(
            id = "ao",
            name = "Awesome Oscillator",
            short = "AO",
            category = IndicatorCategory.MOMENTUM,
            isPane = true,
            description = "Awesome Oscillator measuring market momentum via 34 SMA and 5 SMA.",
            defaultActive = false
        ),
        IndicatorDef(
            id = "mom",
            name = "Momentum / ROC",
            short = "ROC",
            category = IndicatorCategory.MOMENTUM,
            isPane = true,
            description = "Momentum and Rate of Change (ROC) velocity indicator.",
            defaultActive = false
        ),
        IndicatorDef(
            id = "trix",
            name = "TRIX",
            short = "TRIX",
            category = IndicatorCategory.MOMENTUM,
            isPane = true,
            description = "Triple Exponentially Smoothed oscillator filtering out insignificant cycles.",
            defaultActive = false
        ),
        IndicatorDef(
            id = "tsi",
            name = "True Strength Index",
            short = "TSI",
            category = IndicatorCategory.MOMENTUM,
            isPane = true,
            description = "True Strength Index double-smoothed momentum oscillator.",
            defaultActive = false
        ),
        IndicatorDef(
            id = "ultosc",
            name = "Ultimate Oscillator",
            short = "UO",
            category = IndicatorCategory.MOMENTUM,
            isPane = true,
            description = "Ultimate Oscillator capturing momentum across three distinct timeframes.",
            defaultActive = false
        ),
        IndicatorDef(
            id = "aroon",
            name = "Aroon",
            short = "ARN",
            category = IndicatorCategory.MOMENTUM,
            isPane = true,
            description = "Aroon Up and Down indicator measuring time elapsed between highs and lows.",
            defaultActive = false
        ),
        IndicatorDef(
            id = "vortex",
            name = "Vortex",
            short = "VI",
            category = IndicatorCategory.MOMENTUM,
            isPane = true,
            description = "Vortex Indicator identifying start of positive and negative trend movements.",
            defaultActive = false
        ),
        IndicatorDef(
            id = "chop",
            name = "Choppiness Index",
            short = "CHOP",
            category = IndicatorCategory.VOLATILITY,
            isPane = true,
            description = "Choppiness Index measuring whether market is trending or consolidating.",
            defaultActive = false
        ),
        IndicatorDef(
            id = "atrpane",
            name = "ATR",
            short = "ATR",
            category = IndicatorCategory.VOLATILITY,
            isPane = true,
            description = "Average True Range volatility oscillator plotted in a dedicated pane.",
            defaultActive = false
        ),
        IndicatorDef(
            id = "bbwidth",
            name = "Bollinger Width",
            short = "BBW",
            category = IndicatorCategory.VOLATILITY,
            isPane = true,
            description = "Bollinger Band Width measuring percentage distance between bands.",
            defaultActive = false
        ),
        IndicatorDef(
            id = "obv",
            name = "On-Balance Volume",
            short = "OBV",
            category = IndicatorCategory.VOLUME,
            isPane = true,
            description = "On-Balance Volume tracking cumulative volume flow confirming price trends.",
            defaultActive = false
        ),
        IndicatorDef(
            id = "mfi",
            name = "Money Flow Index",
            short = "MFI",
            category = IndicatorCategory.VOLUME,
            isPane = true,
            description = "Money Flow Index volume-weighted RSI measuring buying and selling pressure.",
            defaultActive = false
        ),
        IndicatorDef(
            id = "cmf",
            name = "Chaikin Money Flow",
            short = "CMF",
            category = IndicatorCategory.VOLUME,
            isPane = true,
            description = "Chaikin Money Flow measuring accumulation/distribution over 20 periods.",
            defaultActive = false
        ),
        IndicatorDef(
            id = "relvol",
            name = "Relative Volume",
            short = "RVOL",
            category = IndicatorCategory.VOLUME,
            isPane = true,
            description = "Relative Volume comparing current bar volume to historic average.",
            defaultActive = false
        ),
        IndicatorDef(
            id = "volosc",
            name = "Volume Oscillator",
            short = "VO",
            category = IndicatorCategory.VOLUME,
            isPane = true,
            description = "Volume Oscillator measuring difference between two volume moving averages.",
            defaultActive = false
        ),
        IndicatorDef(
            id = "sweeps",
            name = "Liquidity Sweeps",
            short = "SWP",
            category = IndicatorCategory.SMART_MONEY,
            isPane = false,
            description = "Liquidity Sweeps tracking stop runs above prior highs and lows.",
            defaultActive = false
        ),
        IndicatorDef(
            id = "eqhl",
            name = "Equal Highs & Lows",
            short = "EQ",
            category = IndicatorCategory.SMART_MONEY,
            isPane = false,
            description = "Equal Highs and Equal Lows identifying liquidity target pools.",
            defaultActive = false
        ),
        IndicatorDef(
            id = "premium",
            name = "Premium / Discount",
            short = "P/D",
            category = IndicatorCategory.SMART_MONEY,
            isPane = false,
            description = "Premium / Discount equilibrium zones based on recent swing range.",
            defaultActive = false
        ),
        IndicatorDef(
            id = "sessions",
            name = "Session VWAP Bands",
            short = "SVB",
            category = IndicatorCategory.VOLUME,
            isPane = false,
            description = "Session VWAP Bands framing Asian, London, and New York volatility.",
            defaultActive = false
        ),
        IndicatorDef(
            id = "rsidiv",
            name = "RSI Divergence",
            short = "DIV",
            category = IndicatorCategory.MOMENTUM,
            isPane = true,
            description = "RSI Divergence radar detecting regular and hidden divergence signals.",
            defaultActive = false
        ),
        IndicatorDef(
            id = "utbot",
            name = "UT Bot",
            short = "UTB",
            category = IndicatorCategory.TREND,
            isPane = false,
            description = "UT Bot alerts providing ATR-based trailing momentum buy/sell signals.",
            defaultActive = false
        ),
        IndicatorDef(
            id = "ssl",
            name = "SSL Channel",
            short = "SSL",
            category = IndicatorCategory.TREND,
            isPane = false,
            description = "SSL Channel hybrid indicator based on SMA highs and lows.",
            defaultActive = false
        ),
        IndicatorDef(
            id = "rangefilter",
            name = "Range Filter",
            short = "RF",
            category = IndicatorCategory.TREND,
            isPane = false,
            description = "Range Filter volatility filter removing ranging chop.",
            defaultActive = false
        ),
        IndicatorDef(
            id = "trendmagic",
            name = "Trend Magic",
            short = "TM",
            category = IndicatorCategory.TREND,
            isPane = false,
            description = "Trend Magic combining ATR and CCI for robust trend confirmation.",
            defaultActive = false
        ),
        IndicatorDef(
            id = "kama",
            name = "Kaufman Adaptive MA",
            short = "KAMA",
            category = IndicatorCategory.TREND,
            isPane = false,
            description = "Kaufman Adaptive Moving Average adapting speed to market noise.",
            defaultActive = false
        ),
        IndicatorDef(
            id = "mcginley",
            name = "McGinley Dynamic",
            short = "MCG",
            category = IndicatorCategory.TREND,
            isPane = false,
            description = "McGinley Dynamic indicator adjusting automatically to market speed.",
            defaultActive = false
        ),
        IndicatorDef(
            id = "nwe",
            name = "Nadaraya-Watson Envelope",
            short = "NWE",
            category = IndicatorCategory.VOLATILITY,
            isPane = false,
            description = "Nadaraya-Watson Envelope non-parametric kernel regression bands.",
            defaultActive = false
        ),
        IndicatorDef(
            id = "qqe",
            name = "QQE",
            short = "QQE",
            category = IndicatorCategory.MOMENTUM,
            isPane = true,
            description = "Quantitative Qualitative Estimation smoothed RSI with volatility trailing line.",
            defaultActive = false
        ),
        IndicatorDef(
            id = "wae",
            name = "Waddah Attar Explosion",
            short = "WAE",
            category = IndicatorCategory.MOMENTUM,
            isPane = true,
            description = "Waddah Attar Explosion indicator combining MACD and Bollinger Bands.",
            defaultActive = false
        ),
        IndicatorDef(
            id = "stc",
            name = "Schaff Trend Cycle",
            short = "STC",
            category = IndicatorCategory.MOMENTUM,
            isPane = true,
            description = "Schaff Trend Cycle combining MACD and cycle stochastic smoothing.",
            defaultActive = false
        ),
        IndicatorDef(
            id = "fisher",
            name = "Fisher Transform",
            short = "FISH",
            category = IndicatorCategory.MOMENTUM,
            isPane = true,
            description = "Fisher Transform converting price distribution into Gaussian curve.",
            defaultActive = false
        ),
        IndicatorDef(
            id = "smi",
            name = "Stochastic Momentum Index",
            short = "SMI",
            category = IndicatorCategory.MOMENTUM,
            isPane = true,
            description = "Stochastic Momentum Index measuring distance of close from midpoint.",
            defaultActive = false
        ),
        IndicatorDef(
            id = "crsi",
            name = "Connors RSI",
            short = "CRSI",
            category = IndicatorCategory.MOMENTUM,
            isPane = true,
            description = "Connors RSI 3-component composite oscillator for mean reversion.",
            defaultActive = false
        ),
        IndicatorDef(
            id = "cvd",
            name = "Cumulative Volume Delta",
            short = "CVD",
            category = IndicatorCategory.VOLUME,
            isPane = true,
            description = "Cumulative Volume Delta measuring aggressive buying vs selling volume.",
            defaultActive = false
        ),
        IndicatorDef(
            id = "elder",
            name = "Elder Impulse",
            short = "ELD",
            category = IndicatorCategory.LOOK,
            isPane = false,
            description = "Elder Impulse System coloring bars based on EMA and MACD momentum.",
            defaultActive = false
        ),
    )

    /** Find indicator by ID */
    fun findIndicator(id: String): IndicatorDef? = ALL_68_INDICATORS.find { it.id == id }

    /** Filter by category */
    fun getByCategory(category: IndicatorCategory): List<IndicatorDef> {
        return if (category == IndicatorCategory.ALL) ALL_68_INDICATORS
        else ALL_68_INDICATORS.filter { it.category == category }
    }

    /** Live search query across name, short code, and description */
    fun search(query: String, category: IndicatorCategory = IndicatorCategory.ALL): List<IndicatorDef> {
        val q = query.trim().lowercase()
        val pool = getByCategory(category)
        if (q.isEmpty()) return pool
        return pool.filter {
            it.name.lowercase().contains(q) ||
            it.short.lowercase().contains(q) ||
            it.id.lowercase().contains(q) ||
            it.description.lowercase().contains(q)
        }
    }
}
