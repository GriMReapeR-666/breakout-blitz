package com.setuprush.candy.ui

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.setuprush.candy.engine.Candle
import com.setuprush.candy.engine.IndicatorEngine
import com.setuprush.candy.engine.SetupEngine
import com.setuprush.candy.engine.SetupExecutionTimingEngine
import com.setuprush.candy.engine.SetupRecord
import com.setuprush.candy.theme.GameTheme
import com.setuprush.candy.theme.ThemeManager
import kotlin.math.abs
import kotlin.math.max
import kotlin.math.min
import kotlin.math.min

/**
 * Pure native Jetpack Compose candlestick chart renderer.
 * Features 120 Hz ultra-fluid hardware accelerated drawing, dynamic viewport scaling,
 * intrabar tick pulse, touch-hold long / drag-down short trading gestures,
 * and live SMC (BOS/CHoCH, FVG), Supertrend, and Pattern Lock visual signals.
 */
@Composable
fun ChartCanvas(
    candles: List<Candle>,
    tapePosition: Double,
    theme: GameTheme = ThemeManager.currentTheme.value,
    activeSetup: SetupRecord? = null,
    supertrendEnabled: Boolean = true,
    structureEnabled: Boolean = true,
    fvgEnabled: Boolean = true,
    emaCrossEnabled: Boolean = true,
    patternLockEnabled: Boolean = true,
    onLongEntry: () -> Unit = {},
    onShortEntry: () -> Unit = {},
    onCashOut: (Double) -> Unit = {},
    modifier: Modifier = Modifier
) {
    var isHolding by remember { mutableStateOf(false) }
    var activeDirection by remember { mutableStateOf(0) } // +1 = Long, -1 = Short
    var entryPrice by remember { mutableStateOf(100.0) }
    var livePnl by remember { mutableStateOf(0.0) }

    val currentPrice = remember(candles, tapePosition) {
        if (candles.isEmpty()) 100.0 else SetupEngine.priceAt(candles, tapePosition)
    }

    LaunchedEffect(currentPrice, isHolding, activeDirection) {
        if (isHolding && activeDirection != 0) {
            livePnl = SetupEngine.ridePnl(activeDirection, entryPrice, currentPrice, notional = 1000.0)
        } else {
            livePnl = 0.0
        }
    }

    // Precalculate signals
    val supertrend = remember(candles.size, supertrendEnabled) {
        if (supertrendEnabled) IndicatorEngine.computeSupertrend(candles) else emptyList()
    }
    val structureLines = remember(candles.size, structureEnabled) {
        if (structureEnabled) IndicatorEngine.computeMarketStructure(candles) else emptyList()
    }
    val fvgs = remember(candles.size, fvgEnabled) {
        if (fvgEnabled) IndicatorEngine.computeFairValueGaps(candles) else emptyList()
    }
    val emaPoints = remember(candles.size, emaCrossEnabled) {
        if (emaCrossEnabled) IndicatorEngine.computeEmaCross(candles) else emptyList()
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(theme.bg)
            .pointerInput(Unit) {
                detectDragGestures(
                    onDragStart = { offset ->
                        isHolding = true
                        activeDirection = 1 // Default to Long
                        entryPrice = currentPrice
                        onLongEntry()
                    },
                    onDrag = { change, dragAmount ->
                        change.consume()
                        // Dragging down shifts into Short
                        if (dragAmount.y > 15f && activeDirection != -1) {
                            activeDirection = -1
                            entryPrice = currentPrice
                            onShortEntry()
                        } else if (dragAmount.y < -15f && activeDirection != 1) {
                            activeDirection = 1
                            entryPrice = currentPrice
                            onLongEntry()
                        }
                    },
                    onDragEnd = {
                        if (isHolding) {
                            onCashOut(livePnl)
                        }
                        isHolding = false
                        activeDirection = 0
                    },
                    onDragCancel = {
                        isHolding = false
                        activeDirection = 0
                    }
                )
            }
    ) {
        // Compose Native Canvas
        Canvas(modifier = Modifier.fillMaxSize()) {
            if (candles.isEmpty()) return@Canvas

            val w = size.width
            val h = size.height

            // Visible window of candles (e.g. 24 bars)
            val windowBars = 24
            val endIndex = min(candles.size, max(windowBars, tapePosition.toInt() + 4))
            val startIndex = max(0, endIndex - windowBars)
            val visible = candles.subList(startIndex, endIndex)

            var minP = Double.MAX_VALUE
            var maxP = Double.MIN_VALUE
            for (c in visible) {
                if (c.l < minP) minP = c.l
                if (c.h > maxP) maxP = c.h
            }
            val pad = (maxP - minP) * 0.15
            minP -= pad
            maxP += pad
            val pRange = max(0.01, maxP - minP)

            fun yOf(p: Double): Float = (h - ((p - minP) / pRange * h)).toFloat()

            val barSpacing = w / windowBars
            val barWidth = barSpacing * 0.65f

            // 1. Draw grid lines
            for (g in 1..4) {
                val gy = h * (g / 5f)
                drawLine(
                    color = theme.grid,
                    start = Offset(0f, gy),
                    end = Offset(w, gy),
                    strokeWidth = 1f
                )
            }

            // 2. Draw SMC Fair Value Gaps (FVG)
            if (fvgEnabled) {
                for (fvg in fvgs) {
                    if (fvg.startIdx in startIndex until endIndex) {
                        val localIdx = fvg.startIdx - startIndex
                        val x = localIdx * barSpacing
                        val yTop = yOf(fvg.top)
                        val yBot = yOf(fvg.bottom)
                        val fvgColor = if (fvg.isBull) theme.bull.copy(alpha = 0.12f) else theme.bear.copy(alpha = 0.12f)
                        drawRect(
                            color = fvgColor,
                            topLeft = Offset(x, min(yTop, yBot)),
                            size = Size(barSpacing * 3f, abs(yBot - yTop))
                        )
                    }
                }
            }

            // 3. Draw Candlesticks using exact theme colors
            visible.forEachIndexed { i, c ->
                val x = i * barSpacing + barSpacing * 0.5f
                val isUp = c.c >= c.o
                val bodyColor = if (isUp) theme.up else theme.down
                val wickColor = if (isUp) theme.wickUp else theme.wickDown

                // High-Low Wick
                drawLine(
                    color = wickColor,
                    start = Offset(x, yOf(c.h)),
                    end = Offset(x, yOf(c.l)),
                    strokeWidth = 2.5f,
                    cap = StrokeCap.Round
                )

                // Open-Close Body
                val topY = yOf(max(c.o, c.c))
                val botY = yOf(min(c.o, c.c))
                val bodyH = max(3f, botY - topY)

                drawRect(
                    color = bodyColor,
                    topLeft = Offset(x - barWidth / 2f, topY),
                    size = Size(barWidth, bodyH)
                )

                // Outline stroke if specified
                if (theme.outline != Color.Transparent && theme.outline != bodyColor) {
                    drawRect(
                        color = theme.outline,
                        topLeft = Offset(x - barWidth / 2f, topY),
                        size = Size(barWidth, bodyH),
                        style = Stroke(width = 1.2f)
                    )
                }
            }

            // 4. Draw EMA 9 / 21 Cross Lines & Signals
            if (emaCrossEnabled && emaPoints.size == candles.size) {
                var prevFastPt: Offset? = null
                var prevSlowPt: Offset? = null
                for (i in 0 until visible.size) {
                    val globalIdx = startIndex + i
                    val ep = emaPoints[globalIdx]
                    val x = i * barSpacing + barSpacing * 0.5f
                    val yFast = yOf(ep.fast)
                    val ySlow = yOf(ep.slow)
                    val fastPt = Offset(x, yFast)
                    val slowPt = Offset(x, ySlow)

                    if (prevFastPt != null && prevSlowPt != null) {
                        drawLine(color = theme.bull.copy(alpha = 0.5f), start = prevFastPt, end = fastPt, strokeWidth = 1.6f)
                        drawLine(color = theme.bear.copy(alpha = 0.5f), start = prevSlowPt, end = slowPt, strokeWidth = 1.6f)
                    }
                    prevFastPt = fastPt
                    prevSlowPt = slowPt

                    if (ep.signal == "CROSS_UP") {
                        drawCircle(color = theme.bull, radius = 7f, center = Offset(x, yFast))
                    } else if (ep.signal == "CROSS_DOWN") {
                        drawCircle(color = theme.bear, radius = 7f, center = Offset(x, ySlow))
                    }
                }
            }

            // 5. Draw Supertrend Line & BUY/SELL signals
            if (supertrendEnabled && supertrend.size == candles.size) {
                var prevPoint: Offset? = null
                for (i in 0 until visible.size) {
                    val globalIdx = startIndex + i
                    val st = supertrend[globalIdx]
                    val x = i * barSpacing + barSpacing * 0.5f
                    val y = yOf(st.value)
                    val pt = Offset(x, y)

                    if (prevPoint != null) {
                        drawLine(
                            color = if (st.isBull) theme.bull else theme.bear,
                            start = prevPoint,
                            end = pt,
                            strokeWidth = 2.2f,
                            cap = StrokeCap.Round
                        )
                    }
                    prevPoint = pt

                    // BUY / SELL marker tags
                    if (st.signal == "BUY") {
                        drawCircle(color = theme.bull, radius = 6f, center = Offset(x, yOf(visible[i].l) + 12f))
                    } else if (st.signal == "SELL") {
                        drawCircle(color = theme.bear, radius = 6f, center = Offset(x, yOf(visible[i].h) - 12f))
                    }
                }
            }

            // 6. Draw Market Structure (BOS / CHoCH) lines
            if (structureEnabled) {
                for (sl in structureLines) {
                    if (sl.endIdx >= startIndex && sl.startIdx <= endIndex) {
                        val x1 = max(0f, (sl.startIdx - startIndex) * barSpacing)
                        val x2 = min(w, (sl.endIdx - startIndex) * barSpacing)
                        val y = yOf(sl.level)
                        drawLine(
                            color = if (sl.label == "CHoCH") theme.bear.copy(alpha = 0.8f) else theme.bull.copy(alpha = 0.8f),
                            start = Offset(x1, y),
                            end = Offset(x2, y),
                            strokeWidth = 1.8f,
                            pathEffect = if (sl.label == "CHoCH") PathEffect.dashPathEffect(floatArrayOf(8f, 6f)) else null
                        )
                    }
                }
            }

            // 6. Draw Active Trade Indicators
            if (isHolding) {
                val entryY = yOf(entryPrice)
                val tradeColor = if (activeDirection == 1) theme.bull else theme.bear

                // Entry price line
                drawLine(
                    color = tradeColor.copy(alpha = 0.6f),
                    start = Offset(0f, entryY),
                    end = Offset(w, entryY),
                    strokeWidth = 2f
                )

                // Current price tick pulse
                val currY = yOf(currentPrice)
                drawCircle(
                    color = if (livePnl >= 0) theme.bull else theme.bear,
                    radius = 8f,
                    center = Offset(w - 24f, currY)
                )
            }
        }

        // Live HUD Overlay
        Column(modifier = Modifier.fillMaxWidth()) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "SETUP RUSH · ${theme.name.uppercase()}",
                        color = theme.bull,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Black
                    )
                    Text(
                        text = String.format("$%.2f", currentPrice),
                        color = theme.text,
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                if (isHolding) {
                    val pnlColor = if (livePnl >= 0) theme.bull else theme.bear
                    val prefix = if (livePnl >= 0) "+$" else "-$"
                    val dirText = if (activeDirection == 1) "LONG" else "SHORT"

                    Column(horizontalAlignment = Alignment.End) {
                        Text(
                            text = dirText,
                            color = if (activeDirection == 1) theme.bull else theme.bear,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "$prefix${String.format("%.2f", abs(livePnl))}",
                            color = pnlColor,
                            fontSize = 22.sp,
                            fontWeight = FontWeight.Black
                        )
                    }
                } else {
                    Text(
                        text = "HOLD TO RIDE",
                        color = theme.muted,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }

            // Real-Time Pattern Lock Banner (Candle Vision parity)
            if (patternLockEnabled) {
                activeSetup?.let { setup ->
                    Surface(
                        color = theme.bg.copy(alpha = 0.92f),
                        shape = RoundedCornerShape(12.dp),
                        border = androidx.compose.foundation.BorderStroke(1.dp, if (setup.dir == 1) theme.bull.copy(alpha = 0.6f) else theme.bear.copy(alpha = 0.6f)),
                        modifier = Modifier
                            .align(Alignment.CenterHorizontally)
                            .padding(horizontal = 16.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Text(
                                text = "⚡ PATTERN LOCKED:",
                                color = theme.muted,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = setup.name.uppercase(),
                                color = theme.text,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Black
                            )
                            Text(
                                text = if (setup.dir == 1) "BULLISH" else "BEARISH",
                                color = if (setup.dir == 1) theme.bull else theme.bear,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.ExtraBold
                            )
                            Text(
                                text = "${(setup.winRate * 100).toInt()}% WIN",
                                color = theme.muted,
                                fontSize = 11.sp
                            )
                        }
                    }
                }
            }

            // Real-Time Trade Execution Timing & Missed Setup Feedback
            SetupExecutionTimingEngine.currentFeedback?.let { fb ->
                Surface(
                    color = theme.bg.copy(alpha = 0.95f),
                    shape = RoundedCornerShape(12.dp),
                    border = androidx.compose.foundation.BorderStroke(
                        1.5.dp,
                        when (fb.grade) {
                            SetupExecutionTimingEngine.ExecutionTimingGrade.PERFECT_ENTRY -> Color(0xFF00E676)
                            SetupExecutionTimingEngine.ExecutionTimingGrade.GOOD_ENTRY -> theme.bull
                            SetupExecutionTimingEngine.ExecutionTimingGrade.LATE_ENTRY -> Color(0xFFFFB300)
                            SetupExecutionTimingEngine.ExecutionTimingGrade.FOMO_EARLY -> theme.bear
                            SetupExecutionTimingEngine.ExecutionTimingGrade.MISSED_SETUP -> Color(0xFFFF9100)
                            SetupExecutionTimingEngine.ExecutionTimingGrade.DISCIPLINED_PASS -> Color(0xFF00B0FF)
                        }
                    ),
                    modifier = Modifier
                        .align(Alignment.CenterHorizontally)
                        .padding(horizontal = 16.dp).padding(top = if (patternLockEnabled && activeSetup != null) 42.dp else 0.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Text(
                                text = "${fb.grade.badge} ${fb.grade.title}",
                                color = when (fb.grade) {
                                    SetupExecutionTimingEngine.ExecutionTimingGrade.PERFECT_ENTRY -> Color(0xFF00E676)
                                    SetupExecutionTimingEngine.ExecutionTimingGrade.GOOD_ENTRY -> theme.bull
                                    SetupExecutionTimingEngine.ExecutionTimingGrade.LATE_ENTRY -> Color(0xFFFFB300)
                                    SetupExecutionTimingEngine.ExecutionTimingGrade.FOMO_EARLY -> theme.bear
                                    SetupExecutionTimingEngine.ExecutionTimingGrade.MISSED_SETUP -> Color(0xFFFF9100)
                                    SetupExecutionTimingEngine.ExecutionTimingGrade.DISCIPLINED_PASS -> Color(0xFF00B0FF)
                                },
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Black
                            )
                            Text(
                                text = fb.message,
                                color = theme.text,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        Text(
                            text = "💡 Leak Tip: ${fb.leakTip}",
                            color = theme.muted,
                            fontSize = 10.sp
                        )
                    }
                }
            }
        }
    }
}
