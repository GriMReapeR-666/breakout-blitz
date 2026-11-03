import json

with open('scripts/all_indicators_catalog.json', 'r', encoding='utf-8') as f:
    inds = json.load(f)

print(f"Generating IndicatorCatalog.kt for {len(inds)} indicators...")

category_map = {
    'Trend': 'IndicatorCategory.TREND',
    'Smart money': 'IndicatorCategory.SMART_MONEY',
    'Volume': 'IndicatorCategory.VOLUME',
    'Levels': 'IndicatorCategory.LEVELS',
    'Volatility': 'IndicatorCategory.VOLATILITY',
    'Look': 'IndicatorCategory.LOOK',
    'Momentum': 'IndicatorCategory.MOMENTUM'
}

descriptions = {
    'supertrend': 'ATR trailing stop bands with BUY & SELL entry tags.',
    'structure': 'Market Structure BOS (Break of Structure) & CHoCH (Change of Character) signals.',
    'fvg': '3-candle imbalance gap boxes with active mitigation zones.',
    'orderblocks': 'Institutional order block zones formed before displacement moves.',
    'volprofile': 'Horizontal traded volume distribution profile showing Point of Control (POC).',
    'levels': 'Horizontal algorithmic support and resistance reaction levels.',
    'bbands': 'Bollinger Bands (20 SMA, 2.0 std dev) envelope with squeeze detection.',
    'ichimoku': 'Ichimoku Kinko Hyo Tenkan, Kijun, and Senkou Span cloud bands.',
    'vwap': 'Volume-Weighted Average Price intraday benchmark anchor.',
    'ribbon': 'Exponential Moving Average multi-ribbon fan (20, 50, 100, 200).',
    'heikin': 'Heikin-Ashi smoothed noise-reduction candlesticks.',
    'rsi': 'Relative Strength Index (14) oscillator with 70/30 overbought/oversold bands.',
    'macd': 'Moving Average Convergence Divergence (12, 26, 9) line and histogram.',
    'squeeze': 'John Carter Squeeze Momentum indicator identifying volatility compression breakouts.',
    'wavetrend': 'WaveTrend oscillator by LazyBear detecting cycle turning points.',
    'emacross': 'Fast/Slow EMA 50/200 cross tracking Golden and Death crosses.',
    'hull': 'Hull Moving Average with zero lag and directional color changes.',
    'psar': 'Parabolic Stop and Reverse trailing acceleration dots.',
    'keltner': 'Keltner Channels based on EMA and Average True Range bands.',
    'donchian': 'Donchian Channels plotting 20-period highest high and lowest low.',
    'chandelier': 'Chandelier Exit trailing stop based on ATR from highest high.',
    'alligator': 'Bill Williams Alligator Jaw (13), Teeth (8), and Lips (5) moving averages.',
    'tema': 'Triple Exponential Moving Average smoothing noise with reduced lag.',
    'linreg': 'Linear Regression trend channel with standard error bands.',
    'envelope': 'Moving Average Envelope with percentage bands above and below.',
    'pivots': 'Classic Floor Trader Pivot Points (PP, R1, R2, S1, S2).',
    'fib': 'Auto Fibonacci Retracement levels (0.236, 0.382, 0.5, 0.618, 0.786).',
    'zigzag': 'ZigZag pivot swing filter connecting significant highs and lows.',
    'stoch': 'Stochastic Oscillator (%K 14, %D 3) with 80/20 threshold zones.',
    'stochrsi': 'Stochastic RSI combining Stoch formula applied to RSI values.',
    'cci': 'Commodity Channel Index measuring deviation from statistical mean.',
    'williams': 'Williams %R oscillator identifying extreme overbought/oversold levels.',
    'adx': 'Average Directional Index with +DI and -DI measuring trend strength.',
    'ao': 'Awesome Oscillator measuring market momentum via 34 SMA and 5 SMA.',
    'mom': 'Momentum and Rate of Change (ROC) velocity indicator.',
    'trix': 'Triple Exponentially Smoothed oscillator filtering out insignificant cycles.',
    'tsi': 'True Strength Index double-smoothed momentum oscillator.',
    'ultosc': 'Ultimate Oscillator capturing momentum across three distinct timeframes.',
    'aroon': 'Aroon Up and Down indicator measuring time elapsed between highs and lows.',
    'vortex': 'Vortex Indicator identifying start of positive and negative trend movements.',
    'chop': 'Choppiness Index measuring whether market is trending or consolidating.',
    'atrpane': 'Average True Range volatility oscillator plotted in a dedicated pane.',
    'bbwidth': 'Bollinger Band Width measuring percentage distance between bands.',
    'obv': 'On-Balance Volume tracking cumulative volume flow confirming price trends.',
    'mfi': 'Money Flow Index volume-weighted RSI measuring buying and selling pressure.',
    'cmf': 'Chaikin Money Flow measuring accumulation/distribution over 20 periods.',
    'relvol': 'Relative Volume comparing current bar volume to historic average.',
    'volosc': 'Volume Oscillator measuring difference between two volume moving averages.',
    'sweeps': 'Liquidity Sweeps tracking stop runs above prior highs and lows.',
    'eqhl': 'Equal Highs and Equal Lows identifying liquidity target pools.',
    'premium': 'Premium / Discount equilibrium zones based on recent swing range.',
    'sessions': 'Session VWAP Bands framing Asian, London, and New York volatility.',
    'rsidiv': 'RSI Divergence radar detecting regular and hidden divergence signals.',
    'utbot': 'UT Bot alerts providing ATR-based trailing momentum buy/sell signals.',
    'ssl': 'SSL Channel hybrid indicator based on SMA highs and lows.',
    'rangefilter': 'Range Filter volatility filter removing ranging chop.',
    'trendmagic': 'Trend Magic combining ATR and CCI for robust trend confirmation.',
    'kama': 'Kaufman Adaptive Moving Average adapting speed to market noise.',
    'mcginley': 'McGinley Dynamic indicator adjusting automatically to market speed.',
    'nwe': 'Nadaraya-Watson Envelope non-parametric kernel regression bands.',
    'qqe': 'Quantitative Qualitative Estimation smoothed RSI with volatility trailing line.',
    'wae': 'Waddah Attar Explosion indicator combining MACD and Bollinger Bands.',
    'stc': 'Schaff Trend Cycle combining MACD and cycle stochastic smoothing.',
    'fisher': 'Fisher Transform converting price distribution into Gaussian curve.',
    'smi': 'Stochastic Momentum Index measuring distance of close from midpoint.',
    'crsi': 'Connors RSI 3-component composite oscillator for mean reversion.',
    'cvd': 'Cumulative Volume Delta measuring aggressive buying vs selling volume.',
    'elder': 'Elder Impulse System coloring bars based on EMA and MACD momentum.'
}

kotlin_code = """package com.setuprush.candy.engine

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
"""

for ind in inds:
    cat = category_map[ind['tag']]
    desc = descriptions.get(ind['id'], f"{ind['name']} technical analysis indicator.")
    default_active = "true" if ind['id'] in ['supertrend', 'structure', 'fvg', 'emacross'] else "false"
    is_pane = "true" if ind['pane'] else "false"
    kotlin_code += f"""        IndicatorDef(
            id = "{ind['id']}",
            name = "{ind['name']}",
            short = "{ind['short']}",
            category = {cat},
            isPane = {is_pane},
            description = "{desc}",
            defaultActive = {default_active}
        ),
"""

kotlin_code += """    )

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
"""

with open('app/src/main/java/com/setuprush/candy/engine/IndicatorCatalog.kt', 'w', encoding='utf-8') as f:
    f.write(kotlin_code)

print("Saved app/src/main/java/com/setuprush/candy/engine/IndicatorCatalog.kt successfully!")
