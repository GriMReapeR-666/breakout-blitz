package com.setuprush.candy.engine

import kotlin.math.abs
import kotlin.math.max
import kotlin.math.min

/**
 * Technical Indicator & Smart Money Concepts (SMC) Signal Engine.
 * Implements Supertrend, Market Structure (BOS/CHoCH), Fair Value Gaps (FVG),
 * and EMA Crossovers matching Pine Script v6 specifications from Candle Vision.
 */
object IndicatorEngine {

    data class SupertrendPoint(
        val value: Double,
        val isBull: Boolean,
        val signal: String? // "BUY" or "SELL" or null
    )

    data class StructureLine(
        val startIdx: Int,
        val endIdx: Int,
        val level: Double,
        val label: String, // "BOS" or "CHoCH"
        val isBull: Boolean
    )

    data class FairValueGap(
        val startIdx: Int,
        val top: Double,
        val bottom: Double,
        val isBull: Boolean
    )

    data class EmaPoint(
        val fast: Double,
        val slow: Double,
        val signal: String? // "CROSS_UP" or "CROSS_DOWN"
    )

    /** Compute EMA series */
    fun ema(values: List<Double>, period: Int): DoubleArray {
        val n = values.size
        val out = DoubleArray(n)
        if (n == 0) return out
        val k = 2.0 / (period + 1.0)
        out[0] = values[0]
        for (i in 1 until n) {
            out[i] = values[i] * k + out[i - 1] * (1.0 - k)
        }
        return out
    }

    /** Compute Supertrend with 10-period ATR and 3.0 multiplier */
    fun computeSupertrend(candles: List<Candle>, period: Int = 10, mult: Double = 3.0): List<SupertrendPoint> {
        val n = candles.size
        if (n < period) return emptyList()

        val tr = DoubleArray(n)
        tr[0] = candles[0].h - candles[0].l
        for (i in 1 until n) {
            val hl = candles[i].h - candles[i].l
            val hpc = abs(candles[i].h - candles[i - 1].c)
            val lpc = abs(candles[i].l - candles[i - 1].c)
            tr[i] = max(hl, max(hpc, lpc))
        }

        // ATR via RMA/EMA
        val atr = DoubleArray(n)
        var sum = 0.0
        for (i in 0 until period) sum += tr[i]
        atr[period - 1] = sum / period
        for (i in period until n) {
            atr[i] = (atr[i - 1] * (period - 1) + tr[i]) / period
        }

        var isBull = true
        var prevSt = candles[0].c
        val result = mutableListOf<SupertrendPoint>()

        for (i in 0 until n) {
            if (i < period) {
                result.add(SupertrendPoint(candles[i].c, true, null))
                continue
            }

            val src = (candles[i].h + candles[i].l) / 2.0
            val band = atr[i] * mult
            val up = src - band
            val down = src + band

            val prevBull = isBull
            if (candles[i].c > prevSt) {
                isBull = true
            } else if (candles[i].c < prevSt) {
                isBull = false
            }

            val currentSt = if (isBull) max(up, prevSt) else min(down, prevSt)
            prevSt = currentSt

            val signal = if (isBull && !prevBull) "BUY" else if (!isBull && prevBull) "SELL" else null
            result.add(SupertrendPoint(currentSt, isBull, signal))
        }
        return result
    }

    /** Compute Market Structure Breaks (BOS) and Changes of Character (CHoCH) */
    fun computeMarketStructure(candles: List<Candle>, swingLen: Int = 3): List<StructureLine> {
        val lines = mutableListOf<StructureLine>()
        val n = candles.size
        if (n < swingLen * 2 + 1) return lines

        var lastHigh = Double.MIN_VALUE
        var lastHighIdx = -1
        var lastLow = Double.MAX_VALUE
        var lastLowIdx = -1
        var trend = 0 // +1 = up, -1 = down

        for (i in swingLen until n - swingLen) {
            val c = candles[i]
            // Swing High check
            var isPivotHigh = true
            var isPivotLow = true
            for (j in 1..swingLen) {
                if (candles[i - j].h >= c.h || candles[i + j].h > c.h) isPivotHigh = false
                if (candles[i - j].l <= c.l || candles[i + j].l < c.l) isPivotLow = false
            }

            if (isPivotHigh) {
                lastHigh = c.h
                lastHighIdx = i
            }
            if (isPivotLow) {
                lastLow = c.l
                lastLowIdx = i
            }

            // Check breakouts on current bar
            if (lastHighIdx != -1 && candles[i].c > lastHigh) {
                val choch = trend < 0
                lines.add(StructureLine(lastHighIdx, i, lastHigh, if (choch) "CHoCH" else "BOS", isBull = true))
                trend = 1
                lastHigh = Double.MAX_VALUE
            } else if (lastLowIdx != -1 && candles[i].c < lastLow) {
                val choch = trend > 0
                lines.add(StructureLine(lastLowIdx, i, lastLow, if (choch) "CHoCH" else "BOS", isBull = false))
                trend = -1
                lastLow = Double.MIN_VALUE
            }
        }
        return lines
    }

    /** Detect Fair Value Gaps (3-candle price voids) */
    fun computeFairValueGaps(candles: List<Candle>): List<FairValueGap> {
        val fvgs = mutableListOf<FairValueGap>()
        if (candles.size < 3) return fvgs

        for (i in 2 until candles.size) {
            val bar1 = candles[i - 2]
            val bar3 = candles[i]

            // Bullish FVG: Bar 1 High < Bar 3 Low
            if (bar3.l > bar1.h) {
                fvgs.add(FairValueGap(i - 1, top = bar3.l, bottom = bar1.h, isBull = true))
            }
            // Bearish FVG: Bar 1 Low > Bar 3 High
            else if (bar3.h < bar1.l) {
                fvgs.add(FairValueGap(i - 1, top = bar1.l, bottom = bar3.h, isBull = false))
            }
        }
        return fvgs
    }

    /** Compute EMA 9/21 cross */
    fun computeEmaCross(candles: List<Candle>): List<EmaPoint> {
        val closes = candles.map { it.c }
        val fast = ema(closes, 9)
        val slow = ema(closes, 21)
        val out = mutableListOf<EmaPoint>()

        for (i in closes.indices) {
            val sig = if (i > 0) {
                if (fast[i] > slow[i] && fast[i - 1] <= slow[i - 1]) "CROSS_UP"
                else if (fast[i] < slow[i] && fast[i - 1] >= slow[i - 1]) "CROSS_DOWN"
                else null
            } else null
            out.add(EmaPoint(fast[i], slow[i], sig))
        }
        return out
    }

    data class BollingerBandPoint(
        val middle: Double,
        val upper: Double,
        val lower: Double
    )

    data class OrderBlockZone(
        val startIdx: Int,
        val top: Double,
        val bottom: Double,
        val isBull: Boolean
    )

    /** Compute Bollinger Bands (20 SMA, 2.0 Multiplier) */
    fun computeBollingerBands(candles: List<Candle>, period: Int = 20, mult: Double = 2.0): List<BollingerBandPoint> {
        val n = candles.size
        val out = mutableListOf<BollingerBandPoint>()
        if (n == 0) return out
        val closes = candles.map { it.c }

        for (i in 0 until n) {
            val start = max(0, i - period + 1)
            val sub = closes.subList(start, i + 1)
            val mean = sub.sum() / sub.size
            var variance = 0.0
            for (v in sub) variance += (v - mean) * (v - mean)
            val std = kotlin.math.sqrt(variance / sub.size)
            out.add(BollingerBandPoint(mean, mean + mult * std, mean - mult * std))
        }
        return out
    }

    /** Compute RSI (14-period) */
    fun computeRsi(candles: List<Candle>, period: Int = 14): DoubleArray {
        val n = candles.size
        val out = DoubleArray(n)
        if (n < 2) return out

        val gains = DoubleArray(n)
        val losses = DoubleArray(n)
        for (i in 1 until n) {
            val diff = candles[i].c - candles[i - 1].c
            if (diff > 0) gains[i] = diff else losses[i] = -diff
        }

        var avgGain = 0.0
        var avgLoss = 0.0
        val p = min(period, n - 1)
        for (i in 1..p) {
            avgGain += gains[i]
            avgLoss += losses[i]
        }
        avgGain /= p
        avgLoss /= p

        out[p] = if (avgLoss == 0.0) 100.0 else 100.0 - (100.0 / (1.0 + avgGain / avgLoss))
        for (i in (p + 1) until n) {
            avgGain = (avgGain * (period - 1) + gains[i]) / period
            avgLoss = (avgLoss * (period - 1) + losses[i]) / period
            out[i] = if (avgLoss == 0.0) 100.0 else 100.0 - (100.0 / (1.0 + avgGain / avgLoss))
        }
        return out
    }

    /** Compute Institutional Order Blocks */
    fun computeOrderBlocks(candles: List<Candle>): List<OrderBlockZone> {
        val obs = mutableListOf<OrderBlockZone>()
        if (candles.size < 4) return obs

        for (i in 1 until candles.size - 1) {
            val prev = candles[i]
            val next = candles[i + 1]
            val nextBody = abs(next.c - next.o)
            val prevBody = abs(prev.c - prev.o)

            // Bullish Order Block: Red candle followed by large displacement green candle
            if (prev.c < prev.o && next.c > next.o && nextBody > prevBody * 1.8) {
                obs.add(OrderBlockZone(i, top = max(prev.o, prev.c), bottom = min(prev.o, prev.c), isBull = true))
            }
            // Bearish Order Block: Green candle followed by large displacement red candle
            else if (prev.c > prev.o && next.c < next.o && nextBody > prevBody * 1.8) {
                obs.add(OrderBlockZone(i, top = max(prev.o, prev.c), bottom = min(prev.o, prev.c), isBull = false))
            }
        }
        return obs
    }
}
