package com.setuprush.candy

import com.setuprush.candy.engine.SetupEngine
import org.junit.Assert.*
import org.junit.Test

class SetupEngineTest {

    @Test
    fun testMulberry32PRNGParity() {
        val rng = SetupEngine.mulberry32(1337L)
        val first = rng()
        assertEquals(0.1844118325971067, first, 1e-12)

        val second = rng()
        assertEquals(0.18998925131745636, second, 1e-12)
    }

    @Test
    fun testTapeGeneration() {
        val engine = SetupEngine(1337L)
        assertEquals(60, engine.candles.size)
        assertEquals(100.0, engine.home, 1e-9)

        val first = engine.candles[0]
        assertTrue(first.o in 95.0..105.0)
        assertTrue(first.h >= first.o && first.h >= first.c)
        assertTrue(first.l <= first.o && first.l <= first.c)
    }

    @Test
    fun testIntrabarBrownianWalk() {
        val path = SetupEngine.walkPath(100.0, 105.0, 98.0, 102.0, 42L)
        assertEquals(33, path.size)
        assertEquals(100.0, path[0], 1e-9)
        assertEquals(102.0, path[32], 1e-9)

        for (p in path) {
            assertTrue("Price $p out of range [98, 105]", p in 97.99..105.01)
        }
    }

    @Test
    fun testContinuousPriceEvaluation() {
        val engine = SetupEngine(1337L)
        val p0 = SetupEngine.priceAt(engine.candles, 0.0)
        val pHalf = SetupEngine.priceAt(engine.candles, 0.5)
        val p1 = SetupEngine.priceAt(engine.candles, 1.0)

        assertEquals(engine.candles[0].o, p0, 1e-9)
        assertTrue(pHalf in engine.candles[0].l..engine.candles[0].h)
        assertEquals(engine.candles[1].o, p1, 1e-9)
    }

    @Test
    fun testPnLCalculation() {
        // Long trade: entry 100, price 105, notional 1000, 5 bps fee
        val longPnl = SetupEngine.ridePnl(dir = 1, entry = 100.0, currentPrice = 105.0, notional = 1000.0, feeBps = 5.0)
        assertEquals(49.0, longPnl, 1e-9)

        // Short trade: entry 100, price 95, notional 1000, 5 bps fee
        val shortPnl = SetupEngine.ridePnl(dir = -1, entry = 100.0, currentPrice = 95.0, notional = 1000.0, feeBps = 5.0)
        assertEquals(49.0, shortPnl, 1e-9)
    }

    @Test
    fun testLiquidationSolving() {
        val engine = SetupEngine(1337L)
        // Position entered at tape pos 0, dir = 1, entry = candles[0].o, limit = -100 (liquidation threshold)
        val entry = engine.candles[0].o
        val liq = SetupEngine.firstLiq(
            candles = engine.candles,
            from = 0.0,
            to = 10.0,
            dir = 1,
            entry = entry,
            notional = 1000.0,
            limit = -500.0, // Large loss limit
            feeBps = 5.0
        )
        // Ensure solving terminates without crashing
        assertTrue(liq == null || liq in 0.0..10.0)
    }
}
