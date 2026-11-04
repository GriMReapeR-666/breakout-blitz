package com.setuprush.candy

import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.Density
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.Outline
import androidx.compose.ui.graphics.Path

import kotlin.math.*

import android.content.Intent
import android.content.res.Configuration
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.setuprush.candy.engine.IndicatorCatalog
import com.setuprush.candy.engine.MarketSymbolCatalog
import com.setuprush.candy.engine.MatchPerformanceEngine
import com.setuprush.candy.engine.MonetizationManager
import com.setuprush.candy.engine.OpponentDuelEngine
import com.setuprush.candy.engine.PlayerProfileManager
import com.setuprush.candy.engine.SetupEngine
import com.setuprush.candy.engine.SetupExecutionTimingEngine
import com.setuprush.candy.engine.SetupRecord
import com.setuprush.candy.engine.StrategyCatalog
import com.setuprush.candy.engine.StrategyMasteryManager
import com.setuprush.candy.engine.TradingModelManager
import com.setuprush.candy.theme.ThemeManager
import com.setuprush.candy.ui.*

/**
 * 100% Native Jetpack Compose game screen.
 * Delivers exact 1:1 parity with the authentic Setup Rush interface principles,
 * camera tracking math (`_t` and `Ot`), intraday micro-tick physics,
 * tactile hold-to-ride gestures, pattern-locked banners, and institutional trading metrics.
 */
class NativeGameActivity : ComponentActivity() {

    private lateinit var bridge: NativeBridge

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // Fail-safe crash logger to prevent unhandled background aborts
        val defaultHandler = Thread.getDefaultUncaughtExceptionHandler()
        Thread.setDefaultUncaughtExceptionHandler { thread, throwable ->
            android.util.Log.e("BreakoutBlitzCrash", "FATAL CRASH on thread ${thread.name}", throwable)
            defaultHandler?.uncaughtException(thread, throwable)
        }

        bridge = NativeBridge(this)
        com.setuprush.candy.engine.SoundEffectManager.initialize(this)

        setContent {
            val theme by ThemeManager.currentTheme
            
            // Sync status and navigation bar color with active theme background safely
            DisposableEffect(theme) {
                try {
                    window.statusBarColor = android.graphics.Color.argb(
                        (theme.bg.alpha * 255).toInt(),
                        (theme.bg.red * 255).toInt(),
                        (theme.bg.green * 255).toInt(),
                        (theme.bg.blue * 255).toInt()
                    )
                    window.navigationBarColor = window.statusBarColor
                } catch (e: Throwable) {
                    // Safe fallback for OEM ROMs with restricted system bar manipulation
                }
                onDispose {}
            }

            MaterialTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = theme.bg
                ) {
                    NativeGameScreen(
                        bridge = bridge
                    )
                }
            }
        }
    }

    override fun onDestroy() {
        com.setuprush.candy.engine.SoundEffectManager.release()
        super.onDestroy()
    }
}


/**
 * Setup Rush Authentic 45-degree Chamfer Geometry
 * Top-Right and Bottom-Left corners cut at 45 degrees (--ch polygon parity).
 */
class ChamferShape(private val chamferDp: Dp = 8.dp) : Shape {
    override fun createOutline(size: Size, layoutDirection: LayoutDirection, density: Density): Outline {
        val ch = with(density) { chamferDp.toPx() }
        val path = Path().apply {
            moveTo(0f, 0f)
            lineTo(size.width - ch, 0f)
            lineTo(size.width, ch)
            lineTo(size.width, size.height)
            lineTo(ch, size.height)
            lineTo(0f, size.height - ch)
            close()
        }
        return Outline.Generic(path)
    }
}

@Composable
fun NativeGameScreen(
    bridge: NativeBridge,
    onSwitchToWebView: (() -> Unit)? = null
) {
    val theme by ThemeManager.currentTheme
    val configuration = LocalConfiguration.current
    val isLandscape = configuration.orientation == Configuration.ORIENTATION_LANDSCAPE
    var activeSymbol by remember { mutableStateOf(MarketSymbolCatalog.activeSymbol) }
    var engine by remember { mutableStateOf(SetupEngine(seed = System.currentTimeMillis(), symbol = activeSymbol)) }
    val camera = remember(engine) {
        SetupEngine.ChartCamera(initialPrice = activeSymbol.basePrice).apply {
            if (engine.candles.isNotEmpty()) {
                update(10.0, engine.candles, 0.016)
            }
        }
    }

    var tapePosition by remember { mutableDoubleStateOf(10.0) }
    var totalScore by remember { mutableDoubleStateOf(10000.0) } // Default career bank $10,000
    var streak by remember { mutableIntStateOf(0) }
    var playerTakesCount by remember { mutableIntStateOf(0) }
    var playerStopsCount by remember { mutableIntStateOf(0) }
    var isPlayerHolding by remember { mutableStateOf(false) }
    var activeDirection by remember { mutableIntStateOf(1) } // +1 Long, -1 Short
    var tradeEntryPrice by remember { mutableDoubleStateOf(100.0) }
    var livePnl by remember { mutableDoubleStateOf(0.0) }
    var elapsedSeconds by remember { mutableDoubleStateOf(0.0) }
    var currentLeverage by remember { mutableDoubleStateOf(2.0) }
    var lastOutcome by remember { mutableStateOf<String?>(null) }
    var isPaused by remember { mutableStateOf(false) }
    var tapeSpeedMultiplier by remember { mutableFloatStateOf(1.0f) }
    var isTapePaused by remember { mutableStateOf(false) }

    // Granular Indicator & Signal States
    var showIndicatorDialog by remember { mutableStateOf(false) }
    var activeIndicatorIds by remember { mutableStateOf(setOf("supertrend", "structure", "fvg", "emacross")) }
    val supertrendEnabled = activeIndicatorIds.contains("supertrend")
    val structureEnabled = activeIndicatorIds.contains("structure")
    val fvgEnabled = activeIndicatorIds.contains("fvg")
    val emaCrossEnabled = activeIndicatorIds.contains("emacross")
    val patternLockEnabled = true

    // Master Dialogs State (Defaults to true to show CODM-style Tactical Menu Hub on start)
    var showMasterMenuHub by remember { mutableStateOf(false) }
    var showPreMatchShowcase by remember { mutableStateOf(false) }
    var pendingFormatId by remember { mutableStateOf("deathmatch_1v1") }
    var showModeStrategyDialog by remember { mutableStateOf(false) }
    var showTradingDeskLobbyDialog by remember { mutableStateOf(false) }
    var showMasteryTutorialDialog by remember { mutableStateOf(false) }
    var showPostMatchDebriefDialog by remember { mutableStateOf(false) }
    val sessionTradeReviews = remember { mutableStateListOf<MatchPerformanceEngine.TradeReviewRecord>() }
    var peakRidePnl by remember { mutableDoubleStateOf(0.0) }

    var currentMode by remember { mutableStateOf(GameMode.PRO) }
    var currentLevel by remember { mutableStateOf(DeskLevel.DESK_2) }
    var activeStrategyKey by remember { mutableStateOf<String?>(null) }

    val activeIndCount = (if (supertrendEnabled) 1 else 0) +
            (if (structureEnabled) 1 else 0) +
            (if (fvgEnabled) 1 else 0) +
            (if (emaCrossEnabled) 1 else 0)

    // Current price evaluation
    val currentPrice = remember(engine.candles, tapePosition) {
        if (engine.candles.isEmpty()) 100.0 else SetupEngine.priceAt(engine.candles, tapePosition)
    }

    // Active trade P&L calculation with authentic institutional notional
    LaunchedEffect(currentPrice, isPlayerHolding, activeDirection, tradeEntryPrice, currentLeverage) {
        if (isPlayerHolding) {
            val margin = 1000.0
            val notional = margin * currentLeverage
            livePnl = SetupEngine.ridePnl(activeDirection, tradeEntryPrice, currentPrice, notional = notional)
            if (livePnl > peakRidePnl) peakRidePnl = livePnl
        } else {
            livePnl = 0.0
        }
    }

    // Continuous 60/120 Hz Physics & Camera Loop (Setup Rush mt & Ot parity)
    LaunchedEffect(engine, currentLevel, isPaused, isTapePaused, tapeSpeedMultiplier) {
        var prevNanos = 0L
        while (!isPaused) {
            withFrameNanos { nowNanos ->
                if (prevNanos != 0L) {
                    val dt = ((nowNanos - prevNanos) / 1_000_000_000.0).coerceIn(0.001, 0.05)
                    if (!isTapePaused) {
                        elapsedSeconds += dt

                        // Leverage ramping physics (J.lev = min(20, 2 + elapsed * 0.09))
                        currentLeverage = min(20.0, 2.0 + elapsedSeconds * 0.09)

                        // Continuous tape progression at readable tactical speed
                        val baseSpeed = 0.85 * tapeSpeedMultiplier
                        val speed = baseSpeed * currentLevel.speedMul
                        val prevK = tapePosition
                        tapePosition += dt * speed

                        // Camera follow update
                        camera.update(
                            k = tapePosition,
                            candles = engine.candles,
                            dt = dt,
                            posEntry = if (isPlayerHolding) tradeEntryPrice else null
                        )

                        // Ensure future candles exist ahead of tape
                        val currentIdx = tapePosition.toInt()
                        if (currentIdx + 18 > engine.candles.size) {
                            engine.ensure(currentIdx, 25, activeStrategyKey)
                        }

                        // Intrabar candle close event
                        if (floor(tapePosition).toInt() > floor(prevK).toInt()) {
                            if (isPlayerHolding) bridge.vibrate("tick")
                        }
                    }

                    // Check live setup progression & missed execution detection
                    if (engine.candles.isNotEmpty()) {
                        val currentIdx = tapePosition.toInt()
                        val curPrice = SetupEngine.priceAt(engine.candles, tapePosition)
                        val activeSetup = engine.setups.lastOrNull()

                        val timingFb = SetupExecutionTimingEngine.checkTapeProgression(
                            activeSetup = activeSetup,
                            currentPrice = curPrice,
                            isPlayerHolding = isPlayerHolding
                        )
                        if (timingFb != null) {
                            lastOutcome = "${timingFb.grade.badge} ${timingFb.message}"
                            if (timingFb.grade == SetupExecutionTimingEngine.ExecutionTimingGrade.MISSED_SETUP) {
                                bridge.vibrate("liquidation")
                            }
                        }

                        // Liquidation / Stop-out check (if drawdown exceeds 80% of margin)
                        if (isPlayerHolding) {
                            val margin = 1000.0
                            val notional = margin * currentLeverage
                            val pnl = SetupEngine.ridePnl(activeDirection, tradeEntryPrice, curPrice, notional)
                            if (pnl <= -margin * 0.8) {
                                isPlayerHolding = false
                                totalScore += pnl
                                streak = 0
                                playerStopsCount++
                                bridge.vibrate("liquidation")
                                lastOutcome = "STOPPED OUT: -$${String.format("%.2f", abs(pnl))}"
                                OpponentDuelEngine.recordPlayerStopLoss()
                                StrategyMasteryManager.recordEvaluationTrade(
                                    isWin = false,
                                    rMultiple = -1.0,
                                    pnl = pnl,
                                    entryPrice = tradeEntryPrice,
                                    exitPrice = curPrice,
                                    timingGrade = "STOPPED_OUT"
                                )
                                sessionTradeReviews.add(
                                    MatchPerformanceEngine.TradeReviewRecord(
                                        tradeNumber = sessionTradeReviews.size + 1,
                                        setupName = activeSetup?.name ?: "Market Setup",
                                        direction = activeDirection,
                                        entryPrice = tradeEntryPrice,
                                        exitPrice = curPrice,
                                        pnl = pnl,
                                        rMultiple = -1.0,
                                        timingBadge = "🛑 STOPPED OUT",
                                        mistakeCategory = MatchPerformanceEngine.MistakeCategory.DISCIPLINED_STOP,
                                        mistakeDiagnosis = "Loss contained to planned 1.0R risk budget.",
                                        actionableSolution = "Taking small planned losses preserves capital. Wait for next valid setup."
                                    )
                                )
                                activeStrategyKey?.let {
                                    StrategyMasteryManager.recordTrade(it, isWin = false)
                                }
                                val mission = StrategyMasteryManager.activeMission
                                if (mission != null) {
                                    lastOutcome = "MISSION ${mission.levelId} FAILED: STOPPED OUT · RETRY"
                                }
                                if (currentMode == GameMode.BLOWUP) {
                                    MatchPerformanceEngine.generateDebrief(
                                        modeId = "blowup",
                                        symbol = activeSymbol.id,
                                        playerTakes = playerTakesCount,
                                        playerStops = playerStopsCount,
                                        playerPnl = totalScore,
                                        playerBestStreak = streak,
                                        precisionScore = 0.70,
                                        tradeReviews = sessionTradeReviews.toList()
                                    )
                                    showPostMatchDebriefDialog = true
                                    lastOutcome = "💥 BLOWN UP! COMBINE OVER AT LEVERAGE ${currentLeverage.toInt()}×"
                                }
                            }
                        }

                        // Update competitive opponent duel
                        if (OpponentDuelEngine.isCompetitiveActive) {
                            OpponentDuelEngine.updateTick(curPrice, currentIdx, activeSetup)
                        }
                    }
                }
                prevNanos = nowNanos
            }
        }
    }

    val activeSetup = engine.setups.lastOrNull()

    val performLongEntry = {
            if (!isPlayerHolding) {
                val startP = SetupEngine.priceAt(engine.candles, tapePosition)
                isPlayerHolding = true
                activeDirection = 1
                tradeEntryPrice = startP
                peakRidePnl = 0.0
                bridge.vibrate("hold_long")
                val fb = SetupExecutionTimingEngine.evaluateEntry(
                    activeSetup = activeSetup,
                    tapePosition = tapePosition,
                    isLong = true
                )
                lastOutcome = "${fb.grade.badge} ${fb.grade.title}: ENTERED LONG [${activeSymbol.id}]"
            }
        }

        val performShortEntry = {
            if (!isPlayerHolding) {
                val startP = SetupEngine.priceAt(engine.candles, tapePosition)
                isPlayerHolding = true
                activeDirection = -1
                tradeEntryPrice = startP
                peakRidePnl = 0.0
                bridge.vibrate("slide_short")
                val fb = SetupExecutionTimingEngine.evaluateEntry(
                    activeSetup = activeSetup,
                    tapePosition = tapePosition,
                    isLong = false
                )
                lastOutcome = "${fb.grade.badge} ${fb.grade.title}: ENTERED SHORT [${activeSymbol.id}]"
            }
        }

        val performCashOut = {
            if (isPlayerHolding) {
                val pnl = livePnl
                val isWin = pnl > 0
                val rMult = if (isWin) 2.0 else -1.0
                val exitP = currentPrice
                isPlayerHolding = false
                totalScore += pnl

                // Record into Strategy Mastery 5-Trade Evaluation Series
                val eval = StrategyMasteryManager.recordEvaluationTrade(
                    isWin = isWin,
                    rMultiple = rMult,
                    pnl = pnl,
                    entryPrice = tradeEntryPrice,
                    exitPrice = exitP,
                    timingGrade = "CLEAN"
                )

                // Setup Rush MFE Capture Ratio Calculation (from journal.js: r.cap < 0.3)
                val captureRatio = if (peakRidePnl > 0) (pnl / peakRidePnl).coerceIn(0.0, 1.0) else 1.0
                val timingGrade = SetupExecutionTimingEngine.currentFeedback?.grade ?: SetupExecutionTimingEngine.ExecutionTimingGrade.GOOD_ENTRY
                val isCutEarly = isWin && captureRatio < 0.35 && peakRidePnl >= 120.0

                val mistakeCat = when {
                    !isWin && timingGrade == SetupExecutionTimingEngine.ExecutionTimingGrade.LATE_ENTRY -> MatchPerformanceEngine.MistakeCategory.CHASED_ENTRY
                    !isWin && timingGrade == SetupExecutionTimingEngine.ExecutionTimingGrade.FOMO_EARLY -> MatchPerformanceEngine.MistakeCategory.UNCONFIRMED_EARLY
                    !isWin -> MatchPerformanceEngine.MistakeCategory.DISCIPLINED_STOP
                    isCutEarly -> MatchPerformanceEngine.MistakeCategory.PREMATURE_EXIT
                    else -> MatchPerformanceEngine.MistakeCategory.NONE_FLAWLESS
                }
                val diag = when (mistakeCat) {
                    MatchPerformanceEngine.MistakeCategory.CHASED_ENTRY -> "Entered late after market expansion; unfavorable risk:reward."
                    MatchPerformanceEngine.MistakeCategory.UNCONFIRMED_EARLY -> "Entered before setup confirmation closed; trapped on false wick."
                    MatchPerformanceEngine.MistakeCategory.PREMATURE_EXIT -> "Cut winner early: captured only ${(captureRatio * 100).toInt()}% of expansion (+${String.format("%.2f", pnl)} of +${String.format("%.2f", peakRidePnl)} peak)."
                    MatchPerformanceEngine.MistakeCategory.DISCIPLINED_STOP -> "Disciplined exit; loss contained to planned 1.0R risk."
                    MatchPerformanceEngine.MistakeCategory.STOP_LOSS_IGNORED -> "Drawdown exceeded planned invalidation."
                    MatchPerformanceEngine.MistakeCategory.COUNTER_TREND -> "Executed counter to higher timeframe momentum."
                    MatchPerformanceEngine.MistakeCategory.NONE_FLAWLESS -> "Optimal execution and disciplined exit at target."
                }
                val sol = when (mistakeCat) {
                    MatchPerformanceEngine.MistakeCategory.CHASED_ENTRY -> "Never chase. Wait for the retest pullback into the zone or pass."
                    MatchPerformanceEngine.MistakeCategory.UNCONFIRMED_EARLY -> "Wait for candle confirmation close before entering."
                    MatchPerformanceEngine.MistakeCategory.PREMATURE_EXIT -> "Shift stop to breakeven at +1R and let it expand to the +2.0R target."
                    MatchPerformanceEngine.MistakeCategory.DISCIPLINED_STOP -> "Taking small planned losses is normal. Prepare for next setup."
                    MatchPerformanceEngine.MistakeCategory.STOP_LOSS_IGNORED -> "Cut losses immediately at invalidation price."
                    MatchPerformanceEngine.MistakeCategory.COUNTER_TREND -> "Align entries with the Tactical Confirmation Trigger."
                    MatchPerformanceEngine.MistakeCategory.NONE_FLAWLESS -> "Optimal execution. Repeat this exact trade process."
                }
                val moveGrade = when {
                    isWin && rMult >= 1.8 && (timingGrade == SetupExecutionTimingEngine.ExecutionTimingGrade.PERFECT_ENTRY || captureRatio >= 0.85) -> MatchPerformanceEngine.MoveGrade.BRILLIANT
                    isWin && rMult >= 1.5 -> MatchPerformanceEngine.MoveGrade.BEST
                    isWin && !isCutEarly -> MatchPerformanceEngine.MoveGrade.EXCELLENT
                    isWin -> MatchPerformanceEngine.MoveGrade.INACCURACY
                    mistakeCat == MatchPerformanceEngine.MistakeCategory.DISCIPLINED_STOP -> MatchPerformanceEngine.MoveGrade.DISCIPLINED_STOP
                    mistakeCat == MatchPerformanceEngine.MistakeCategory.CHASED_ENTRY || mistakeCat == MatchPerformanceEngine.MistakeCategory.UNCONFIRMED_EARLY -> MatchPerformanceEngine.MoveGrade.MISTAKE
                    else -> MatchPerformanceEngine.MoveGrade.BLUNDER
                }

                val calculatedStop = tradeEntryPrice - activeDirection * (tradeEntryPrice * 0.002)
                val calculatedTarget = tradeEntryPrice + activeDirection * 2.0 * (tradeEntryPrice * 0.002)

                sessionTradeReviews.add(
                    MatchPerformanceEngine.TradeReviewRecord(
                        tradeNumber = sessionTradeReviews.size + 1,
                        setupName = activeSetup?.name ?: "Market Setup",
                        direction = activeDirection,
                        entryPrice = tradeEntryPrice,
                        exitPrice = exitP,
                        pnl = pnl,
                        rMultiple = rMult,
                        timingBadge = timingGrade.badge + " " + timingGrade.title,
                        mistakeCategory = mistakeCat,
                        mistakeDiagnosis = diag,
                        actionableSolution = sol,
                        moveGrade = moveGrade,
                        stopLossPrice = calculatedStop,
                        targetPrice = calculatedTarget,
                        mfeCaptureRatio = captureRatio,
                        candleIndex = tapePosition.toInt()
                    )
                )

                if (isWin) {
                    streak++
                    playerTakesCount++
                    bridge.vibrate("cash_out")
                    lastOutcome = "CASHED OUT: +$${String.format("%.2f", pnl)}"
                    OpponentDuelEngine.recordPlayerCashOut(pnl)
                    activeStrategyKey?.let {
                        StrategyMasteryManager.recordTrade(it, isWin = true, rMultiple = 2.0)
                    }
                    val mission = StrategyMasteryManager.activeMission
                    if (mission != null) {
                        val stars = if (pnl >= 120.0) 3 else 2
                        StrategyMasteryManager.completeMission(mission.levelId, stars, pnl)
                        lastOutcome = "MISSION ${mission.levelId} CLEARED! +$${String.format("%.2f", pnl)} ($stars STARS ⭐)"
                    }
                } else {
                    streak = 0
                    playerStopsCount++
                    bridge.vibrate("liquidation")
                    lastOutcome = "LOSS: -$${String.format("%.2f", abs(pnl))}"
                    OpponentDuelEngine.recordPlayerStopLoss()
                    activeStrategyKey?.let {
                        StrategyMasteryManager.recordTrade(it, isWin = false)
                    }
                    val mission = StrategyMasteryManager.activeMission
                    if (mission != null) {
                        lastOutcome = "MISSION ${mission.levelId} FAILED: -$${String.format("%.2f", abs(pnl))} · RETRY"
                    }
                }

                // Announce Series Evaluation Outcome
                if (eval != null && eval.completedCount >= 5) {
                    if (eval.isPassed) {
                        lastOutcome = "🎉 3-STAR GRANDMASTER MASTERY CERTIFIED! (${eval.winCount}/5 WINS · +${String.format("%.1f", eval.netR)}R)"
                    } else {
                        lastOutcome = "SERIES ENDED: ${eval.winCount}/5 WINS · +${String.format("%.1f", eval.netR)}R (REQUIRES 3+ WINS & +3.0R)"
                    }
                }

                // Duel check
                if (OpponentDuelEngine.isCompetitiveActive) {
                    val rivalWins = OpponentDuelEngine.activeRival?.roundsWon ?: 0
                    if (OpponentDuelEngine.playerRoundsWon >= 2 || rivalWins >= 2) {
                        MatchPerformanceEngine.generateDebrief(
                            modeId = "1v1",
                            symbol = activeSymbol.id,
                            playerTakes = playerTakesCount,
                            playerStops = playerStopsCount,
                            playerPnl = totalScore,
                            playerBestStreak = streak,
                            precisionScore = 0.85,
                            tradeReviews = sessionTradeReviews.toList()
                        )
                        showPostMatchDebriefDialog = true
                    }
                }

                // Sniper 3-Bullet mode check (Setup Rush Call It parity)
                if (currentMode == GameMode.SNIPER && sessionTradeReviews.size >= 3) {
                    MatchPerformanceEngine.generateDebrief(
                        modeId = "sniper",
                        symbol = activeSymbol.id,
                        playerTakes = playerTakesCount,
                        playerStops = playerStopsCount,
                        playerPnl = totalScore,
                        playerBestStreak = streak,
                        precisionScore = 0.90,
                        tradeReviews = sessionTradeReviews.toList()
                    )
                    showPostMatchDebriefDialog = true
                    lastOutcome = "🎯 3/3 SNIPER BULLETS SPENT · SESSION COMPLETE"
                }
            }
        }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(theme.bg)
    ) {
        // =========================================================================
        // 1. FULL-SCREEN CHART CANVAS (100% Edge-to-Edge Tape under all HUD)
        // =========================================================================
        ChartCanvas(
            candles = engine.candles,
            tapePosition = tapePosition,
            theme = theme,
            camera = camera,
            activeSetup = activeSetup,
            supertrendEnabled = supertrendEnabled,
            structureEnabled = structureEnabled,
            fvgEnabled = fvgEnabled,
            emaCrossEnabled = emaCrossEnabled,
            patternLockEnabled = patternLockEnabled,
            isExternalHolding = isPlayerHolding,
            externalDirection = activeDirection,
            externalEntryPrice = tradeEntryPrice,
            externalLivePnl = livePnl,
            currentLeverage = currentLeverage,
            onLongEntry = performLongEntry,
            onShortEntry = performShortEntry,
            onCashOut = { performCashOut() },
            modifier = Modifier.fillMaxSize()
        )

        // =========================================================================
        // 2. SETUP RUSH EXACT TOP HUD (Zero-letterbox glass overlay)
        // =========================================================================
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .statusBarsPadding()
                .padding(horizontal = 12.dp, vertical = 8.dp)
                .align(Alignment.TopCenter),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.Top
        ) {
            // [TOP-LEFT: Setup Rush Chamfered P&L / Bank Card]
            val displayPnl = if (isPlayerHolding) livePnl else (totalScore - 10000.0)
            val pnlText = if (currentMode == GameMode.BLOWUP && !isPlayerHolding) {
                "$${String.format("%,.0f", totalScore)}"
            } else if (isPlayerHolding) {
                val sign = if (displayPnl >= 0) "+$" else "-$"
                "$sign${String.format("%,.0f", kotlin.math.abs(displayPnl))}"
            } else {
                val sign = if (displayPnl >= 0) "+$" else "-$"
                "$sign${String.format("%,.0f", kotlin.math.abs(displayPnl))}"
            }
            Surface(
                color = Color(0xFF0E1117).copy(alpha = 0.92f),
                shape = ChamferShape(8.dp),
                border = BorderStroke(1.dp, Color(0xFF262D3B)),
                modifier = Modifier.width(135.dp)
            ) {
                Column(
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp)
                ) {
                    Text(
                        text = if (currentMode == GameMode.BLOWUP) "BANK" else "P&L",
                        color = Color(0xFF717886),
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace,
                        letterSpacing = 1.2.sp
                    )
                    Text(
                        text = pnlText,
                        color = if (displayPnl < 0) Color(0xFFFF4B3E) else Color.White,
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Black,
                        fontFamily = FontFamily.Monospace,
                        maxLines = 1
                    )
                    val subText = if (currentMode == GameMode.BLOWUP) "LEV ${String.format("%.1f", currentLeverage)}×"
                                  else if (isPlayerHolding) "RIDING"
                                  else if (playerTakesCount + playerStopsCount == 0) "1ST RIDE"
                                  else "${((playerTakesCount.toDouble() / (playerTakesCount + playerStopsCount)) * 100).toInt()}% WIN"
                    Text(
                        text = subText,
                        color = Color(0xFF717886),
                        fontSize = 9.5.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    // Setup Rush exact 18-segment tick meter (▪▪▪▪▪▫▫▫▫)
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(2.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        val totalTicks = 18
                        val totalTrades = playerTakesCount + playerStopsCount
                        val activeTicks = if (totalTrades == 0) (if (isPlayerHolding) 3 else 1)
                                          else ((playerTakesCount.toDouble() / totalTrades) * totalTicks).toInt().coerceIn(1, totalTicks)
                        for (idx in 0 until totalTicks) {
                            Box(
                                modifier = Modifier
                                    .width(4.dp)
                                    .height(3.dp)
                                    .background(
                                        if (idx < activeTicks) (if (idx == activeTicks - 1 && isPlayerHolding) Color(0xFFFF5A1F) else Color.White)
                                        else Color(0xFF1E2430)
                                    )
                            )
                        }
                    }
                }
            }

            // [TOP-RIGHT: Setup Rush Chamfered Cluster]
            Column(
                horizontalAlignment = Alignment.End,
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                // Top Row: Mode Badge + Timer Box
                Row(
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Mode Chamfer Badge
                    Surface(
                        color = Color(0xFF0E1117).copy(alpha = 0.92f),
                        shape = ChamferShape(6.dp),
                        border = BorderStroke(1.dp, Color(0xFF262D3B)),
                        modifier = Modifier.clickable { showModeStrategyDialog = true }
                    ) {
                        Box(
                            contentAlignment = Alignment.Center,
                            modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp)
                        ) {
                            Text(
                                text = currentMode.displayName.split(" ")[0].uppercase(),
                                color = Color.White,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Black,
                                fontFamily = FontFamily.Monospace,
                                letterSpacing = 1.sp
                            )
                        }
                    }

                    // Timer Chamfer Box with Bottom Progress Bar
                    val remainingSec = kotlin.math.max(0, 120 - elapsedSeconds.toInt())
                    val minPart = remainingSec / 60
                    val secPart = remainingSec % 60
                    val isUrgent = remainingSec <= 15
                    Surface(
                        color = Color(0xFF0E1117).copy(alpha = 0.92f),
                        shape = ChamferShape(6.dp),
                        border = BorderStroke(1.dp, if (isUrgent) Color(0xFFFF4B3E) else Color(0xFF262D3B))
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                            ) {
                                Text(
                                    text = "${minPart}:${secPart.toString().padStart(2, '0')}",
                                    color = Color.White,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Black,
                                    fontFamily = FontFamily.Monospace
                                )
                            }
                            val timeFrac = (remainingSec / 120f).coerceIn(0f, 1f)
                            Box(
                                modifier = Modifier
                                    .align(Alignment.BottomStart)
                                    .fillMaxWidth(timeFrac)
                                    .height(2.5.dp)
                                    .background(if (isUrgent) Color(0xFFFF4B3E) else Color(0xFFFF5A1F))
                            )
                        }
                    }
                }

                // Bottom Row: Segmented 4-Button Toolbar: [ ⏸ | 🔊 | 📈 | 〉]
                Surface(
                    color = Color(0xFF0E1117).copy(alpha = 0.92f),
                    shape = ChamferShape(6.dp),
                    border = BorderStroke(1.dp, Color(0xFF262D3B))
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Button 1: Pause / Resume
                        Box(
                            contentAlignment = Alignment.Center,
                            modifier = Modifier
                                .size(width = 34.dp, height = 30.dp)
                                .clickable {
                                    isTapePaused = !isTapePaused
                                    bridge.vibrate("tick")
                                    lastOutcome = if (isTapePaused) "TAPE PAUSED · ANALYZE SETUP" else "TAPE RESUMED"
                                }
                        ) {
                            Text(
                                text = if (isTapePaused) "▶" else "00",
                                color = if (isTapePaused) Color(0xFFFFC700) else Color.White,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                fontFamily = FontFamily.Monospace
                            )
                        }
                        Box(modifier = Modifier.width(1.dp).height(20.dp).background(Color(0xFF262D3B)))

                        // Button 2: Sound Toggle
                        Box(
                            contentAlignment = Alignment.Center,
                            modifier = Modifier
                                .size(width = 34.dp, height = 30.dp)
                                .clickable {
                                    com.setuprush.candy.engine.SoundEffectManager.isMuted = !com.setuprush.candy.engine.SoundEffectManager.isMuted
                                    bridge.vibrate("tick")
                                }
                        ) {
                            Text(
                                text = if (com.setuprush.candy.engine.SoundEffectManager.isMuted) "🔇" else "🔊",
                                fontSize = 11.sp
                            )
                        }
                        Box(modifier = Modifier.width(1.dp).height(20.dp).background(Color(0xFF262D3B)))

                        // Button 3: Indicators Toggle
                        Box(
                            contentAlignment = Alignment.Center,
                            modifier = Modifier
                                .size(width = 34.dp, height = 30.dp)
                                .clickable { showIndicatorDialog = true }
                        ) {
                            Text(
                                text = "📈",
                                fontSize = 11.sp
                            )
                        }
                        Box(modifier = Modifier.width(1.dp).height(20.dp).background(Color(0xFF262D3B)))

                        // Button 4: Desk / Menu Drawer
                        Box(
                            contentAlignment = Alignment.Center,
                            modifier = Modifier
                                .size(width = 34.dp, height = 30.dp)
                                .clickable {
                                    showMasterMenuHub = !showMasterMenuHub
                                    bridge.vibrate("tick")
                                }
                        ) {
                            Text(
                                text = "〉",
                                color = Color(0xFFAAAAAA),
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }
        }

        // =========================================================================
        // 3. FLOATING OUTCOME TOAST
        // =========================================================================
        lastOutcome?.let { msg ->
            Surface(
                color = Color(0xFF0E1117).copy(alpha = 0.94f),
                shape = ChamferShape(6.dp),
                border = BorderStroke(1.dp, Color(0xFF262D3B)),
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .navigationBarsPadding()
                    .padding(bottom = 60.dp)
            ) {
                Text(
                    text = msg,
                    color = if (msg.contains("+") || msg.contains("LONG") || msg.contains("VICTORY") || msg.contains("CASHED")) Color.White
                    else if (msg.contains("-") || msg.contains("LOSS") || msg.contains("STOPPED") || msg.contains("LIQUIDATED")) Color(0xFFFF4B3E)
                    else Color(0xFF8A99AD),
                    fontSize = 10.5.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace,
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 5.dp)
                )
            }
        }

        // =========================================================================
        // 4. SETUP RUSH DOCK: MICRO-HINT & TACTILE TRIGGERS (Zero-Deformation)
        // =========================================================================
        Row(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .navigationBarsPadding()
                .padding(start = 14.dp, end = 14.dp, bottom = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Tactile SHORT Trigger (Low-profile chamfered button)
            val isShortActive = isPlayerHolding && activeDirection == -1
            Surface(
                color = if (isShortActive) Color(0xFFFF4B3E) else Color(0xFF0E1117).copy(alpha = 0.85f),
                shape = ChamferShape(4.dp),
                border = BorderStroke(1.dp, if (isShortActive) Color(0xFFFF4B3E) else Color(0xFFFF4B3E).copy(alpha = 0.5f)),
                modifier = Modifier
                    .height(30.dp)
                    .pointerInput(currentMode) {
                        if (currentMode != GameMode.RUSH) {
                            awaitEachGesture {
                                awaitFirstDown(requireUnconsumed = false)
                                performShortEntry()
                                do {
                                    val event = awaitPointerEvent()
                                } while (event.changes.any { it.pressed })
                                performCashOut()
                            }
                        }
                    }
            ) {
                Box(contentAlignment = Alignment.Center, modifier = Modifier.padding(horizontal = 10.dp)) {
                    Text(
                        text = if (isShortActive) "SHORT RIDING" else "▼ SHORT",
                        color = if (isShortActive) Color.White else Color(0xFFFF8A80),
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Black,
                        fontFamily = FontFamily.Monospace
                    )
                }
            }

            // Centered Setup Rush Authentic Micro-Hint Pill
            Surface(
                color = Color(0xFF0E1117).copy(alpha = 0.90f),
                shape = ChamferShape(4.dp),
                border = BorderStroke(1.dp, Color(0xFF262D3B)),
                modifier = Modifier.weight(1f, fill = false)
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(5.dp)
                            .clip(CircleShape)
                            .background(if (isPlayerHolding) (if (activeDirection > 0) Color.White else Color(0xFFFF4B3E)) else Color(0xFFFF5A1F))
                    )
                    Text(
                        text = if (isPlayerHolding) {
                            "${if (activeDirection > 0) "Long" else "Short"} ${currentLeverage.toInt()}× · let go to cash out"
                        } else {
                            "Tap speeds up · hold to ride"
                        },
                        color = if (isPlayerHolding) Color.White else Color(0xFF8A99AD),
                        fontSize = 8.5.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace,
                        maxLines = 1
                    )
                    Box(
                        modifier = Modifier
                            .width(22.dp)
                            .height(2.5.dp)
                            .background(Color(0xFF1E2430))
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxHeight()
                                .fillMaxWidth(if (tapeSpeedMultiplier > 1f) 1f else if (tapeSpeedMultiplier > 0.5f) 0.5f else 0.25f)
                                .background(Color(0xFFFF5A1F))
                        )
                    }
                }
            }

            // Tactile LONG Trigger (Low-profile chamfered button)
            val isLongActive = isPlayerHolding && activeDirection == 1
            Surface(
                color = if (isLongActive) Color.White else Color(0xFF0E1117).copy(alpha = 0.85f),
                shape = ChamferShape(4.dp),
                border = BorderStroke(1.dp, if (isLongActive) Color.White else Color(0xFF8A99AD).copy(alpha = 0.5f)),
                modifier = Modifier
                    .height(30.dp)
                    .pointerInput(Unit) {
                        awaitEachGesture {
                            awaitFirstDown(requireUnconsumed = false)
                            performLongEntry()
                            do {
                                val event = awaitPointerEvent()
                            } while (event.changes.any { it.pressed })
                            performCashOut()
                        }
                    }
            ) {
                Box(contentAlignment = Alignment.Center, modifier = Modifier.padding(horizontal = 10.dp)) {
                    Text(
                        text = if (isLongActive) "LONG RIDING" else "▲ LONG",
                        color = if (isLongActive) Color.Black else Color.White,
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Black,
                        fontFamily = FontFamily.Monospace
                    )
                }
            }
        }

                // =========================================================================
        // 7. MODALS & DIALOGS OVERLAYS
        // =========================================================================
        if (showMasterMenuHub) {
            GameMenuHub(
                theme = theme,
                onLaunchGame = { formatId, sym, model ->
                    StrategyMasteryManager.activeMission = null
                    pendingFormatId = formatId
                    activeSymbol = sym
                    TradingModelManager.activeTradingModel = model
                    activeStrategyKey = model.setupKey
                    val isCompetitive = (formatId == "1v1" || formatId == "5v5" || formatId == "tournament")
                    currentMode = when (formatId) {
                        "daily" -> GameMode.DAILY
                        "rush" -> GameMode.RUSH
                        "blowup" -> GameMode.BLOWUP
                        "sniper" -> GameMode.SNIPER
                        "solo" -> GameMode.PRACTICE
                        else -> GameMode.PRO
                    }
                    val runSeed = if (formatId == "daily") {
                        (System.currentTimeMillis() / (24 * 60 * 60 * 1000L)) * 86400000L
                    } else {
                        System.currentTimeMillis()
                    }
                    engine = SetupEngine(seed = runSeed, symbol = sym, targetSetupKey = model.setupKey, exclusiveMode = (formatId == "solo"))
                    tapePosition = 10.0
                    isPlayerHolding = false
                    elapsedSeconds = 0.0
                    currentLeverage = if (formatId == "blowup") 2.0 else model.leverage
                    SetupExecutionTimingEngine.resetStats()
                    if (isCompetitive) {
                        OpponentDuelEngine.startMatch(formatId, PlayerProfileManager.currentProfile.mmrRating)
                        StrategyMasteryManager.resetEvaluation()
                    } else {
                        OpponentDuelEngine.endMatch()
                        StrategyMasteryManager.startEvaluation(model.setupKey)
                    }
                    showMasterMenuHub = false
                    showPreMatchShowcase = isCompetitive
                    lastOutcome = "COMMENCED ${formatId.uppercase()}: ${sym.id} | SETUP: ${model.setupKey.uppercase()}"
                },
                onOpenAcademyDrill = { setup, tier ->
                    StrategyMasteryManager.activeMission = null
                    activeStrategyKey = setup.key
                    engine = SetupEngine(seed = System.currentTimeMillis(), symbol = activeSymbol, targetSetupKey = setup.key, exclusiveMode = true)
                    tapePosition = 10.0
                    isPlayerHolding = false
                    SetupExecutionTimingEngine.resetStats()
                    OpponentDuelEngine.endMatch()
                    StrategyMasteryManager.startEvaluation(setup.key)
                    if (tier >= 4) {
                        currentLevel = DeskLevel.DESK_3
                    }
                    showMasterMenuHub = false
                    lastOutcome = "LAUNCHED TIER $tier TRIAL DRILL: ${setup.name.uppercase()}"
                },
                onLaunchMission = { mission ->
                    StrategyMasteryManager.activeMission = mission
                    activeStrategyKey = mission.targetSetupKey
                    val sym = MarketSymbolCatalog.findSymbol(mission.symbolId) ?: activeSymbol
                    activeSymbol = sym
                    currentLeverage = mission.defaultLeverage
                    engine = SetupEngine(seed = System.currentTimeMillis(), symbol = sym, targetSetupKey = mission.targetSetupKey, exclusiveMode = true)
                    tapePosition = 10.0
                    isPlayerHolding = false
                    SetupExecutionTimingEngine.resetStats()
                    OpponentDuelEngine.endMatch()
                    StrategyMasteryManager.startEvaluation(mission.targetSetupKey)
                    showMasterMenuHub = false
                    lastOutcome = "OPERATION LAUNCHED: ${mission.callsign} | CATCH ${mission.targetSetupKey.uppercase()}"
                },
                onClose = { showMasterMenuHub = false },
                modifier = Modifier
                    .fillMaxSize()
            )
        }

        // Dialogs & Modals Overlays
        if (showIndicatorDialog) {
            IndicatorPickerDialog(
                theme = theme,
                activeIds = activeIndicatorIds,
                onToggle = { id ->
                    activeIndicatorIds = if (activeIndicatorIds.contains(id)) {
                        activeIndicatorIds - id
                    } else {
                        if (activeIndicatorIds.size < 4) {
                            activeIndicatorIds + id
                        } else {
                            activeIndicatorIds.drop(1).toSet() + id
                        }
                    }
                    bridge.vibrate("tick")
                    lastOutcome = "TOGGLED: ${IndicatorCatalog.findIndicator(id)?.name?.uppercase() ?: id.uppercase()}"
                },
                onDismiss = { showIndicatorDialog = false }
            )
        }

        if (showModeStrategyDialog) {
            ModeStrategyPickerDialog(
                theme = theme,
                currentMode = currentMode,
                currentLevel = currentLevel,
                selectedStrategyKey = activeStrategyKey,
                onModeSelected = { mode ->
                    currentMode = mode
                    lastOutcome = "MODE: ${mode.displayName}"
                },
                onLevelSelected = { level ->
                    currentLevel = level
                    lastOutcome = "LEVEL: ${level.displayName}"
                },
                onStrategySelected = { stratKey ->
                    activeStrategyKey = stratKey
                    engine = SetupEngine(seed = System.currentTimeMillis(), symbol = activeSymbol)
                    tapePosition = 10.0
                    isPlayerHolding = false
                    SetupExecutionTimingEngine.resetStats()
                    lastOutcome = if (stratKey == null) "FILTER: ALL SETUPS" else "LOCKED: ${StrategyCatalog.findStrategy(stratKey)?.name}"
                },
                onDismiss = { showModeStrategyDialog = false }
            )
        }

        if (showTradingDeskLobbyDialog) {
            TradingDeskLobbyDialog(
                theme = theme,
                onDeployModel = { format, model, symbol ->
                    activeSymbol = symbol
                    MarketSymbolCatalog.selectSymbol(symbol.id)
                    TradingModelManager.selectModel(model.id)
                    activeStrategyKey = model.setupKey
                    playerTakesCount = 0
                    playerStopsCount = 0
                    engine = SetupEngine(seed = System.currentTimeMillis(), symbol = symbol)
                    tapePosition = 10.0
                    isPlayerHolding = false
                    SetupExecutionTimingEngine.resetStats()
                    OpponentDuelEngine.startMatch(format.id, PlayerProfileManager.currentProfile.mmrRating)
                    showTradingDeskLobbyDialog = false
                    showPreMatchShowcase = true
                    lastOutcome = "DEPLOYED ${format.badge}: ${symbol.id} | MODEL: ${model.name.uppercase()}"
                    bridge.vibrate("hold_long")
                },
                onDismiss = { showTradingDeskLobbyDialog = false }
            )
        }

        if (showMasteryTutorialDialog) {
            StrategyMasteryTutorialDialog(
                theme = theme,
                onLaunchMasteryDrill = { setup, tier ->
                    activeStrategyKey = setup.key
                    engine = SetupEngine(seed = System.currentTimeMillis(), symbol = activeSymbol)
                    tapePosition = 10.0
                    isPlayerHolding = false
                    SetupExecutionTimingEngine.resetStats()
                    if (tier >= 4) currentLevel = DeskLevel.DESK_3
                    showMasteryTutorialDialog = false
                    lastOutcome = "LAUNCHED TIER $tier TRIAL DRILL: ${setup.name.uppercase()}"
                    bridge.vibrate("hold_long")
                },
                onDismiss = { showMasteryTutorialDialog = false }
            )
        }

        if (showPostMatchDebriefDialog) {
            val debrief = MatchPerformanceEngine.lastDebrief ?: MatchPerformanceEngine.generateDebrief(
                modeId = if (OpponentDuelEngine.isCompetitiveActive) "1v1" else "solo",
                symbol = activeSymbol.id,
                playerTakes = max(1, playerTakesCount),
                playerStops = playerStopsCount,
                playerPnl = totalScore,
                playerBestStreak = max(1, streak),
                precisionScore = 0.84,
                tradeReviews = sessionTradeReviews.toList()
            )
            PostMatchDebriefDialog(
                theme = theme,
                debrief = debrief,
                onPlayAgain = {
                    engine = SetupEngine(seed = System.currentTimeMillis(), symbol = activeSymbol)
                    tapePosition = 10.0
                    playerTakesCount = 0
                    playerStopsCount = 0
                    sessionTradeReviews.clear()
                    if (OpponentDuelEngine.isCompetitiveActive) {
                        OpponentDuelEngine.startMatch("1v1", PlayerProfileManager.currentProfile.mmrRating)
                    }
                    showPostMatchDebriefDialog = false
                },
                onOpenLobby = {
                    showPostMatchDebriefDialog = false
                    showMasterMenuHub = true
                },
                onDismiss = { showPostMatchDebriefDialog = false },
                onReplayTape = {
                    showPostMatchDebriefDialog = false
                    tapePosition = kotlin.math.max(10.0, tapePosition - 35.0)
                    isTapePaused = true
                    tapeSpeedMultiplier = 0.5f
                    lastOutcome = "⏪ REPLAYING TAPE AT 0.5× · USE SPEED PILL TO SCRUB"
                }
            )
        }

        if (showPreMatchShowcase) {
            PreMatchShowcaseDialog(
                theme = theme,
                formatTitle = pendingFormatId.replace("_", " ").uppercase(),
                symbol = activeSymbol,
                model = TradingModelManager.activeTradingModel,
                onDeployMatch = {
                    showPreMatchShowcase = false
                }
            )
        }
    }
}

/**
 * Dedicated Tactical Hold-to-Ride Action Bar with Dual Triggers:
 * - Left Trigger: SELL / SHORT (Crimson/Ruby theme)
 * - Right Trigger: BUY / LONG (Emerald/Cyan theme)
 * 
 * Interaction:
 * - Touch & hold: Immediately opens trade and holds position!
 * - Real-time profit ticks inside the button itself and on chart!
 * - Release finger: Cashes out immediately and banks P&L!
 */
@Composable
fun TacticalHoldToRideBar(
    isPlayerHolding: Boolean,
    activeDirection: Int, // 1 = Long, -1 = Short
    livePnl: Double,
    currentLeverage: Double,
    canShort: Boolean = true,
    theme: com.setuprush.candy.theme.GameTheme,
    onStartLong: () -> Unit,
    onStartShort: () -> Unit,
    onRelease: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        color = Color(0xFF0A0A0B).copy(alpha = 0.88f),
        shape = RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp),
        border = BorderStroke(1.dp, Color(0xFF1E2838).copy(alpha = 0.6f)),
        modifier = modifier
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(5.dp)
        ) {
            // Setup Rush Authentic Tactical Micro-Hint Pill
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                modifier = Modifier
                    .align(Alignment.CenterHorizontally)
                    .background(Color(0xFF141923).copy(alpha = 0.85f), RoundedCornerShape(12.dp))
                    .padding(horizontal = 10.dp, vertical = 3.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(5.dp)
                        .clip(CircleShape)
                        .background(if (isPlayerHolding) (if (activeDirection > 0) Color(0xFF00E676) else Color(0xFFFF334B)) else Color(0xFFFF5A1F))
                )
                Text(
                    text = if (isPlayerHolding) {
                        "${if (activeDirection > 0) "Long" else "Short"} ${currentLeverage.toInt()}× · let go to cash out"
                    } else {
                        "Tap speeds up · hold to ride"
                    },
                    color = if (isPlayerHolding) Color(0xFFFFC700) else Color(0xFF8A99AD),
                    fontSize = 9.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace
                )
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // ==================== SELL / SHORT BUTTON ====================
                val isShortActive = isPlayerHolding && activeDirection == -1
                val isLongActive = isPlayerHolding && activeDirection == 1
                val shortAlpha = if (isLongActive) 0.35f else 1f
                val shortBorder = if (isShortActive) Color(0xFFFF334B) else Color(0xFFFF334B).copy(alpha = 0.7f)
                val shortBg = if (isShortActive) {
                    Brush.horizontalGradient(listOf(Color(0xFFFF1744), Color(0xFFFF6D00)))
                } else {
                    Brush.verticalGradient(listOf(Color(0xFF221116), Color(0xFF140A0D)))
                }

                Surface(
                    shape = RoundedCornerShape(12.dp),
                    border = BorderStroke(if (isShortActive) 2.dp else 1.5.dp, shortBorder),
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight()
                        .shadow(if (isShortActive) 10.dp else 4.dp, RoundedCornerShape(12.dp), spotColor = Color(0xFFFF334B))
                        .pointerInput(isLongActive, canShort) {
                            if (!isLongActive && canShort) {
                                awaitEachGesture {
                                    awaitFirstDown(requireUnconsumed = false)
                                    onStartShort()
                                    do {
                                        val event = awaitPointerEvent()
                                    } while (event.changes.any { it.pressed })
                                    onRelease()
                                }
                            }
                        }
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(shortBg)
                            .padding(horizontal = 8.dp, vertical = 4.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        if (isShortActive) {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.Center
                            ) {
                                val pnlSign = if (livePnl >= 0) "+$" else "-$"
                                val pnlText = "$pnlSign${String.format("%.2f", abs(livePnl))}"
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    Text(
                                        text = "⚡ SHORT ${currentLeverage.toInt()}×",
                                        color = Color.White,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Black,
                                        fontFamily = FontFamily.Monospace
                                    )
                                    Text(
                                        text = pnlText,
                                        color = Color.White,
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.Black,
                                        fontFamily = FontFamily.Monospace
                                    )
                                }
                                Text(
                                    text = "RELEASE TO CASH OUT",
                                    color = Color.White.copy(alpha = 0.85f),
                                    fontSize = 8.5.sp,
                                    fontWeight = FontWeight.Bold,
                                    fontFamily = FontFamily.Monospace
                                )
                            }
                        } else {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                modifier = Modifier.padding(horizontal = 6.dp)
                            ) {
                                Text("🔻", fontSize = 16.sp)
                                Column {
                                    Text(
                                        text = if (canShort) "SELL / SHORT" else "RUSH: LONG ONLY",
                                        color = Color(0xFFFF5268).copy(alpha = if (canShort) shortAlpha else 0.3f),
                                        fontSize = if (canShort) 13.sp else 10.sp,
                                        fontWeight = FontWeight.Black,
                                        fontFamily = FontFamily.Monospace
                                    )
                                    Text(
                                        text = if (canShort) "HOLD TO RIDE" else "SHORT LOCKED",
                                        color = Color(0xFFB57784).copy(alpha = if (canShort) shortAlpha else 0.3f),
                                        fontSize = 8.sp,
                                        fontWeight = FontWeight.Bold,
                                        fontFamily = FontFamily.Monospace
                                    )
                                }
                            }
                        }
                    }
                }

                // ==================== BUY / LONG BUTTON ====================
                val longBorder = if (isLongActive) Color(0xFF00E676) else Color(0xFF00E676).copy(alpha = 0.7f)
                val longBg = if (isLongActive) {
                    Brush.horizontalGradient(listOf(Color(0xFF00C853), Color(0xFF00E5FF)))
                } else {
                    Brush.verticalGradient(listOf(Color(0xFF0A2216), Color(0xFF06140D)))
                }
                val longAlpha = if (isShortActive) 0.35f else 1f

                Surface(
                    shape = RoundedCornerShape(12.dp),
                    border = BorderStroke(if (isLongActive) 2.dp else 1.5.dp, longBorder),
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight()
                        .shadow(if (isLongActive) 10.dp else 4.dp, RoundedCornerShape(12.dp), spotColor = Color(0xFF00E676))
                        .pointerInput(isShortActive) {
                            if (!isShortActive) {
                                awaitEachGesture {
                                    awaitFirstDown(requireUnconsumed = false)
                                    onStartLong()
                                    do {
                                        val event = awaitPointerEvent()
                                    } while (event.changes.any { it.pressed })
                                    onRelease()
                                }
                            }
                        }
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(longBg)
                            .padding(horizontal = 8.dp, vertical = 4.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        if (isLongActive) {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.Center
                            ) {
                                val pnlSign = if (livePnl >= 0) "+$" else "-$"
                                val pnlText = "$pnlSign${String.format("%.2f", abs(livePnl))}"
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    Text(
                                        text = "⚡ LONG ${currentLeverage.toInt()}×",
                                        color = Color.Black,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Black,
                                        fontFamily = FontFamily.Monospace
                                    )
                                    Text(
                                        text = pnlText,
                                        color = Color.Black,
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.Black,
                                        fontFamily = FontFamily.Monospace
                                    )
                                }
                                Text(
                                    text = "RELEASE TO CASH OUT",
                                    color = Color.Black.copy(alpha = 0.8f),
                                    fontSize = 8.5.sp,
                                    fontWeight = FontWeight.Bold,
                                    fontFamily = FontFamily.Monospace
                                )
                            }
                        } else {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                modifier = Modifier.padding(horizontal = 6.dp)
                            ) {
                                Text("▲", fontSize = 16.sp, color = Color(0xFF00E676))
                                Column {
                                    Text(
                                        text = "BUY / LONG",
                                        color = Color(0xFF00E676).copy(alpha = longAlpha),
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.Black,
                                        fontFamily = FontFamily.Monospace
                                    )
                                    Text(
                                        text = "HOLD TO RIDE",
                                        color = Color(0xFF6EAE8C).copy(alpha = longAlpha),
                                        fontSize = 8.sp,
                                        fontWeight = FontWeight.Bold,
                                        fontFamily = FontFamily.Monospace
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
