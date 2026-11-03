package com.setuprush.candy

import android.content.Intent
import android.content.res.Configuration
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
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
 * Runs SetupEngine directly on Android runtime at 60/120 Hz ultra-fluid hardware acceleration
 * with full dynamic theming, multi-asset markets (BTC, ETH, SOL, SUI, GOLD, DOGE),
 * pre-trade trading model desk lobby, 5-tier strategy mastery academy,
 * live 1v1 head-to-head prop duel opponent radar,
 * and post-match performance debrief with trading performance metrics (profit factor, volume, precision, challenges).
 */
class NativeGameActivity : ComponentActivity() {

    private lateinit var bridge: NativeBridge

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        bridge = NativeBridge(this)
        com.setuprush.candy.engine.SoundEffectManager.initialize(this)
        com.setuprush.candy.engine.SoundEffectManager.initialize(this)

        setContent {
            val theme by ThemeManager.currentTheme
            
            // Sync status bar color with active theme background
            DisposableEffect(theme) {
                window.statusBarColor = android.graphics.Color.argb(
                    (theme.bg.alpha * 255).toInt(),
                    (theme.bg.red * 255).toInt(),
                    (theme.bg.green * 255).toInt(),
                    (theme.bg.blue * 255).toInt()
                )
                window.navigationBarColor = window.statusBarColor
                onDispose {}
            }

            NativeGameScreen(
                bridge = bridge
            )
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
    var activeSymbol by remember { mutableStateOf(MarketSymbolCatalog.activeSymbol) }
    var engine by remember { mutableStateOf(SetupEngine(seed = System.currentTimeMillis(), symbol = activeSymbol)) }
    var tapePosition by remember { mutableDoubleStateOf(10.0) }
    var totalScore by remember { mutableDoubleStateOf(0.0) }
    var streak by remember { mutableIntStateOf(0) }
    var playerTakesCount by remember { mutableIntStateOf(0) }
    var playerStopsCount by remember { mutableIntStateOf(0) }
    var isPlayerHolding by remember { mutableStateOf(false) }
    var lastOutcome by remember { mutableStateOf<String?>(null) }
    
    // Granular Indicator & Signal States (Parity with Candle Vision LIBRARY & MAX_IND: 4)
    var showIndicatorDialog by remember { mutableStateOf(false) }
    var activeIndicatorIds by remember { mutableStateOf(setOf("supertrend", "structure", "fvg", "emacross")) }
    val supertrendEnabled = activeIndicatorIds.contains("supertrend")
    val structureEnabled = activeIndicatorIds.contains("structure")
    val fvgEnabled = activeIndicatorIds.contains("fvg")
    val emaCrossEnabled = activeIndicatorIds.contains("emacross")
    val patternLockEnabled = true

    // Orientation & AAA Master Menu Hub State
    val configuration = LocalConfiguration.current
    val isLandscape = configuration.orientation == Configuration.ORIENTATION_LANDSCAPE
    var showMasterMenuHub by remember { mutableStateOf(false) }
    var showPreMatchShowcase by remember { mutableStateOf(false) }
    var pendingFormatId by remember { mutableStateOf("deathmatch_1v1") }

    // Dialogs State
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

    // Smooth continuous tape progression with dynamic speed scaling
    LaunchedEffect(engine, currentLevel) {
        var prevNanos = 0L
        while (true) {
            withFrameNanos { nowNanos ->
                if (prevNanos != 0L) {
                    val dt = (nowNanos - prevNanos) / 1_000_000_000.0
                    // Move tape forward with speed scaling based on Desk level
                    tapePosition += dt * 0.85 * currentLevel.speedMul
                    
                    // Generate new candles ahead of time as tape moves
                    val currentIdx = tapePosition.toInt()
                    if (currentIdx + 15 > engine.candles.size) {
                        engine.ensure(currentIdx, 20, activeStrategyKey)
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

                        // Update live rival trader tick actions if competitive match is active
                        if (OpponentDuelEngine.isCompetitiveActive) {
                            OpponentDuelEngine.updateTick(curPrice, currentIdx, activeSetup)
                        }
                    }
                }
                prevNanos = nowNanos
            }
        }
    }

    Scaffold(
        containerColor = theme.bg,
        topBar = {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 14.dp, vertical = 6.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Surface(
                            shape = RoundedCornerShape(4.dp),
                            color = theme.bull.copy(alpha = 0.2f)
                        ) {
                            Text(
                                text = activeSymbol.id,
                                color = theme.bull,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Black,
                                modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
                            )
                        }
                        Text(
                            text = "BANK: $${String.format("%.2f", totalScore)}",
                            color = theme.bull,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Black
                        )
                    }
                    Text(
                        text = "STREAK: $streak 🔥 · TP: $playerTakesCount · SL: $playerStopsCount",
                        color = theme.text,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }

                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    // Pre-Trade Trading Desk Lobby Button
                    Button(
                        onClick = {
                            showTradingDeskLobbyDialog = true
                            bridge.vibrate("tick")
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = theme.bull.copy(alpha = 0.28f)),
                        shape = RoundedCornerShape(8.dp),
                        contentPadding = PaddingValues(horizontal = 7.dp, vertical = 3.dp)
                    ) {
                        Text(
                            text = "📊 DESK LOBBY",
                            color = theme.text,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Black
                        )
                    }

                    // Post-Match Performance Metrics & Debrief Button
                    Button(
                        onClick = {
                            MatchPerformanceEngine.generateDebrief(
                                modeId = if (OpponentDuelEngine.isCompetitiveActive) "1v1" else "solo",
                                symbol = activeSymbol.id,
                                playerTakes = max(1, playerTakesCount),
                                playerStops = playerStopsCount,
                                playerPnl = totalScore,
                                playerBestStreak = max(1, streak),
                                precisionScore = 0.84
                            )
                            showPostMatchDebriefDialog = true
                            bridge.vibrate("tick")
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = theme.bull.copy(alpha = 0.20f)),
                        shape = RoundedCornerShape(8.dp),
                        contentPadding = PaddingValues(horizontal = 7.dp, vertical = 3.dp)
                    ) {
                        Text(
                            text = "📈 DEBRIEF",
                            color = theme.bull,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Black
                        )
                    }

                    // Strategy Mastery Academy & Tutorials Button
                    Button(
                        onClick = {
                            showMasteryTutorialDialog = true
                            bridge.vibrate("tick")
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = theme.grid.copy(alpha = 0.20f)),
                        shape = RoundedCornerShape(8.dp),
                        contentPadding = PaddingValues(horizontal = 7.dp, vertical = 3.dp)
                    ) {
                        Text(
                            text = "🎓 ACADEMY",
                            color = theme.text,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    // Game Mode & Strategy Selector Button
                    Button(
                        onClick = {
                            showModeStrategyDialog = true
                            bridge.vibrate("tick")
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (activeStrategyKey != null) theme.bull.copy(alpha = 0.25f) else theme.grid.copy(alpha = 0.15f)
                        ),
                        shape = RoundedCornerShape(8.dp),
                        contentPadding = PaddingValues(horizontal = 7.dp, vertical = 3.dp)
                    ) {
                        val stratShort = activeStrategyKey?.let {
                            StrategyCatalog.findStrategy(it)?.name?.split(" ")?.firstOrNull() ?: it
                        } ?: "ALL"
                        Text(
                            text = "🎯 ${currentMode.displayName} · $stratShort",
                            color = theme.text,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    // Indicator & Signals Button
                    Button(
                        onClick = {
                            showIndicatorDialog = true
                            bridge.vibrate("tick")
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (activeIndCount > 0) theme.bull.copy(alpha = 0.25f) else theme.grid.copy(alpha = 0.15f)
                        ),
                        shape = RoundedCornerShape(8.dp),
                        contentPadding = PaddingValues(horizontal = 7.dp, vertical = 3.dp)
                    ) {
                        Text(
                            text = "IND: $activeIndCount/4 ⚡",
                            color = theme.text,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    // Quick Theme Switcher Button
                    Button(
                        onClick = {
                            val next = ThemeManager.cycleNext()
                            bridge.vibrate("tick")
                            lastOutcome = "THEME: ${next.name.uppercase()}"
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = theme.grid.copy(alpha = 0.25f)),
                        shape = RoundedCornerShape(8.dp),
                        contentPadding = PaddingValues(horizontal = 7.dp, vertical = 3.dp)
                    ) {
                        Text(
                            text = "🎨 ${theme.name.uppercase()}",
                            color = theme.text,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Button(
                        onClick = {
                            engine = SetupEngine(seed = System.currentTimeMillis(), symbol = activeSymbol)
                            tapePosition = 10.0
                            isPlayerHolding = false
                            SetupExecutionTimingEngine.resetStats()
                            playerTakesCount = 0
                            playerStopsCount = 0
                            OpponentDuelEngine.endMatch()
                            lastOutcome = "RESET: NEW TAPE GENERATED"
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = theme.grid.copy(alpha = 0.15f)),
                        shape = RoundedCornerShape(8.dp),
                        contentPadding = PaddingValues(horizontal = 7.dp, vertical = 3.dp)
                    ) {
                        Text("RESET", color = theme.text, fontSize = 10.sp)
                    }

                    Button(
                        onClick = { showPostMatchDebriefDialog = true },
                        colors = ButtonDefaults.buttonColors(containerColor = theme.bull.copy(alpha = 0.18f)),
                        shape = RoundedCornerShape(8.dp),
                        contentPadding = PaddingValues(horizontal = 7.dp, vertical = 3.dp)
                    ) {
                        Text("📊 STATS", color = theme.bull, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                    }

                    Button(
                        onClick = { showMasterMenuHub = !showMasterMenuHub },
                        colors = ButtonDefaults.buttonColors(containerColor = theme.bull.copy(alpha = 0.22f)),
                        shape = RoundedCornerShape(8.dp),
                        contentPadding = PaddingValues(horizontal = 7.dp, vertical = 3.dp)
                    ) {
                        Text(
                            text = if (showMasterMenuHub) "BACK" else "⚔️ MENU",
                            color = theme.bull,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Black
                        )
                    }
                }
            }
        }
    ) { innerPadding ->
        if (showMasterMenuHub) {
            GameMenuHub(
                theme = theme,
                onLaunchGame = { formatId, sym, model ->
                    pendingFormatId = formatId
                    activeSymbol = sym
                    TradingModelManager.activeTradingModel = model
                    engine = SetupEngine(seed = System.currentTimeMillis(), symbol = sym)
                    val s = StrategyCatalog.findSetup(model.setupKey)
                    if (s != null) {
                        val isBear = s.bias == "BEARISH"
                        val base = sym.basePrice
                        val riskAmt = base * 0.025
                        engine.setups.add(
                            SetupRecord(
                                key = s.key,
                                name = s.name,
                                dir = if (isBear) -1 else 1,
                                at = 15,
                                entry = base,
                                stop = if (isBear) base + riskAmt else base - riskAmt,
                                risk = riskAmt,
                                winRate = s.winRate
                            )
                        )
                    }
                    tapePosition = 10.0
                    isPlayerHolding = false
                    SetupExecutionTimingEngine.resetStats()
                    OpponentDuelEngine.startMatch(formatId, PlayerProfileManager.currentProfile.mmrRating)
                    showMasterMenuHub = false
                    showPreMatchShowcase = true
                    lastOutcome = "COMMENCED ${formatId.uppercase()}: ${sym.id}"
                },
                onOpenAcademyDrill = { setup, tier ->
                    activeStrategyKey = setup.key
                    engine = SetupEngine(seed = System.currentTimeMillis(), symbol = activeSymbol)
                    val isBear = setup.bias == "BEARISH"
                    val base = activeSymbol.basePrice
                    val riskAmt = base * 0.025
                    engine.setups.add(
                        SetupRecord(
                            key = setup.key,
                            name = setup.name,
                            dir = if (isBear) -1 else 1,
                            at = 15,
                            entry = base,
                            stop = if (isBear) base + riskAmt else base - riskAmt,
                            risk = riskAmt,
                            winRate = setup.winRate
                        )
                    )
                    tapePosition = 10.0
                    isPlayerHolding = false
                    SetupExecutionTimingEngine.resetStats()
                    if (tier >= 4) {
                        currentLevel = DeskLevel.DESK_3
                    }
                    showMasterMenuHub = false
                    lastOutcome = "LAUNCHED TIER $tier TRIAL DRILL: ${setup.name.uppercase()}"
                },
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
            )
        } else if (isLandscape) {
            // ==================== LANDSCAPE ULTRAWIDE COCKPIT ====================
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
            ) {
                // Left Flank (Tactical Side HUD)
                Surface(
                    color = theme.bg.copy(alpha = 0.95f),
                    border = BorderStroke(1.dp, theme.grid.copy(alpha = 0.25f)),
                    modifier = Modifier
                        .width(170.dp)
                        .fillMaxHeight()
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(8.dp),
                        verticalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            Text("TACTICAL HUD", color = theme.bull, fontSize = 11.sp, fontWeight = FontWeight.Black)
                            Text("${activeSymbol.id} (${activeSymbol.badge})", color = theme.text, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                            Text("LEV: ${TradingModelManager.activeTradingModel.leverage.toInt()}x", color = theme.muted, fontSize = 9.sp)

                            if (OpponentDuelEngine.isCompetitiveActive) {
                                OpponentRadarHUD(theme = theme)
                            } else {
                                Surface(shape = RoundedCornerShape(6.dp), color = theme.grid.copy(alpha = 0.1f), modifier = Modifier.fillMaxWidth()) {
                                    Column(modifier = Modifier.padding(6.dp)) {
                                        Text("MODEL:", color = theme.muted, fontSize = 8.sp)
                                        Text(TradingModelManager.activeTradingModel.name, color = theme.text, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                                    }
                                }
                            }
                        }

                        Button(
                            onClick = {
                                isPlayerHolding = true
                                bridge.vibrate("slide_short")
                                val fb = SetupExecutionTimingEngine.evaluateEntry(
                                    activeSetup = engine.setups.lastOrNull(),
                                    tapePosition = tapePosition,
                                    isLong = false
                                )
                                lastOutcome = "${fb.grade.badge} ${fb.grade.title}: ENTERED SHORT [${activeSymbol.id}]"
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = theme.bear),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.fillMaxWidth().height(42.dp)
                        ) {
                            Text("SHORT 🔻", color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Black)
                        }
                    }
                }

                // Center Panoramic Canvas
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight()
                ) {
                    ChartCanvas(
                        candles = engine.candles,
                        tapePosition = tapePosition,
                        theme = theme,
                        activeSetup = engine.setups.lastOrNull(),
                        supertrendEnabled = supertrendEnabled,
                        structureEnabled = structureEnabled,
                        fvgEnabled = fvgEnabled,
                        emaCrossEnabled = emaCrossEnabled,
                        patternLockEnabled = patternLockEnabled,
                        onLongEntry = {
                            isPlayerHolding = true
                            bridge.vibrate("hold_long")
                            val fb = SetupExecutionTimingEngine.evaluateEntry(
                                activeSetup = engine.setups.lastOrNull(),
                                tapePosition = tapePosition,
                                isLong = true
                            )
                            lastOutcome = "${fb.grade.badge} ${fb.grade.title}: ENTERED LONG [${activeSymbol.id}]"
                        },
                        onShortEntry = {
                            isPlayerHolding = true
                            bridge.vibrate("slide_short")
                            val fb = SetupExecutionTimingEngine.evaluateEntry(
                                activeSetup = engine.setups.lastOrNull(),
                                tapePosition = tapePosition,
                                isLong = false
                            )
                            lastOutcome = "${fb.grade.badge} ${fb.grade.title}: ENTERED SHORT [${activeSymbol.id}]"
                        },
                        onCashOut = { pnl ->
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
                            } else {
                                streak = 0
                                playerStopsCount++
                                bridge.vibrate("liquidation")
                                lastOutcome = "LOSS: -$${String.format("%.2f", kotlin.math.abs(pnl))}"
                                OpponentDuelEngine.recordPlayerStopLoss()
                                activeStrategyKey?.let {
                                    StrategyMasteryManager.recordTrade(it, isWin = false)
                                }
                            }

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
                        },
                        modifier = Modifier.fillMaxSize()
                    )

                    lastOutcome?.let { msg ->
                        Surface(
                            color = theme.bg.copy(alpha = 0.92f),
                            shape = RoundedCornerShape(16.dp),
                            modifier = Modifier
                                .align(Alignment.BottomCenter)
                                .padding(bottom = 12.dp)
                        ) {
                            Text(
                                text = msg,
                                color = if (msg.contains("+") || msg.contains("LONG") || msg.contains("VICTORY") || msg.contains("CASHED")) theme.bull
                                else if (msg.contains("-") || msg.contains("LOSS") || msg.contains("DEFEAT")) theme.bear
                                else theme.text,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp)
                            )
                        }
                    }
                }

                // Right Flank (Tactical Telemetry & Long Action)
                Surface(
                    color = theme.bg.copy(alpha = 0.95f),
                    border = BorderStroke(1.dp, theme.grid.copy(alpha = 0.25f)),
                    modifier = Modifier
                        .width(140.dp)
                        .fillMaxHeight()
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(8.dp),
                        verticalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            Text("TELEMETRY", color = theme.bull, fontSize = 11.sp, fontWeight = FontWeight.Black)
                            Text("SCORE: ${totalScore.toInt()}", color = theme.text, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                            Text("STREAK: $streak 🔥", color = theme.text, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                            Text("IND: $activeIndCount/4 ⚡", color = theme.muted, fontSize = 9.sp)

                            Button(
                                onClick = {
                                    isPlayerHolding = false
                                    bridge.vibrate("cash_out")
                                    totalScore += 200.0
                                    streak++
                                    lastOutcome = "CASHED OUT"
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = theme.grid.copy(alpha = 0.2f)),
                                shape = RoundedCornerShape(6.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text("CASH OUT", color = theme.bull, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                            }
                        }

                        Button(
                            onClick = {
                                isPlayerHolding = true
                                bridge.vibrate("hold_long")
                                val fb = SetupExecutionTimingEngine.evaluateEntry(
                                    activeSetup = engine.setups.lastOrNull(),
                                    tapePosition = tapePosition,
                                    isLong = true
                                )
                                lastOutcome = "${fb.grade.badge} ${fb.grade.title}: ENTERED LONG [${activeSymbol.id}]"
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = theme.bull),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.fillMaxWidth().height(42.dp)
                        ) {
                            Text("LONG 🔺", color = if (theme.isLight) Color.White else Color.Black, fontSize = 11.sp, fontWeight = FontWeight.Black)
                        }
                    }
                }
            }
        } else {
            // ==================== PORTRAIT VERTICAL LAYOUT ====================
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
            ) {
            ChartCanvas(
                candles = engine.candles,
                tapePosition = tapePosition,
                theme = theme,
                activeSetup = engine.setups.lastOrNull(),
                supertrendEnabled = supertrendEnabled,
                structureEnabled = structureEnabled,
                fvgEnabled = fvgEnabled,
                emaCrossEnabled = emaCrossEnabled,
                patternLockEnabled = patternLockEnabled,
                onLongEntry = {
                    isPlayerHolding = true
                    bridge.vibrate("hold_long")
                    val fb = SetupExecutionTimingEngine.evaluateEntry(
                        activeSetup = engine.setups.lastOrNull(),
                        tapePosition = tapePosition,
                        isLong = true
                    )
                    lastOutcome = "${fb.grade.badge} ${fb.grade.title}: ENTERED LONG [${activeSymbol.id}]"
                },
                onShortEntry = {
                    isPlayerHolding = true
                    bridge.vibrate("slide_short")
                    val fb = SetupExecutionTimingEngine.evaluateEntry(
                        activeSetup = engine.setups.lastOrNull(),
                        tapePosition = tapePosition,
                        isLong = false
                    )
                    lastOutcome = "${fb.grade.badge} ${fb.grade.title}: ENTERED SHORT [${activeSymbol.id}]"
                },
                onCashOut = { pnl ->
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
                    } else {
                        streak = 0
                        playerStopsCount++
                        bridge.vibrate("liquidation")
                        lastOutcome = "LOSS: -$${String.format("%.2f", kotlin.math.abs(pnl))}"
                        OpponentDuelEngine.recordPlayerStopLoss()
                        activeStrategyKey?.let {
                            StrategyMasteryManager.recordTrade(it, isWin = false)
                        }
                    }

                    // Automatically pop up post-match debrief if competitive duel completed
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
                },
                modifier = Modifier.fillMaxSize()
            )

            // Live Opponent Radar HUD when in competitive head-to-head match
            if (OpponentDuelEngine.isCompetitiveActive) {
                OpponentRadarHUD(
                    theme = theme,
                    modifier = Modifier
                        .align(Alignment.TopCenter)
                        .padding(top = 8.dp)
                )
            }

            // Status message banner at bottom
            lastOutcome?.let { msg ->
                Surface(
                    color = theme.bg.copy(alpha = 0.92f),
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .padding(bottom = 24.dp)
                ) {
                    Text(
                        text = msg,
                        color = if (msg.contains("+") || msg.contains("LONG") || msg.contains("VICTORY") || msg.contains("CASHED")) theme.bull
                        else if (msg.contains("-") || msg.contains("LOSS") || msg.contains("DEFEAT")) theme.bear
                        else theme.text,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 20.dp, vertical = 7.dp)
                    )
                }
            }

            // Indicator Picker Dialog (All 68 Indicators Parity)
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

            // Mode, Desk Level & Strategy Selection Dialog
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
                        stratKey?.let { key ->
                            val s = StrategyCatalog.findStrategy(key)
                            if (s != null) {
                                val isBear = s.bias == "BEARISH"
                                val base = activeSymbol.basePrice
                                val riskAmt = base * 0.025
                                engine.setups.add(
                                    SetupRecord(
                                        key = s.key,
                                        name = s.name,
                                        dir = if (isBear) -1 else 1,
                                        at = 15,
                                        entry = base,
                                        stop = if (isBear) base + riskAmt else base - riskAmt,
                                        risk = riskAmt,
                                        winRate = s.winRate
                                    )
                                )
                            }
                        }
                        tapePosition = 10.0
                        isPlayerHolding = false
                        SetupExecutionTimingEngine.resetStats()
                        lastOutcome = if (stratKey == null) "FILTER: ALL SETUPS" else "LOCKED: ${StrategyCatalog.findStrategy(stratKey)?.name}"
                    },
                    onDismiss = { showModeStrategyDialog = false }
                )
            }

            // Pre-Trade Trading Desk Lobby Dialog
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

                        // Set up confluence filter
                        activeIndicatorIds = when (model.confluenceIndicatorId) {
                            "conf_supertrend" -> setOf("supertrend", "structure")
                            "conf_bos" -> setOf("structure", "supertrend")
                            "conf_fvg" -> setOf("fvg", "structure")
                            "conf_ema" -> setOf("emacross", "supertrend")
                            "conf_rsi_div" -> setOf("rsidiv", "rsi")
                            else -> emptySet()
                        }

                        engine = SetupEngine(seed = System.currentTimeMillis(), symbol = symbol)
                        val s = StrategyCatalog.findSetup(model.setupKey)
                        if (s != null) {
                            val isBear = s.bias == "BEARISH"
                            val base = symbol.basePrice
                            val riskAmt = base * 0.025
                            engine.setups.add(
                                SetupRecord(
                                    key = s.key,
                                    name = s.name,
                                    dir = if (isBear) -1 else 1,
                                    at = 15,
                                    entry = base,
                                    stop = if (isBear) base + riskAmt else base - riskAmt,
                                    risk = riskAmt,
                                    winRate = s.winRate
                                )
                            )
                        }
                        tapePosition = 10.0
                        isPlayerHolding = false
                        SetupExecutionTimingEngine.resetStats()

                        // Commencing competitive duel against matched rival
                        OpponentDuelEngine.startMatch(format.id, PlayerProfileManager.currentProfile.mmrRating)
                        lastOutcome = "COMMENCED ${format.badge}: ${symbol.id} | MODEL: ${model.name.uppercase()}"
                        bridge.vibrate("hold_long")
                    },
                    onDismiss = { showTradingDeskLobbyDialog = false }
                )
            }

            // 5-Tier Strategy Mastery Academy & Tutorials Dialog
            if (showMasteryTutorialDialog) {
                StrategyMasteryTutorialDialog(
                    theme = theme,
                    onLaunchMasteryDrill = { setup, tier ->
                        activeStrategyKey = setup.key
                        engine = SetupEngine(seed = System.currentTimeMillis(), symbol = activeSymbol)
                        val isBear = setup.bias == "BEARISH"
                        val base = activeSymbol.basePrice
                        val riskAmt = base * 0.025
                        engine.setups.add(
                            SetupRecord(
                                key = setup.key,
                                name = setup.name,
                                dir = if (isBear) -1 else 1,
                                at = 15,
                                entry = base,
                                stop = if (isBear) base + riskAmt else base - riskAmt,
                                risk = riskAmt,
                                winRate = setup.winRate
                            )
                        )
                        tapePosition = 10.0
                        isPlayerHolding = false
                        SetupExecutionTimingEngine.resetStats()

                        if (tier >= 4) {
                            currentLevel = DeskLevel.DESK_3
                        }
                        lastOutcome = "LAUNCHED TIER $tier TRIAL DRILL: ${setup.name.uppercase()}"
                        bridge.vibrate("hold_long")
                    },
                    onDismiss = { showMasteryTutorialDialog = false }
                )
            }

            // Post-Match Performance Debrief Dialog (Performance Telemetrics)
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

            // Pre-Match Operator Intro & Showcase Dialog (Tactical Calling Card)
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
}
}
