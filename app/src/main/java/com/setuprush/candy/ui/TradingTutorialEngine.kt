package com.setuprush.candy.ui

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.setuprush.candy.theme.GameTheme
import kotlin.math.abs
import kotlin.math.max
import kotlin.math.min

/**
 * =========================================================================
 * STATE-DRIVEN APPLICATION VIRTUALIZATION ENGINE FOR TRADING TUTORIALS
 * =========================================================================
 * Modular architecture that decouples declarative trading setups (Manifest)
 * from the rendering and interaction state machine.
 *
 * Core capabilities:
 * - Timeline progression with smooth animated vector drawings.
 * - Dynamic structural technical analysis (Lines, Trendlines, FVG/OB Zones, Moving Averages).
 * - Dual-viewport canvas (Candlestick chart + Lower Oscillator Window like RSI).
 * - Real-time Risk/Reward Position Brackets (Entry, SL, TP).
 * - Interactive Order Gating (blocks advance until player places trade).
 * - Dynamic resolution-independent coordinate auto-scaling.
 */

// 1. Core Candlestick Data
data class TutorialCandle(
    val open: Float,
    val close: Float,
    val high: Float,
    val low: Float
)

// 2. Technical Drawing Vector Types
sealed class TutorialDrawing {
    data class HorizontalLine(
        val price: Float,
        val color: Color,
        val isDashed: Boolean = false,
        val label: String = ""
    ) : TutorialDrawing()

    data class TrendLine(
        val startIdx: Int,
        val startPrice: Float,
        val endIdx: Int,
        val endPrice: Float,
        val color: Color,
        val thickness: Float = 4f
    ) : TutorialDrawing()

    data class StructuralZone(
        val startIdx: Int,
        val endIdx: Int,
        val topPrice: Float,
        val bottomPrice: Float,
        val color: Color,
        val label: String = ""
    ) : TutorialDrawing()

    data class MovingAveragePlot(
        val values: List<Float>,
        val color: Color = Color(0xFFFFB703),
        val thickness: Float = 3.5f
    ) : TutorialDrawing()
}

// 3. Lower Oscillator Sub-Window (e.g. RSI, MACD, Stochastics)
data class LowerIndicatorData(
    val name: String,
    val lineValues: List<Float>, // 0 to 100 values
    val upperThreshold: Float = 70f,
    val lowerThreshold: Float = 30f,
    val color: Color = Color(0xFFC084FC)
)

// 4. Order Execution Position Metrics
data class TutorialPositionMetrics(
    val isLong: Boolean,
    val entryPrice: Float,
    val stopLoss: Float,
    val takeProfit: Float,
    val riskLabel: String = "1.0R Risk",
    val rewardLabel: String = "+2.0R Target"
)

// 5. Single Step in Tutorial Sequence
data class TutorialStep(
    val title: String,
    val instruction: String,
    val visibleCandlesCount: Int,
    val activeDrawings: List<TutorialDrawing>,
    val lowerIndicator: LowerIndicatorData? = null,
    val requiresOrderPlacement: Boolean = false,
    val positionTarget: TutorialPositionMetrics? = null,
    val hintBadge: String = "STEP GUIDANCE"
)

// 6. Complete Setup Manifest Data Contract
data class StrategySetupManifest(
    val key: String,
    val name: String,
    val subtitle: String,
    val marketData: List<TutorialCandle>,
    val steps: List<TutorialStep>
)

/**
 * Pre-Configured Official Setup Manifests Catalog
 */
object StrategyTutorialManifestCatalog {

    // Manifest 1: Break & Retest + RSI Confirmation
    val BREAK_AND_RETEST = StrategySetupManifest(
        key = "break_retest",
        name = "Break & Retest + RSI",
        subtitle = "Support-Turned-Resistance with Overbought RSI Exhaustion",
        marketData = listOf(
            TutorialCandle(320f, 260f, 330f, 250f), // 1. Initial Drop
            TutorialCandle(260f, 210f, 270f, 200f), // 2. Base Support forms at 210
            TutorialCandle(210f, 215f, 230f, 205f), // 3. Base Consolidation
            TutorialCandle(215f, 130f, 220f, 120f), // 4. MASSIVE BREAKOUT DOWN
            TutorialCandle(130f, 160f, 175f, 125f), // 5. Retest rally climbs back
            TutorialCandle(160f, 210f, 215f, 150f), // 6. Structural resistance tag
            TutorialCandle(210f, 110f, 215f, 100f)  // 7. Rejection pattern dump
        ),
        steps = listOf(
            TutorialStep(
                title = "Identify Base Support",
                instruction = "Price action tags a precise horizontal shelf at 210 multiple times. Watch buyers establish this floor.",
                visibleCandlesCount = 3,
                activeDrawings = listOf(
                    TutorialDrawing.HorizontalLine(price = 210f, color = Color(0xFFF59E0B), isDashed = true, label = "SUPPORT 210.00"),
                    TutorialDrawing.MovingAveragePlot(values = listOf(290f, 260f, 230f), color = Color(0xFFFFB703))
                ),
                lowerIndicator = LowerIndicatorData("RSI (14)", lineValues = listOf(65f, 42f, 45f))
            ),
            TutorialStep(
                title = "High Momentum Breakout",
                instruction = "A large bearish expansion candle drops completely below the 210 support line. Market structure flips to bearish resistance.",
                visibleCandlesCount = 4,
                activeDrawings = listOf(
                    TutorialDrawing.HorizontalLine(price = 210f, color = Color(0xFFEF4444), isDashed = false, label = "NEW RESISTANCE"),
                    TutorialDrawing.MovingAveragePlot(values = listOf(290f, 260f, 230f, 180f), color = Color(0xFFFFB703))
                ),
                lowerIndicator = LowerIndicatorData("RSI (14)", lineValues = listOf(65f, 42f, 45f, 22f))
            ),
            TutorialStep(
                title = "Wait for Structural Retest",
                instruction = "Price rallies back to retest the broken 210 level. Notice the lower window: RSI spikes into the 70 overbought zone, signaling buyer exhaustion.",
                visibleCandlesCount = 6,
                activeDrawings = listOf(
                    TutorialDrawing.HorizontalLine(price = 210f, color = Color(0xFFEF4444), isDashed = false, label = "RETEST 210.00"),
                    TutorialDrawing.StructuralZone(startIdx = 4, endIdx = 6, topPrice = 220f, bottomPrice = 205f, color = Color(0xFFEF4444), label = "SUPPLY ZONE"),
                    TutorialDrawing.MovingAveragePlot(values = listOf(290f, 260f, 230f, 180f, 170f, 185f), color = Color(0xFFFFB703))
                ),
                lowerIndicator = LowerIndicatorData("RSI (14)", lineValues = listOf(65f, 42f, 45f, 22f, 52f, 74f))
            ),
            TutorialStep(
                title = "Execute The Trade Setup",
                instruction = "Confluence confirmed: Structural Resistance + Declining EMA + Overbought RSI. Execute your Short entry with Stop Loss above the zone.",
                visibleCandlesCount = 6,
                activeDrawings = listOf(
                    TutorialDrawing.HorizontalLine(price = 210f, color = Color(0xFFEF4444)),
                    TutorialDrawing.StructuralZone(startIdx = 4, endIdx = 6, topPrice = 220f, bottomPrice = 205f, color = Color(0xFFEF4444)),
                    TutorialDrawing.MovingAveragePlot(values = listOf(290f, 260f, 230f, 180f, 170f, 185f), color = Color(0xFFFFB703))
                ),
                lowerIndicator = LowerIndicatorData("RSI (14)", lineValues = listOf(65f, 42f, 45f, 22f, 52f, 74f)),
                requiresOrderPlacement = true,
                positionTarget = TutorialPositionMetrics(
                    isLong = false,
                    entryPrice = 210f,
                    stopLoss = 240f,   // SL above the supply zone
                    takeProfit = 110f  // +2.0R expansion target
                )
            ),
            TutorialStep(
                title = "Execution Verification & Target Hit",
                instruction = "Flawless Execution! Sellers aggressively defended the 210 level, printing a large bearish expansion candle that hit your +2.0R Take Profit target.",
                visibleCandlesCount = 7,
                activeDrawings = listOf(
                    TutorialDrawing.MovingAveragePlot(values = listOf(290f, 260f, 230f, 180f, 170f, 185f, 160f), color = Color(0xFFFFB703))
                ),
                lowerIndicator = LowerIndicatorData("RSI (14)", lineValues = listOf(65f, 42f, 45f, 22f, 52f, 74f, 30f)),
                positionTarget = TutorialPositionMetrics(isLong = false, entryPrice = 210f, stopLoss = 240f, takeProfit = 110f)
            )
        )
    )

    // Manifest 2: Liquidity Sweep Rejection (ICT / SMC)
    val LIQUIDITY_SWEEP = StrategySetupManifest(
        key = "liquidity_sweep",
        name = "Liquidity Sweep Rejection",
        subtitle = "Turtle Soup / Stop-Hunt Reversal at Key Session Lows",
        marketData = listOf(
            TutorialCandle(220f, 160f, 230f, 150f), // 1. Initial Low Shelf
            TutorialCandle(160f, 180f, 190f, 155f), // 2. Minor Bounce
            TutorialCandle(180f, 165f, 185f, 160f), // 3. Compression near low
            TutorialCandle(165f, 170f, 175f, 110f), // 4. DEEP WICK SWEEP (Traps retail shorts)
            TutorialCandle(170f, 240f, 250f, 168f)  // 5. Powerful Bullish Expansion
        ),
        steps = listOf(
            TutorialStep(
                title = "Mark The Liquidity Pool",
                instruction = "Equal lows at 150 represent a dense pool of retail stop losses. Institutional algorithms target this liquidity before reversing.",
                visibleCandlesCount = 3,
                activeDrawings = listOf(
                    TutorialDrawing.HorizontalLine(price = 150f, color = Color(0xFF00E5FF), isDashed = true, label = "LIQUIDITY POOL 150.00")
                )
            ),
            TutorialStep(
                title = "The Liquidity Sweep Wick",
                instruction = "Candle 4 plunges down to 110 to trigger sell stops, but immediately rejects and closes back above the 150 level. This is a textbook liquidity grab.",
                visibleCandlesCount = 4,
                activeDrawings = listOf(
                    TutorialDrawing.HorizontalLine(price = 150f, color = Color(0xFF00E5FF), isDashed = false, label = "SWEPT & RECLAIMED"),
                    TutorialDrawing.StructuralZone(startIdx = 3, endIdx = 4, topPrice = 150f, bottomPrice = 110f, color = Color(0xFF00E5FF), label = "SWEEP ZONE")
                )
            ),
            TutorialStep(
                title = "Enter On The Reclaim",
                instruction = "Place your LONG order as soon as the candle reclaims the 150 level. Set Stop Loss right below the sweep wick low (110) and aim for +2.0R expansion.",
                visibleCandlesCount = 4,
                activeDrawings = listOf(
                    TutorialDrawing.HorizontalLine(price = 150f, color = Color(0xFF00E5FF)),
                    TutorialDrawing.StructuralZone(startIdx = 3, endIdx = 4, topPrice = 150f, bottomPrice = 110f, color = Color(0xFF00E5FF))
                ),
                requiresOrderPlacement = true,
                positionTarget = TutorialPositionMetrics(
                    isLong = true,
                    entryPrice = 150f,
                    stopLoss = 110f,   // Under the wick
                    takeProfit = 230f  // Target highs (+2.0R)
                )
            ),
            TutorialStep(
                title = "Target Harvested (+2.0R)",
                instruction = "Brilliant Execution! The market expands aggressively northward, hitting your Take Profit at 230 for a clean +2.0R gain.",
                visibleCandlesCount = 5,
                activeDrawings = emptyList(),
                positionTarget = TutorialPositionMetrics(isLong = true, entryPrice = 150f, stopLoss = 110f, takeProfit = 230f)
            )
        )
    )

    // Manifest 3: Fair Value Gap (FVG) Mitigation
    val FVG_MITIGATION = StrategySetupManifest(
        key = "fvg",
        name = "Fair Value Gap (FVG)",
        subtitle = "3-Candle Imbalance Mitigation & Institutional Order Filling",
        marketData = listOf(
            TutorialCandle(120f, 140f, 150f, 115f), // 1. Setup Candle
            TutorialCandle(140f, 260f, 270f, 138f), // 2. High Velocity Displace Candle (Leaves gap between 150 and 200)
            TutorialCandle(260f, 270f, 280f, 250f), // 3. Candle 3 low is 250 (FVG between 150 and 250)
            TutorialCandle(270f, 210f, 275f, 200f), // 4. Pullback into FVG zone
            TutorialCandle(210f, 310f, 320f, 205f)  // 5. Continuation rally
        ),
        steps = listOf(
            TutorialStep(
                title = "Identify The 3-Candle Imbalance",
                instruction = "Candle 2 shows extreme displacement. Notice the gap between the high of Candle 1 (150) and low of Candle 3 (250). That un-traded range is a Fair Value Gap.",
                visibleCandlesCount = 3,
                activeDrawings = listOf(
                    TutorialDrawing.StructuralZone(startIdx = 1, endIdx = 3, topPrice = 250f, bottomPrice = 150f, color = Color(0xFFFFC700), label = "FVG IMBALANCE [150 - 250]")
                )
            ),
            TutorialStep(
                title = "Wait For Mitigation Pullback",
                instruction = "Institutions return to balance their books. Candle 4 pulls back directly into the 50% equilibrium of the FVG zone.",
                visibleCandlesCount = 4,
                activeDrawings = listOf(
                    TutorialDrawing.StructuralZone(startIdx = 1, endIdx = 4, topPrice = 250f, bottomPrice = 150f, color = Color(0xFFFFC700), label = "FVG MITIGATION"),
                    TutorialDrawing.HorizontalLine(price = 200f, color = Color(0xFFFFC700), isDashed = true, label = "FVG 50% MIDPOINT")
                )
            ),
            TutorialStep(
                title = "Execute Long on FVG Tap",
                instruction = "Orderflow is primed. Enter LONG at 200 with Stop Loss below Candle 1 high (140) and Take Profit at +2.0R.",
                visibleCandlesCount = 4,
                activeDrawings = listOf(
                    TutorialDrawing.StructuralZone(startIdx = 1, endIdx = 4, topPrice = 250f, bottomPrice = 150f, color = Color(0xFFFFC700))
                ),
                requiresOrderPlacement = true,
                positionTarget = TutorialPositionMetrics(
                    isLong = true,
                    entryPrice = 200f,
                    stopLoss = 140f,
                    takeProfit = 310f
                )
            ),
            TutorialStep(
                title = "Mitigation Complete (+2.0R)",
                instruction = "Imbalance filled, expansion resumed! Candle 5 surges to 310, harvesting the +2.0R target flawlessly.",
                visibleCandlesCount = 5,
                activeDrawings = emptyList(),
                positionTarget = TutorialPositionMetrics(isLong = true, entryPrice = 200f, stopLoss = 140f, takeProfit = 310f)
            )
        )
    )

    val ALL_MANIFESTS = listOf(BREAK_AND_RETEST, LIQUIDITY_SWEEP, FVG_MITIGATION)
}

/**
 * =========================================================================
 * CORE UNIVERSAL JETPACK COMPOSE TUTORIAL ENGINE VIEW
 * =========================================================================
 */
@Composable
fun UniversalTradingTutorialEngineView(
    manifest: StrategySetupManifest,
    onFinish: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    var currentStepIndex by remember { mutableIntStateOf(0) }
    var orderIsPlaced by remember { mutableStateOf(false) }
    val animationProgress = remember { Animatable(0f) }
    val currentStep = manifest.steps[currentStepIndex]

    // Reset order placement states on step transition tracks
    LaunchedEffect(currentStepIndex) {
        orderIsPlaced = false
        animationProgress.snapTo(0f)
        animationProgress.animateTo(
            targetValue = 1f,
            animationSpec = tween(durationMillis = 850)
        )
    }

    // Auto-scaling Price Geometry Calculations
    val allHighs = manifest.marketData.map { it.high }
    val allLows = manifest.marketData.map { it.low }
    val maxPrice = (allHighs.maxOrNull() ?: 350f) + 20f
    val minPrice = (allLows.minOrNull() ?: 90f) - 20f
    val priceSpan = max(1f, maxPrice - minPrice)

    fun priceToY(price: Float, canvasHeight: Float): Float {
        val norm = ((price - minPrice) / priceSpan).coerceIn(0f, 1f)
        return canvasHeight * (1f - norm)
    }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(520.dp)
            .background(Color(0xFF0A0C10), shape = RoundedCornerShape(14.dp))
            .border(BorderStroke(1.dp, Color(0xFF232A38)), shape = RoundedCornerShape(14.dp))
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(top = 12.dp, start = 12.dp, end = 12.dp, bottom = 8.dp)
        ) {
            // ==================== LAYER 1: UPPER MAIN CHART VIEWPORT ====================
            val hasLower = currentStep.lowerIndicator != null
            val mainWeight = if (hasLower) 0.68f else 1.0f

            Canvas(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(mainWeight)
            ) {
                val canvasW = size.width
                val canvasH = size.height

                val totalCandles = manifest.marketData.size
                val candleSpacing = (canvasW - 40f) / max(1, totalCandles)
                val candleWidth = candleSpacing * 0.62f
                val startPaddingX = 20f

                // 1. Gridlines
                val gridLines = 5
                for (i in 1..gridLines) {
                    val gy = (canvasH / (gridLines + 1)) * i
                    drawLine(
                        color = Color(0xFF232A38).copy(alpha = 0.5f),
                        start = Offset(0f, gy),
                        end = Offset(canvasW, gy),
                        strokeWidth = 1.dp.toPx()
                    )
                }

                // 2. Active Candlestick Render Window
                val visibleCandles = manifest.marketData.take(currentStep.visibleCandlesCount)
                visibleCandles.forEachIndexed { idx, candle ->
                    val cx = startPaddingX + idx * candleSpacing + (candleSpacing / 2f)
                    val isBull = candle.close >= candle.open
                    val candleCol = if (isBull) Color(0xFF00E676) else Color(0xFFFF334B)

                    val topWickY = priceToY(candle.high, canvasH)
                    val botWickY = priceToY(candle.low, canvasH)
                    val bodyTopY = priceToY(max(candle.open, candle.close), canvasH)
                    val bodyBotY = priceToY(min(candle.open, candle.close), canvasH)
                    val bodyH = max(3f, bodyBotY - bodyTopY)

                    // Draw Wick
                    drawLine(
                        color = candleCol,
                        start = Offset(cx, topWickY),
                        end = Offset(cx, botWickY),
                        strokeWidth = 2.dp.toPx()
                    )

                    // Draw Candle Body
                    drawRect(
                        color = candleCol,
                        topLeft = Offset(cx - (candleWidth / 2f), bodyTopY),
                        size = Size(candleWidth, bodyH)
                    )
                }

                // 3. Technical Drawings (Lines, Trendlines, Zones, Moving Average)
                currentStep.activeDrawings.forEach { drawing ->
                    when (drawing) {
                        is TutorialDrawing.HorizontalLine -> {
                            val lineY = priceToY(drawing.price, canvasH)
                            val endX = canvasW * animationProgress.value
                            val pathEffect = if (drawing.isDashed) PathEffect.dashPathEffect(floatArrayOf(12f, 12f), 0f) else null

                            drawLine(
                                color = drawing.color,
                                start = Offset(0f, lineY),
                                end = Offset(endX, lineY),
                                strokeWidth = 2.5.dp.toPx(),
                                pathEffect = pathEffect
                            )
                        }
                        is TutorialDrawing.TrendLine -> {
                            val x1 = startPaddingX + drawing.startIdx * candleSpacing + (candleSpacing / 2f)
                            val y1 = priceToY(drawing.startPrice, canvasH)
                            val x2 = startPaddingX + drawing.endIdx * candleSpacing + (candleSpacing / 2f)
                            val y2 = priceToY(drawing.endPrice, canvasH)

                            val curX = x1 + (x2 - x1) * animationProgress.value
                            val curY = y1 + (y2 - y1) * animationProgress.value

                            drawLine(
                                color = drawing.color,
                                start = Offset(x1, y1),
                                end = Offset(curX, curY),
                                strokeWidth = drawing.thickness.dp.toPx()
                            )
                        }
                        is TutorialDrawing.StructuralZone -> {
                            val x1 = startPaddingX + drawing.startIdx * candleSpacing
                            val x2 = startPaddingX + (drawing.endIdx + 1) * candleSpacing
                            val targetW = (x2 - x1) * animationProgress.value
                            val topY = priceToY(drawing.topPrice, canvasH)
                            val botY = priceToY(drawing.bottomPrice, canvasH)
                            val zoneH = abs(botY - topY)

                            // Area fill
                            drawRect(
                                color = drawing.color.copy(alpha = 0.16f),
                                topLeft = Offset(x1, min(topY, botY)),
                                size = Size(targetW, zoneH)
                            )
                            // Border
                            drawRect(
                                color = drawing.color,
                                topLeft = Offset(x1, min(topY, botY)),
                                size = Size(targetW, zoneH),
                                style = Stroke(width = 1.5.dp.toPx())
                            )
                        }
                        is TutorialDrawing.MovingAveragePlot -> {
                            val path = Path()
                            val plotValues = drawing.values.take(currentStep.visibleCandlesCount)
                            plotValues.forEachIndexed { i, valP ->
                                val px = startPaddingX + i * candleSpacing + (candleSpacing / 2f)
                                val py = priceToY(valP, canvasH)
                                if (i == 0) path.moveTo(px, py) else path.lineTo(px, py)
                            }
                            drawPath(
                                path = path,
                                color = drawing.color,
                                style = Stroke(
                                    width = drawing.thickness.dp.toPx(),
                                    pathEffect = PathEffect.cornerPathEffect(8f)
                                )
                            )
                        }
                    }
                }

                // 4. Position Risk/Reward Brackets Overlay
                if (currentStep.positionTarget != null && (!currentStep.requiresOrderPlacement || orderIsPlaced)) {
                    val pos = currentStep.positionTarget
                    val bracketW = (canvasW - startPaddingX) * animationProgress.value
                    val entryY = priceToY(pos.entryPrice, canvasH)
                    val tpY = priceToY(pos.takeProfit, canvasH)
                    val slY = priceToY(pos.stopLoss, canvasH)

                    val greenFill = Color(0xFF00E676).copy(alpha = 0.14f)
                    val redFill = Color(0xFFFF334B).copy(alpha = 0.14f)

                    if (pos.isLong) {
                        // TP bracket (above entry)
                        drawRect(
                            color = greenFill,
                            topLeft = Offset(startPaddingX, tpY),
                            size = Size(bracketW, entryY - tpY)
                        )
                        // SL bracket (below entry)
                        drawRect(
                            color = redFill,
                            topLeft = Offset(startPaddingX, entryY),
                            size = Size(bracketW, slY - entryY)
                        )
                    } else {
                        // Short: SL bracket (above entry)
                        drawRect(
                            color = redFill,
                            topLeft = Offset(startPaddingX, slY),
                            size = Size(bracketW, entryY - slY)
                        )
                        // Short: TP bracket (below entry)
                        drawRect(
                            color = greenFill,
                            topLeft = Offset(startPaddingX, entryY),
                            size = Size(bracketW, tpY - entryY)
                        )
                    }

                    // Dashed Entry Horizon Ray
                    drawLine(
                        color = Color.White,
                        start = Offset(startPaddingX, entryY),
                        end = Offset(startPaddingX + bracketW, entryY),
                        strokeWidth = 2.dp.toPx(),
                        pathEffect = PathEffect.dashPathEffect(floatArrayOf(10f, 10f), 0f)
                    )
                }
            }

            // ==================== LAYER 2: LOWER OSCILLATOR VIEWPORT (RSI) ====================
            if (hasLower) {
                val ind = currentStep.lowerIndicator!!
                Spacer(modifier = Modifier.height(6.dp))

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(0.32f)
                        .background(Color(0xFF131720), shape = RoundedCornerShape(6.dp))
                        .border(BorderStroke(0.5.dp, Color(0xFF232A38)), shape = RoundedCornerShape(6.dp))
                ) {
                    Canvas(modifier = Modifier.fillMaxSize().padding(horizontal = 12.dp, vertical = 6.dp)) {
                        val ow = size.width
                        val oh = size.height
                        val totalCandles = manifest.marketData.size
                        val spacing = (ow - 40f) / max(1, totalCandles)
                        val startPx = 10f

                        // 70 & 30 boundary lines
                        val y70 = oh * (1f - (ind.upperThreshold / 100f))
                        val y30 = oh * (1f - (ind.lowerThreshold / 100f))

                        drawLine(
                            color = Color(0xFFFF5A1F).copy(alpha = 0.5f),
                            start = Offset(0f, y70),
                            end = Offset(ow, y70),
                            strokeWidth = 1.dp.toPx(),
                            pathEffect = PathEffect.dashPathEffect(floatArrayOf(6f, 6f), 0f)
                        )
                        drawLine(
                            color = Color(0xFF00E5FF).copy(alpha = 0.5f),
                            start = Offset(0f, y30),
                            end = Offset(ow, y30),
                            strokeWidth = 1.dp.toPx(),
                            pathEffect = PathEffect.dashPathEffect(floatArrayOf(6f, 6f), 0f)
                        )

                        // Oscillator Line Path
                        val path = Path()
                        val pts = ind.lineValues.take(currentStep.visibleCandlesCount)
                        pts.forEachIndexed { i, v ->
                            val px = startPx + i * spacing + (spacing / 2f)
                            val py = oh * (1f - (v / 100f))
                            if (i == 0) path.moveTo(px, py) else path.lineTo(px, py)
                        }
                        drawPath(
                            path = path,
                            color = ind.color,
                            style = Stroke(width = 2.5.dp.toPx(), pathEffect = PathEffect.cornerPathEffect(6f))
                        )
                    }

                    // Indicator Tag
                    Text(
                        text = "${ind.name}: ${ind.lineValues.take(currentStep.visibleCandlesCount).lastOrNull()?.toInt() ?: 50}",
                        color = ind.color,
                        fontSize = 8.5.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace,
                        modifier = Modifier.align(Alignment.TopStart).padding(6.dp)
                    )
                }
            }
        }

        // ==================== LAYER 3: FLOATING HEADS-UP INSTRUCTION CARD ====================
        Card(
            modifier = Modifier
                .align(Alignment.TopStart)
                .padding(14.dp)
                .width(310.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xF2131720)),
            shape = RoundedCornerShape(10.dp),
            border = BorderStroke(1.dp, Color(0xFF232A38)),
            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
        ) {
            Column(modifier = Modifier.padding(12.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "STEP ${currentStepIndex + 1}/${manifest.steps.size} · ${manifest.name.uppercase()}",
                        color = Color(0xFFFF5A1F),
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Black,
                        fontFamily = FontFamily.Monospace,
                        letterSpacing = 0.8.sp
                    )

                    Surface(
                        shape = RoundedCornerShape(4.dp),
                        color = Color(0xFF232A38)
                    ) {
                        Text(
                            text = currentStep.hintBadge,
                            color = Color.White,
                            fontSize = 7.5.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = currentStep.title,
                    color = Color.White,
                    fontSize = 13.5.sp,
                    fontWeight = FontWeight.Black,
                    fontFamily = FontFamily.Monospace
                )

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = currentStep.instruction,
                    color = Color(0xFF8A99AD),
                    fontSize = 10.5.sp,
                    lineHeight = 14.sp
                )

                Spacer(modifier = Modifier.height(10.dp))

                // Action Branch: Interactive Order Execution vs Next Step Progression
                if (currentStep.requiresOrderPlacement && !orderIsPlaced) {
                    val target = currentStep.positionTarget
                    val isLong = target?.isLong == true
                    val btnColor = if (isLong) Color(0xFF00E676) else Color(0xFFFF334B)
                    val actionLabel = if (isLong) "🚀 PLACE BUY / LONG ENTRY" else "⚡ PLACE SELL / SHORT ENTRY"

                    Button(
                        onClick = { orderIsPlaced = true },
                        colors = ButtonDefaults.buttonColors(containerColor = btnColor),
                        shape = RoundedCornerShape(6.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(40.dp)
                            .shadow(8.dp, RoundedCornerShape(6.dp), spotColor = btnColor)
                    ) {
                        Text(
                            text = actionLabel,
                            color = Color.White,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Black,
                            fontFamily = FontFamily.Monospace,
                            letterSpacing = 0.8.sp
                        )
                    }
                } else {
                    val isLastStep = currentStepIndex == manifest.steps.size - 1

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        if (currentStepIndex > 0) {
                            Button(
                                onClick = { currentStepIndex-- },
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1E2430)),
                                border = BorderStroke(1.dp, Color(0xFF232A38)),
                                shape = RoundedCornerShape(6.dp),
                                modifier = Modifier.weight(0.8f).height(36.dp)
                            ) {
                                Text(
                                    text = "< PREV",
                                    color = Color.White,
                                    fontSize = 9.sp,
                                    fontFamily = FontFamily.Monospace
                                )
                            }
                        }

                        Button(
                            onClick = {
                                if (isLastStep) {
                                    onFinish()
                                } else {
                                    currentStepIndex++
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = if (isLastStep) Color(0xFF00E676) else Color(0xFFFF5A1F)),
                            shape = RoundedCornerShape(6.dp),
                            modifier = Modifier.weight(1.2f).height(36.dp)
                        ) {
                            Text(
                                text = if (isLastStep) "🏆 SETUP MASTERED" else "NEXT LESSON STEP →",
                                color = if (isLastStep) Color.Black else Color.White,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Black,
                                fontFamily = FontFamily.Monospace
                            )
                        }
                    }
                }
            }
        }
    }
}

/**
 * =========================================================================
 * INTERACTIVE DIALOG CONTAINER
 * =========================================================================
 */
@Composable
fun UniversalTradingTutorialDialog(
    theme: GameTheme,
    manifest: StrategySetupManifest = StrategyTutorialManifestCatalog.BREAK_AND_RETEST,
    onDismiss: () -> Unit
) {
    var selectedManifest by remember { mutableStateOf(manifest) }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            shape = RoundedCornerShape(16.dp),
            color = Color(0xFF0A0C10),
            border = BorderStroke(1.5.dp, Color(0xFFFF5A1F)),
            modifier = Modifier
                .fillMaxWidth(0.96f)
                .fillMaxHeight(0.92f)
                .padding(vertical = 8.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(14.dp)
            ) {
                // Header Bar
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "🎓 INTERACTIVE STRATEGY SIMULATOR",
                            color = Color(0xFFFF5A1F),
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Black,
                            fontFamily = FontFamily.Monospace,
                            letterSpacing = 1.sp
                        )
                        Text(
                            text = selectedManifest.subtitle,
                            color = Color(0xFF8A99AD),
                            fontSize = 9.sp
                        )
                    }

                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = Color(0xFFFF5A1F).copy(alpha = 0.2f),
                        border = BorderStroke(1.dp, Color(0xFFFF5A1F).copy(alpha = 0.5f)),
                        modifier = Modifier.clickable { onDismiss() }
                    ) {
                        Text(
                            text = "✕ CLOSE",
                            color = Color(0xFFFF5A1F),
                            fontSize = 9.5.sp,
                            fontWeight = FontWeight.Black,
                            fontFamily = FontFamily.Monospace,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Strategy Picker Tabs
                LazyRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    itemsIndexed(StrategyTutorialManifestCatalog.ALL_MANIFESTS) { _, m ->
                        val isChosen = m.key == selectedManifest.key
                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = if (isChosen) Color(0xFFFF5A1F).copy(alpha = 0.25f) else Color(0xFF131720),
                            border = BorderStroke(1.dp, if (isChosen) Color(0xFFFF5A1F) else Color(0xFF232A38)),
                            modifier = Modifier.clickable { selectedManifest = m }
                        ) {
                            Text(
                                text = m.name.uppercase(),
                                color = if (isChosen) Color(0xFFFF5A1F) else Color.White,
                                fontSize = 9.5.sp,
                                fontWeight = FontWeight.Black,
                                fontFamily = FontFamily.Monospace,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // The Virtualized Interactive Engine Viewport
                UniversalTradingTutorialEngineView(
                    manifest = selectedManifest,
                    onFinish = onDismiss,
                    modifier = Modifier.fillMaxSize()
                )
            }
        }
    }
}
