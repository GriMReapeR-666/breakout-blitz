package com.setuprush.candy.engine

/**
 * Standard Trading Model & Strategy Library Manager.
 * Provides clean, standard financial terminology for:
 * 1. Strategy Libraries (SMC, Price Action, Candlesticks, Trend Following, Mean Reversion, Range Theory)
 * 2. Prebuilt Trading Models (Ready-to-use institutional and retail models)
 * 3. Full Custom Trading Model Builder (Setup + Confluence Filter + Execution Type + Leverage + Risk Rules)
 * 4. Multi-Asset Compatibility (Applicable to any market pair)
 */
object TradingModelManager {

    /** Strategy Library Classification */
    enum class StrategyLibrary(val displayName: String, val tag: String, val description: String) {
        SMART_MONEY_CONCEPTS("Smart Money Concepts (SMC)", "SMC", "Institutional order flow, Fair Value Gaps, order blocks, and structural breaks."),
        PRICE_ACTION("Classical Price Action", "PA", "Horizontal support and resistance, breakouts, retests, and chart patterns."),
        CANDLESTICKS("Candlestick Patterns", "CANDLE", "High-probability Japanese candlestick formations and multi-bar reversals."),
        TREND_FOLLOWING("Trend Following & Momentum", "TREND", "Continuation strategies using moving average ribbons, flags, and volatility bands."),
        MEAN_REVERSION("Mean Reversion & Oscillators", "REVERT", "Counter-trend strategies fading overextended wicks, Bollinger Bands, and RSI divergence."),
        CANDLE_RANGE_THEORY("Candle Range & Session Theory", "CRT", "Auction dynamics, Power of 3 (AMD), Opening Range Breakouts, and session expansion."),
        INDICATOR_TOP("Community Top Indicators", "COMM_TOP", "Community flagship indicators: Squeeze Momentum, WaveTrend, UT Bot, Nadaraya-Watson, QQE Mod, and Waddah Attar.")
    }

    /** Execution Order Type */
    enum class ExecutionOrderType(val displayName: String, val description: String) {
        MARKET_ORDER("Market Execution", "Enters immediately at current market price upon finger touch."),
        LIMIT_RETEST("Limit Retest Order", "Waits for price to pull back and touch the key retest zone.")
    }

    /** Risk Management Protocol */
    enum class RiskManagementProtocol(val displayName: String, val targetMultiple: String, val ruleDescription: String) {
        STANDARD_2R("Standard 1:2 R:R", "+2R Target", "Fixed 1R invalidation stop with +2R take profit target."),
        EXTENDED_3R("Extended 1:3 R:R", "+3R Target", "Tighter invalidation with expanded target for high-conviction setups."),
        TRAILING_BREAKEVEN("Trailing Stop to Break-Even", "+2R Target", "Automatically shifts stop loss to entry price once trade reaches +1R in profit."),
        SWEEP_BUFFER("Liquidity Sweep Tolerance", "+2R Target", "Grants a 1-wick buffer on false stop-runs before triggering liquidation.")
    }

    /** Confluence / Secondary Indicator Overlay */
    data class ConfluenceIndicator(
        val id: String,
        val displayName: String,
        val indicatorCode: String,
        val role: String
    )

    val CONFLUENCE_INDICATORS = listOf(
        ConfluenceIndicator("conf_supertrend", "Supertrend (10, 3.0)", "supertrend", "Directional Volatility Filter"),
        ConfluenceIndicator("conf_bos", "Market Structure (BOS / CHoCH)", "structure", "Structural Level Confirmation"),
        ConfluenceIndicator("conf_fvg", "Fair Value Gap Boxes", "fvg", "Imbalance Retest Zone"),
        ConfluenceIndicator("conf_orderblocks", "Order Blocks (OB)", "orderblocks", "Institutional Liquidity Zone"),
        ConfluenceIndicator("conf_ema", "EMA 9/21 Ribbon", "emacross", "Dynamic Momentum Support/Res"),
        ConfluenceIndicator("conf_bbands", "Bollinger Bands", "bbands", "Volatility Envelope & Squeeze"),
        ConfluenceIndicator("conf_vwap", "VWAP Anchor", "vwap", "Volume-Weighted Benchmark"),
        ConfluenceIndicator("conf_macd", "MACD Histogram", "macd", "Convergence/Divergence Momentum"),
        ConfluenceIndicator("conf_squeeze", "Squeeze Momentum", "squeeze", "Compression Breakout Radar"),
        ConfluenceIndicator("conf_volprofile", "Volume Profile (POC)", "volprofile", "Horizontal Value Area"),
        ConfluenceIndicator("conf_rsi_div", "RSI Divergence Radar", "rsidiv", "Momentum Exhaustion Filter"),
        ConfluenceIndicator("conf_none", "None (Naked Chart)", "none", "Pure Price Action Reading")
    )

    /** Complete Customized Trading Model */
    data class CustomTradingModel(
        val id: String,
        val name: String,
        val library: StrategyLibrary,
        val setupKey: String, // Strategy key from StrategyCatalog
        val confluenceIndicatorId: String,
        val targetSymbolId: String, // "BTC", "ETH", "SOL", "SUI", "GOLD", "DOGE"
        val orderType: ExecutionOrderType,
        val leverage: Double, // 2x, 3x, 5x, 10x
        val riskProtocol: RiskManagementProtocol
    )

    /** Prebuilt Standard Trading Models */
    val PREBUILT_MODELS = mutableListOf(
        CustomTradingModel(
            id = "prebuilt_smc_fvg",
            name = "SMC Fair Value Gap Retest",
            library = StrategyLibrary.SMART_MONEY_CONCEPTS,
            setupKey = "smc_fvg",
            confluenceIndicatorId = "conf_fvg",
            targetSymbolId = "BTC",
            orderType = ExecutionOrderType.LIMIT_RETEST,
            leverage = 5.0,
            riskProtocol = RiskManagementProtocol.EXTENDED_3R
        ),
        CustomTradingModel(
            id = "prebuilt_bull_flag",
            name = "Trend Momentum Flag Runner",
            library = StrategyLibrary.TREND_FOLLOWING,
            setupKey = "flag",
            confluenceIndicatorId = "conf_supertrend",
            targetSymbolId = "ETH",
            orderType = ExecutionOrderType.MARKET_ORDER,
            leverage = 3.0,
            riskProtocol = RiskManagementProtocol.TRAILING_BREAKEVEN
        ),
        CustomTradingModel(
            id = "prebuilt_crt_amd",
            name = "Session Power of 3 (AMD)",
            library = StrategyLibrary.CANDLE_RANGE_THEORY,
            setupKey = "crt_amd",
            confluenceIndicatorId = "conf_ema",
            targetSymbolId = "SOL",
            orderType = ExecutionOrderType.MARKET_ORDER,
            leverage = 10.0,
            riskProtocol = RiskManagementProtocol.STANDARD_2R
        ),
        CustomTradingModel(
            id = "prebuilt_liquidity_sweep",
            name = "Liquidity Sweep & Reversal",
            library = StrategyLibrary.SMART_MONEY_CONCEPTS,
            setupKey = "smc_sweeps",
            confluenceIndicatorId = "conf_bos",
            targetSymbolId = "SUI",
            orderType = ExecutionOrderType.MARKET_ORDER,
            leverage = 5.0,
            riskProtocol = RiskManagementProtocol.SWEEP_BUFFER
        ),
        CustomTradingModel(
            id = "prebuilt_break_retest",
            name = "Classical Support/Resistance Retest",
            library = StrategyLibrary.PRICE_ACTION,
            setupKey = "pa_break_retest",
            confluenceIndicatorId = "conf_none",
            targetSymbolId = "GOLD",
            orderType = ExecutionOrderType.LIMIT_RETEST,
            leverage = 4.0,
            riskProtocol = RiskManagementProtocol.STANDARD_2R
        ),
        CustomTradingModel(
            id = "prebuilt_three_line_strike",
            name = "Three-Line Strike Momentum",
            library = StrategyLibrary.CANDLESTICKS,
            setupKey = "threeLineStrike",
            confluenceIndicatorId = "conf_none",
            targetSymbolId = "BTC",
            orderType = ExecutionOrderType.MARKET_ORDER,
            leverage = 5.0,
            riskProtocol = RiskManagementProtocol.EXTENDED_3R
        ),
        CustomTradingModel(
            id = "prebuilt_squeeze_momentum",
            name = "Squeeze Momentum Breakout (LazyBear)",
            library = StrategyLibrary.INDICATOR_TOP,
            setupKey = "sqz_breakout",
            confluenceIndicatorId = "conf_squeeze",
            targetSymbolId = "BTC",
            orderType = ExecutionOrderType.MARKET_ORDER,
            leverage = 5.0,
            riskProtocol = RiskManagementProtocol.STANDARD_2R
        ),
        CustomTradingModel(
            id = "prebuilt_wavetrend_reversal",
            name = "WaveTrend Extreme Divergence (LazyBear)",
            library = StrategyLibrary.INDICATOR_TOP,
            setupKey = "wt_divergence",
            confluenceIndicatorId = "conf_rsi_div",
            targetSymbolId = "ETH",
            orderType = ExecutionOrderType.LIMIT_RETEST,
            leverage = 5.0,
            riskProtocol = RiskManagementProtocol.EXTENDED_3R
        ),
        CustomTradingModel(
            id = "prebuilt_utbot_momentum",
            name = "UT Bot ATR Trailing Trend (QuantNomad)",
            library = StrategyLibrary.INDICATOR_TOP,
            setupKey = "utbot_trend",
            confluenceIndicatorId = "conf_supertrend",
            targetSymbolId = "SOL",
            orderType = ExecutionOrderType.MARKET_ORDER,
            leverage = 10.0,
            riskProtocol = RiskManagementProtocol.TRAILING_BREAKEVEN
        )
    )

    var activeTradingModel: CustomTradingModel = PREBUILT_MODELS[0]

    fun selectModel(id: String) {
        PREBUILT_MODELS.find { it.id == id }?.let { activeTradingModel = it }
    }

    fun saveModel(model: CustomTradingModel) {
        val idx = PREBUILT_MODELS.indexOfFirst { it.id == model.id }
        if (idx != -1) {
            PREBUILT_MODELS[idx] = model
        } else {
            PREBUILT_MODELS.add(model)
        }
        if (activeTradingModel.id == model.id) {
            activeTradingModel = model
        }
    }

    fun getConfluence(id: String): ConfluenceIndicator? = CONFLUENCE_INDICATORS.find { it.id == id }
}
