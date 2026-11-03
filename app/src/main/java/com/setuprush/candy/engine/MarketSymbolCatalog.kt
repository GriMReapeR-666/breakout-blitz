package com.setuprush.candy.engine

/**
 * Market Symbol & Trading Pair Catalog.
 * Defines assets with realistic base prices, volatility profiles, tick decimals,
 * and competitive squad roles. Any strategy in the game can be applied to any symbol.
 */
object MarketSymbolCatalog {

    data class MarketSymbol(
        val id: String,
        val name: String,
        val pair: String,
        val basePrice: Double,
        val volatility: Double, // Multiplier on Brownian step volatility
        val role: String,
        val description: String,
        val priceDecimals: Int = 2
    )

    val ALL_SYMBOLS = listOf(
        MarketSymbol(
            id = "BTC",
            name = "Bitcoin",
            pair = "BTC/USD",
            basePrice = 65000.0,
            volatility = 0.22,
            role = "Mega-Cap Low-Beta Anchor",
            description = "Deepest market liquidity with institutional order book stability. Ideal for structural SMC setups.",
            priceDecimals = 1
        ),
        MarketSymbol(
            id = "ETH",
            name = "Ether",
            pair = "ETH/USD",
            basePrice = 3400.0,
            volatility = 0.28,
            role = "Trend Swing Trader",
            description = "High adherence to technical trendlines, flags, and EMA ribbon continuation runs.",
            priceDecimals = 2
        ),
        MarketSymbol(
            id = "SOL",
            name = "Solana",
            pair = "SOL/USD",
            basePrice = 165.0,
            volatility = 0.42,
            role = "High-Beta Scalper",
            description = "Fast tick velocity with aggressive range expansion and high-frequency wick volatility.",
            priceDecimals = 2
        ),
        MarketSymbol(
            id = "SUI",
            name = "Sui",
            pair = "SUI/USD",
            basePrice = 2.85,
            volatility = 0.55,
            role = "FVG Breakout Runner",
            description = "Explosive displacement candles that leave wide Fair Value Gaps ripe for retest entries.",
            priceDecimals = 4
        ),
        MarketSymbol(
            id = "GOLD",
            name = "Gold / XAU",
            pair = "XAU/USD",
            basePrice = 2680.0,
            volatility = 0.18,
            role = "Macro Hedge & Reversals",
            description = "Classical session cycle sweeps (London/NY open) and clean double bottom/top reversals.",
            priceDecimals = 2
        ),
        MarketSymbol(
            id = "DOGE",
            name = "Dogecoin",
            pair = "DOGE/USD",
            basePrice = 0.185,
            volatility = 0.65,
            role = "Meme High-Volatility Spiker",
            description = "Extreme parabolic drives and brutal liquidity sweeps. High risk, high adrenaline.",
            priceDecimals = 5
        )
    )

    var activeSymbol: MarketSymbol = ALL_SYMBOLS[0] // Default to BTC

    fun findSymbol(id: String): MarketSymbol? = ALL_SYMBOLS.find { it.id.equals(id, ignoreCase = true) }

    fun selectSymbol(id: String) {
        findSymbol(id)?.let { activeSymbol = it }
    }
}
