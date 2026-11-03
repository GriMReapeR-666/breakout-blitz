package com.setuprush.candy.engine

import kotlin.math.*

/**
 * Clean Kotlin port of Setup Rush simulation engine.
 * Generates an endless candle tape with realistic crypto volatility regimes,
 * intrabar Brownian bridge tick paths (32 ticks/bar), and exact trade mechanics.
 *
 * Zero external dependencies — pure Kotlin stdlib.
 */
class SetupEngine(
    seed: Long = 1337L,
    val symbol: MarketSymbolCatalog.MarketSymbol = MarketSymbolCatalog.activeSymbol
) {

    val rng: () -> Double = mulberry32(seed)
    val candles = mutableListOf<Candle>()
    val setups = mutableListOf<SetupRecord>()
    val home: Double = symbol.basePrice

    init {
        val b = Leg(rng, home, symbol.volatility).apply {
            regime()
            for (i in 0 until 6) {
                if (rng() < 0.5) regime()
                val swingTarget = price + home * (0.012 + rng() * 0.028) * (if (rng() > 0.5) 1.0 else -1.0)
                leg(10, swingTarget, 1.0)
            }
        }
        candles.addAll(b.candles)
        // Seed initial setups along the starting tape
        if (candles.size > 15) {
            appendSetup(atBar = 12)
            if (candles.size > 40) {
                appendSetup(atBar = 35)
            }
        }
    }

    fun lastPrice(): Double = if (candles.isEmpty()) home else candles.last().c

    /** Generates a valid trading setup record and sculpts authentic candlestick anatomy */
    fun appendSetup(preferredKey: String? = null, atBar: Int = max(0, candles.size - 1)) {
        val chosen = (preferredKey?.let { StrategyCatalog.findStrategy(it) }
            ?: StrategyCatalog.ALL_SETUPS[(rng() * StrategyCatalog.ALL_SETUPS.size).toInt().coerceIn(0, StrategyCatalog.ALL_SETUPS.size - 1)])
        
        val dir = when (chosen.bias) {
            "BULLISH" -> 1
            "BEARISH" -> -1
            else -> if (rng() > 0.5) 1 else -1
        }
        val curPrice = lastPrice()
        val risk = max(curPrice * 0.016 * symbol.volatility, curPrice * 0.008)
        val stop = if (dir == 1) curPrice - risk else curPrice + risk
        
        val rec = SetupRecord(
            key = chosen.key,
            name = chosen.name,
            dir = dir,
            at = atBar,
            entry = curPrice,
            stop = stop,
            risk = risk,
            winRate = chosen.winRate
        )
        setups.add(rec)
        embedSetupAnatomy(rec, atBar)
    }

    /**
     * Procedurally sculpts the authentic candlestick geometry on the tape for the specified setup
     * and generates realistic follow-through bars towards the +2R target!
     */
    fun embedSetupAnatomy(setup: SetupRecord, at: Int) {
        if (at < 3 || at >= candles.size) return
        val p = candles[at].c
        val dir = setup.dir
        val range = setup.risk * 0.45

        when {
            setup.key.contains("hammer", ignoreCase = true) || setup.key.contains("pinbar", ignoreCase = true) -> {
                // Pin Bar / Hammer: Small body at extreme, long lower wick (2.5x body) in direction opposite to trade
                val o = p
                val c = if (dir == 1) o + range * 0.35 else o - range * 0.35
                val h = if (dir == 1) c + range * 0.1 else o + range * 2.6
                val l = if (dir == 1) o - range * 2.6 else c - range * 0.1
                candles[at] = Candle(o, h, l, c, candles[at].v * 1.8)
            }
            setup.key.contains("engulfing", ignoreCase = true) -> {
                // Previous bar is small counter-trend
                val prevO = p - dir * range * 0.5
                val prevC = p
                val prevH = max(prevO, prevC) + range * 0.1
                val prevL = min(prevO, prevC) - range * 0.1
                candles[at - 1] = Candle(prevO, prevH, prevL, prevC, candles[at - 1].v)

                // Current bar completely engulfs previous bar in direction of trade
                val o = p
                val c = if (dir == 1) o + range * 1.8 else o - range * 1.8
                val h = max(o, c) + range * 0.2
                val l = min(o, c) - range * 0.2
                candles[at] = Candle(o, h, l, c, candles[at].v * 2.5)
            }
            setup.key.contains("fvg", ignoreCase = true) -> {
                // Fair Value Gap: 3-bar displacement leaving a clear imbalance gap
                val bar1 = candles[at - 2]
                val dispO = bar1.c
                val dispC = if (dir == 1) dispO + range * 2.4 else dispO - range * 2.4
                candles[at - 1] = Candle(dispO, max(dispO, dispC) + range * 0.2, min(dispO, dispC) - range * 0.2, dispC, candles[at - 1].v * 3.2)
                val o3 = dispC
                val c3 = if (dir == 1) o3 + range * 0.8 else o3 - range * 0.8
                val l3 = if (dir == 1) min(o3, c3) - range * 0.2 else min(o3, c3)
                val h3 = if (dir == 1) max(o3, c3) else max(o3, c3) + range * 0.2
                candles[at] = Candle(o3, h3, l3, c3, candles[at].v * 1.6)
            }
            setup.key.contains("flag", ignoreCase = true) -> {
                val o = p
                val c = if (dir == 1) o + range * 1.6 else o - range * 1.6
                candles[at] = Candle(o, max(o, c) + range * 0.3, min(o, c) - range * 0.1, c, candles[at].v * 2.2)
            }
            setup.key.contains("double", ignoreCase = true) || setup.key.contains("dbottom", ignoreCase = true) -> {
                // Double bottom / double top retest
                val retestLow = if (dir == 1) p - range * 1.5 else p + range * 1.5
                candles[at - 2] = Candle(p, p + range * 0.2, retestLow, p - dir * range * 0.2, candles[at - 2].v * 1.4)
                val o = candles[at - 2].c
                val c = if (dir == 1) o + range * 1.5 else o - range * 1.5
                candles[at] = Candle(o, max(o, c) + range * 0.3, min(o, c) - range * 0.1, c, candles[at].v * 2.1)
            }
            setup.key.contains("sweep", ignoreCase = true) || setup.key.contains("liquidity", ignoreCase = true) -> {
                // Liquidity sweep: deep false breakout wick that immediately reverses
                val o = p
                val wickExtreme = if (dir == 1) o - range * 2.8 else o + range * 2.8
                val c = if (dir == 1) o + range * 0.9 else o - range * 0.9
                candles[at] = Candle(o, if (dir == 1) c + range * 0.2 else wickExtreme, if (dir == 1) wickExtreme else c - range * 0.2, c, candles[at].v * 3.0)
            }
            setup.key.contains("order_block", ignoreCase = true) || setup.key.contains("ob", ignoreCase = true) -> {
                // Order block retest and displacement
                val o = p
                val c = if (dir == 1) o + range * 2.0 else o - range * 2.0
                candles[at] = Candle(o, max(o, c) + range * 0.2, min(o, c) - range * 0.2, c, candles[at].v * 2.6)
            }
            else -> {
                // Universal directional momentum displacement
                val o = p
                val c = if (dir == 1) o + range * 1.7 else o - range * 1.7
                candles[at] = Candle(o, max(o, c) + range * 0.3, min(o, c) - range * 0.3, c, candles[at].v * 2.0)
            }
        }

        // Procedurally generate realistic follow-through bars advancing towards target
        val isWin = rng() < setup.winRate
        val targetMove = if (isWin) dir * setup.risk * 2.1 else -dir * setup.risk * 1.1
        var walkPrice = candles[at].c
        for (step in 1..8) {
            val stepDrift = (targetMove / 8.0) * (0.7 + rng() * 0.6)
            val oBar = walkPrice
            val cBar = oBar + stepDrift
            val hBar = max(oBar, cBar) + range * (0.1 + rng() * 0.15)
            val lBar = min(oBar, cBar) - range * (0.1 + rng() * 0.15)
            val nextIdx = at + step
            if (nextIdx < candles.size) {
                candles[nextIdx] = Candle(oBar, hBar, lBar, cBar, candles[nextIdx].v)
            } else {
                candles.add(Candle(oBar, hBar, lBar, cBar, candles[at].v * (0.7 + rng() * 0.5)))
            }
            walkPrice = cBar
        }
    }

    /** Generates additional drift & chop candles before the next setup */
    fun filler(n: Int): List<Candle> {
        val b = Leg(rng, lastPrice(), symbol.volatility).apply { regime() }
        var left = n
        while (left > 0) {
            val len = min(left, 5 + floor(rng() * 8.0).toInt())
            val pull = (home - b.price) * 0.25
            if (rng() < 0.35) b.regime()
            if (rng() < 0.30) b.chop(len, b.price, 0.8)
            else {
                val swingTarget = b.price + pull + (rng() - 0.5) * (home * 0.035)
                b.leg(len, swingTarget, 1.0)
            }
            left -= len
        }
        return b.candles
    }

    /** Ensure at least `n` candles exist beyond tape index `k` */
    fun ensure(k: Int, n: Int, preferredKey: String? = null) {
        var guard = 0
        while (candles.size - 1 < k + n && guard++ < 10) {
            val f = filler(14)
            candles.addAll(f)
            // Periodically trigger a new setup if tape has advanced
            val lastSetupAt = setups.lastOrNull()?.at ?: 0
            if (candles.size - lastSetupAt >= 25) {
                appendSetup(preferredKey, atBar = candles.size - 6)
            }
        }
    }

    companion object {
        const val PATH_N2 = 32

        /** Deterministic 32-bit PRNG (Mulberry32) */
        fun mulberry32(seed: Long): () -> Double {
            var a = (seed and 0xFFFFFFFFL).toInt()
            return {
                a += 0x6D2B79F5.toInt()
                var t = (a xor (a ushr 15)) * (1 or a)
                t = (t + ((t xor (t ushr 7)) * (61 or t))) xor t
                ((t xor (t ushr 14)).toLong() and 0xFFFFFFFFL).toDouble() / 4294967296.0
            }
        }

        /** Standard normal random variable via Box-Muller transform */
        fun normal(rng: () -> Double): Double {
            var u = 0.0
            var v = 0.0
            while (u == 0.0) u = rng()
            while (v == 0.0) v = rng()
            return sqrt(-2.0 * ln(u)) * cos(2.0 * Math.PI * v)
        }

        /**
         * 32-step Brownian bridge intrabar walk between Open, High, Low, and Close.
         * Exact tick path reproduction guaranteed by candle seed.
         */
        fun walkPath(o: Double, h: Double, l: Double, cl: Double, seed: Long): DoubleArray {
            val n = PATH_N2
            val p = DoubleArray(n + 1)
            val range = h - l
            if (!(range > 0.0)) {
                p.fill(o)
                p[n] = cl
                return p
            }

            val r = mulberry32(seed)
            val up = cl >= o
            val counterFirst = r() < 0.62
            val first = if (up) (if (counterFirst) l else h) else (if (counterFirst) h else l)
            val second = if (first == l) h else l

            val i1 = 1 + floor(r() * (n * 2.0 / 3.0 - 1.0)).toInt()
            val i2 = i1 + 2 + floor(r() * (n - 2.0 - i1)).toInt()

            fun fold(x: Double): Double {
                var v = x
                var count = 0
                while (count++ < 4 && (v > h || v < l)) {
                    v = if (v > h) 2.0 * h - v else 2.0 * l - v
                }
                return if (v > h) h else if (v < l) l else v
            }

            fun leg(startIdx: Int, endIdx: Int, startVal: Double, endVal: Double) {
                val m = endIdx - startIdx
                val w = DoubleArray(m + 1)
                for (s in 1..m) {
                    w[s] = w[s - 1] + (r() + r() + r() - 1.5) * (if (r() < 0.2) 2.4 else 0.8)
                }
                for (s in 1 until m) {
                    val progress = s.toDouble() / m.toDouble()
                    val bridge = startVal + (endVal - startVal) * progress + (w[s] - progress * w[m]) * range * 0.11
                    p[startIdx + s] = fold(bridge)
                }
            }

            p[0] = o
            p[i1] = first
            p[i2] = second
            p[n] = cl

            leg(0, i1, o, first)
            leg(i1, i2, first, second)
            leg(i2, n, second, cl)

            return p
        }

        fun barPath(candle: Candle, index: Int): DoubleArray {
            if (candle.path != null && candle.path!!.size == PATH_N2 + 1) return candle.path!!
            val seed = (((index + 1) * 2654435761L) xor 0x5bd1e995L) and 0xFFFFFFFFL
            candle.path = walkPath(candle.o, candle.h, candle.l, candle.c, seed)
            return candle.path!!
        }

        /** Continuous price evaluation at tape index `k` */
        fun priceAt(candles: List<Candle>, k: Double): Double {
            if (candles.isEmpty()) return 100.0
            val i = min(candles.size - 1, max(0, floor(k).toInt()))
            val f = max(0.0, min(1.0, k - i))
            val p = barPath(candles[i], i)
            val n = p.size - 1
            val s = min(n - 1, floor(f * n).toInt())
            return p[s] + (p[s + 1] - p[s]) * (f * n - s)
        }

        /**
         * Real-time forming candle at fractional tape index `k`.
         * Open is fixed at candle open, Close is live price, High & Low expand
         * along the 32 Brownian bridge steps traversed so far.
         */
        fun formingAt(candles: List<Candle>, k: Double): Candle {
            if (candles.isEmpty()) return Candle(100.0, 100.0, 100.0, 100.0, 100.0)
            val i = min(candles.size - 1, max(0, floor(k).toInt()))
            val f = max(0.0, min(1.0, k - i))
            val p = barPath(candles[i], i)
            val n = p.size - 1
            val s = min(n - 1, floor(f * n).toInt())
            val cl = p[s] + (p[s + 1] - p[s]) * (f * n - s)
            var h = max(p[0], cl)
            var l = min(p[0], cl)
            for (j in 1..s) {
                if (p[j] > h) h = p[j]
                if (p[j] < l) l = p[j]
            }
            return Candle(p[0], h, l, cl, candles[i].v)
        }

        /** Position P&L in dollars */
        fun ridePnl(dir: Int, entry: Double, currentPrice: Double, notional: Double, feeBps: Double = 5.0): Double {
            val gross = (dir * (currentPrice - entry)) / entry * notional
            val fee = (2.0 * feeBps / 10000.0) * notional
            return gross - fee
        }

        /**
         * Comprehensive institutional trading P&L calculator with leverage, fees, and ROI%
         */
        fun calculatePnl(
            dir: Int,
            entry: Double,
            currentPrice: Double,
            margin: Double = 1000.0,
            leverage: Double = 10.0,
            targetPrice: Double? = null,
            stopPrice: Double? = null,
            feeBps: Double = 4.0
        ): TradePnlResult {
            val notional = margin * leverage
            val priceReturn = if (entry > 0.0) (currentPrice - entry) / entry else 0.0
            val gross = dir * priceReturn * notional
            val fee = (2.0 * feeBps / 10000.0) * notional
            val net = gross - fee
            val roi = if (margin > 0.0) (net / margin) * 100.0 else 0.0

            val targetHit = when {
                targetPrice == null -> false
                dir == 1 -> currentPrice >= targetPrice
                else -> currentPrice <= targetPrice
            }

            val stoppedOut = when {
                stopPrice == null -> net <= -margin * 0.85
                dir == 1 -> currentPrice <= stopPrice
                else -> currentPrice >= stopPrice
            }

            return TradePnlResult(
                grossPnl = gross,
                netPnl = net,
                fee = fee,
                roiPct = roi,
                notional = notional,
                isTargetHit = targetHit,
                isStoppedOut = stoppedOut
            )
        }

        /** Solves first tape position where liquidation occurs */
        fun firstLiq(
            candles: List<Candle>,
            from: Double,
            to: Double,
            dir: Int,
            entry: Double,
            notional: Double,
            limit: Double,
            feeBps: Double = 5.0
        ): Double? {
            val fee = (2.0 * feeBps / 10000.0) * notional
            val move = ((limit + fee) / notional) * entry
            val bad = { p: Double -> dir * (p - entry) <= move }

            var k = from
            var guard = 0
            while (k < to && guard++ < 100000) {
                val i = floor(k).toInt()
                if (i >= candles.size) break
                val path = barPath(candles[i], i)
                val n = path.size - 1
                val s = min(n - 1, floor((k - i) * n).toInt())
                val end = min(to, i + (s + 1.0) / n)
                val pA = priceAt(candles, k)
                val pB = priceAt(candles, end)
                if (bad(pA)) return k
                if (bad(pB)) return k + ((entry + dir * move - pA) / (pB - pA)) * (end - k)
                k = end
            }
            return null
        }
    }

    /**
     * Exact 1:1 Camera Tracking Math from Setup Rush (`_t` and `Ot`).
     * Supports sub-pixel continuous horizontal auto-scrolling and exponential vertical price following.
     */
    class ChartCamera(
        var n: Double = 28.0, // visible candle count
        initialPrice: Double = 100.0
    ) {
        var right: Double = 15.0 // fractional candle index at right margin
        var lo: Double = if (initialPrice > 0.0) initialPrice * 0.98 else 0.0
        var hi: Double = if (initialPrice > 0.0) initialPrice * 1.02 else 1.0
        var tLo: Double = lo
        var tHi: Double = hi
        var follow: Boolean = true
        var showP: Double = initialPrice
        var init: Boolean = false

        fun update(k: Double, candles: List<Candle>, dt: Double, posEntry: Double? = null) {
            if (candles.isEmpty()) return
            val dtClamped = min(0.05, max(0.001, dt))

            // 1. Horizontal auto-scroll follow
            val targetRight = k + 1.0 + n * 0.18
            if (!init) {
                right = targetRight
                init = true
            } else if (follow) {
                right += (targetRight - right) * (1.0 - exp(-dtClamped * 8.0))
            }
            val x0 = right - n

            // 2. Vertical price window bounds calculation
            val cIdx = min(candles.size - 1, max(0, floor(k).toInt()))
            val lIdx = max(0, floor(x0).toInt())
            val uIdx = min(cIdx, ceil(right).toInt())

            var minP = Double.MAX_VALUE
            var maxP = -Double.MAX_VALUE
            for (i in lIdx..uIdx) {
                val bar = if (i == cIdx) formingAt(candles, k) else candles[i]
                if (bar.l < minP) minP = bar.l
                if (bar.h > maxP) maxP = bar.h
            }
            if (minP >= maxP || minP == Double.MAX_VALUE) {
                val bar = formingAt(candles, k)
                minP = bar.l
                maxP = bar.h
            }
            if (posEntry != null && follow) {
                minP = min(minP, posEntry)
                maxP = max(maxP, posEntry)
            }

            // Price headroom and floor padding (14% padding)
            val span = max(0.01, maxP - minP)
            val pad = span * 0.14
            val targetMinP = minP - pad
            val targetMaxP = maxP + pad

            if (tLo <= 0.0 || tHi <= tLo) {
                tLo = targetMinP
                tHi = targetMaxP
                lo = targetMinP
                hi = targetMaxP
            } else {
                val curSpan = tHi - tLo
                val expand = targetMinP < tLo || targetMaxP > tHi
                if (expand) {
                    tLo = targetMinP - (if (targetMinP < tLo) span * 0.18 else 0.0)
                    tHi = targetMaxP + (if (targetMaxP > tHi) span * 0.18 else 0.0)
                } else if (curSpan > span * 1.35) {
                    tLo = targetMinP
                    tHi = targetMaxP
                }
                val smoothFactor = 1.0 - exp(-dtClamped * 5.0)
                lo += (tLo - lo) * smoothFactor
                hi += (tHi - hi) * smoothFactor
            }

            // 3. Smooth active price tracking (Ot function in Setup Rush)
            val curPrice = priceAt(candles, k)
            if (showP <= 0.0 || abs(curPrice - showP) > abs(curPrice) * 0.1) {
                showP = curPrice
            } else {
                showP += (curPrice - showP) * (1.0 - exp(-dtClamped * 11.0))
            }
        }

        fun barSpacing(width: Float): Float = width / max(1f, n.toFloat())

        fun barWidth(width: Float): Float = barSpacing(width) * 0.72f

        fun xOf(candleIndex: Double, width: Float): Float {
            val spacing = barSpacing(width)
            val x0 = right - n
            val raw = ((candleIndex - x0).toFloat() + 0.5f) * spacing
            return if (raw.isNaN()) 0f else raw.coerceIn(-2000f, width + 2000f)
        }

        fun yOf(price: Double, height: Float): Float {
            val range = max(0.0001, hi - lo)
            val raw = (height - ((price - lo) / range * height)).toFloat()
            return if (raw.isNaN()) height / 2f else raw.coerceIn(-2000f, height + 2000f)
        }
    }

    /** Candle generator leg with volatility regimes */
    class Leg(val rng: () -> Double, startPrice: Double, var vol: Double = 0.26) {
        val candles = mutableListOf<Candle>()
        var price = startPrice
        var wick = 0.16
        var vb = 1400.0

        fun regime() {
            vol = 0.18 + rng() * 0.20
            wick = 0.07 + rng() * 0.18
            vb = 900.0 + rng() * 2200.0
        }

        fun push(o: Double, h: Double, l: Double, c: Double, v: Double? = null) {
            val volFinal = v ?: (vb * (0.7 + rng() * 0.8))
            candles.add(Candle(o, max(h, max(o, c)), min(l, min(o, c)), c, volFinal))
            price = c
        }

        fun bar(drift: Double, volMul: Double = 1.0) {
            // Price-proportional volatility scaling:
            // Realistic percentage swings across BTC ($65k), ETH ($2.6k), SOL ($180), GOLD ($2.7k)
            val barVol = price * (vol * 0.007 * volMul)
            val open = price + normal(rng) * (barVol * 0.08)
            val close = open + drift + normal(rng) * barVol
            val body = abs(close - open)
            val wickSpread = price * (0.0012 + rng() * wick * 0.003)
            val up = wickSpread + body * (0.05 + rng() * 0.2)
            val dn = wickSpread + body * (0.05 + rng() * 0.2)
            val shock = 1.0 + min(1.8, body / max(price * 0.002, barVol * 2.4))
            push(open, max(open, close) + up, min(open, close) - dn, close, vb * (0.65 + rng() * 0.9) * shock)
        }

        fun leg(n: Int, to: Double, volMul: Double = 1.0) {
            for (k in 0 until n) {
                val left = n - k
                val drift = ((to - price) / left) * (0.8 + rng() * 0.4)
                bar(drift, volMul)
            }
        }

        fun chop(n: Int, level: Double, volMul: Double = 0.8) {
            for (k in 0 until n) {
                bar((level - price) * 0.25, volMul)
            }
        }
    }
}

data class Candle(
    val o: Double,
    val h: Double,
    val l: Double,
    val c: Double,
    val v: Double,
    var path: DoubleArray? = null
)

data class SetupRecord(
    val key: String,
    val name: String,
    val dir: Int,
    val at: Int,
    val entry: Double,
    val stop: Double,
    val risk: Double,
    val winRate: Double
)

data class TradePnlResult(
    val grossPnl: Double,
    val netPnl: Double,
    val fee: Double,
    val roiPct: Double,
    val notional: Double,
    val isTargetHit: Boolean,
    val isStoppedOut: Boolean
)
