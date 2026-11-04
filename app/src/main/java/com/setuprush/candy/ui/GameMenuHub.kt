package com.setuprush.candy.ui

import android.content.res.Configuration
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.setuprush.candy.engine.*
import com.setuprush.candy.theme.GameTheme
import com.setuprush.candy.theme.ThemeCatalog
import com.setuprush.candy.theme.ThemeManager
import kotlin.math.abs
import kotlin.math.max

// Setup Rush & Quantitative Desk Palette
private val deskCarbonBg = Color(0xFF0A0C10)
private val deskPanelBg = Color(0xFF131720)
private val deskCardBg = Color(0xFF181D28)
private val deskGold = Color(0xFFFFC700)
private val deskCyan = Color(0xFF00E5FF)
private val deskRed = Color(0xFFFF334B)
private val deskGreen = Color(0xFF00E676)
private val deskMuted = Color(0xFF8A99AD)
private val deskBorder = Color(0xFF232A38)
private val deskOrange = Color(0xFFFF5A1F)

enum class SetupRushNavTab(val label: String, val icon: String) {
    MODES("ARENA", "⚡"),
    ACADEMY("ACADEMY", "🎓"),
    GAME_REVIEW("REVIEW", "♟️"),
    LEADERBOARD("COMBINE", "🏆"),
    JOURNAL("JOURNAL", "📋"),
    THEMES("THEMES", "🎨")
}

@Composable
fun GameMenuHub(
    theme: GameTheme,
    onLaunchGame: (formatId: String, symbol: MarketSymbolCatalog.MarketSymbol, model: TradingModelManager.CustomTradingModel) -> Unit,
    onOpenAcademyDrill: (setup: StrategyCatalog.SetupCardData, tier: Int) -> Unit,
    onLaunchMission: (mission: StrategyMasteryManager.SoloCampaignMission) -> Unit = {},
    onClose: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    var selectedTab by remember { mutableStateOf(SetupRushNavTab.MODES) }
    var activeSymbol by remember { mutableStateOf(MarketSymbolCatalog.activeSymbol) }
    var activeFormat by remember { mutableStateOf(OpponentDuelEngine.MATCH_FORMATS[0]) }
    var activeModel by remember { mutableStateOf(TradingModelManager.activeTradingModel) }
    var activePresetSlotIndex by remember { mutableIntStateOf(TradingModelManager.activePresetSlotIndex) }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(deskCarbonBg)
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            // ==================== 1. MINIMAL SETUP RUSH TOP HEADER ====================
            Surface(
                color = deskPanelBg,
                border = BorderStroke(1.dp, deskBorder),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .statusBarsPadding()
                        .padding(horizontal = 14.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Left: Brand Mark & Title
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = deskOrange
                        ) {
                            Text(
                                text = "⚡",
                                fontSize = 12.sp,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp)
                            )
                        }
                        Column {
                            Text(
                                text = "SETUP RUSH",
                                color = Color.White,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Black,
                                fontFamily = FontFamily.Monospace,
                                letterSpacing = 1.sp
                            )
                            Text(
                                text = "INSTITUTIONAL TAPE ENGINE",
                                color = deskOrange,
                                fontSize = 8.sp,
                                fontWeight = FontWeight.Bold,
                                fontFamily = FontFamily.Monospace
                            )
                        }
                    }

                    // Middle / Right: Capital Pill & Close Button
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = deskCardBg,
                            border = BorderStroke(1.dp, deskBorder)
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                horizontalArrangement = Arrangement.spacedBy(6.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text("💼", fontSize = 10.sp)
                                Text(
                                    text = "$10,000",
                                    color = deskCyan,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Black,
                                    fontFamily = FontFamily.Monospace
                                )
                                Text(
                                    text = "· ${PlayerProfileManager.currentProfile.mmrRating} MMR",
                                    color = deskGold,
                                    fontSize = 9.5.sp,
                                    fontWeight = FontWeight.Bold,
                                    fontFamily = FontFamily.Monospace
                                )
                            }
                        }

                        // Close / Return to Live Tape Button
                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = deskOrange.copy(alpha = 0.18f),
                            border = BorderStroke(1.dp, deskOrange.copy(alpha = 0.6f)),
                            modifier = Modifier.clickable { onClose() }
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Text(
                                    text = "✕ TAPE",
                                    color = deskOrange,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Black,
                                    fontFamily = FontFamily.Monospace
                                )
                            }
                        }
                    }
                }
            }

            // ==================== 2. HORIZONTAL TAB CAROUSEL ====================
            Surface(
                color = deskCarbonBg,
                border = BorderStroke(0.5.dp, deskBorder),
                modifier = Modifier.fillMaxWidth()
            ) {
                LazyRow(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 10.dp, vertical = 6.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(SetupRushNavTab.values()) { tab ->
                        val isSelected = selectedTab == tab
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = if (isSelected) deskOrange else deskPanelBg,
                            border = BorderStroke(1.dp, if (isSelected) deskOrange else deskBorder),
                            modifier = Modifier.clickable { selectedTab = tab }
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Text(tab.icon, fontSize = 12.sp)
                                Text(
                                    text = tab.label,
                                    color = if (isSelected) Color.White else deskMuted,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Black,
                                    fontFamily = FontFamily.Monospace,
                                    letterSpacing = 0.5.sp
                                )
                            }
                        }
                    }
                }
            }

            // ==================== 3. SCROLLABLE TAB CONTENT ====================
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .padding(horizontal = 14.dp, vertical = 8.dp)
            ) {
                when (selectedTab) {
                    SetupRushNavTab.MODES -> SetupRushModesContent(
                        activeSymbol = activeSymbol,
                        activeFormat = activeFormat,
                        activeModel = activeModel,
                        onSelectSymbol = { activeSymbol = it },
                        onSelectFormat = { activeFormat = it },
                        onCommence = { onLaunchGame(activeFormat.id, activeSymbol, activeModel) }
                    )
                    SetupRushNavTab.ACADEMY -> SetupRushAcademyContent(
                        onLaunchDrill = { setup, tier -> onOpenAcademyDrill(setup, tier) },
                        onDeployMission = onLaunchMission
                    )
                    SetupRushNavTab.GAME_REVIEW -> SetupRushGameReviewContent(theme = theme)
                    SetupRushNavTab.LEADERBOARD -> SetupRushLeaderboardContent()
                    SetupRushNavTab.JOURNAL -> SetupRushJournalContent(theme = theme)
                    SetupRushNavTab.THEMES -> SetupRushThemesContent()
                }
            }
        }
    }
}

// ==================== SUB-VIEW: 1. MODES & COMMENCE SESSION ====================

@Composable
fun SetupRushModesContent(
    activeSymbol: MarketSymbolCatalog.MarketSymbol,
    activeFormat: OpponentDuelEngine.MatchFormat,
    activeModel: TradingModelManager.CustomTradingModel,
    onSelectSymbol: (MarketSymbolCatalog.MarketSymbol) -> Unit,
    onSelectFormat: (OpponentDuelEngine.MatchFormat) -> Unit,
    onCommence: () -> Unit
) {
    val regime = MarketSessionEngine.getDailyMarketRegime()

    LazyColumn(
        verticalArrangement = Arrangement.spacedBy(10.dp),
        modifier = Modifier.fillMaxSize()
    ) {
        // Market Killzone & Regime Banner
        item {
            Surface(
                shape = RoundedCornerShape(10.dp),
                color = deskPanelBg,
                border = BorderStroke(1.dp, deskBorder),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .clip(CircleShape)
                                .background(deskGreen)
                        )
                        Column {
                            Text(
                                text = "LIVE REGIME: ${regime.regimeName.uppercase()}",
                                color = deskGold,
                                fontSize = 10.5.sp,
                                fontWeight = FontWeight.Black,
                                fontFamily = FontFamily.Monospace
                            )
                            Text(
                                text = "${MarketSessionEngine.activeSession.sessionName} · ${MarketSessionEngine.activeSession.utcHours} (${MarketSessionEngine.activeSession.volatilityMul}x Volatility)",
                                color = deskMuted,
                                fontSize = 8.5.sp,
                                fontFamily = FontFamily.Monospace
                            )
                        }
                    }

                    Surface(
                        shape = RoundedCornerShape(4.dp),
                        color = deskGold.copy(alpha = 0.15f),
                        border = BorderStroke(1.dp, deskGold.copy(alpha = 0.5f))
                    ) {
                        Text(
                            text = "ORDERFLOW ACTIVE",
                            color = deskGold,
                            fontSize = 8.5.sp,
                            fontWeight = FontWeight.Black,
                            fontFamily = FontFamily.Monospace,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                }
            }
        }

        // Target Market Asset Pair Selector
        item {
            Text(
                text = "TARGET ASSET PAIR",
                color = deskMuted,
                fontSize = 10.sp,
                fontWeight = FontWeight.Black,
                fontFamily = FontFamily.Monospace,
                letterSpacing = 1.sp
            )
            Spacer(modifier = Modifier.height(4.dp))
            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                items(MarketSymbolCatalog.ALL_SYMBOLS) { sym ->
                    val isSelected = activeSymbol.id == sym.id
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = if (isSelected) deskOrange.copy(alpha = 0.2f) else deskPanelBg,
                        border = BorderStroke(1.5.dp, if (isSelected) deskOrange else deskBorder),
                        modifier = Modifier.clickable { onSelectSymbol(sym) }
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Text(sym.badge, fontSize = 14.sp)
                            Column {
                                Text(
                                    text = sym.id,
                                    color = if (isSelected) deskOrange else Color.White,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Black,
                                    fontFamily = FontFamily.Monospace
                                )
                                Text(
                                    text = "$${sym.basePrice.toInt()} · ${sym.volatilityTier}",
                                    color = deskMuted,
                                    fontSize = 8.sp,
                                    fontFamily = FontFamily.Monospace
                                )
                            }
                        }
                    }
                }
            }
        }

        // Trading Game Modes List
        item {
            Text(
                text = "SELECT TRADING GAME MODE",
                color = deskMuted,
                fontSize = 10.sp,
                fontWeight = FontWeight.Black,
                fontFamily = FontFamily.Monospace,
                letterSpacing = 1.sp
            )
        }

        items(OpponentDuelEngine.MATCH_FORMATS) { fmt ->
            val isSelected = activeFormat.id == fmt.id
            Surface(
                shape = RoundedCornerShape(10.dp),
                color = if (isSelected) deskOrange.copy(alpha = 0.12f) else deskPanelBg,
                border = BorderStroke(1.5.dp, if (isSelected) deskOrange else deskBorder),
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onSelectFormat(fmt) }
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Text(fmt.badge, fontSize = 16.sp)
                            Text(
                                text = fmt.title.uppercase(),
                                color = if (isSelected) Color.White else deskMuted,
                                fontSize = 12.5.sp,
                                fontWeight = FontWeight.Black,
                                fontFamily = FontFamily.Monospace
                            )
                            if (isSelected) {
                                Surface(
                                    shape = RoundedCornerShape(4.dp),
                                    color = deskOrange
                                ) {
                                    Text(
                                        text = "SELECTED",
                                        color = Color.White,
                                        fontSize = 7.5.sp,
                                        fontWeight = FontWeight.Black,
                                        fontFamily = FontFamily.Monospace,
                                        modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
                                    )
                                }
                            }
                        }
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = fmt.subtitle,
                            color = deskMuted,
                            fontSize = 9.sp,
                            fontFamily = FontFamily.Monospace
                        )
                    }

                    RadioButton(
                        selected = isSelected,
                        onClick = { onSelectFormat(fmt) },
                        colors = RadioButtonDefaults.colors(
                            selectedColor = deskOrange,
                            unselectedColor = deskBorder
                        )
                    )
                }
            }
        }

        // Prominent Commence Session Button
        item {
            Spacer(modifier = Modifier.height(6.dp))
            Button(
                onClick = onCommence,
                colors = ButtonDefaults.buttonColors(containerColor = deskOrange),
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp)
                    .shadow(16.dp, RoundedCornerShape(10.dp), spotColor = deskOrange)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Text("⚡", fontSize = 18.sp)
                    Text(
                        text = "COMMENCE ${activeFormat.title.uppercase()}",
                        color = Color.White,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Black,
                        fontFamily = FontFamily.Monospace,
                        letterSpacing = 1.5.sp
                    )
                }
            }
            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

// ==================== SUB-VIEW: 2. STRATEGY ACADEMY CURRICULUM ====================

@Composable
fun SetupRushAcademyContent(
    onLaunchDrill: (setup: StrategyCatalog.SetupCardData, tier: Int) -> Unit,
    onDeployMission: (StrategyMasteryManager.SoloCampaignMission) -> Unit
) {
    var selectedSetupKey by remember { mutableStateOf("sweep") }
    val setupCards = StrategyCatalog.ALL_SETUPS
    val activeSetupCard = setupCards.find { it.key == selectedSetupKey } ?: setupCards[0]
    val prog = StrategyMasteryManager.getProgress(selectedSetupKey)

    LazyColumn(
        verticalArrangement = Arrangement.spacedBy(12.dp),
        modifier = Modifier.fillMaxSize()
    ) {
        // Strategy Selection Horizontal Row
        item {
            Text(
                text = "SELECT STRATEGY MODEL",
                color = deskMuted,
                fontSize = 10.sp,
                fontWeight = FontWeight.Black,
                fontFamily = FontFamily.Monospace,
                letterSpacing = 1.sp
            )
            Spacer(modifier = Modifier.height(4.dp))
            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                items(setupCards) { sc ->
                    val isSel = sc.key == selectedSetupKey
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = if (isSel) deskOrange.copy(alpha = 0.2f) else deskPanelBg,
                        border = BorderStroke(1.5.dp, if (isSel) deskOrange else deskBorder),
                        modifier = Modifier.clickable { selectedSetupKey = sc.key }
                    ) {
                        Text(
                            text = sc.name.uppercase(),
                            color = if (isSel) deskOrange else Color.White,
                            fontSize = 10.5.sp,
                            fontWeight = FontWeight.Black,
                            fontFamily = FontFamily.Monospace,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                        )
                    }
                }
            }
        }

        // Active Strategy Overview Card
        item {
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = deskPanelBg,
                border = BorderStroke(1.dp, deskBorder),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = activeSetupCard.name.uppercase(),
                            color = Color.White,
                            fontSize = 13.5.sp,
                            fontWeight = FontWeight.Black,
                            fontFamily = FontFamily.Monospace
                        )
                        Surface(
                            shape = RoundedCornerShape(4.dp),
                            color = deskOrange.copy(alpha = 0.2f)
                        ) {
                            Text(
                                text = "CURRENT: T${prog.tier} ${prog.tierTitle}",
                                color = deskOrange,
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Black,
                                fontFamily = FontFamily.Monospace,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp)
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = activeSetupCard.lesson,
                        color = deskMuted,
                        fontSize = 9.5.sp
                    )
                }
            }
        }

        // 5-Tier Mastery Progression Levels
        item {
            Text(
                text = "5-TIER MASTERY PROGRESSION",
                color = deskMuted,
                fontSize = 10.sp,
                fontWeight = FontWeight.Black,
                fontFamily = FontFamily.Monospace,
                letterSpacing = 1.sp
            )
        }

        val tiers = listOf(
            Triple(1, "Tier 1: Recognition & Trigger Lock", "Pass constraint: Catch rate >= 80% on closed pattern trigger. Max loss <= 1.0R."),
            Triple(2, "Tier 2: Invalidation & Risk Discipline", "Pass constraint: Zero chased entries (> 1.2 ATR late). Cut immediately at stop."),
            Triple(3, "Tier 3: Runner Capture & MFE Ratio", "Pass constraint: MFE capture ratio >= 65%. Hold to full +2.0R target."),
            Triple(4, "Tier 4: Volatility Stress Test", "Pass constraint: Profit factor >= 1.75 across 10 dynamic volatility session expansions."),
            Triple(5, "Tier 5: Grandmaster Combine", "Pass constraint: 15 trades, Win rate >= 65%, PF >= 2.20, LuxAlgo Edge Score >= 85.")
        )

        items(tiers) { (tierNum, title, desc) ->
            val isUnlocked = tierNum <= prog.tier
            val isCompleted = tierNum < prog.tier

            Surface(
                shape = RoundedCornerShape(10.dp),
                color = if (isUnlocked) deskPanelBg else deskCardBg.copy(alpha = 0.4f),
                border = BorderStroke(1.dp, if (isCompleted) deskGreen else if (isUnlocked) deskOrange else deskBorder),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f).padding(end = 8.dp)) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Text(
                                text = if (isCompleted) "✅" else if (isUnlocked) "🎯" else "🔒",
                                fontSize = 12.sp
                            )
                            Text(
                                text = title,
                                color = if (isUnlocked) Color.White else deskMuted,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Black,
                                fontFamily = FontFamily.Monospace
                            )
                        }
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = desc,
                            color = deskMuted,
                            fontSize = 8.5.sp
                        )
                    }

                    if (isUnlocked) {
                        Button(
                            onClick = { onLaunchDrill(activeSetupCard, tierNum) },
                            colors = ButtonDefaults.buttonColors(containerColor = if (isCompleted) deskCardBg else deskOrange),
                            border = if (isCompleted) BorderStroke(1.dp, deskGreen) else null,
                            shape = RoundedCornerShape(6.dp),
                            modifier = Modifier.height(34.dp)
                        ) {
                            Text(
                                text = if (isCompleted) "REPLAY" else "DRILL",
                                color = Color.White,
                                fontSize = 9.sp,
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

// ==================== SUB-VIEW: 3. CHESS.COM GAME REVIEW ====================

@Composable
fun SetupRushGameReviewContent(theme: GameTheme) {
    val debrief = MatchPerformanceEngine.lastDebrief
    var selectedMoveIndex by remember { mutableIntStateOf(0) }

    if (debrief == null || debrief.tradeReviews.isEmpty()) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text("♟️", fontSize = 36.sp)
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "NO COMPLETED TRADING SESSION",
                    color = Color.White,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Black,
                    fontFamily = FontFamily.Monospace
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Execute trades in ARENA or ACADEMY to generate your Chess.com candlestick review.",
                    color = deskMuted,
                    fontSize = 10.sp,
                    textAlign = TextAlign.Center
                )
            }
        }
        return
    }

    val trades = debrief.tradeReviews
    val activeMove = trades.getOrElse(selectedMoveIndex) { trades[0] }

    LazyColumn(
        verticalArrangement = Arrangement.spacedBy(10.dp),
        modifier = Modifier.fillMaxSize()
    ) {
        // Accuracy Header Card (Chess.com Style)
        item {
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = deskPanelBg,
                border = BorderStroke(1.5.dp, deskOrange),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "SESSION ACCURACY RATING",
                                color = deskMuted,
                                fontSize = 9.5.sp,
                                fontWeight = FontWeight.Black,
                                fontFamily = FontFamily.Monospace
                            )
                            Row(verticalAlignment = Alignment.Bottom, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                Text(
                                    text = "${String.format("%.1f", debrief.accuracyScore)}%",
                                    color = deskOrange,
                                    fontSize = 26.sp,
                                    fontWeight = FontWeight.Black,
                                    fontFamily = FontFamily.Monospace
                                )
                                Text(
                                    text = "· GRADE ${debrief.performanceGrade}",
                                    color = Color.White,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(bottom = 4.dp)
                                )
                            }
                        }

                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = deskOrange.copy(alpha = 0.2f),
                            border = BorderStroke(1.dp, deskOrange)
                        ) {
                            Text(
                                text = "${debrief.modeTitle.take(16).uppercase()}",
                                color = deskOrange,
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Black,
                                fontFamily = FontFamily.Monospace,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    // Accuracy Progress Bar
                    val frac = (debrief.accuracyScore / 100.0).toFloat().coerceIn(0f, 1f)
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(6.dp)
                            .clip(RoundedCornerShape(3.dp))
                            .background(Color(0xFF232A38))
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxHeight()
                                .fillMaxWidth(frac)
                                .background(deskOrange)
                        )
                    }
                }
            }
        }

        // Move Classification Pills Row
        item {
            Text(
                text = "MOVE CLASSIFICATION BREAKDOWN",
                color = deskMuted,
                fontSize = 10.sp,
                fontWeight = FontWeight.Black,
                fontFamily = FontFamily.Monospace,
                letterSpacing = 1.sp
            )
            Spacer(modifier = Modifier.height(4.dp))

            val counts = debrief.moveCounts
            LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                items(MatchPerformanceEngine.MoveGrade.values()) { grade ->
                    val count = counts[grade] ?: 0
                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = Color(grade.colorHex).copy(alpha = 0.15f),
                        border = BorderStroke(1.dp, Color(grade.colorHex).copy(alpha = 0.5f))
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Text(grade.badge, fontSize = 11.sp)
                            Text(
                                text = "${grade.label} ($count)",
                                color = Color(grade.colorHex),
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Black,
                                fontFamily = FontFamily.Monospace
                            )
                        }
                    }
                }
            }
        }

        // Interactive Move Scrubber
        item {
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = deskPanelBg,
                border = BorderStroke(1.dp, deskBorder),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    // Navigation Bar: Previous < Trade X of Y > Next
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Button(
                            onClick = { if (selectedMoveIndex > 0) selectedMoveIndex-- },
                            enabled = selectedMoveIndex > 0,
                            colors = ButtonDefaults.buttonColors(containerColor = deskCardBg),
                            shape = RoundedCornerShape(6.dp),
                            modifier = Modifier.height(32.dp)
                        ) {
                            Text("< PREV", fontSize = 9.sp, fontFamily = FontFamily.Monospace)
                        }

                        Text(
                            text = "TRADE ${selectedMoveIndex + 1} OF ${trades.size}",
                            color = Color.White,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Black,
                            fontFamily = FontFamily.Monospace
                        )

                        Button(
                            onClick = { if (selectedMoveIndex < trades.size - 1) selectedMoveIndex++ },
                            enabled = selectedMoveIndex < trades.size - 1,
                            colors = ButtonDefaults.buttonColors(containerColor = deskCardBg),
                            shape = RoundedCornerShape(6.dp),
                            modifier = Modifier.height(32.dp)
                        ) {
                            Text("NEXT >", fontSize = 9.sp, fontFamily = FontFamily.Monospace)
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // Move Grade Badge & Title
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Text(activeMove.moveGrade.badge, fontSize = 16.sp)
                            Text(
                                text = "${activeMove.moveGrade.label} (${activeMove.moveGrade.symbol})",
                                color = Color(activeMove.moveGrade.colorHex),
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Black,
                                fontFamily = FontFamily.Monospace
                            )
                        }

                        Text(
                            text = "${if (activeMove.pnl >= 0) "+$" else "-$"}${String.format("%.2f", abs(activeMove.pnl))} (${if (activeMove.rMultiple >= 0) "+" else ""}${String.format("%.1f", activeMove.rMultiple)}R)",
                            color = if (activeMove.pnl >= 0) deskGreen else deskRed,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Black,
                            fontFamily = FontFamily.Monospace
                        )
                    }

                    Spacer(modifier = Modifier.height(6.dp))

                    // Key Metrics Pill Box
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = deskCardBg,
                        border = BorderStroke(1.dp, deskBorder),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 10.dp, vertical = 6.dp),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column {
                                Text("SETUP", color = deskMuted, fontSize = 7.5.sp, fontFamily = FontFamily.Monospace)
                                Text(activeMove.setupName.take(14), color = Color.White, fontSize = 9.5.sp, fontWeight = FontWeight.Bold)
                            }
                            Column {
                                Text("DIRECTION", color = deskMuted, fontSize = 7.5.sp, fontFamily = FontFamily.Monospace)
                                Text(if (activeMove.direction > 0) "LONG ▲" else "SHORT ▼", color = if (activeMove.direction > 0) deskGreen else deskRed, fontSize = 9.5.sp, fontWeight = FontWeight.Black)
                            }
                            Column {
                                Text("ENTRY → EXIT", color = deskMuted, fontSize = 7.5.sp, fontFamily = FontFamily.Monospace)
                                Text("${String.format("%.2f", activeMove.entryPrice)} → ${String.format("%.2f", activeMove.exitPrice)}", color = Color.White, fontSize = 9.5.sp, fontFamily = FontFamily.Monospace)
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    // Coach Commentary
                    Text(
                        text = "🧠 COACH DIAGNOSIS:",
                        color = deskGold,
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Black,
                        fontFamily = FontFamily.Monospace
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = activeMove.mistakeDiagnosis,
                        color = Color.White,
                        fontSize = 10.sp
                    )

                    Spacer(modifier = Modifier.height(6.dp))

                    // Actionable Rule / Solution
                    Text(
                        text = "💡 PLAYBOOK RULE:",
                        color = deskCyan,
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Black,
                        fontFamily = FontFamily.Monospace
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = activeMove.actionableSolution,
                        color = deskMuted,
                        fontSize = 10.sp
                    )
                }
            }
        }
    }
}

// ==================== SUB-VIEW: 4. COMBINE LEADERBOARD ====================

@Composable
fun SetupRushLeaderboardContent() {
    val leaders = listOf(
        TraderLeaderRow(1, "Satoshi_Sniper", "TITAN", "Sweep Rejection", 94, 76.5, 3.42, 2.1, 42500.0),
        TraderLeaderRow(2, "Apex_Flow", "SIGMA", "FVG Retest", 91, 71.0, 2.85, 3.0, 38200.0),
        TraderLeaderRow(3, "Wick_Harvester", "PROP", "Order Block", 88, 68.4, 2.60, 3.8, 31400.0),
        TraderLeaderRow(4, "Delta_Neutral", "QUANT", "Break of Structure", 86, 65.0, 2.25, 4.2, 27900.0),
        TraderLeaderRow(5, "Titan_Scalper", "TITAN", "Trend Expansion", 84, 62.8, 2.10, 4.5, 24100.0)
    )

    LazyColumn(
        verticalArrangement = Arrangement.spacedBy(10.dp),
        modifier = Modifier.fillMaxSize()
    ) {
        item {
            Surface(
                shape = RoundedCornerShape(10.dp),
                color = deskPanelBg,
                border = BorderStroke(1.dp, deskBorder),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Text(
                        text = "DAILY COMBINE STANDINGS · 24H SYNCHRONIZED SEED",
                        color = deskGold,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Black,
                        fontFamily = FontFamily.Monospace
                    )
                    Text(
                        text = "Traders ranked by LuxAlgo Edge Score (Win Rate 40%, Profit Factor 35%, Streak 25%).",
                        color = deskMuted,
                        fontSize = 8.5.sp
                    )
                }
            }
        }

        items(leaders) { r ->
            Surface(
                shape = RoundedCornerShape(8.dp),
                color = deskPanelBg,
                border = BorderStroke(1.dp, if (r.rank == 1) deskGold else deskBorder),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Surface(
                            shape = RoundedCornerShape(4.dp),
                            color = if (r.rank == 1) deskGold else deskCardBg
                        ) {
                            Text(
                                text = "#${r.rank}",
                                color = if (r.rank == 1) Color.Black else Color.White,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Black,
                                fontFamily = FontFamily.Monospace,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                        Column {
                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                Text(r.name, color = Color.White, fontSize = 11.5.sp, fontWeight = FontWeight.Black, fontFamily = FontFamily.Monospace)
                                Text("[${r.desk}]", color = deskCyan, fontSize = 8.5.sp, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace)
                            }
                            Text("Model: ${r.strategy}", color = deskMuted, fontSize = 8.5.sp)
                        }
                    }

                    Column(horizontalAlignment = Alignment.End) {
                        Text(
                            text = "EDGE: ${r.edgeScore}/100",
                            color = deskGold,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Black,
                            fontFamily = FontFamily.Monospace
                        )
                        Text(
                            text = "${r.winRate}% WR · ${r.profitFactor} PF · +$${String.format("%,.0f", r.pnl)}",
                            color = deskGreen,
                            fontSize = 8.5.sp,
                            fontFamily = FontFamily.Monospace
                        )
                    }
                }
            }
        }
    }
}

private data class TraderLeaderRow(
    val rank: Int,
    val name: String,
    val desk: String,
    val strategy: String,
    val edgeScore: Int,
    val winRate: Double,
    val profitFactor: Double,
    val maxDrawdownPct: Double,
    val pnl: Double
)

// ==================== SUB-VIEW: 5. JOURNAL & LEAK ANALYSIS ====================

@Composable
fun SetupRushJournalContent(theme: GameTheme) {
    val debrief = MatchPerformanceEngine.lastDebrief
    val coachNotes = MatchPerformanceEngine.generateCoachingInsights(
        playbookKey = "sweep",
        winRatePct = debrief?.playerStats?.winRatePct ?: 65.0,
        profitFactor = debrief?.playerStats?.profitFactor ?: 2.1,
        totalTrades = debrief?.tradeReviews?.size ?: 5,
        chasedCount = SetupExecutionTimingEngine.chasedEntriesCount,
        prematureExitCount = debrief?.tradeReviews?.count { it.mistakeCategory == MatchPerformanceEngine.MistakeCategory.PREMATURE_EXIT } ?: 0,
        bestStreak = debrief?.playerStats?.bestStreak ?: 3
    )

    LazyColumn(
        verticalArrangement = Arrangement.spacedBy(10.dp),
        modifier = Modifier.fillMaxSize()
    ) {
        item {
            Surface(
                shape = RoundedCornerShape(10.dp),
                color = deskPanelBg,
                border = BorderStroke(1.dp, deskBorder),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Text(
                        text = "ALGORITHMIC RISK & LEAK COACHING",
                        color = deskGold,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Black,
                        fontFamily = FontFamily.Monospace
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Automated post-session edge diagnostics derived from Setup Rush journal.js.",
                        color = deskMuted,
                        fontSize = 8.5.sp
                    )
                }
            }
        }

        items(coachNotes) { note ->
            Surface(
                shape = RoundedCornerShape(8.dp),
                color = deskPanelBg,
                border = BorderStroke(1.dp, deskBorder),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(12.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.Top
                ) {
                    Text("💡", fontSize = 14.sp)
                    Text(
                        text = note,
                        color = Color.White,
                        fontSize = 10.sp
                    )
                }
            }
        }
    }
}

// ==================== SUB-VIEW: 6. SETUP RUSH THEMES ====================

@Composable
fun SetupRushThemesContent() {
    val allThemes = ThemeCatalog.all()
    val currentId = ThemeManager.currentTheme.value.id

    LazyColumn(
        verticalArrangement = Arrangement.spacedBy(10.dp),
        modifier = Modifier.fillMaxSize()
    ) {
        item {
            Text(
                text = "SETUP RUSH VISUAL THEMES (14 PALETTES)",
                color = deskMuted,
                fontSize = 10.sp,
                fontWeight = FontWeight.Black,
                fontFamily = FontFamily.Monospace,
                letterSpacing = 1.sp
            )
        }

        items(allThemes) { t ->
            val isCurrent = t.id == currentId
            Surface(
                shape = RoundedCornerShape(10.dp),
                color = if (isCurrent) deskOrange.copy(alpha = 0.15f) else deskPanelBg,
                border = BorderStroke(1.5.dp, if (isCurrent) deskOrange else deskBorder),
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { ThemeManager.setTheme(t.id) }
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Surface(
                            shape = RoundedCornerShape(4.dp),
                            color = t.bg,
                            border = BorderStroke(1.dp, t.grid),
                            modifier = Modifier.size(24.dp)
                        ) {}
                        Column {
                            Text(
                                text = t.name.uppercase(),
                                color = if (isCurrent) Color.White else deskMuted,
                                fontSize = 11.5.sp,
                                fontWeight = FontWeight.Black,
                                fontFamily = FontFamily.Monospace
                            )
                            Text(
                                text = "${t.desc} · ID: ${t.id}",
                                color = deskMuted,
                                fontSize = 8.5.sp
                            )
                        }
                    }

                    if (isCurrent) {
                        Surface(
                            shape = RoundedCornerShape(4.dp),
                            color = deskOrange
                        ) {
                            Text(
                                text = "ACTIVE",
                                color = Color.White,
                                fontSize = 8.sp,
                                fontWeight = FontWeight.Black,
                                fontFamily = FontFamily.Monospace,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}
