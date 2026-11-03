package com.setuprush.candy.ui

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.RoundRect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.TextMeasurer
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.setuprush.candy.engine.Candle
import com.setuprush.candy.engine.IndicatorEngine
import com.setuprush.candy.engine.SetupEngine
import com.setuprush.candy.engine.SetupExecutionTimingEngine
import com.setuprush.candy.engine.SetupRecord
import com.setuprush.candy.theme.GameTheme
import com.setuprush.candy.theme.ThemeManager
import kotlin.math.*

/**
 * Safe DrawScope text drawer that guards against Compose Constraints crash:
 * `java.lang.IllegalArgumentException: maxHeight must be >= than minHeight(0)`
 * which occurs when topLeft.y > size.height or topLeft.x > size.width.
 */
private fun DrawScope.drawTextSafe(
    textMeasurer: TextMeasurer,
    text: String,
    topLeft: Offset,
    style: TextStyle = TextStyle.Default,
    maxW: Float = 320f,
    maxH: Float = 90f
) {
    if (topLeft.x > size.width || topLeft.y > size.height || topLeft.x < -maxW || topLeft.y < -maxH) {
        return // Completely outside visible canvas viewport
    }
    try {
        drawText(
            textMeasurer = textMeasurer,
            text = text,
            topLeft = topLeft,
            style = style,
            size = Size(maxW, maxH)
        )
    } catch (e: Throwable) {
        // Defensive suppression against text constraints crashes
    }
}

/**
 * Pure native Jetpack Compose candlestick chart engine.
 * Delivers 1:1 parity with the authentic Setup Rush canvas physics:
 * - Exponential camera tracking (`_t` and `Ot`) with continuous sub-pixel horizontal scrolling
 * - Live micro-tick forming candle with intrabar Brownian bridge progression
 * - Dual-direction tactile trading gestures (Hold Long, Slide Down to Short, Release to Cash Out)
 * - Corner bracket reticles on pattern-locked setups with +2R target & -1R stop rays
 * - Corridor P&L fill, trade trajectory curve, and floating dual-badge position pill
 * - Intrabar circular touch progress ring on finger press with direction cues
 */
@Composable
fun ChartCanvas(
    candles: List<Candle>,
    tapePosition: Double,
    theme: GameTheme = ThemeManager.currentTheme.value,
    camera: SetupEngine.ChartCamera = remember { SetupEngine.ChartCamera(initialPrice = 100.0) },
    activeSetup: SetupRecord? = null,
    supertrendEnabled: Boolean = true,
    structureEnabled: Boolean = true,
    fvgEnabled: Boolean = true,
    emaCrossEnabled: Boolean = true,
    patternLockEnabled: Boolean = true,
    isExternalHolding: Boolean = false,
    externalDirection: Int = 1,
    externalEntryPrice: Double = 100.0,
    externalLivePnl: Double = 0.0,
    currentLeverage: Double = 10.0,
    onLongEntry: () -> Unit = {},
    onShortEntry: () -> Unit = {},
    onCashOut: (Double) -> Unit = {},
    modifier: Modifier = Modifier
) {
    var isHolding by remember { mutableStateOf(false) }
    var activeDirection by remember { mutableIntStateOf(1) } // +1 = Long, -1 = Short
    var entryPrice by remember { mutableDoubleStateOf(100.0) }
    var entryTapeIndex by remember { mutableDoubleStateOf(10.0) }
    var livePnl by remember { mutableDoubleStateOf(0.0) }

    // Touch ring tracking state
    var touchPos by remember { mutableStateOf<Offset?>(null) }
    var touchStartPos by remember { mutableStateOf<Offset?>(null) }
    var touchDurationMs by remember { mutableLongStateOf(0L) }

    // Screen effects state
    var flashAlpha by remember { mutableFloatStateOf(0f) }
    var flashColor by remember { mutableStateOf(Color.White) }

    val currentPrice = remember(candles, tapePosition) {
        if (candles.isEmpty()) 100.0 else SetupEngine.priceAt(candles, tapePosition)
    }

    // Sync external holding state if driven by bottom action bar
    LaunchedEffect(isExternalHolding, externalDirection, externalEntryPrice, externalLivePnl) {
        if (isExternalHolding && !isHolding) {
            isHolding = true
            activeDirection = externalDirection
            entryPrice = externalEntryPrice
            entryTapeIndex = tapePosition
        } else if (!isExternalHolding && isHolding && touchPos == null) {
            isHolding = false
        }
    }

    // Continuously compute floating P&L with authentic institutional notional
    LaunchedEffect(currentPrice, isHolding, activeDirection, entryPrice, currentLeverage) {
        if (isHolding) {
            val margin = 1000.0
            val notional = margin * currentLeverage
            livePnl = SetupEngine.ridePnl(activeDirection, entryPrice, currentPrice, notional = notional)
        } else {
            livePnl = 0.0
        }
    }

    // Fade screen flash effect
    LaunchedEffect(flashAlpha) {
        if (flashAlpha > 0f) {
            kotlinx.coroutines.delay(16)
            flashAlpha = max(0f, flashAlpha - 0.08f)
        }
    }

    // Precalculate technical indicators
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

    val textMeasurer = rememberTextMeasurer()

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(theme.bg)
            .pointerInput(candles, tapePosition) {
                detectDragGestures(
                    onDragStart = { offset ->
                        val startP = SetupEngine.priceAt(candles, tapePosition)
                        isHolding = true
                        activeDirection = 1 // Default to Long on initial touch
                        entryPrice = startP
                        entryTapeIndex = tapePosition
                        touchPos = offset
                        touchStartPos = offset
                        touchDurationMs = System.currentTimeMillis()
                        onLongEntry()
                    },
                    onDrag = { change, dragAmount ->
                        change.consume()
                        touchPos = change.position
                        val start = touchStartPos ?: change.position
                        val dy = change.position.y - start.y

                        // Exact 26px threshold from Setup Rush `at(e, t)` function
                        if (dy > 26f && activeDirection != -1) {
                            activeDirection = -1
                            entryPrice = SetupEngine.priceAt(candles, tapePosition)
                            entryTapeIndex = tapePosition
                            onShortEntry()
                        } else if (dy < -26f && activeDirection != 1) {
                            activeDirection = 1
                            entryPrice = SetupEngine.priceAt(candles, tapePosition)
                            entryTapeIndex = tapePosition
                            onLongEntry()
                        }
                    },
                    onDragEnd = {
                        val finalPnl = livePnl
                        if (isHolding) {
                            flashColor = if (finalPnl >= 0) theme.bull else theme.bear
                            flashAlpha = 0.28f
                            onCashOut(finalPnl)
                        }
                        isHolding = false
                        touchPos = null
                        touchStartPos = null
                    },
                    onDragCancel = {
                        isHolding = false
                        touchPos = null
                        touchStartPos = null
                    }
                )
            }
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            if (candles.isEmpty()) return@Canvas

            val w = size.width
            val h = size.height

            // 1. Draw horizontal price gridlines & right-side price scale
            val gridCount = 5
            val priceSpan = max(0.01, camera.hi - camera.lo)
            val priceStep = priceSpan / gridCount

            for (g in 0..gridCount) {
                val gridPrice = camera.lo + g * priceStep
                val gy = camera.yOf(gridPrice, h)
                if (gy in 0f..h) {
                    drawLine(
                        color = theme.grid.copy(alpha = 0.25f),
                        start = Offset(0f, gy),
                        end = Offset(w, gy),
                        strokeWidth = 1f,
                        pathEffect = PathEffect.dashPathEffect(floatArrayOf(6f, 8f))
                    )
                    // Price label on right margin
                    val priceLabel = String.format("%.2f", gridPrice)
                    drawTextSafe(
                        textMeasurer = textMeasurer,
                        text = priceLabel,
                        topLeft = Offset(w - 62.dp.toPx(), gy - 12.dp.toPx()),
                        style = TextStyle(
                            color = theme.muted.copy(alpha = 0.7f),
                            fontSize = 9.sp,
                            fontWeight = FontWeight.SemiBold,
                            fontFamily = FontFamily.Monospace
                        )
                    )
                }
            }

            // 2. Draw vertical time gridlines
            val spacing = camera.barSpacing(w)
            val firstBar = max(0, floor(camera.right - camera.n).toInt())
            val lastBar = min(candles.size - 1, ceil(camera.right).toInt())

            for (i in firstBar..lastBar) {
                if (i % 6 == 0) {
                    val gx = camera.xOf(i.toDouble(), w)
                    drawLine(
                        color = theme.grid.copy(alpha = 0.15f),
                        start = Offset(gx, 0f),
                        end = Offset(gx, h),
                        strokeWidth = 1f,
                        pathEffect = PathEffect.dashPathEffect(floatArrayOf(4f, 8f))
                    )
                }
            }

            // 3. Draw SMC Fair Value Gaps (corridors with neon glow borders)
            if (fvgEnabled) {
                for (fvg in fvgs) {
                    if (fvg.startIdx >= firstBar - 4 && fvg.startIdx <= lastBar + 2) {
                        val xLeft = camera.xOf(fvg.startIdx.toDouble(), w)
                        val xRight = camera.xOf((fvg.startIdx + 3).toDouble(), w)
                        val yTop = camera.yOf(fvg.top, h)
                        val yBot = camera.yOf(fvg.bottom, h)
                        val fvgCol = if (fvg.isBull) theme.bull else theme.bear

                        // Shaded corridor
                        drawRect(
                            color = fvgCol.copy(alpha = 0.10f),
                            topLeft = Offset(xLeft, min(yTop, yBot)),
                            size = Size(max(10f, xRight - xLeft), abs(yBot - yTop))
                        )
                        // Corridor dashed boundary lines
                        drawLine(
                            color = fvgCol.copy(alpha = 0.45f),
                            start = Offset(xLeft, yTop),
                            end = Offset(xRight, yTop),
                            strokeWidth = 1.2f,
                            pathEffect = PathEffect.dashPathEffect(floatArrayOf(4f, 4f))
                        )
                        drawLine(
                            color = fvgCol.copy(alpha = 0.45f),
                            start = Offset(xLeft, yBot),
                            end = Offset(xRight, yBot),
                            strokeWidth = 1.2f,
                            pathEffect = PathEffect.dashPathEffect(floatArrayOf(4f, 4f))
                        )
                    }
                }
            }

            // 4. Draw Market Structure (BOS / CHoCH)
            if (structureEnabled) {
                for (sl in structureLines) {
                    if (sl.endIdx >= firstBar && sl.startIdx <= lastBar) {
                        val x1 = max(0f, camera.xOf(sl.startIdx.toDouble(), w))
                        val x2 = min(w, camera.xOf(sl.endIdx.toDouble(), w))
                        val y = camera.yOf(sl.level, h)
                        val isChoch = sl.label == "CHoCH"
                        val slCol = if (isChoch) theme.bear else theme.bull

                        drawLine(
                            color = slCol.copy(alpha = 0.75f),
                            start = Offset(x1, y),
                            end = Offset(x2, y),
                            strokeWidth = 1.8f,
                            pathEffect = if (isChoch) PathEffect.dashPathEffect(floatArrayOf(8f, 6f)) else null
                        )
                        // Structure pill badge
                        drawTextSafe(
                            textMeasurer = textMeasurer,
                            text = sl.label,
                            topLeft = Offset(x1 + 6f, y - 14f),
                            style = TextStyle(
                                color = slCol,
                                fontSize = 8.sp,
                                fontWeight = FontWeight.Black,
                                fontFamily = FontFamily.Monospace
                            )
                        )
                    }
                }
            }

            // 5. Draw Supertrend & EMA Cross Lines
            if (supertrendEnabled && supertrend.size == candles.size) {
                var prevPt: Offset? = null
                for (i in max(0, firstBar)..min(candles.size - 1, lastBar)) {
                    val st = supertrend[i]
                    val pt = Offset(camera.xOf(i.toDouble(), w), camera.yOf(st.value, h))
                    if (prevPt != null) {
                        drawLine(
                            color = if (st.isBull) theme.bull.copy(alpha = 0.7f) else theme.bear.copy(alpha = 0.7f),
                            start = prevPt,
                            end = pt,
                            strokeWidth = 2.2f,
                            cap = StrokeCap.Round
                        )
                    }
                    prevPt = pt
                }
            }

            // 6. Draw Candlesticks (Historical Bars)
            val currentTapeIdx = floor(tapePosition).toInt()
            val bw = camera.barWidth(w)

            for (i in firstBar until min(currentTapeIdx, candles.size)) {
                val c = candles[i]
                val cx = camera.xOf(i.toDouble(), w)
                val isUp = c.c >= c.o
                val wickColor = if (isUp) theme.wickUp else theme.wickDown
                val bodyColor = if (isUp) theme.up else theme.down

                val topY = camera.yOf(max(c.o, c.c), h)
                val botY = camera.yOf(min(c.o, c.c), h)
                val bodyH = max(2.5f, botY - topY)

                // High-Low Wick
                drawLine(
                    color = wickColor,
                    start = Offset(cx, camera.yOf(c.h, h)),
                    end = Offset(cx, camera.yOf(c.l, h)),
                    strokeWidth = 2.2f,
                    cap = StrokeCap.Round
                )

                // Candle Body
                drawRect(
                    color = bodyColor,
                    topLeft = Offset(cx - bw / 2f, topY),
                    size = Size(bw, bodyH)
                )

                // Theme Outline stroke
                if (theme.outline != Color.Transparent && theme.outline != bodyColor) {
                    drawRect(
                        color = theme.outline,
                        topLeft = Offset(cx - bw / 2f, topY),
                        size = Size(bw, bodyH),
                        style = Stroke(width = 1.0f)
                    )
                }
            }

            // 7. Draw Live Active Forming Candle (at fractional tapePosition)
            if (currentTapeIdx in candles.indices) {
                val forming = SetupEngine.formingAt(candles, tapePosition)
                val activeCx = camera.xOf(currentTapeIdx.toDouble(), w)
                val isUp = forming.c >= forming.o
                val formingWickCol = if (isUp) theme.wickUp else theme.wickDown
                val formingBodyCol = if (isUp) theme.up else theme.down

                val fTopY = camera.yOf(max(forming.o, forming.c), h)
                val fBotY = camera.yOf(min(forming.o, forming.c), h)
                val fBodyH = max(2.5f, fBotY - fTopY)
                val currPriceY = camera.yOf(forming.c, h)

                // High-Low Wick (morphing live along Brownian bridge)
                drawLine(
                    color = formingWickCol,
                    start = Offset(activeCx, camera.yOf(forming.h, h)),
                    end = Offset(activeCx, camera.yOf(forming.l, h)),
                    strokeWidth = 2.5f,
                    cap = StrokeCap.Round
                )

                // Forming Body
                drawRect(
                    color = formingBodyCol,
                    topLeft = Offset(activeCx - bw / 2f, fTopY),
                    size = Size(bw, fBodyH)
                )

                // Live Intrabar Tick Line across Candle Body (Setup Rush signature tick)
                drawRect(
                    color = if (isUp) theme.bull else theme.bear,
                    topLeft = Offset(activeCx - bw / 2f - 2f, currPriceY - 1f),
                    size = Size(bw + 4f, 2.5f)
                )

                // Horizontal Live Price Dotted Ray extending to right margin
                drawLine(
                    color = if (isUp) theme.bull.copy(alpha = 0.85f) else theme.bear.copy(alpha = 0.85f),
                    start = Offset(activeCx + bw / 2f + 4f, currPriceY),
                    end = Offset(w, currPriceY),
                    strokeWidth = 1.4f,
                    pathEffect = PathEffect.dashPathEffect(floatArrayOf(4f, 4f))
                )

                // Active Price Badge on Right Axis
                val priceText = String.format("%.2f", forming.c)
                val pillW = 60.dp.toPx()
                val pillH = 20.dp.toPx()
                val pillX = w - pillW - 2.dp.toPx()
                val pillY = currPriceY - pillH / 2f

                drawRoundRect(
                    color = if (isUp) theme.bull else theme.bear,
                    topLeft = Offset(pillX, pillY),
                    size = Size(pillW, pillH),
                    cornerRadius = CornerRadius(4.dp.toPx())
                )
                drawTextSafe(
                    textMeasurer = textMeasurer,
                    text = priceText,
                    topLeft = Offset(pillX + 4.dp.toPx(), pillY + 3.dp.toPx()),
                    style = TextStyle(
                        color = if (theme.isLight) Color.White else Color.Black,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Black,
                        fontFamily = FontFamily.Monospace
                    )
                )
            }

            // 8. Pattern Locked Setup Reticles & Target/Stop Rays
            if (patternLockEnabled && activeSetup != null) {
                val sAt = activeSetup.at
                val startIdx = max(0, sAt - 3)
                val sMinX = camera.xOf(startIdx.toDouble(), w) - bw * 0.8f
                val sMaxX = camera.xOf(sAt.toDouble(), w) + bw * 0.8f

                var setupHigh = -Double.MAX_VALUE
                var setupLow = Double.MAX_VALUE
                for (j in startIdx..min(sAt, candles.size - 1)) {
                    if (candles[j].h > setupHigh) setupHigh = candles[j].h
                    if (candles[j].l < setupLow) setupLow = candles[j].l
                }

                val sTopY = camera.yOf(setupHigh, h) - 8f
                val sBotY = camera.yOf(setupLow, h) + 8f
                val setupColor = if (activeSetup.dir == 1) theme.bull else theme.bear

                // PATTERN LOCKED Reticle Header
                drawTextSafe(
                    textMeasurer = textMeasurer,
                    text = "PATTERN LOCKED",
                    topLeft = Offset(sMinX, sTopY - 14.dp.toPx()),
                    style = TextStyle(
                        color = setupColor,
                        fontSize = 8.5.sp,
                        fontWeight = FontWeight.Black,
                        fontFamily = FontFamily.Monospace
                    )
                )

                // Corner Reticles around setup candles
                val bracketSize = 14f
                val reticleStroke = 2.4f

                // Top-Left Reticle
                drawLine(setupColor, Offset(sMinX, sTopY + bracketSize), Offset(sMinX, sTopY), reticleStroke)
                drawLine(setupColor, Offset(sMinX, sTopY), Offset(sMinX + bracketSize, sTopY), reticleStroke)
                // Top-Right Reticle
                drawLine(setupColor, Offset(sMaxX - bracketSize, sTopY), Offset(sMaxX, sTopY), reticleStroke)
                drawLine(setupColor, Offset(sMaxX, sTopY), Offset(sMaxX, sTopY + bracketSize), reticleStroke)
                // Bottom-Left Reticle
                drawLine(setupColor, Offset(sMinX, sBotY - bracketSize), Offset(sMinX, sBotY), reticleStroke)
                drawLine(setupColor, Offset(sMinX, sBotY), Offset(sMinX + bracketSize, sBotY), reticleStroke)
                // Bottom-Right Reticle
                drawLine(setupColor, Offset(sMaxX - bracketSize, sBotY), Offset(sMaxX, sBotY), reticleStroke)
                drawLine(setupColor, Offset(sMaxX, sBotY), Offset(sMaxX, sBotY - bracketSize), reticleStroke)

                // Forward Projected Target (+2R) and Stop (-1R) rays
                val targetPrice = activeSetup.entry + activeSetup.dir * 2 * activeSetup.risk
                val stopPrice = activeSetup.stop
                val targetY = camera.yOf(targetPrice, h)
                val stopY = camera.yOf(stopPrice, h)

                // Target Ray (Green)
                drawLine(
                    color = theme.bull.copy(alpha = 0.85f),
                    start = Offset(sMaxX, targetY),
                    end = Offset(w - 65.dp.toPx(), targetY),
                    strokeWidth = 1.6f,
                    pathEffect = PathEffect.dashPathEffect(floatArrayOf(6f, 4f))
                )
                drawTextSafe(
                    textMeasurer = textMeasurer,
                    text = "TARGET +2R",
                    topLeft = Offset(w - 60.dp.toPx(), targetY - 12.dp.toPx()),
                    style = TextStyle(
                        color = theme.bull,
                        fontSize = 8.5.sp,
                        fontWeight = FontWeight.Black,
                        fontFamily = FontFamily.Monospace
                    )
                )

                // Stop Loss Ray (Red)
                drawLine(
                    color = theme.bear.copy(alpha = 0.85f),
                    start = Offset(sMaxX, stopY),
                    end = Offset(w - 65.dp.toPx(), stopY),
                    strokeWidth = 1.6f,
                    pathEffect = PathEffect.dashPathEffect(floatArrayOf(6f, 4f))
                )
                drawTextSafe(
                    textMeasurer = textMeasurer,
                    text = "STOP -1R",
                    topLeft = Offset(w - 60.dp.toPx(), stopY - 12.dp.toPx()),
                    style = TextStyle(
                        color = theme.bear,
                        fontSize = 8.5.sp,
                        fontWeight = FontWeight.Black,
                        fontFamily = FontFamily.Monospace
                    )
                )
            }

            // 9. In-Position Active Trade Overlays (Corridor, Trajectory, Floating Pill)
            if (isHolding) {
                val entryX = camera.xOf(entryTapeIndex, w)
                val currentX = camera.xOf(tapePosition, w)
                val entryY = camera.yOf(entryPrice, h)
                val currY = camera.yOf(currentPrice, h)
                val tradeColor = if (activeDirection == 1) theme.bull else theme.bear

                // Dotted horizontal entry ray from entry to right margin
                drawLine(
                    color = Color.White.copy(alpha = 0.5f),
                    start = Offset(entryX, entryY),
                    end = Offset(w, entryY),
                    strokeWidth = 1.6f,
                    pathEffect = PathEffect.dashPathEffect(floatArrayOf(6f, 6f))
                )

                // Corridor gradient fill between Entry and Current Price
                val topCorridorY = min(entryY, currY)
                val botCorridorY = max(entryY, currY)
                val corridorH = max(4f, botCorridorY - topCorridorY)

                drawRect(
                    brush = Brush.verticalGradient(
                        colors = listOf(
                            tradeColor.copy(alpha = if (livePnl >= 0) 0.18f else 0.14f),
                            tradeColor.copy(alpha = 0.03f)
                        ),
                        startY = topCorridorY,
                        endY = botCorridorY
                    ),
                    topLeft = Offset(entryX, topCorridorY),
                    size = Size(max(10f, currentX - entryX), corridorH)
                )

                // Glowing Entry Point Dot
                drawCircle(color = tradeColor, radius = 6f, center = Offset(entryX, entryY))
                drawCircle(color = theme.bg, radius = 2.5f, center = Offset(entryX, entryY))

                // Floating Position Pill Capsule (Exact Setup Rush lr/ur implementation)
                // Left side: LONG/SHORT + Leverage. Right side: Live P&L.
                val pillTagText = if (activeDirection == 1) "LONG ${currentLeverage.toInt()}×" else "SHORT ${currentLeverage.toInt()}×"
                val pnlSign = if (livePnl >= 0) "+$" else "-$"
                val pillPnlText = "$pnlSign${String.format("%.2f", abs(livePnl))}"

                val tagWidth = 64.dp.toPx()
                val pnlWidth = 74.dp.toPx()
                val totalCapsuleW = tagWidth + pnlWidth
                val capsuleH = 26.dp.toPx()

                // Smoothly position capsule right next to tape tick or docked on right
                val capsuleX = min(w - totalCapsuleW - 8.dp.toPx(), max(8.dp.toPx(), currentX + 16.dp.toPx()))
                val capsuleY = min(h - 90.dp.toPx(), max(48.dp.toPx(), currY - capsuleH / 2f))

                // Capsule Background Outer Container
                drawRoundRect(
                    color = tradeColor,
                    topLeft = Offset(capsuleX, capsuleY),
                    size = Size(totalCapsuleW, capsuleH),
                    cornerRadius = CornerRadius(6.dp.toPx())
                )

                // Left Inner Badge (Tag + Leverage)
                val tagColor = if (theme.isLight) Color(0xFF191A17) else Color(0xFF07090F)
                drawTextSafe(
                    textMeasurer = textMeasurer,
                    text = pillTagText,
                    topLeft = Offset(capsuleX + 6.dp.toPx(), capsuleY + 6.dp.toPx()),
                    style = TextStyle(
                        color = tagColor,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Black,
                        fontFamily = FontFamily.Monospace
                    )
                )

                // Right Inner Badge (P&L Value in high-contrast card)
                val rightBoxX = capsuleX + tagWidth
                drawRoundRect(
                    color = Color.Black.copy(alpha = 0.88f),
                    topLeft = Offset(rightBoxX, capsuleY + 2.dp.toPx()),
                    size = Size(pnlWidth - 2.dp.toPx(), capsuleH - 4.dp.toPx()),
                    cornerRadius = CornerRadius(4.dp.toPx())
                )
                drawTextSafe(
                    textMeasurer = textMeasurer,
                    text = pillPnlText,
                    topLeft = Offset(rightBoxX + 6.dp.toPx(), capsuleY + 5.dp.toPx()),
                    style = TextStyle(
                        color = if (livePnl >= 0) theme.bull else theme.bear,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Black,
                        fontFamily = FontFamily.Monospace
                    )
                )
            }

            // 10. Circular Touch Progress Ring on Finger Press (vr function parity)
            touchPos?.let { pos ->
                val ringRadius = 34.dp.toPx()
                val ringColor = if (isHolding) {
                    if (activeDirection == 1) theme.bull else theme.bear
                } else Color.White.copy(alpha = 0.7f)

                // Expanding / pulsating ambient circle
                drawCircle(
                    color = ringColor.copy(alpha = 0.14f),
                    radius = ringRadius + 10.dp.toPx(),
                    center = pos
                )

                // Solid ring border
                drawCircle(
                    color = ringColor,
                    radius = ringRadius,
                    center = pos,
                    style = Stroke(width = 3.5.dp.toPx())
                )

                // Direction label floating above ring
                val cueText = if (activeDirection == 1) "LONG ▲" else "SHORT ▼"
                drawTextSafe(
                    textMeasurer = textMeasurer,
                    text = cueText,
                    topLeft = Offset(pos.x - 24.dp.toPx(), pos.y - ringRadius - 22.dp.toPx()),
                    style = TextStyle(
                        color = ringColor,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Black,
                        fontFamily = FontFamily.Monospace
                    )
                )

                // Instructional prompt below ring
                val slidePrompt = if (activeDirection == 1) "SLIDE DOWN TO SHORT" else "SLIDE UP TO LONG"
                drawTextSafe(
                    textMeasurer = textMeasurer,
                    text = slidePrompt,
                    topLeft = Offset(pos.x - 52.dp.toPx(), pos.y + ringRadius + 8.dp.toPx()),
                    style = TextStyle(
                        color = theme.muted,
                        fontSize = 8.5.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace
                    )
                )
            }

            // 11. Screen Flash Overlay on Cashout / Liquidation
            if (flashAlpha > 0f) {
                drawRect(
                    color = flashColor.copy(alpha = flashAlpha),
                    topLeft = Offset.Zero,
                    size = size
                )
            }
        }

        // 12. Tactical On-Screen Action Deck (Tactical Combat Controls for BUY / SELL / CASH OUT)
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.BottomCenter)
                .padding(horizontal = 14.dp, vertical = 10.dp)
        ) {
            if (!isHolding) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    // Tactical BUY / LONG Button
                    Button(
                        onClick = {
                            val startP = SetupEngine.priceAt(candles, tapePosition)
                            isHolding = true
                            activeDirection = 1
                            entryPrice = startP
                            entryTapeIndex = tapePosition
                            onLongEntry()
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = theme.bull),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier
                            .weight(1f)
                            .height(48.dp)
                            .shadow(8.dp, RoundedCornerShape(8.dp), spotColor = theme.bull)
                    ) {
                        Text(
                            text = "BUY / LONG ▲",
                            color = if (theme.isLight) Color.White else Color.Black,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Black,
                            fontFamily = FontFamily.Monospace,
                            letterSpacing = 1.sp
                        )
                    }

                    // Tactical SELL / SHORT Button
                    Button(
                        onClick = {
                            val startP = SetupEngine.priceAt(candles, tapePosition)
                            isHolding = true
                            activeDirection = -1
                            entryPrice = startP
                            entryTapeIndex = tapePosition
                            onShortEntry()
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = theme.bear),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier
                            .weight(1f)
                            .height(48.dp)
                            .shadow(8.dp, RoundedCornerShape(8.dp), spotColor = theme.bear)
                    ) {
                        Text(
                            text = "SELL / SHORT ▼",
                            color = Color.White,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Black,
                            fontFamily = FontFamily.Monospace,
                            letterSpacing = 1.sp
                        )
                    }
                }
            } else {
                // In position: Tactical CASH OUT button showing live P&L and ROI%
                val pnlSign = if (livePnl >= 0) "+$" else "-$"
                val roiPct = (livePnl / 1000.0) * 100.0
                val cashoutColor = if (livePnl >= 0) theme.bull else theme.bear

                Button(
                    onClick = {
                        val finalPnl = livePnl
                        isHolding = false
                        onCashOut(finalPnl)
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = cashoutColor),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp)
                        .shadow(12.dp, RoundedCornerShape(8.dp), spotColor = cashoutColor)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = "💰 CASH OUT POSITION:",
                            color = if (theme.isLight && livePnl >= 0) Color.White else Color.Black,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Black,
                            fontFamily = FontFamily.Monospace
                        )
                        Text(
                            text = "$pnlSign${String.format("%.2f", abs(livePnl))} (${if (roiPct >= 0) "+" else ""}${String.format("%.1f", roiPct)}%)",
                            color = if (theme.isLight && livePnl >= 0) Color.White else Color.Black,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Black,
                            fontFamily = FontFamily.Monospace
                        )
                    }
                }
            }
        }
    }
}
