package com.setuprush.candy.ui

import android.content.res.Configuration
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.setuprush.candy.engine.*
import com.setuprush.candy.theme.GameTheme
import com.setuprush.candy.theme.ThemeManager

/**
 * Tactical Combat Mobile Style Game Menu Hub & Command Center.
 * Features:
 * - Military Obsidian & Gunmetal Carbon Theme (#080B10, #121822) with Tactical Gold (#FFC700) & Cyan (#00E5FF).
 * - Calling Card Header: Rank Insignia, MMR Rating, Prestige Level, CP & Credits Currency Vault.
 * - Slanted Tactical [ DEPLOY TO COMBAT ] Button with chamfer cutouts.
 * - 35-Level Solo Player Campaign Missions with 3-Star objectives and direct chart setup injection.
 * - 7 Strategy Family 5-Tier Interactive Skill Trees with unlockable perk nodes.
 * - Tactical Trading Model Studio, Barracks Dossier, and Combat Settings.
 */
enum class MenuNavPillar(val label: String, val icon: String, val subtitle: String) {
    LOBBY("PLAY", "⚔️", "Matchmaking & Arena"),
    ACADEMY("CAMPAIGN", "🎯", "35 Solo Operations & Academy"),
    SKILL_TREE("SKILLS", "🧬", "Perk Mastery Trees"),
    LOADOUT("LOADOUT", "🪖", "Trading Model Bench"),
    BARRACKS("BARRACKS", "🪪", "Career & Dossier"),
    SETTINGS("SETTINGS", "⚙️", "Combat HUD & Audio"),
    STORE("VAULT", "🪙", "Desk Pass & CP")
}

@Composable
fun GameMenuHub(
    theme: GameTheme,
    onLaunchGame: (formatId: String, symbol: MarketSymbolCatalog.MarketSymbol, model: TradingModelManager.CustomTradingModel) -> Unit,
    onOpenAcademyDrill: (setup: StrategyCatalog.SetupCardData, tier: Int) -> Unit,
    onLaunchMission: (mission: StrategyMasteryManager.SoloCampaignMission) -> Unit = {},
    modifier: Modifier = Modifier
) {
    var selectedPillar by remember { mutableStateOf(MenuNavPillar.LOBBY) }
    val configuration = LocalConfiguration.current
    val isLandscape = configuration.orientation == Configuration.ORIENTATION_LANDSCAPE

    var activeSymbol by remember { mutableStateOf(MarketSymbolCatalog.activeSymbol) }
    var activeFormat by remember { mutableStateOf(OpponentDuelEngine.MATCH_FORMATS[0]) }
    var activeModel by remember { mutableStateOf(TradingModelManager.activeTradingModel) }

    // Tactical Color Palette
    val tacticalCarbonBg = Color(0xFF090C12)
    val tacticalPanelBg = Color(0xFF131924)
    val tacticalGold = Color(0xFFFFC700)
    val tacticalCyan = Color(0xFF00E5FF)
    val tacticalRed = Color(0xFFFF334B)
    val tacticalGreen = Color(0xFF00E676)
    val tacticalMuted = Color(0xFF8A99AD)
    val tacticalBorder = Color(0xFF263345)

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(tacticalCarbonBg)
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            // ==================== TOP OPERATOR DOSSIER BAR ====================
            Surface(
                color = tacticalPanelBg,
                border = BorderStroke(1.dp, tacticalBorder),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 14.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Operator Calling Card & Rank
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = tacticalGold.copy(alpha = 0.2f),
                            border = BorderStroke(1.5.dp, tacticalGold)
                        ) {
                            Text(
                                text = "VIPER",
                                color = tacticalGold,
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
                                    color = tacticalCyan.copy(alpha = 0.25f),
                                    shape = RoundedCornerShape(3.dp)
                                ) {
                                    Text(
                                        text = "LV. 55 PRESTIGE",
                                        color = tacticalCyan,
                                        fontSize = 8.sp,
                                        fontWeight = FontWeight.Black,
                                        fontFamily = FontFamily.Monospace,
                                        modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
                                    )
                                }
                            }
                            Text(
                                text = "🦅 LEGENDARY · ${PlayerProfileManager.currentProfile.mmrRating} MMR",
                                color = tacticalGold,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                fontFamily = FontFamily.Monospace
                            )
                        }
                    }

                    // Tactical Currency Vault (CP, Credits, SP)
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = Color(0xFF1B2332),
                            border = BorderStroke(1.dp, tacticalBorder)
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp),
                                horizontalArrangement = Arrangement.spacedBy(4.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text("🧬", fontSize = 10.sp)
                                Text(
                                    text = "${StrategyMasteryManager.availableSkillPoints} SP",
                                    color = tacticalCyan,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Black,
                                    fontFamily = FontFamily.Monospace
                                )
                            }
                        }

                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = Color(0xFF1B2332),
                            border = BorderStroke(1.dp, tacticalBorder)
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp),
                                horizontalArrangement = Arrangement.spacedBy(4.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text("🪙", fontSize = 10.sp)
                                Text(
                                    text = "${MonetizationManager.deskCredits}",
                                    color = tacticalGold,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Black,
                                    fontFamily = FontFamily.Monospace
                                )
                            }
                        }
                    }
                }
            }

            // ==================== TACTICAL NAVIGATION STRIP ====================
            Surface(
                color = tacticalCarbonBg,
                border = BorderStroke(0.5.dp, tacticalBorder),
                modifier = Modifier.fillMaxWidth()
            ) {
                LazyRow(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 10.dp, vertical = 6.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(MenuNavPillar.values()) { pillar ->
                        val isSelected = selectedPillar == pillar
                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = if (isSelected) tacticalGold else tacticalPanelBg,
                            border = BorderStroke(1.dp, if (isSelected) tacticalGold else tacticalBorder),
                            modifier = Modifier.clickable { selectedPillar = pillar }
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Text(pillar.icon, fontSize = 12.sp)
                                Text(
                                    text = pillar.label,
                                    color = if (isSelected) Color.Black else Color.White,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Black,
                                    fontFamily = FontFamily.Monospace,
                                    letterSpacing = 1.sp
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
                    MenuNavPillar.LOBBY -> TacticalLobbyContent(
                        tacticalGold = tacticalGold,
                        tacticalCyan = tacticalCyan,
                        tacticalPanelBg = tacticalPanelBg,
                        tacticalBorder = tacticalBorder,
                        tacticalMuted = tacticalMuted,
                        activeSymbol = activeSymbol,
                        activeFormat = activeFormat,
                        activeModel = activeModel,
                        onSelectSymbol = { activeSymbol = it },
                        onSelectFormat = { activeFormat = it },
                        onDeploy = { onLaunchGame(activeFormat.id, activeSymbol, activeModel) },
                        onOpenCampaign = { selectedPillar = MenuNavPillar.ACADEMY }
                    )
                    MenuNavPillar.ACADEMY -> TacticalCampaignContent(
                        tacticalGold = tacticalGold,
                        tacticalCyan = tacticalCyan,
                        tacticalPanelBg = tacticalPanelBg,
                        tacticalBorder = tacticalBorder,
                        tacticalMuted = tacticalMuted,
                        onDeployMission = { mission ->
                            StrategyMasteryManager.activeMission = mission
                            val matchSymbol = MarketSymbolCatalog.findSymbol(mission.symbolId) ?: activeSymbol
                            activeSymbol = matchSymbol
                            onLaunchMission(mission)
                        }
                    )
                    MenuNavPillar.SKILL_TREE -> TacticalSkillTreeContent(
                        tacticalGold = tacticalGold,
                        tacticalCyan = tacticalCyan,
                        tacticalPanelBg = tacticalPanelBg,
                        tacticalBorder = tacticalBorder,
                        tacticalMuted = tacticalMuted
                    )
                    MenuNavPillar.LOADOUT -> LoadoutContent(
                        theme = theme,
                        activeModel = activeModel,
                        onSelectModel = { activeModel = it }
                    )
                    MenuNavPillar.BARRACKS -> BarracksContent(theme = theme)
                    MenuNavPillar.SETTINGS -> TacticalSettingsContent(
                        tacticalGold = tacticalGold,
                        tacticalCyan = tacticalCyan,
                        tacticalPanelBg = tacticalPanelBg,
                        tacticalBorder = tacticalBorder,
                        tacticalMuted = tacticalMuted
                    )
                    MenuNavPillar.STORE -> StoreContent(theme = theme)
                }
            }
        }
    }
}

// ==================== TACTICAL LOBBY VIEW ====================

@Composable
fun TacticalLobbyContent(
    tacticalGold: Color,
    tacticalCyan: Color,
    tacticalPanelBg: Color,
    tacticalBorder: Color,
    tacticalMuted: Color,
    activeSymbol: MarketSymbolCatalog.MarketSymbol,
    activeFormat: OpponentDuelEngine.MatchFormat,
    activeModel: TradingModelManager.CustomTradingModel,
    onSelectSymbol: (MarketSymbolCatalog.MarketSymbol) -> Unit,
    onSelectFormat: (OpponentDuelEngine.MatchFormat) -> Unit,
    onDeploy: () -> Unit,
    onOpenCampaign: () -> Unit
) {
    LazyColumn(
        verticalArrangement = Arrangement.spacedBy(10.dp),
        modifier = Modifier.fillMaxSize()
    ) {
        // Combat Arena / Asset Intel
        item {
            Text(
                text = "1. TARGET COMBAT ARENA (ASSET PAIR)",
                color = tacticalMuted,
                fontSize = 11.sp,
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
                        color = if (isSelected) tacticalGold.copy(alpha = 0.18f) else tacticalPanelBg,
                        border = BorderStroke(1.5.dp, if (isSelected) tacticalGold else tacticalBorder),
                        modifier = Modifier.clickable { onSelectSymbol(sym) }
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Text(sym.badge, fontSize = 16.sp)
                            Column {
                                Text(
                                    text = sym.id,
                                    color = if (isSelected) tacticalGold else Color.White,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Black,
                                    fontFamily = FontFamily.Monospace
                                )
                                Text(
                                    text = "$${sym.basePrice.toInt()} · ${sym.volatilityTier}",
                                    color = tacticalMuted,
                                    fontSize = 9.sp,
                                    fontFamily = FontFamily.Monospace
                                )
                            }
                        }
                    }
                }
            }
        }

        // Match Operation Modes
        item {
            Text(
                text = "2. COMBAT OPERATION FORMAT",
                color = tacticalMuted,
                fontSize = 11.sp,
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
                        color = if (isSelected) tacticalCyan.copy(alpha = 0.18f) else tacticalPanelBg,
                        border = BorderStroke(1.5.dp, if (isSelected) tacticalCyan else tacticalBorder),
                        modifier = Modifier
                            .width(170.dp)
                            .clickable { onSelectFormat(fmt) }
                    ) {
                        Column(modifier = Modifier.padding(10.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(fmt.badge, color = tacticalCyan, fontSize = 12.sp, fontWeight = FontWeight.Black)
                                if (isSelected) {
                                    Text("ACTIVE", color = tacticalCyan, fontSize = 8.sp, fontWeight = FontWeight.Black, fontFamily = FontFamily.Monospace)
                                }
                            }
                            Text(text = fmt.title, color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Black, fontFamily = FontFamily.Monospace)
                            Text(text = fmt.subtitle, color = tacticalMuted, fontSize = 9.sp)
                        }
                    }
                }
            }
        }

        // 3. Market Session Killzone & Historical Regime
        item {
            val regime = MarketSessionEngine.getDailyMarketRegime()
            var activeSession by remember { mutableStateOf(MarketSessionEngine.activeSession) }
            var activeTf by remember { mutableStateOf(MarketSessionEngine.activeTimeframe) }

            Text(
                text = "3. MARKET SESSION KILLZONE & REGIME",
                color = tacticalMuted,
                fontSize = 11.sp,
                fontWeight = FontWeight.Black,
                fontFamily = FontFamily.Monospace,
                letterSpacing = 1.sp
            )
            Spacer(modifier = Modifier.height(4.dp))
            Surface(
                shape = RoundedCornerShape(8.dp),
                color = tacticalPanelBg,
                border = BorderStroke(1.dp, tacticalBorder),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(10.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(regime.regimeName, color = tacticalGold, fontSize = 11.sp, fontWeight = FontWeight.Black, fontFamily = FontFamily.Monospace)
                        Text(regime.historicalDate, color = Color.White, fontSize = 9.sp, fontFamily = FontFamily.Monospace)
                    }
                    Text(regime.description, color = tacticalMuted, fontSize = 9.sp)
                }
            }

            Spacer(modifier = Modifier.height(6.dp))
            LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                items(MarketSessionEngine.MarketSession.values()) { sess ->
                    val isSel = activeSession == sess
                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = if (isSel) tacticalGold.copy(alpha = 0.2f) else tacticalPanelBg,
                        border = BorderStroke(1.dp, if (isSel) tacticalGold else tacticalBorder),
                        modifier = Modifier.clickable {
                            activeSession = sess
                            MarketSessionEngine.activeSession = sess
                        }
                    ) {
                        Column(modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp)) {
                            Text(sess.sessionName, color = if (isSel) tacticalGold else Color.White, fontSize = 10.sp, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace)
                            Text("${sess.utcHours} · ${sess.volatilityMul}x Vol", color = tacticalMuted, fontSize = 8.sp, fontFamily = FontFamily.Monospace)
                        }
                    }
                }
            }
        }

        // Solo Campaign Shortcut Banner
        item {
            Surface(
                shape = RoundedCornerShape(10.dp),
                color = tacticalGold.copy(alpha = 0.12f),
                border = BorderStroke(1.dp, tacticalGold.copy(alpha = 0.5f)),
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onOpenCampaign() }
            ) {
                Row(
                    modifier = Modifier.padding(12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "🎯 35-LEVEL SOLO STRATEGY CAMPAIGN",
                            color = tacticalGold,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Black,
                            fontFamily = FontFamily.Monospace
                        )
                        Text(
                            text = "Learn & master all 7 strategy families. Catch real chart setups to win 3 stars!",
                            color = Color.White,
                            fontSize = 9.5.sp
                        )
                    }
                    Text("OPEN ➔", color = tacticalGold, fontSize = 11.sp, fontWeight = FontWeight.Black, fontFamily = FontFamily.Monospace)
                }
            }
        }

        // Equipped Trading Model Loadout Summary
        item {
            Text(
                text = "3. EQUIPPED MODEL LOADOUT",
                color = tacticalMuted,
                fontSize = 11.sp,
                fontWeight = FontWeight.Black,
                fontFamily = FontFamily.Monospace,
                letterSpacing = 1.sp
            )
            Spacer(modifier = Modifier.height(4.dp))
            Surface(
                shape = RoundedCornerShape(8.dp),
                color = tacticalPanelBg,
                border = BorderStroke(1.dp, tacticalBorder),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = activeModel.name.uppercase(),
                            color = Color.White,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Black,
                            fontFamily = FontFamily.Monospace
                        )
                        Text(
                            text = "SETUP: ${activeModel.setupKey.uppercase()} | LEV: ${activeModel.leverage.toInt()}x | ${activeModel.riskProtocol.targetMultiple}",
                            color = tacticalCyan,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace
                        )
                    }
                    Surface(
                        shape = RoundedCornerShape(4.dp),
                        color = Color(0xFF00E676).copy(alpha = 0.2f),
                        border = BorderStroke(1.dp, Color(0xFF00E676))
                    ) {
                        Text(
                            text = "EQUIPPED ⚡",
                            color = Color(0xFF00E676),
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Black,
                            fontFamily = FontFamily.Monospace,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                }
            }
        }

        // Giant Slanted Tactical Deploy Button
        item {
            Spacer(modifier = Modifier.height(10.dp))
            Button(
                onClick = onDeploy,
                colors = ButtonDefaults.buttonColors(containerColor = tacticalGold),
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp)
                    .shadow(12.dp, RoundedCornerShape(8.dp), spotColor = tacticalGold)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Text("⚔️", fontSize = 18.sp)
                    Text(
                        text = "DEPLOY TO COMBAT · ${activeFormat.badge}",
                        color = Color.Black,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Black,
                        fontFamily = FontFamily.Monospace,
                        letterSpacing = 2.sp
                    )
                }
            }
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
    var selectedFamily by remember { mutableStateOf(StrategyCatalog.StrategyFamily.PRICE_ACTION) }
    val missions = remember(selectedFamily) { StrategyMasteryManager.getMissionsForFamily(selectedFamily) }

    LazyColumn(
        verticalArrangement = Arrangement.spacedBy(10.dp),
        modifier = Modifier.fillMaxSize()
    ) {
        item {
            Text(
                text = "SOLO STRATEGY CAMPAIGN (35 OPERATIONS)",
                color = tacticalGold,
                fontSize = 14.sp,
                fontWeight = FontWeight.Black,
                fontFamily = FontFamily.Monospace,
                letterSpacing = 1.sp
            )
            Text(
                text = "Select a Strategy Family and launch solo operations. The target setup WILL occur on the chart!",
                color = tacticalMuted,
                fontSize = 9.5.sp
            )
            Spacer(modifier = Modifier.height(6.dp))

            // 7 Strategy Family Selector Tabs
            LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                items(StrategyCatalog.StrategyFamily.values()) { fam ->
                    val isSel = selectedFamily == fam
                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = if (isSel) tacticalCyan else tacticalPanelBg,
                        border = BorderStroke(1.dp, if (isSel) tacticalCyan else tacticalBorder),
                        modifier = Modifier.clickable { selectedFamily = fam }
                    ) {
                        Text(
                            text = fam.displayName.uppercase(),
                            color = if (isSel) Color.Black else Color.White,
                            fontSize = 9.5.sp,
                            fontWeight = FontWeight.Black,
                            fontFamily = FontFamily.Monospace,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
                        )
                    }
                }
            }
        }

        // Mission Cards Grid
        items(missions) { mission ->
            val isUnlocked = mission.isUnlocked
            Surface(
                shape = RoundedCornerShape(8.dp),
                color = tacticalPanelBg,
                border = BorderStroke(1.dp, if (isUnlocked) tacticalBorder else tacticalBorder.copy(alpha = 0.5f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            Surface(
                                shape = RoundedCornerShape(4.dp),
                                color = if (isUnlocked) tacticalGold.copy(alpha = 0.2f) else Color.DarkGray
                            ) {
                                Text(
                                    text = "OP ${mission.levelId}",
                                    color = if (isUnlocked) tacticalGold else Color.Gray,
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.Black,
                                    fontFamily = FontFamily.Monospace,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                            }
                            Text(
                                text = mission.title,
                                color = if (isUnlocked) Color.White else Color.Gray,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Black,
                                fontFamily = FontFamily.Monospace
                            )
                        }

                        // Star Rating
                        Row(horizontalArrangement = Arrangement.spacedBy(2.dp)) {
                            for (s in 1..3) {
                                Text(
                                    text = if (s <= mission.starsEarned) "⭐" else "☆",
                                    fontSize = 11.sp
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = mission.briefing,
                        color = if (isUnlocked) tacticalMuted else Color.Gray,
                        fontSize = 9.5.sp
                    )
                    Spacer(modifier = Modifier.height(6.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "TARGET: ${mission.targetSetupKey.uppercase()} [${mission.targetBias}] · ${mission.symbolId}",
                            color = tacticalCyan,
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace
                        )

                        Button(
                            onClick = { if (isUnlocked) onDeployMission(mission) },
                            enabled = isUnlocked,
                            colors = ButtonDefaults.buttonColors(
                                containerColor = tacticalGold,
                                disabledContainerColor = Color(0xFF263345)
                            ),
                            shape = RoundedCornerShape(6.dp),
                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp)
                        ) {
                            Text(
                                text = if (isUnlocked) "DEPLOY OPERATION ➔" else "🔒 LOCKED",
                                color = if (isUnlocked) Color.Black else Color.Gray,
                                fontSize = 9.5.sp,
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

// ==================== TACTICAL SKILL TREE VIEW (7 FAMILIES x 5 TIERS) ====================

@Composable
fun TacticalSkillTreeContent(
    tacticalGold: Color,
    tacticalCyan: Color,
    tacticalPanelBg: Color,
    tacticalBorder: Color,
    tacticalMuted: Color
) {
    var selectedFamily by remember { mutableStateOf(StrategyCatalog.StrategyFamily.SMC) }
    var availableSp by remember { mutableIntStateOf(StrategyMasteryManager.availableSkillPoints) }
    val nodes = remember(selectedFamily, availableSp) { StrategyMasteryManager.getSkillTreeForFamily(selectedFamily) }

    LazyColumn(
        verticalArrangement = Arrangement.spacedBy(10.dp),
        modifier = Modifier.fillMaxSize()
    ) {
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "TACTICAL PERK SKILL TREES",
                        color = tacticalCyan,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Black,
                        fontFamily = FontFamily.Monospace,
                        letterSpacing = 1.sp
                    )
                    Text(
                        text = "Spend Skill Points (SP) to unlock trading perks and leverage boosts.",
                        color = tacticalMuted,
                        fontSize = 9.5.sp
                    )
                }

                Surface(
                    shape = RoundedCornerShape(6.dp),
                    color = tacticalCyan.copy(alpha = 0.2f),
                    border = BorderStroke(1.dp, tacticalCyan)
                ) {
                    Text(
                        text = "🧬 $availableSp SP AVAILABLE",
                        color = tacticalCyan,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Black,
                        fontFamily = FontFamily.Monospace,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }
            Spacer(modifier = Modifier.height(6.dp))

            // Family Selector
            LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                items(StrategyCatalog.StrategyFamily.values()) { fam ->
                    val isSel = selectedFamily == fam
                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = if (isSel) tacticalGold else tacticalPanelBg,
                        border = BorderStroke(1.dp, if (isSel) tacticalGold else tacticalBorder),
                        modifier = Modifier.clickable { selectedFamily = fam }
                    ) {
                        Text(
                            text = fam.displayName.uppercase(),
                            color = if (isSel) Color.Black else Color.White,
                            fontSize = 9.5.sp,
                            fontWeight = FontWeight.Black,
                            fontFamily = FontFamily.Monospace,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
                        )
                    }
                }
            }
        }

        // 5-Tier Nodes in sequence
        items(nodes) { node ->
            val unlocked = node.isUnlocked
            Surface(
                shape = RoundedCornerShape(8.dp),
                color = if (unlocked) tacticalPanelBg else tacticalPanelBg.copy(alpha = 0.6f),
                border = BorderStroke(1.5.dp, if (unlocked) tacticalGold else tacticalBorder),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        Text(node.icon, fontSize = 24.sp)
                        Column {
                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                Text(
                                    text = "TIER ${node.tier}: ${node.name.uppercase()}",
                                    color = if (unlocked) tacticalGold else Color.White,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Black,
                                    fontFamily = FontFamily.Monospace
                                )
                                Text(
                                    text = "· ${node.perkTitle}",
                                    color = tacticalCyan,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                            Text(
                                text = node.perkDesc,
                                color = tacticalMuted,
                                fontSize = 9.sp
                            )
                        }
                    }

                    if (unlocked) {
                        Surface(
                            shape = RoundedCornerShape(4.dp),
                            color = Color(0xFF00E676).copy(alpha = 0.2f),
                            border = BorderStroke(1.dp, Color(0xFF00E676))
                        ) {
                            Text(
                                text = "ACTIVE ✓",
                                color = Color(0xFF00E676),
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Black,
                                fontFamily = FontFamily.Monospace,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp)
                            )
                        }
                    } else {
                        Button(
                            onClick = {
                                if (StrategyMasteryManager.unlockPerk(node.id)) {
                                    availableSp = StrategyMasteryManager.availableSkillPoints
                                }
                            },
                            enabled = availableSp >= node.spCost,
                            colors = ButtonDefaults.buttonColors(
                                containerColor = tacticalCyan,
                                disabledContainerColor = Color(0xFF263345)
                            ),
                            shape = RoundedCornerShape(6.dp),
                            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp)
                        ) {
                            Text(
                                text = "UNLOCK (${node.spCost} SP)",
                                color = if (availableSp >= node.spCost) Color.Black else Color.Gray,
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

// ==================== TACTICAL SETTINGS VIEW ====================

@Composable
fun TacticalSettingsContent(
    tacticalGold: Color,
    tacticalCyan: Color,
    tacticalPanelBg: Color,
    tacticalBorder: Color,
    tacticalMuted: Color
) {
    var highFpsEnabled by remember { mutableStateOf(true) }
    var hapticsEnabled by remember { mutableStateOf(true) }
    var soundFxVolume by remember { mutableFloatStateOf(1.0f) }

    LazyColumn(
        verticalArrangement = Arrangement.spacedBy(10.dp),
        modifier = Modifier.fillMaxSize()
    ) {
        item {
            Text(
                text = "COMBAT HUD & GRAPHICS CONFIGURATION",
                color = tacticalGold,
                fontSize = 14.sp,
                fontWeight = FontWeight.Black,
                fontFamily = FontFamily.Monospace,
                letterSpacing = 1.sp
            )
        }

        item {
            Surface(
                shape = RoundedCornerShape(8.dp),
                color = tacticalPanelBg,
                border = BorderStroke(1.dp, tacticalBorder),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text("120 HZ HIGH REFRESH RATE", color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace)
                            Text("Uncapped frame timing for ultra-smooth candle rendering", color = tacticalMuted, fontSize = 9.sp)
                        }
                        Switch(
                            checked = highFpsEnabled,
                            onCheckedChange = { highFpsEnabled = it },
                            colors = SwitchDefaults.colors(checkedThumbColor = tacticalGold, checkedTrackColor = tacticalGold.copy(alpha = 0.3f))
                        )
                    }

                    Divider(color = tacticalBorder)

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text("TACTICAL HAPTIC FEEDBACK", color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace)
                            Text("Vibrate on entry, cash-out, and liquidation", color = tacticalMuted, fontSize = 9.sp)
                        }
                        Switch(
                            checked = hapticsEnabled,
                            onCheckedChange = { hapticsEnabled = it },
                            colors = SwitchDefaults.colors(checkedThumbColor = tacticalGold, checkedTrackColor = tacticalGold.copy(alpha = 0.3f))
                        )
                    }

                    Divider(color = tacticalBorder)

                    Column {
                        Text("MASTER AUDIO & COMBAT SFX", color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace)
                        Slider(
                            value = soundFxVolume,
                            onValueChange = { soundFxVolume = it },
                            colors = SliderDefaults.colors(thumbColor = tacticalGold, activeTrackColor = tacticalGold)
                        )
                    }
                }
            }
        }
    }
}

// ==================== SUB-VIEWS (LOADOUT, BARRACKS, STORE) ====================

@Composable
fun LoadoutContent(
    theme: GameTheme,
    activeModel: TradingModelManager.CustomTradingModel,
    onSelectModel: (TradingModelManager.CustomTradingModel) -> Unit
) {
    LazyColumn(
        verticalArrangement = Arrangement.spacedBy(10.dp),
        modifier = Modifier.fillMaxSize()
    ) {
        item {
            Text(
                text = "TRADING MODEL BENCH (DESK LOADOUT)",
                color = Color(0xFFFFC700),
                fontSize = 14.sp,
                fontWeight = FontWeight.Black,
                fontFamily = FontFamily.Monospace
            )
            Text(
                text = "Equip or customize your primary setup model, leverage, and risk protocols",
                color = Color(0xFF8A99AD),
                fontSize = 10.sp
            )
        }

        items(TradingModelManager.PREBUILT_MODELS) { model ->
            val isSelected = activeModel.id == model.id
            Surface(
                shape = RoundedCornerShape(8.dp),
                color = if (isSelected) Color(0xFFFFC700).copy(alpha = 0.15f) else Color(0xFF131924),
                border = BorderStroke(1.5.dp, if (isSelected) Color(0xFFFFC700) else Color(0xFF263345)),
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onSelectModel(model) }
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(model.name.uppercase(), color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Black, fontFamily = FontFamily.Monospace)
                        Text(
                            text = if (isSelected) "EQUIPPED" else "TAP TO EQUIP",
                            color = if (isSelected) Color(0xFFFFC700) else Color(0xFF8A99AD),
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Black,
                            fontFamily = FontFamily.Monospace
                        )
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Library: ${model.library.displayName} · Pair: ${model.targetSymbolId}",
                        color = Color(0xFF8A99AD),
                        fontSize = 9.sp
                    )
                    Text(
                        text = "Order: ${model.orderType.displayName} | ${model.leverage.toInt()}x Lev | ${model.riskProtocol.displayName}",
                        color = Color(0xFF00E5FF),
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace
                    )
                }
            }
        }
    }
}

@Composable
fun BarracksContent(theme: GameTheme) {
    val p = PlayerProfileManager.currentProfile
    var activeSuit by remember { mutableStateOf(CharacterCustomizationManager.getEquippedSuit()) }
    var activeWatch by remember { mutableStateOf(CharacterCustomizationManager.getEquippedWatch()) }

    LazyColumn(
        verticalArrangement = Arrangement.spacedBy(10.dp),
        modifier = Modifier.fillMaxSize()
    ) {
        item {
            Text(
                text = "OPERATOR BARRACKS & TRADING RECORD",
                color = Color(0xFFFFC700),
                fontSize = 14.sp,
                fontWeight = FontWeight.Black,
                fontFamily = FontFamily.Monospace
            )
        }

        item {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                Surface(shape = RoundedCornerShape(8.dp), color = Color(0xFF131924), modifier = Modifier.weight(1f)) {
                    Column(modifier = Modifier.padding(10.dp)) {
                        Text("WIN RATE", color = Color(0xFF8A99AD), fontSize = 9.sp, fontFamily = FontFamily.Monospace)
                        Text("${(p.careerWinRate * 100).toInt()}%", color = Color(0xFFFFC700), fontSize = 16.sp, fontWeight = FontWeight.Black, fontFamily = FontFamily.Monospace)
                    }
                }
                Surface(shape = RoundedCornerShape(8.dp), color = Color(0xFF131924), modifier = Modifier.weight(1f)) {
                    Column(modifier = Modifier.padding(10.dp)) {
                        Text("PROFIT FACTOR", color = Color(0xFF8A99AD), fontSize = 9.sp, fontFamily = FontFamily.Monospace)
                        Text(String.format("%.2f", p.profitFactor), color = Color(0xFFFFC700), fontSize = 16.sp, fontWeight = FontWeight.Black, fontFamily = FontFamily.Monospace)
                    }
                }
                Surface(shape = RoundedCornerShape(8.dp), color = Color(0xFF131924), modifier = Modifier.weight(1f)) {
                    Column(modifier = Modifier.padding(10.dp)) {
                        Text("BEST STREAK", color = Color(0xFF8A99AD), fontSize = 9.sp, fontFamily = FontFamily.Monospace)
                        Text("${p.bestWinStreak} 🔥", color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.Black, fontFamily = FontFamily.Monospace)
                    }
                }
            }
        }
    }
}

@Composable
fun StoreContent(theme: GameTheme) {
    var deskCredits by remember { mutableStateOf(MonetizationManager.deskCredits) }

    LazyColumn(
        verticalArrangement = Arrangement.spacedBy(10.dp),
        modifier = Modifier.fillMaxSize()
    ) {
        item {
            Text(
                text = "DESK VAULT & CP PASS",
                color = Color(0xFFFFC700),
                fontSize = 14.sp,
                fontWeight = FontWeight.Black,
                fontFamily = FontFamily.Monospace
            )
            Text(
                text = MonetizationManager.GOOGLE_PLAY_COMPLIANCE_DISCLAIMER,
                color = Color(0xFF8A99AD),
                fontSize = 8.sp
            )
        }

        item {
            Surface(
                shape = RoundedCornerShape(8.dp),
                color = Color(0xFF131924),
                border = BorderStroke(1.dp, Color(0xFF263345)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text("PRESTIGE TRADER BUNDLE", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Black, fontFamily = FontFamily.Monospace)
                        Text("5,000 Credits + 5 Skill Points + Golden Candlestick Theme", color = Color(0xFF8A99AD), fontSize = 9.sp)
                    }
                    Button(
                        onClick = {
                            MonetizationManager.addCredits(5000)
                            StrategyMasteryManager.availableSkillPoints += 5
                            deskCredits = MonetizationManager.deskCredits
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFFC700)),
                        shape = RoundedCornerShape(6.dp)
                    ) {
                        Text("CLAIM FREE", color = Color.Black, fontSize = 10.sp, fontWeight = FontWeight.Black, fontFamily = FontFamily.Monospace)
                    }
                }
            }
        }
    }
}

// ==================== SUB-VIEWS (LOADOUT, BARRACKS, STORE) ====================

@Composable
fun LoadoutContent(
    theme: GameTheme,
    activeModel: TradingModelManager.CustomTradingModel,
    onSelectModel: (TradingModelManager.CustomTradingModel) -> Unit
) {
    LazyColumn(
        verticalArrangement = Arrangement.spacedBy(10.dp),
        modifier = Modifier.fillMaxSize()
    ) {
        item {
            Text(
                text = "TRADING MODEL BENCH (DESK LOADOUT)",
                color = Color(0xFFFFC700),
                fontSize = 14.sp,
                fontWeight = FontWeight.Black,
                fontFamily = FontFamily.Monospace
            )
            Text(
                text = "Equip or customize your primary setup model, leverage, and risk protocols",
                color = Color(0xFF8A99AD),
                fontSize = 10.sp
            )
        }

        items(TradingModelManager.PREBUILT_MODELS) { model ->
            val isSelected = activeModel.id == model.id
            Surface(
                shape = RoundedCornerShape(8.dp),
                color = if (isSelected) Color(0xFFFFC700).copy(alpha = 0.15f) else Color(0xFF131924),
                border = BorderStroke(1.5.dp, if (isSelected) Color(0xFFFFC700) else Color(0xFF263345)),
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onSelectModel(model) }
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(model.name.uppercase(), color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Black, fontFamily = FontFamily.Monospace)
                        Text(
                            text = if (isSelected) "EQUIPPED" else "TAP TO EQUIP",
                            color = if (isSelected) Color(0xFFFFC700) else Color(0xFF8A99AD),
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Black,
                            fontFamily = FontFamily.Monospace
                        )
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Library: ${model.library.displayName} · Pair: ${model.targetSymbolId}",
                        color = Color(0xFF8A99AD),
                        fontSize = 9.sp
                    )
                    Text(
                        text = "Order: ${model.orderType.displayName} | ${model.leverage.toInt()}x Lev | ${model.riskProtocol.displayName}",
                        color = Color(0xFF00E5FF),
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace
                    )
                }
            }
        }
    }
}

@Composable
fun BarracksContent(theme: GameTheme) {
    val p = PlayerProfileManager.currentProfile
    var activeSuit by remember { mutableStateOf(CharacterCustomizationManager.getEquippedSuit()) }
    var activeWatch by remember { mutableStateOf(CharacterCustomizationManager.getEquippedWatch()) }

    LazyColumn(
        verticalArrangement = Arrangement.spacedBy(10.dp),
        modifier = Modifier.fillMaxSize()
    ) {
        item {
            Text(
                text = "OPERATOR BARRACKS & TRADING RECORD",
                color = Color(0xFFFFC700),
                fontSize = 14.sp,
                fontWeight = FontWeight.Black,
                fontFamily = FontFamily.Monospace
            )
        }

        item {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                Surface(shape = RoundedCornerShape(8.dp), color = Color(0xFF131924), modifier = Modifier.weight(1f)) {
                    Column(modifier = Modifier.padding(10.dp)) {
                        Text("WIN RATE", color = Color(0xFF8A99AD), fontSize = 9.sp, fontFamily = FontFamily.Monospace)
                        Text("${(p.careerWinRate * 100).toInt()}%", color = Color(0xFFFFC700), fontSize = 16.sp, fontWeight = FontWeight.Black, fontFamily = FontFamily.Monospace)
                    }
                }
                Surface(shape = RoundedCornerShape(8.dp), color = Color(0xFF131924), modifier = Modifier.weight(1f)) {
                    Column(modifier = Modifier.padding(10.dp)) {
                        Text("PROFIT FACTOR", color = Color(0xFF8A99AD), fontSize = 9.sp, fontFamily = FontFamily.Monospace)
                        Text(String.format("%.2f", p.profitFactor), color = Color(0xFFFFC700), fontSize = 16.sp, fontWeight = FontWeight.Black, fontFamily = FontFamily.Monospace)
                    }
                }
                Surface(shape = RoundedCornerShape(8.dp), color = Color(0xFF131924), modifier = Modifier.weight(1f)) {
                    Column(modifier = Modifier.padding(10.dp)) {
                        Text("BEST STREAK", color = Color(0xFF8A99AD), fontSize = 9.sp, fontFamily = FontFamily.Monospace)
                        Text("${p.bestWinStreak} 🔥", color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.Black, fontFamily = FontFamily.Monospace)
                    }
                }
            }
        }
    }
}

@Composable
fun StoreContent(theme: GameTheme) {
    var deskCredits by remember { mutableStateOf(MonetizationManager.deskCredits) }

    LazyColumn(
        verticalArrangement = Arrangement.spacedBy(10.dp),
        modifier = Modifier.fillMaxSize()
    ) {
        item {
            Text(
                text = "DESK VAULT & CP PASS",
                color = Color(0xFFFFC700),
                fontSize = 14.sp,
                fontWeight = FontWeight.Black,
                fontFamily = FontFamily.Monospace
            )
            Text(
                text = MonetizationManager.GOOGLE_PLAY_COMPLIANCE_DISCLAIMER,
                color = Color(0xFF8A99AD),
                fontSize = 8.sp
            )
        }

        item {
            Surface(
                shape = RoundedCornerShape(8.dp),
                color = Color(0xFF131924),
                border = BorderStroke(1.dp, Color(0xFF263345)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text("PRESTIGE TRADER BUNDLE", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Black, fontFamily = FontFamily.Monospace)
                        Text("5,000 Credits + 5 Skill Points + Golden Candlestick Theme", color = Color(0xFF8A99AD), fontSize = 9.sp)
                    }
                    Button(
                        onClick = {
                            MonetizationManager.addCredits(5000)
                            StrategyMasteryManager.availableSkillPoints += 5
                            deskCredits = MonetizationManager.deskCredits
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFFC700)),
                        shape = RoundedCornerShape(6.dp)
                    ) {
                        Text("CLAIM FREE", color = Color.Black, fontSize = 10.sp, fontWeight = FontWeight.Black, fontFamily = FontFamily.Monospace)
                    }
                }
            }
        }
    }
}
