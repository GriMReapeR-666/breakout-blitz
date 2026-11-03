package com.setuprush.candy

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

    // Granular Indicator & Signal States
    var showIndicatorDialog by remember { mutableStateOf(false) }
    var activeIndicatorIds by remember { mutableStateOf(setOf("supertrend", "structure", "fvg", "emacross")) }
    val supertrendEnabled = activeIndicatorIds.contains("supertrend")
    val structureEnabled = activeIndicatorIds.contains("structure")
    val fvgEnabled = activeIndicatorIds.contains("fvg")
    val emaCrossEnabled = activeIndicatorIds.contains("emacross")
    val patternLockEnabled = true

    // Master Dialogs State (Defaults to true to show CODM-style Tactical Menu Hub on start)
    var showMasterMenuHub by remember { mutableStateOf(true) }
    var showPreMatchShowcase by remember { mutableStateOf(false) }
    var pendingFormatId by remember { mutableStateOf("deathmatch_1v1") }
    var showModeStrategyDialog by remember { mutableStateOf(false) }
    var showTradingDeskLobbyDialog by remember { mutableStateOf(false) }
    var showMasteryTutorialDialog by remember { mutableStateOf(false) }
    var showPostMatchDebriefDialog by remember { mutableStateOf(false) }

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
        } else {
            livePnl = 0.0
        }
    }

    // Continuous 60/120 Hz Physics & Camera Loop (Setup Rush mt & Ot parity)
    LaunchedEffect(engine, currentLevel, isPaused) {
        var prevNanos = 0L
        while (!isPaused) {
            withFrameNanos { nowNanos ->
                if (prevNanos != 0L) {
                    val dt = ((nowNanos - prevNanos) / 1_000_000_000.0).coerceIn(0.001, 0.05)
                    elapsedSeconds += dt

                    // Leverage ramping physics (J.lev = min(20, 2 + elapsed * 0.09))
                    currentLeverage = min(20.0, 2.0 + elapsedSeconds * 0.09)

                    // Continuous tape progression
                    val baseSpeed = 1.1 + min(2.2, elapsedSeconds * 0.012)
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

                    // Check live setup progression & missed execution detection
                    if (engine.candles.isNotEmpty()) {
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
                                activeStrategyKey?.let {
                                    StrategyMasteryManager.recordTrade(it, isWin = false)
                                }
                                val mission = StrategyMasteryManager.activeMission
                                if (mission != null) {
                                    lastOutcome = "MISSION ${mission.levelId} FAILED: STOPPED OUT · RETRY"
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

    Scaffold(
        containerColor = theme.bg,
        topBar = {
            // Clean, Institutional Setup Rush Header HUD
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 14.dp, vertical = 6.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Top-Left Chamfered HUD Card (Balance, Streak, Win-Rate)
                Surface(
                    color = theme.bg.copy(alpha = 0.95f),
                    shape = RoundedCornerShape(8.dp),
                    border = BorderStroke(1.dp, theme.bull.copy(alpha = 0.35f)),
                    modifier = Modifier.padding(vertical = 2.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
                    ) {
                        Text(
                            text = "CAREER BANK",
                            color = theme.muted,
                            fontSize = 8.5.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace,
                            letterSpacing = 1.sp
                        )
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Text(
                                text = "$${String.format("%,.2f", totalScore)}",
                                color = if (totalScore >= 10000.0) theme.bull else theme.bear,
                                fontSize = 17.sp,
                                fontWeight = FontWeight.Black,
                                fontFamily = FontFamily.Monospace
                            )
                            if (streak > 1) {
                                Surface(
                                    color = Color(0xFFFF5A1F),
                                    shape = RoundedCornerShape(4.dp)
                                ) {
                                    Text(
                                        text = "×$streak 🔥",
                                        color = Color.White,
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Black,
                                        fontFamily = FontFamily.Monospace,
                                        modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
                                    )
                                }
                            }
                        }
                        Text(
                            text = "LEV ${currentLeverage.toInt()}× · ${playerTakesCount + playerStopsCount} RIDES (${if (playerTakesCount + playerStopsCount > 0) ((playerTakesCount.toDouble() / (playerTakesCount + playerStopsCount)) * 100).toInt() else 0}% WIN)",
                            color = theme.muted,
                            fontSize = 8.sp,
                            fontWeight = FontWeight.SemiBold,
                            fontFamily = FontFamily.Monospace
                        )
                    }
                }

                // Top-Center Market Symbol & Session Killzone Badge
                Surface(
                    color = theme.bg.copy(alpha = 0.90f),
                    shape = RoundedCornerShape(20.dp),
                    border = BorderStroke(1.dp, theme.grid.copy(alpha = 0.35f))
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(5.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(6.dp)
                                .clip(CircleShape)
                                .background(theme.bull)
                        )
                        Text(
                            text = activeSymbol.id,
                            color = theme.text,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Black,
                            fontFamily = FontFamily.Monospace
                        )
                        Text(
                            text = "· ${activeSymbol.badge}",
                            color = theme.muted,
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace
                        )
                    }
                }

                // Top-Right Quick Controls (Timer, Theme, Indicators, Menu)
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    // Match Timer Pill
                    Surface(
                        color = theme.bg.copy(alpha = 0.92f),
                        shape = RoundedCornerShape(6.dp),
                        border = BorderStroke(1.dp, theme.grid.copy(alpha = 0.25f))
                    ) {
                        val minutes = (elapsedSeconds / 60).toInt()
                        val seconds = (elapsedSeconds % 60).toInt()
                        Text(
                            text = String.format("%02d:%02d", minutes, seconds),
                            color = theme.text,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Black,
                            fontFamily = FontFamily.Monospace,
                            modifier = Modifier.padding(horizontal = 7.dp, vertical = 4.dp)
                        )
                    }

                    // Quick Theme Cycle
                    IconButton(
                        onClick = {
                            val next = ThemeManager.cycleNext()
                            bridge.vibrate("tick")
                            lastOutcome = "THEME: ${next.name.uppercase()}"
                        },
                        modifier = Modifier.size(32.dp)
                    ) {
                        Text("🎨", fontSize = 14.sp)
                    }

                    // Quick Indicators Toggle
                    Surface(
                        color = theme.grid.copy(alpha = 0.2f),
                        shape = RoundedCornerShape(6.dp),
                        modifier = Modifier.clickable {
                            showIndicatorDialog = true
                            bridge.vibrate("tick")
                        }
                    ) {
                        Text(
                            text = "⚡$activeIndCount/4",
                            color = theme.text,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 4.dp)
                        )
                    }

                    // Tactical HQ Menu / Return Button
                    Surface(
                        color = if (showMasterMenuHub) Color(0xFF1E2838) else Color(0xFFFFC700).copy(alpha = 0.18f),
                        shape = RoundedCornerShape(8.dp),
                        border = BorderStroke(1.dp, if (showMasterMenuHub) Color(0xFF384556) else Color(0xFFFFC700)),
                        modifier = Modifier.clickable {
                            showMasterMenuHub = !showMasterMenuHub
                            bridge.vibrate("tick")
                        }
                    ) {
                        Text(
                            text = if (showMasterMenuHub) "▶ GAME" else "◀ HQ MENU",
                            color = if (showMasterMenuHub) Color.White else Color(0xFFFFC700),
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Black,
                            fontFamily = FontFamily.Monospace,
                            modifier = Modifier.padding(horizontal = 9.dp, vertical = 5.dp)
                        )
                    }
                }
            }
        }
    ) { innerPadding ->
        val performLongEntry = {
            if (!isPlayerHolding) {
                val startP = SetupEngine.priceAt(engine.candles, tapePosition)
                isPlayerHolding = true
                activeDirection = 1
                tradeEntryPrice = startP
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
                isPlayerHolding = false
                totalScore += pnl
                if (pnl > 0) {
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
                            precisionScore = 0.85
                        )
                        showPostMatchDebriefDialog = true
                    }
                }
            }
        }

        if (showMasterMenuHub) {
            GameMenuHub(
                theme = theme,
                onLaunchGame = { formatId, sym, model ->
                    StrategyMasteryManager.activeMission = null
                    pendingFormatId = formatId
                    activeSymbol = sym
                    TradingModelManager.activeTradingModel = model
                    activeStrategyKey = model.setupKey
                    engine = SetupEngine(seed = System.currentTimeMillis(), symbol = sym)
                    engine.appendSetup(model.setupKey, atBar = 16)
                    tapePosition = 10.0
                    isPlayerHolding = false
                    SetupExecutionTimingEngine.resetStats()
                    OpponentDuelEngine.startMatch(formatId, PlayerProfileManager.currentProfile.mmrRating)
                    showMasterMenuHub = false
                    showPreMatchShowcase = true
                    lastOutcome = "COMMENCED ${formatId.uppercase()}: ${sym.id}"
                },
                onOpenAcademyDrill = { setup, tier ->
                    StrategyMasteryManager.activeMission = null
                    activeStrategyKey = setup.key
                    engine = SetupEngine(seed = System.currentTimeMillis(), symbol = activeSymbol)
                    engine.appendSetup(setup.key, atBar = 16)
                    tapePosition = 10.0
                    isPlayerHolding = false
                    SetupExecutionTimingEngine.resetStats()
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
                    engine = SetupEngine(seed = System.currentTimeMillis(), symbol = sym)
                    engine.appendSetup(mission.targetSetupKey, atBar = 16)
                    tapePosition = 10.0
                    isPlayerHolding = false
                    SetupExecutionTimingEngine.resetStats()
                    showMasterMenuHub = false
                    lastOutcome = "OPERATION LAUNCHED: ${mission.callsign} | CATCH ${mission.targetSetupKey.uppercase()}"
                },
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
            )
        } else {
            // Full Tactical Combat Screen: Chart Stage + Dedicated Bottom Action Bar
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
            ) {
                // 1. Chart Stage (Takes full available vertical space above action bar)
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                ) {
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

                    // 1b. Tactical Solo Campaign Mission Objective HUD Banner
                    val currentMission = StrategyMasteryManager.activeMission
                    if (currentMission != null) {
                        Surface(
                            color = Color(0xFF131924).copy(alpha = 0.95f),
                            shape = RoundedCornerShape(8.dp),
                            border = BorderStroke(1.2.dp, Color(0xFFFFC700)),
                            modifier = Modifier
                                .align(Alignment.TopCenter)
                                .padding(top = 4.dp, start = 14.dp, end = 14.dp)
                                .shadow(6.dp, RoundedCornerShape(8.dp), spotColor = Color(0xFFFFC700))
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 5.dp),
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text("🎯", fontSize = 13.sp)
                                Column {
                                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                        Text(
                                            text = "OP ${currentMission.levelId}: ${currentMission.callsign}",
                                            color = Color(0xFFFFC700),
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Black,
                                            fontFamily = FontFamily.Monospace
                                        )
                                        Text(
                                            text = "· ${currentMission.targetBias} [${currentMission.targetSetupKey.uppercase()}]",
                                            color = Color(0xFF00E5FF),
                                            fontSize = 10.sp,
                                            fontWeight = FontWeight.Bold,
                                            fontFamily = FontFamily.Monospace
                                        )
                                    }
                                    Text(
                                        text = "${currentMission.briefing} · CATCH MOVE TO REACH +2R TAKE PROFIT",
                                        color = Color(0xFF8A99AD),
                                        fontSize = 8.5.sp,
                                        fontFamily = FontFamily.Monospace
                                    )
                                }
                            }
                        }
                    }

                    // 2. Mid-Upper Pattern Locked Banner (Setup Rush signature banner)
                    AnimatedVisibility(
                        visible = activeSetup != null,
                        enter = fadeIn(animationSpec = tween(220)) + scaleIn(initialScale = 0.92f),
                        exit = fadeOut(animationSpec = tween(180)),
                        modifier = Modifier
                            .align(Alignment.TopCenter)
                            .padding(top = if (currentMission != null) 42.dp else 10.dp)
                    ) {
                        activeSetup?.let { setup ->
                            val isBull = setup.dir == 1
                            val badgeColor = if (isBull) theme.bull else theme.bear

                            Surface(
                                color = theme.bg.copy(alpha = 0.94f),
                                shape = RoundedCornerShape(12.dp),
                                border = BorderStroke(1.5.dp, badgeColor.copy(alpha = 0.75f)),
                                modifier = Modifier
                                    .padding(horizontal = 16.dp)
                                    .shadow(8.dp, RoundedCornerShape(12.dp), ambientColor = badgeColor, spotColor = badgeColor)
                            ) {
                                Column(
                                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    verticalArrangement = Arrangement.spacedBy(3.dp)
                                ) {
                                    Surface(
                                        color = badgeColor,
                                        shape = RoundedCornerShape(4.dp)
                                    ) {
                                        Text(
                                            text = "⚡ PATTERN LOCKED · ×${max(1, streak)} STREAK",
                                            color = if (theme.isLight) Color.White else Color.Black,
                                            fontSize = 9.sp,
                                            fontWeight = FontWeight.Black,
                                            fontFamily = FontFamily.Monospace,
                                            letterSpacing = 1.sp,
                                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 1.5.dp)
                                        )
                                    }
                                    Text(
                                        text = setup.name.uppercase(),
                                        color = theme.text,
                                        fontSize = 15.sp,
                                        fontWeight = FontWeight.Black,
                                        fontFamily = FontFamily.Monospace
                                    )
                                    Row(
                                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            text = if (isBull) "BULLISH ▲" else "BEARISH ▼",
                                            color = badgeColor,
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Black,
                                            fontFamily = FontFamily.Monospace
                                        )
                                        Text(
                                            text = if (isBull) "HOLD BUY TO RIDE" else "HOLD SELL TO SHORT",
                                            color = theme.text,
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Bold,
                                            fontFamily = FontFamily.Monospace
                                        )
                                        Text(
                                            text = "${(setup.winRate * 100).toInt()}% WIN",
                                            color = theme.muted,
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.SemiBold,
                                            fontFamily = FontFamily.Monospace
                                        )
                                    }
                                }
                            }
                        }
                    }

                    // 3. Competitive Opponent Radar (if duel is active)
                    if (OpponentDuelEngine.isCompetitiveActive) {
                        OpponentRadarHUD(
                            theme = theme,
                            modifier = Modifier
                                .align(Alignment.TopEnd)
                                .padding(top = 70.dp, end = 12.dp)
                        )
                    }

                    // 4. Floating Outcome Toast Message
                    lastOutcome?.let { msg ->
                        Surface(
                            color = theme.bg.copy(alpha = 0.94f),
                            shape = RoundedCornerShape(20.dp),
                            border = BorderStroke(1.dp, theme.grid.copy(alpha = 0.35f)),
                            modifier = Modifier
                                .align(Alignment.BottomCenter)
                                .padding(bottom = 12.dp)
                        ) {
                            Text(
                                text = msg,
                                color = if (msg.contains("+") || msg.contains("LONG") || msg.contains("VICTORY") || msg.contains("CASHED")) theme.bull
                                else if (msg.contains("-") || msg.contains("LOSS") || msg.contains("STOPPED") || msg.contains("LIQUIDATED")) theme.bear
                                else theme.text,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                fontFamily = FontFamily.Monospace,
                                modifier = Modifier.padding(horizontal = 14.dp, vertical = 5.dp)
                            )
                        }
                    }
                }

                // 2. Tactical Dedicated Hold-to-Ride Action Bar (BUY & SELL Buttons with zero overlap)
                TacticalHoldToRideBar(
                    isPlayerHolding = isPlayerHolding,
                    activeDirection = activeDirection,
                    livePnl = livePnl,
                    currentLeverage = currentLeverage,
                    theme = theme,
                    onStartLong = performLongEntry,
                    onStartShort = performShortEntry,
                    onRelease = performCashOut,
                    modifier = Modifier.fillMaxWidth()
                )
            }
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
                precisionScore = 0.84
            )
            PostMatchDebriefDialog(
                theme = theme,
                debrief = debrief,
                onPlayAgain = {
                    engine = SetupEngine(seed = System.currentTimeMillis(), symbol = activeSymbol)
                    tapePosition = 10.0
                    playerTakesCount = 0
                    playerStopsCount = 0
                    if (OpponentDuelEngine.isCompetitiveActive) {
                        OpponentDuelEngine.startMatch("1v1", PlayerProfileManager.currentProfile.mmrRating)
                    }
                    showPostMatchDebriefDialog = false
                },
                onOpenLobby = {
                    showPostMatchDebriefDialog = false
                    showTradingDeskLobbyDialog = true
                },
                onDismiss = { showPostMatchDebriefDialog = false }
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
    theme: com.setuprush.candy.theme.GameTheme,
    onStartLong: () -> Unit,
    onStartShort: () -> Unit,
    onRelease: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        color = Color(0xFF090D14),
        border = BorderStroke(1.dp, Color(0xFF1B2433)),
        modifier = modifier
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(5.dp)
        ) {
            // Micro hint text
            Text(
                text = if (isPlayerHolding) "HOLDING POSITION · RELEASE BUTTON TO CASH OUT" else "PRESS & HOLD BUTTON TO RIDE · RELEASE TO CASH OUT",
                color = if (isPlayerHolding) Color(0xFFFFC700) else Color(0xFF8A99AD),
                fontSize = 8.5.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = FontFamily.Monospace,
                modifier = Modifier.align(Alignment.CenterHorizontally)
            )

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
                        .pointerInput(isLongActive) {
                            if (!isLongActive) {
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
                                        text = "SELL / SHORT",
                                        color = Color(0xFFFF5268).copy(alpha = shortAlpha),
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.Black,
                                        fontFamily = FontFamily.Monospace
                                    )
                                    Text(
                                        text = "HOLD TO RIDE",
                                        color = Color(0xFFB57784).copy(alpha = shortAlpha),
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
