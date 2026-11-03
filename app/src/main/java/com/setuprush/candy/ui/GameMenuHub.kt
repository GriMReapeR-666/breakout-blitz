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

// Top-Level Institutional Theme Palette
private val deskCarbonBg = Color(0xFF090C12)
private val deskPanelBg = Color(0xFF131924)
private val deskGold = Color(0xFFFFC700)
private val deskCyan = Color(0xFF00E5FF)
private val deskRed = Color(0xFFFF334B)
private val deskGreen = Color(0xFF00E676)
private val deskMuted = Color(0xFF8A99AD)
private val deskBorder = Color(0xFF263345)

/**
 * Clash Royale Anatomy 5-Pillar Navigation Hub.
 * Completely purged of FPS/military references in favor of institutional quantitative trading desk mechanics:
 * - Tab 1: VAULT (Shop, Desk Passes, Capital Allocations, Themes)
 * - Tab 2: ARSENAL (5 Strategy Presets, Primary Model, Tactical Confirmation Trigger, Gunsmith Risk Tuning, Mastery Milestones)
 * - Tab 3 (CENTER): DESK / HOME (Trophy Road MMR, Active Preset Card, Market Killzone, prominent [ COMMENCE SESSION ])
 * - Tab 4: ARENA (Matchmaking: 1v1 Ranked Prop Duel, Solo Strategy Grind, 35-Level Campaign Operations, 100-Trader Royale)
 * - Tab 5: JOURNAL (Career Performance Ledger, Strategy Win-Rates, Mistakes Post-Mortem, Cockpit Audio & HUD)
 */
enum class MenuNavPillar(val label: String, val icon: String, val subtitle: String) {
    VAULT("VAULT", "🪙", "Capital & Passes"),
    ARSENAL("ARSENAL", "📊", "Presets & Tuning"),
    DESK("DESK", "🏛️", "Live Trading Desk"),
    ARENA("ARENA", "⚔️", "Matchmaking & Modes"),
    JOURNAL("JOURNAL", "📋", "Ledger & Mistakes")
}

@Composable
fun GameMenuHub(
    theme: GameTheme,
    onLaunchGame: (formatId: String, symbol: MarketSymbolCatalog.MarketSymbol, model: TradingModelManager.CustomTradingModel) -> Unit,
    onOpenAcademyDrill: (setup: StrategyCatalog.SetupCardData, tier: Int) -> Unit,
    onLaunchMission: (mission: StrategyMasteryManager.SoloCampaignMission) -> Unit = {},
    modifier: Modifier = Modifier
) {
    // Default to Center Desk (Clash Royale Battle Home Tab)
    var selectedPillar by remember { mutableStateOf(MenuNavPillar.DESK) }
    val configuration = LocalConfiguration.current
    val isLandscape = configuration.orientation == Configuration.ORIENTATION_LANDSCAPE

    var activeSymbol by remember { mutableStateOf(MarketSymbolCatalog.activeSymbol) }
    var activeFormat by remember { mutableStateOf(OpponentDuelEngine.MATCH_FORMATS[0]) }
    var activePresetSlotIndex by remember { mutableIntStateOf(TradingModelManager.activePresetSlotIndex) }
    var activeModel by remember { mutableStateOf(TradingModelManager.activeTradingModel) }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(deskCarbonBg)
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            // ==================== TOP TRADER PROFILE & CREDENTIALS BAR ====================
            Surface(
                color = deskPanelBg,
                border = BorderStroke(1.dp, deskBorder),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 14.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Trader Calling Card & MMR Rank
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = deskGold.copy(alpha = 0.2f),
                            border = BorderStroke(1.5.dp, deskGold)
                        ) {
                            Text(
                                text = "PROP",
                                color = deskGold,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Black,
                                fontFamily = FontFamily.Monospace,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp)
                            )
                        }
                        Column {
                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                Text(
                                    text = PlayerProfileManager.currentProfile.username.uppercase(),
                                    color = Color.White,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Black,
                                    fontFamily = FontFamily.Monospace,
                                    letterSpacing = 1.sp
                                )
                                Surface(
                                    color = deskCyan.copy(alpha = 0.25f),
                                    shape = RoundedCornerShape(3.dp)
                                ) {
                                    Text(
                                        text = "SENIOR TRADER",
                                        color = deskCyan,
                                        fontSize = 8.sp,
                                        fontWeight = FontWeight.Black,
                                        fontFamily = FontFamily.Monospace,
                                        modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
                                    )
                                }
                            }
                            Text(
                                text = "🏆 PRO RANK · ${PlayerProfileManager.currentProfile.mmrRating} MMR",
                                color = deskGold,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                fontFamily = FontFamily.Monospace
                            )
                        }
                    }

                    // Virtual Desk Treasury
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = Color(0xFF1B2332),
                            border = BorderStroke(1.dp, deskBorder)
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp),
                                horizontalArrangement = Arrangement.spacedBy(4.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text("💼", fontSize = 10.sp)
                                Text(
                                    text = "$10,000",
                                    color = deskCyan,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Black,
                                    fontFamily = FontFamily.Monospace
                                )
                            }
                        }

                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = Color(0xFF1B2332),
                            border = BorderStroke(1.dp, deskBorder)
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp),
                                horizontalArrangement = Arrangement.spacedBy(4.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text("🪙", fontSize = 10.sp)
                                Text(
                                    text = "${MonetizationManager.deskCredits}",
                                    color = deskGold,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Black,
                                    fontFamily = FontFamily.Monospace
                                )
                            }
                        }
                    }
                }
            }

            // ==================== MAIN CONTENT VIEWPORT ====================
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 8.dp)
            ) {
                when (selectedPillar) {
                    MenuNavPillar.DESK -> DeskHomeContent(
                        activeSymbol = activeSymbol,
                        activeFormat = activeFormat,
                        activeModel = activeModel,
                        onSelectSymbol = { activeSymbol = it },
                        onSelectFormat = { activeFormat = it },
                        onCommenceSession = { onLaunchGame(activeFormat.id, activeSymbol, activeModel) },
                        onOpenArsenal = { selectedPillar = MenuNavPillar.ARSENAL },
                        onOpenArena = { selectedPillar = MenuNavPillar.ARENA }
                    )
                    MenuNavPillar.ARSENAL -> ArsenalPresetsContent(
                        theme = theme,
                        activePresetIndex = activePresetSlotIndex,
                        onSelectPresetIndex = { idx ->
                            activePresetSlotIndex = idx
                            TradingModelManager.selectPreset(idx)
                            activeModel = TradingModelManager.activeTradingModel
                        },
                        activeModel = activeModel,
                        onModelUpdated = { updated ->
                            activeModel = updated
                            TradingModelManager.activeTradingModel = updated
                        }
                    )
                    MenuNavPillar.ARENA -> ArenaMatchmakingContent(
                        activeSymbol = activeSymbol,
                        activeFormat = activeFormat,
                        onSelectFormat = { activeFormat = it },
                        onLaunchFormat = { fmt ->
                            activeFormat = fmt
                            onLaunchGame(fmt.id, activeSymbol, activeModel)
                        },
                        onDeployMission = { mission ->
                            StrategyMasteryManager.activeMission = mission
                            val matchSymbol = MarketSymbolCatalog.findSymbol(mission.symbolId) ?: activeSymbol
                            activeSymbol = matchSymbol
                            onLaunchMission(mission)
                        }
                    )
                    MenuNavPillar.JOURNAL -> JournalCareerContent(theme = theme)
                    MenuNavPillar.VAULT -> StoreContent(theme = theme)
                }
            }

            // ==================== CLASH ROYALE 5-PILLAR BOTTOM NAVIGATION BAR ====================
            Surface(
                color = deskPanelBg,
                border = BorderStroke(1.dp, deskBorder),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 4.dp, vertical = 4.dp),
                    horizontalArrangement = Arrangement.SpaceAround,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    MenuNavPillar.values().forEach { pillar ->
                        val isSelected = selectedPillar == pillar
                        val isCenterDesk = pillar == MenuNavPillar.DESK

                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = if (isSelected) {
                                if (isCenterDesk) deskGold.copy(alpha = 0.25f) else deskCyan.copy(alpha = 0.2f)
                            } else Color.Transparent,
                            border = BorderStroke(
                                1.dp,
                                if (isSelected) (if (isCenterDesk) deskGold else deskCyan) else Color.Transparent
                            ),
                            modifier = Modifier
                                .weight(if (isCenterDesk) 1.2f else 1.0f)
                                .clickable { selectedPillar = pillar }
                                .padding(horizontal = 2.dp)
                        ) {
                            Column(
                                modifier = Modifier.padding(vertical = 6.dp),
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.Center
                            ) {
                                Text(
                                    text = pillar.icon,
                                    fontSize = if (isCenterDesk) 18.sp else 15.sp
                                )
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = pillar.label,
                                    color = if (isSelected) (if (isCenterDesk) deskGold else deskCyan) else deskMuted,
                                    fontSize = if (isCenterDesk) 9.5.sp else 8.5.sp,
                                    fontWeight = FontWeight.Black,
                                    fontFamily = FontFamily.Monospace,
                                    letterSpacing = 0.5.sp
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

// ==================== 1. CENTER TAB: DESK / HOME (CLASH ROYALE BATTLE TAB) ====================

@Composable
fun DeskHomeContent(
    activeSymbol: MarketSymbolCatalog.MarketSymbol,
    activeFormat: OpponentDuelEngine.MatchFormat,
    activeModel: TradingModelManager.CustomTradingModel,
    onSelectSymbol: (MarketSymbolCatalog.MarketSymbol) -> Unit,
    onSelectFormat: (OpponentDuelEngine.MatchFormat) -> Unit,
    onCommenceSession: () -> Unit,
    onOpenArsenal: () -> Unit,
    onOpenArena: () -> Unit
) {
    val prog = StrategyMasteryManager.getProgress(activeModel.setupKey)
    val confluence = TradingModelManager.getConfluence(activeModel.confluenceIndicatorId)
    val regime = MarketSessionEngine.getDailyMarketRegime()

    LazyColumn(
        verticalArrangement = Arrangement.spacedBy(10.dp),
        modifier = Modifier.fillMaxSize()
    ) {
        // Market Killzone & Regime Banner
        item {
            Surface(
                shape = RoundedCornerShape(8.dp),
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
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            Box(modifier = Modifier.size(6.dp).clip(CircleShape).background(deskGreen))
                            Text(
                                text = "LIVE REGIME: ${regime.regimeName}",
                                color = deskGold,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Black,
                                fontFamily = FontFamily.Monospace
                            )
                        }
                        Text(
                            text = "${MarketSessionEngine.activeSession.sessionName} · ${MarketSessionEngine.activeSession.utcHours} (${MarketSessionEngine.activeSession.volatilityMul}x Volatility)",
                            color = deskMuted,
                            fontSize = 8.5.sp,
                            fontFamily = FontFamily.Monospace
                        )
                    }

                    Surface(
                        shape = RoundedCornerShape(4.dp),
                        color = deskGold.copy(alpha = 0.15f),
                        border = BorderStroke(1.dp, deskGold.copy(alpha = 0.5f))
                    ) {
                        Text(
                            text = "NEW YORK OPEN",
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
                        color = if (isSelected) deskGold.copy(alpha = 0.18f) else deskPanelBg,
                        border = BorderStroke(1.5.dp, if (isSelected) deskGold else deskBorder),
                        modifier = Modifier.clickable { onSelectSymbol(sym) }
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 7.dp),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Text(sym.badge, fontSize = 15.sp)
                            Column {
                                Text(
                                    text = sym.id,
                                    color = if (isSelected) deskGold else Color.White,
                                    fontSize = 11.5.sp,
                                    fontWeight = FontWeight.Black,
                                    fontFamily = FontFamily.Monospace
                                )
                                Text(
                                    text = "$${sym.basePrice.toInt()} · ${sym.volatilityTier}",
                                    color = deskMuted,
                                    fontSize = 8.5.sp,
                                    fontFamily = FontFamily.Monospace
                                )
                            }
                        }
                    }
                }
            }
        }

        // Prominent Active Preset Deck Card (Clash Royale Style)
        item {
            Text(
                text = "ACTIVE STRATEGY PRESET DECK",
                color = deskMuted,
                fontSize = 10.sp,
                fontWeight = FontWeight.Black,
                fontFamily = FontFamily.Monospace,
                letterSpacing = 1.sp
            )
            Spacer(modifier = Modifier.height(4.dp))
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = deskPanelBg,
                border = BorderStroke(1.5.dp, deskGold),
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onOpenArsenal() }
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = TradingModelManager.activePreset.slotName,
                                color = deskGold,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Black,
                                fontFamily = FontFamily.Monospace
                            )
                            Text(
                                text = "PRIMARY MODEL: ${activeModel.name.uppercase()}",
                                color = Color.White,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Black
                            )
                        }

                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = deskCyan.copy(alpha = 0.2f),
                            border = BorderStroke(1.dp, deskCyan)
                        ) {
                            Text(
                                text = "TIER ${prog.tier} · ${prog.tierTitle}",
                                color = deskCyan,
                                fontSize = 8.5.sp,
                                fontWeight = FontWeight.Black,
                                fontFamily = FontFamily.Monospace,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    // Tactical Parameters Grid
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = Color(0xFF1B2332),
                            border = BorderStroke(1.dp, deskBorder),
                            modifier = Modifier.weight(1f)
                        ) {
                            Column(modifier = Modifier.padding(6.dp)) {
                                Text("CONFIRMATION", color = deskMuted, fontSize = 7.5.sp, fontWeight = FontWeight.Bold)
                                Text(
                                    text = confluence?.displayName?.take(16) ?: "NAKED TAPE",
                                    color = Color.White,
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.Black
                                )
                            }
                        }

                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = Color(0xFF1B2332),
                            border = BorderStroke(1.dp, deskBorder),
                            modifier = Modifier.weight(0.7f)
                        ) {
                            Column(modifier = Modifier.padding(6.dp)) {
                                Text("LEVERAGE", color = deskMuted, fontSize = 7.5.sp, fontWeight = FontWeight.Bold)
                                Text(
                                    text = "${activeModel.leverage.toInt()}x GEAR",
                                    color = deskCyan,
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.Black
                                )
                            }
                        }

                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = Color(0xFF1B2332),
                            border = BorderStroke(1.dp, deskBorder),
                            modifier = Modifier.weight(0.7f)
                        ) {
                            Column(modifier = Modifier.padding(6.dp)) {
                                Text("RISK : REWARD", color = deskMuted, fontSize = 7.5.sp, fontWeight = FontWeight.Bold)
                                Text(
                                    text = "1.0R : +${activeModel.takeProfitTargetR.toInt()}R",
                                    color = deskGreen,
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.Black
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "⚡ Tap card to customize Primary Setup, Indicator Trigger & Risk Gunsmith in Arsenal",
                        color = deskMuted,
                        fontSize = 8.5.sp
                    )
                }
            }
        }

        // Match Mode Chips
        item {
            Text(
                text = "MATCHMAKING FORMAT",
                color = deskMuted,
                fontSize = 10.sp,
                fontWeight = FontWeight.Black,
                fontFamily = FontFamily.Monospace,
                letterSpacing = 1.sp
            )
            Spacer(modifier = Modifier.height(4.dp))
            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                items(OpponentDuelEngine.MATCH_FORMATS) { fmt ->
                    val isSelected = activeFormat.id == fmt.id
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = if (isSelected) deskCyan.copy(alpha = 0.18f) else deskPanelBg,
                        border = BorderStroke(1.5.dp, if (isSelected) deskCyan else deskBorder),
                        modifier = Modifier
                            .width(160.dp)
                            .clickable { onSelectFormat(fmt) }
                    ) {
                        Column(modifier = Modifier.padding(8.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(fmt.badge, color = deskCyan, fontSize = 11.sp, fontWeight = FontWeight.Black)
                                if (isSelected) {
                                    Text("ACTIVE", color = deskCyan, fontSize = 7.5.sp, fontWeight = FontWeight.Black, fontFamily = FontFamily.Monospace)
                                }
                            }
                            Text(text = fmt.title, color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Black, fontFamily = FontFamily.Monospace)
                            Text(text = fmt.subtitle, color = deskMuted, fontSize = 8.5.sp)
                        }
                    }
                }
            }
        }

        // Giant Prominent Clash Royale Commence Session Button
        item {
            Spacer(modifier = Modifier.height(6.dp))
            Button(
                onClick = onCommenceSession,
                colors = ButtonDefaults.buttonColors(containerColor = deskGold),
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(54.dp)
                    .shadow(16.dp, RoundedCornerShape(10.dp), spotColor = deskGold)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Text("🏛️", fontSize = 20.sp)
                    Text(
                        text = "COMMENCE TRADING SESSION",
                        color = Color.Black,
                        fontSize = 13.5.sp,
                        fontWeight = FontWeight.Black,
                        fontFamily = FontFamily.Monospace,
                        letterSpacing = 1.5.sp
                    )
                }
            }
        }
    }
}

// ==================== 2. TAB: ARSENAL / STRATEGY PRESETS & TUNING ====================

@Composable
fun ArsenalPresetsContent(
    theme: GameTheme,
    activePresetIndex: Int,
    onSelectPresetIndex: (Int) -> Unit,
    activeModel: TradingModelManager.CustomTradingModel,
    onModelUpdated: (TradingModelManager.CustomTradingModel) -> Unit
) {
    val prog = StrategyMasteryManager.getProgress(activeModel.setupKey)
    val perks = StrategyMasteryManager.getPerksForStrategy(activeModel.setupKey)

    LazyColumn(
        verticalArrangement = Arrangement.spacedBy(12.dp),
        modifier = Modifier.fillMaxSize()
    ) {
        // Preset Slots Switcher Bar (Presets 1 to 5)
        item {
            Text(
                text = "STRATEGY PRESET SLOTS (SAVED DECKS)",
                color = deskGold,
                fontSize = 11.sp,
                fontWeight = FontWeight.Black,
                fontFamily = FontFamily.Monospace,
                letterSpacing = 1.sp
            )
            Spacer(modifier = Modifier.height(6.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                (1..5).forEach { slotIdx ->
                    val isSlotActive = activePresetIndex == slotIdx
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = if (isSlotActive) deskGold else deskPanelBg,
                        border = BorderStroke(1.5.dp, if (isSlotActive) deskGold else deskBorder),
                        modifier = Modifier
                            .weight(1f)
                            .clickable { onSelectPresetIndex(slotIdx) }
                    ) {
                        Column(
                            modifier = Modifier.padding(vertical = 8.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = "SLOT $slotIdx",
                                color = if (isSlotActive) Color.Black else Color.White,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Black,
                                fontFamily = FontFamily.Monospace
                            )
                        }
                    }
                }
            }
        }

        // Card 1: Primary Trading Model (Core Setup)
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
                            text = "1. PRIMARY TRADING MODEL (CORE SETUP)",
                            color = deskMuted,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Black,
                            fontFamily = FontFamily.Monospace
                        )
                        Surface(
                            shape = RoundedCornerShape(4.dp),
                            color = deskGold.copy(alpha = 0.2f)
                        ) {
                            Text(
                                text = activeModel.library.tag,
                                color = deskGold,
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Black,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = activeModel.name.uppercase(),
                        color = Color.White,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Black
                    )
                    Text(
                        text = activeModel.library.description,
                        color = deskMuted,
                        fontSize = 9.5.sp
                    )

                    Spacer(modifier = Modifier.height(10.dp))
                    Text(
                        text = "SWAP PRIMARY SETUP PATTERN:",
                        color = deskMuted,
                        fontSize = 8.5.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        items(TradingModelManager.PREBUILT_MODELS) { m ->
                            val isChosen = activeModel.id == m.id
                            Surface(
                                shape = RoundedCornerShape(6.dp),
                                color = if (isChosen) deskGold.copy(alpha = 0.2f) else Color(0xFF1B2332),
                                border = BorderStroke(1.dp, if (isChosen) deskGold else deskBorder),
                                modifier = Modifier.clickable {
                                    val updated = activeModel.copy(
                                        id = m.id,
                                        name = m.name,
                                        library = m.library,
                                        setupKey = m.setupKey
                                    )
                                    onModelUpdated(updated)
                                }
                            ) {
                                Text(
                                    text = m.setupKey.uppercase(),
                                    color = if (isChosen) deskGold else Color.White,
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.Black,
                                    fontFamily = FontFamily.Monospace,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 5.dp)
                                )
                            }
                        }
                    }
                }
            }
        }

        // Card 2: Tactical Confirmation Trigger (Indicator Filter)
        item {
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = deskPanelBg,
                border = BorderStroke(1.dp, deskBorder),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Text(
                        text = "2. TACTICAL CONFIRMATION TRIGGER (INDICATOR CONFLUENCE)",
                        color = deskMuted,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Black,
                        fontFamily = FontFamily.Monospace
                    )
                    Spacer(modifier = Modifier.height(6.dp))

                    val activeConf = TradingModelManager.getConfluence(activeModel.confluenceIndicatorId)
                    Text(
                        text = activeConf?.displayName?.uppercase() ?: "NAKED PRICE ACTION",
                        color = deskCyan,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Black
                    )
                    Text(
                        text = activeConf?.role ?: "Pure price action tape reading without lag.",
                        color = deskMuted,
                        fontSize = 9.5.sp
                    )

                    Spacer(modifier = Modifier.height(8.dp))
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        items(TradingModelManager.CONFLUENCE_INDICATORS) { conf ->
                            val isSel = activeModel.confluenceIndicatorId == conf.id
                            Surface(
                                shape = RoundedCornerShape(6.dp),
                                color = if (isSel) deskCyan.copy(alpha = 0.2f) else Color(0xFF1B2332),
                                border = BorderStroke(1.dp, if (isSel) deskCyan else deskBorder),
                                modifier = Modifier.clickable {
                                    val updated = activeModel.copy(confluenceIndicatorId = conf.id)
                                    onModelUpdated(updated)
                                }
                            ) {
                                Text(
                                    text = conf.displayName.take(18),
                                    color = if (isSel) deskCyan else Color.White,
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 5.dp)
                                )
                            }
                        }
                    }
                }
            }
        }

        // Card 3: Execution & Risk Gunsmith Parameters
        item {
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = deskPanelBg,
                border = BorderStroke(1.dp, deskBorder),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Text(
                        text = "3. EXECUTION & RISK PARAMETERS (GUNSMITH TUNING)",
                        color = deskMuted,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Black,
                        fontFamily = FontFamily.Monospace
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    // Leverage Gear
                    Text("LEVERAGE GEAR:", color = deskMuted, fontSize = 8.5.sp, fontWeight = FontWeight.Bold)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        listOf(2.0, 5.0, 10.0, 25.0, 50.0).forEach { lev ->
                            val isLev = activeModel.leverage == lev
                            val isLocked = lev == 50.0 && prog.tier < 5
                            Surface(
                                shape = RoundedCornerShape(6.dp),
                                color = if (isLev) deskCyan.copy(alpha = 0.25f) else Color(0xFF1B2332),
                                border = BorderStroke(1.dp, if (isLev) deskCyan else deskBorder),
                                modifier = Modifier
                                    .weight(1f)
                                    .clickable(enabled = !isLocked) {
                                        onModelUpdated(activeModel.copy(leverage = lev))
                                    }
                            ) {
                                Text(
                                    text = if (isLocked) "50x 🔒" else "${lev.toInt()}x",
                                    color = if (isLocked) deskMuted else (if (isLev) deskCyan else Color.White),
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Black,
                                    textAlign = TextAlign.Center,
                                    modifier = Modifier.padding(vertical = 6.dp)
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    // Take Profit Multiple Target
                    Text("TAKE PROFIT TARGET (R-MULTIPLE):", color = deskMuted, fontSize = 8.5.sp, fontWeight = FontWeight.Bold)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        listOf(1.5 to "+1.5R SCALP", 2.0 to "+2.0R STANDARD", 3.0 to "+3.0R RUNNER").forEach { (r, label) ->
                            val isSel = activeModel.takeProfitTargetR == r
                            Surface(
                                shape = RoundedCornerShape(6.dp),
                                color = if (isSel) deskGreen.copy(alpha = 0.2f) else Color(0xFF1B2332),
                                border = BorderStroke(1.dp, if (isSel) deskGreen else deskBorder),
                                modifier = Modifier
                                    .weight(1f)
                                    .clickable {
                                        onModelUpdated(activeModel.copy(takeProfitTargetR = r))
                                    }
                            ) {
                                Text(
                                    text = label,
                                    color = if (isSel) deskGreen else Color.White,
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.Black,
                                    textAlign = TextAlign.Center,
                                    modifier = Modifier.padding(vertical = 6.dp)
                                )
                            }
                        }
                    }
                }
            }
        }

        // Card 4: Strategy Mastery Progression (Perks Earned Naturally)
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
                            text = "STRATEGY MASTERY PROGRESSION",
                            color = deskMuted,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Black,
                            fontFamily = FontFamily.Monospace
                        )
                        Text(
                            text = "TIER ${prog.tier} / 5",
                            color = deskGold,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Black,
                            fontFamily = FontFamily.Monospace
                        )
                    }

                    Spacer(modifier = Modifier.height(6.dp))

                    // XP Bar
                    val xpRatio = (prog.xp.toFloat() / prog.xpRequired.toFloat()).coerceIn(0f, 1f)
                    LinearProgressIndicator(
                        progress = { xpRatio },
                        color = deskGold,
                        trackColor = Color(0xFF1B2332),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(8.dp)
                            .clip(RoundedCornerShape(4.dp))
                    )

                    Spacer(modifier = Modifier.height(6.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("${prog.xp} / ${prog.xpRequired} XP", color = deskMuted, fontSize = 9.sp)
                        Text("WIN RATE: ${(prog.winRate * 100).toInt()}% (${prog.winCount}W - ${prog.lossCount}L)", color = Color.White, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                    }

                    Spacer(modifier = Modifier.height(10.dp))
                    Text("UNLOCKED STRATEGY PERKS (EARNED VIA MASTERY):", color = deskMuted, fontSize = 8.5.sp, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(4.dp))
                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        perks.forEach { perk ->
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Text(if (perk.isUnlocked) perk.icon else "🔒", fontSize = 11.sp)
                                Text(
                                    text = "T${perk.tier}: ${perk.title}",
                                    color = if (perk.isUnlocked) Color.White else deskMuted,
                                    fontSize = 9.5.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = "— ${perk.description}",
                                    color = deskMuted,
                                    fontSize = 8.5.sp,
                                    maxLines = 1
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

// ==================== 3. TAB: ARENA / MATCHMAKING & CAMPAIGN ====================

@Composable
fun ArenaMatchmakingContent(
    activeSymbol: MarketSymbolCatalog.MarketSymbol,
    activeFormat: OpponentDuelEngine.MatchFormat,
    onSelectFormat: (OpponentDuelEngine.MatchFormat) -> Unit,
    onLaunchFormat: (OpponentDuelEngine.MatchFormat) -> Unit,
    onDeployMission: (StrategyMasteryManager.SoloCampaignMission) -> Unit
) {
    var arenaTab by remember { mutableIntStateOf(0) } // 0 = Matchmaking Arena, 1 = 35 Solo Operations

    Column(modifier = Modifier.fillMaxSize()) {
        // Tab Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Surface(
                shape = RoundedCornerShape(8.dp),
                color = if (arenaTab == 0) deskCyan.copy(alpha = 0.2f) else deskPanelBg,
                border = BorderStroke(1.dp, if (arenaTab == 0) deskCyan else deskBorder),
                modifier = Modifier
                    .weight(1f)
                    .clickable { arenaTab = 0 }
            ) {
                Text(
                    text = "⚔️ COMPETITIVE ARENA",
                    color = if (arenaTab == 0) deskCyan else deskMuted,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Black,
                    modifier = Modifier.padding(vertical = 8.dp),
                    textAlign = TextAlign.Center
                )
            }

            Surface(
                shape = RoundedCornerShape(8.dp),
                color = if (arenaTab == 1) deskGold.copy(alpha = 0.2f) else deskPanelBg,
                border = BorderStroke(1.dp, if (arenaTab == 1) deskGold else deskBorder),
                modifier = Modifier
                    .weight(1f)
                    .clickable { arenaTab = 1 }
            ) {
                Text(
                    text = "🎯 35 OPERATIONS",
                    color = if (arenaTab == 1) deskGold else deskMuted,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Black,
                    modifier = Modifier.padding(vertical = 8.dp),
                    textAlign = TextAlign.Center
                )
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        if (arenaTab == 0) {
            // Competitive Modes List
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier.fillMaxSize()
            ) {
                items(OpponentDuelEngine.MATCH_FORMATS) { fmt ->
                    val isSel = activeFormat.id == fmt.id
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = if (isSel) deskCyan.copy(alpha = 0.12f) else deskPanelBg,
                        border = BorderStroke(1.5.dp, if (isSel) deskCyan else deskBorder),
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onSelectFormat(fmt) }
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(14.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                    Text(fmt.badge, fontSize = 16.sp)
                                    Text(fmt.title, color = Color.White, fontSize = 13.sp, fontWeight = FontWeight.Black)
                                }
                                Text(fmt.subtitle, color = deskCyan, fontSize = 9.5.sp, fontWeight = FontWeight.Bold)
                            }

                            Button(
                                onClick = { onLaunchFormat(fmt) },
                                colors = ButtonDefaults.buttonColors(containerColor = if (isSel) deskCyan else Color(0xFF1B2332)),
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier.padding(start = 8.dp)
                            ) {
                                Text(
                                    text = "COMMENCE",
                                    color = if (isSel) Color.Black else Color.White,
                                    fontSize = 9.5.sp,
                                    fontWeight = FontWeight.Black
                                )
                            }
                        }
                    }
                }
            }
        } else {
            // 35 Operations Campaign View
            TacticalCampaignContent(
                tacticalGold = deskGold,
                tacticalCyan = deskCyan,
                tacticalPanelBg = deskPanelBg,
                tacticalBorder = deskBorder,
                tacticalMuted = deskMuted,
                onDeployMission = onDeployMission
            )
        }
    }
}

// ==================== 4. TAB: JOURNAL / CAREER LEDGER & POST-MORTEM ====================

@Composable
fun JournalCareerContent(
    theme: GameTheme
) {
    val allProgress = StrategyMasteryManager.getAllProgress()
    val totalTrades = allProgress.sumOf { it.winCount + it.lossCount }
    val totalWins = allProgress.sumOf { it.winCount }
    val overallWinRate = if (totalTrades == 0) 0.0 else (totalWins.toDouble() / totalTrades.toDouble()) * 100.0

    LazyColumn(
        verticalArrangement = Arrangement.spacedBy(10.dp),
        modifier = Modifier.fillMaxSize()
    ) {
        // Career Performance Metrics Cards
        item {
            Text(
                text = "CAREER PERFORMANCE LEDGER",
                color = deskGold,
                fontSize = 11.sp,
                fontWeight = FontWeight.Black,
                fontFamily = FontFamily.Monospace,
                letterSpacing = 1.sp
            )
            Spacer(modifier = Modifier.height(6.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = deskPanelBg,
                    border = BorderStroke(1.dp, deskBorder),
                    modifier = Modifier.weight(1f)
                ) {
                    Column(modifier = Modifier.padding(10.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("WIN RATE", color = deskMuted, fontSize = 8.sp, fontWeight = FontWeight.Bold)
                        Text("${String.format("%.1f", overallWinRate)}%", color = deskGreen, fontSize = 16.sp, fontWeight = FontWeight.Black)
                    }
                }

                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = deskPanelBg,
                    border = BorderStroke(1.dp, deskBorder),
                    modifier = Modifier.weight(1f)
                ) {
                    Column(modifier = Modifier.padding(10.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("TOTAL TRADES", color = deskMuted, fontSize = 8.sp, fontWeight = FontWeight.Bold)
                        Text("$totalTrades", color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.Black)
                    }
                }

                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = deskPanelBg,
                    border = BorderStroke(1.dp, deskBorder),
                    modifier = Modifier.weight(1f)
                ) {
                    Column(modifier = Modifier.padding(10.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("PROFIT FACTOR", color = deskMuted, fontSize = 8.sp, fontWeight = FontWeight.Bold)
                        Text("2.40", color = deskGold, fontSize = 16.sp, fontWeight = FontWeight.Black)
                    }
                }
            }
        }

        // Common Execution Mistakes & Post-Mortem Solutions Guide
        item {
            Text(
                text = "TRADING LEAKS & POST-MORTEM SOLUTIONS",
                color = deskMuted,
                fontSize = 10.sp,
                fontWeight = FontWeight.Black,
                fontFamily = FontFamily.Monospace,
                letterSpacing = 1.sp
            )
            Spacer(modifier = Modifier.height(4.dp))
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                listOf(
                    Triple("⚠️ CHASED EXTENDED MOVE", "Entering >2 candles late after price expanded.", "Never chase. Wait for the retest pullback into the key zone or pass."),
                    Triple("⏳ PREMATURE CASH-OUT", "Exiting at +0.5R, leaving +1.5R on the table.", "Trust your invalidation stop. Shift stop to breakeven at +1R and hold for full target."),
                    Triple("❌ PREMATURE FOMO", "Entering before the setup candle closed.", "Wait for the confirmation close to avoid being trapped on long wick fakeouts."),
                    Triple("🛑 STOP-LOSS IGNORED", "Holding position through deep drawdown into liquidation.", "Always honor your invalidation point. Cut the loss immediately when setup fails.")
                ).forEach { (title, diag, sol) ->
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = deskPanelBg,
                        border = BorderStroke(1.dp, deskBorder),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(10.dp)) {
                            Text(title, color = deskGold, fontSize = 10.sp, fontWeight = FontWeight.Black)
                            Text("Diagnosis: $diag", color = deskMuted, fontSize = 9.sp)
                            Text("💡 Solution: $sol", color = deskGreen, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }

        // Strategy Breakdown Ledger
        item {
            Text(
                text = "STRATEGY WIN-RATE MATRIX",
                color = deskMuted,
                fontSize = 10.sp,
                fontWeight = FontWeight.Black,
                fontFamily = FontFamily.Monospace,
                letterSpacing = 1.sp
            )
            Spacer(modifier = Modifier.height(4.dp))
            Surface(
                shape = RoundedCornerShape(10.dp),
                color = deskPanelBg,
                border = BorderStroke(1.dp, deskBorder),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(10.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    allProgress.forEach { p ->
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(p.key.uppercase(), color = Color.White, fontSize = 10.sp, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace)
                            Text("TIER ${p.tier} · ${p.winCount}W/${p.lossCount}L (${(p.winRate * 100).toInt()}%)", color = deskCyan, fontSize = 9.sp, fontFamily = FontFamily.Monospace)
                        }
                    }
                }
            }
        }

        // Terminal Cockpit Settings
        item {
            TacticalSettingsContent()
        }
    }
}

// ==================== TACTICAL SOLO CAMPAIGN VIEW (35 LEVELS) ====================

@Composable
fun TacticalCampaignContent(
    tacticalGold: Color,
    tacticalCyan: Color,
    tacticalPanelBg: Color,
    tacticalBorder: Color,
    tacticalMuted: Color,
    onDeployMission: (StrategyMasteryManager.SoloCampaignMission) -> Unit
) {
    val missions = remember { StrategyMasteryManager.getAllCampaignMissions() }
    val families = StrategyCatalog.StrategyFamily.values()
    var selectedFamily by remember { mutableStateOf(StrategyCatalog.StrategyFamily.SMC) }

    LazyColumn(
        verticalArrangement = Arrangement.spacedBy(10.dp),
        modifier = Modifier.fillMaxSize()
    ) {
        // Strategy Family Selector Pills
        item {
            Text(
                text = "STRATEGY FAMILY CURRICULUM",
                color = tacticalMuted,
                fontSize = 11.sp,
                fontWeight = FontWeight.Black,
                fontFamily = FontFamily.Monospace,
                letterSpacing = 1.sp
            )
            Spacer(modifier = Modifier.height(4.dp))
            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                items(families) { fam ->
                    val isSelected = selectedFamily == fam
                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = if (isSelected) tacticalGold else tacticalPanelBg,
                        border = BorderStroke(1.dp, if (isSelected) tacticalGold else tacticalBorder),
                        modifier = Modifier.clickable { selectedFamily = fam }
                    ) {
                        Text(
                            text = fam.name,
                            color = if (isSelected) Color.Black else Color.White,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Black,
                            fontFamily = FontFamily.Monospace,
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                        )
                    }
                }
            }
        }

        // 5 Progressive Missions in Selected Family
        val famMissions = missions.filter { it.family == selectedFamily }
        items(famMissions) { mission ->
            val isLocked = !mission.isUnlocked
            Surface(
                shape = RoundedCornerShape(10.dp),
                color = if (isLocked) tacticalPanelBg.copy(alpha = 0.5f) else tacticalPanelBg,
                border = BorderStroke(1.5.dp, if (mission.starsEarned > 0) tacticalGold else tacticalBorder),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            Text(
                                text = "OP ${mission.levelId}: ${mission.title}",
                                color = if (isLocked) tacticalMuted else Color.White,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Black,
                                fontFamily = FontFamily.Monospace
                            )
                            if (mission.starsEarned > 0) {
                                Text(
                                    text = "⭐".repeat(mission.starsEarned),
                                    fontSize = 11.sp
                                )
                            }
                        }
                        Text(
                            text = "PATTERN: ${mission.targetSetupKey.uppercase()} [${mission.targetBias}] · ${mission.symbolId}",
                            color = if (isLocked) tacticalMuted else tacticalCyan,
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace
                        )
                        Text(
                            text = mission.briefing,
                            color = tacticalMuted,
                            fontSize = 9.sp,
                            maxLines = 2
                        )
                    }

                    Button(
                        onClick = { onDeployMission(mission) },
                        enabled = !isLocked,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (mission.starsEarned > 0) tacticalGold else tacticalCyan,
                            disabledContainerColor = Color(0xFF1B2332)
                        ),
                        shape = RoundedCornerShape(6.dp),
                        modifier = Modifier.padding(start = 8.dp)
                    ) {
                        Text(
                            text = if (isLocked) "LOCKED 🔒" else if (mission.starsEarned > 0) "REPLAY" else "START",
                            color = if (isLocked) tacticalMuted else Color.Black,
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

// ==================== TERMINAL COCKPIT SETTINGS ====================

@Composable
fun TacticalSettingsContent() {
    var isMuted by remember { mutableStateOf(SoundEffectManager.isMuted) }
    var hapticsOn by remember { mutableStateOf(true) }

    Surface(
        shape = RoundedCornerShape(10.dp),
        color = deskPanelBg,
        border = BorderStroke(1.dp, deskBorder),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Text(
                text = "TERMINAL COCKPIT & AUDIO CONFIGURATION",
                color = deskGold,
                fontSize = 12.sp,
                fontWeight = FontWeight.Black,
                fontFamily = FontFamily.Monospace
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text("MASTER AUDIO & DESK SFX", color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace)
                    Text("Realistic terminal tick, cash out, and stop-loss notifications", color = deskMuted, fontSize = 9.sp)
                }
                Switch(
                    checked = !isMuted,
                    onCheckedChange = {
                        isMuted = !it
                        SoundEffectManager.isMuted = !it
                    }
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text("TACTILE HAPTIC FEEDBACK", color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace)
                    Text("Intense tactile vibration upon entering and exiting trades", color = deskMuted, fontSize = 9.sp)
                }
                Switch(
                    checked = hapticsOn,
                    onCheckedChange = { hapticsOn = it }
                )
            }
        }
    }
}

// ==================== STORE / VAULT VIEW ====================

@Composable
fun StoreContent(theme: GameTheme) {
    val currentTheme = ThemeManager.currentTheme.value
    val themes = remember { ThemeCatalog.all() }

    LazyColumn(
        verticalArrangement = Arrangement.spacedBy(10.dp),
        modifier = Modifier.fillMaxSize()
    ) {
        item {
            Text(
                text = "PROP VAULT & CAPITAL ALLOCATIONS",
                color = deskGold,
                fontSize = 14.sp,
                fontWeight = FontWeight.Black,
                fontFamily = FontFamily.Monospace
            )
            Text(
                text = "Combine evaluations, desk passes, and chart themes earned through performance",
                color = deskMuted,
                fontSize = 10.sp
            )
        }

        item {
            Surface(
                shape = RoundedCornerShape(8.dp),
                color = deskGold.copy(alpha = 0.15f),
                border = BorderStroke(1.dp, deskGold),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Text("👑 COMBINE ALLOCATION PASS", color = deskGold, fontSize = 12.sp, fontWeight = FontWeight.Black, fontFamily = FontFamily.Monospace)
                    Text("5,000 Credits + Golden Candlestick Theme + Instant 50x Max Leverage", color = deskMuted, fontSize = 9.sp)
                    Spacer(modifier = Modifier.height(6.dp))
                    Button(
                        onClick = {
                            MonetizationManager.addCredits(5000)
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = deskGold)
                    ) {
                        Text("CLAIM CREDITS", color = Color.Black, fontSize = 10.sp, fontWeight = FontWeight.Black, fontFamily = FontFamily.Monospace)
                    }
                }
            }
        }

        item {
            Text("CHART THEMES & VISUAL RETICLES", color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace)
            Spacer(modifier = Modifier.height(4.dp))
            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                items(themes) { th ->
                    val isSel = currentTheme.id == th.id
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = deskPanelBg,
                        border = BorderStroke(1.dp, if (isSel) deskGold else deskBorder),
                        modifier = Modifier
                            .width(130.dp)
                            .clickable { ThemeManager.setTheme(th.id) }
                    ) {
                        Column(modifier = Modifier.padding(8.dp)) {
                            Text(th.name, color = Color.White, fontSize = 10.sp, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace)
                            Text(if (isSel) "ACTIVE" else "SELECT", color = deskGold, fontSize = 8.sp, fontWeight = FontWeight.Black, fontFamily = FontFamily.Monospace)
                        }
                    }
                }
            }
        }
    }
}
