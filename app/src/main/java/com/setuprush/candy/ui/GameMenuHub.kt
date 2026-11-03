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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.setuprush.candy.engine.*
import com.setuprush.candy.theme.GameTheme
import kotlin.math.max

/**
 * Tactical Game Menu Hub & Navigation Suite for Prop Trading.
 * Implements industry-leading tactical trading hub UX design principles:
 * - 5 Tactical Pillars: LOBBY, LOADOUT, BARRACKS, ACADEMY, STORE.
 * - Adaptive Dual-Orientation (Portrait & Landscape responsive layouts).
 * - Original, proprietary intellectual property (IP) design.
 */
enum class MenuNavPillar(val label: String, val icon: String, val subtitle: String) {
    LOBBY("PLAY", "⚔️", "Matchmaking & Formats"),
    LOADOUT("LOADOUT", "🛠️", "Trading Model Studio"),
    BARRACKS("BARRACKS", "🪪", "Dossier & Performance Stats"),
    ACADEMY("ACADEMY", "🎓", "Strategy Mastery Tree"),
    STORE("STORE", "🪙", "Desk Pass & Cockpits")
}

@Composable
fun GameMenuHub(
    theme: GameTheme,
    onLaunchGame: (formatId: String, symbol: MarketSymbolCatalog.MarketSymbol, model: TradingModelManager.CustomTradingModel) -> Unit,
    onOpenAcademyDrill: (setup: StrategyCatalog.SetupCardData, tier: Int) -> Unit,
    modifier: Modifier = Modifier
) {
    var selectedPillar by remember { mutableStateOf(MenuNavPillar.LOBBY) }
    val configuration = LocalConfiguration.current
    val isLandscape = configuration.orientation == Configuration.ORIENTATION_LANDSCAPE

    var activeSymbol by remember { mutableStateOf(MarketSymbolCatalog.activeSymbol) }
    var activeFormat by remember { mutableStateOf(OpponentDuelEngine.MATCH_FORMATS[0]) }
    var activeModel by remember { mutableStateOf(TradingModelManager.activeTradingModel) }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(theme.bg)
    ) {
        if (isLandscape) {
            // ==================== LANDSCAPE COCKPIT LAYOUT ====================
            Row(modifier = Modifier.fillMaxSize()) {
                // Left Tactical Navigation Column
                Surface(
                    color = theme.bg.copy(alpha = 0.95f),
                    border = BorderStroke(1.dp, theme.grid.copy(alpha = 0.3f)),
                    modifier = Modifier
                        .width(170.dp)
                        .fillMaxHeight()
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(10.dp),
                        verticalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            // Title & Tag
                            Text(
                                text = "PROP RUSH",
                                color = theme.bull,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Black,
                                letterSpacing = 2.sp
                            )
                            Text(
                                text = "TACTICAL TRADING",
                                color = theme.muted,
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.height(6.dp))

                            // 5 Navigation Tabs
                            MenuNavPillar.values().forEach { pillar ->
                                val isSelected = selectedPillar == pillar
                                Surface(
                                    shape = RoundedCornerShape(8.dp),
                                    color = if (isSelected) theme.bull.copy(alpha = 0.22f) else Color.Transparent,
                                    border = if (isSelected) BorderStroke(1.dp, theme.bull) else null,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable { selectedPillar = pillar }
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 7.dp),
                                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        Text(pillar.icon, fontSize = 13.sp)
                                        Text(
                                            text = pillar.label,
                                            color = if (isSelected) theme.bull else theme.text,
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                }
                            }
                        }

                        // Credits & Profile Mini-Card
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = theme.grid.copy(alpha = 0.12f),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(8.dp)) {
                                Text(
                                    text = PlayerProfileManager.currentProfile.username,
                                    color = theme.text,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = "🪙 ${MonetizationManager.deskCredits} CREDITS",
                                    color = Color(0xFFFFD700),
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.Black
                                )
                            }
                        }
                    }
                }

                // Center/Right Main Content Panel
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight()
                        .padding(12.dp)
                ) {
                    when (selectedPillar) {
                        MenuNavPillar.LOBBY -> LobbyContent(
                            theme = theme,
                            isLandscape = true,
                            activeSymbol = activeSymbol,
                            activeFormat = activeFormat,
                            activeModel = activeModel,
                            onSelectSymbol = { activeSymbol = it },
                            onSelectFormat = { activeFormat = it },
                            onLaunch = { onLaunchGame(activeFormat.id, activeSymbol, activeModel) }
                        )
                        MenuNavPillar.LOADOUT -> LoadoutContent(
                            theme = theme,
                            activeModel = activeModel,
                            onSelectModel = { activeModel = it }
                        )
                        MenuNavPillar.BARRACKS -> BarracksContent(theme = theme)
                        MenuNavPillar.ACADEMY -> AcademyContent(
                            theme = theme,
                            onLaunchDrill = onOpenAcademyDrill
                        )
                        MenuNavPillar.STORE -> StoreContent(theme = theme)
                    }
                }
            }
        } else {
            // ==================== PORTRAIT VERTICAL LAYOUT ====================
            Column(modifier = Modifier.fillMaxSize()) {
                // Top Tactical Header
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 10.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "PROP RUSH",
                            color = theme.bull,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Black,
                            letterSpacing = 2.sp
                        )
                        Text(
                            text = "COMPETITIVE TRADING LEAGUE",
                            color = theme.muted,
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = theme.grid.copy(alpha = 0.15f)
                        ) {
                            Text(
                                text = "🪙 ${MonetizationManager.deskCredits}",
                                color = Color(0xFFFFD700),
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = theme.bull.copy(alpha = 0.2f)
                        ) {
                            Text(
                                text = "${PlayerProfileManager.currentProfile.mmrRating} MMR",
                                color = theme.bull,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }
                    }
                }

                // Center Content Area
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                        .padding(horizontal = 14.dp)
                ) {
                    when (selectedPillar) {
                        MenuNavPillar.LOBBY -> LobbyContent(
                            theme = theme,
                            isLandscape = false,
                            activeSymbol = activeSymbol,
                            activeFormat = activeFormat,
                            activeModel = activeModel,
                            onSelectSymbol = { activeSymbol = it },
                            onSelectFormat = { activeFormat = it },
                            onLaunch = { onLaunchGame(activeFormat.id, activeSymbol, activeModel) }
                        )
                        MenuNavPillar.LOADOUT -> LoadoutContent(
                            theme = theme,
                            activeModel = activeModel,
                            onSelectModel = { activeModel = it }
                        )
                        MenuNavPillar.BARRACKS -> BarracksContent(theme = theme)
                        MenuNavPillar.ACADEMY -> AcademyContent(
                            theme = theme,
                            onLaunchDrill = onOpenAcademyDrill
                        )
                        MenuNavPillar.STORE -> StoreContent(theme = theme)
                    }
                }

                // Bottom Navigation Bar
                Surface(
                    color = theme.bg,
                    border = BorderStroke(1.dp, theme.grid.copy(alpha = 0.25f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp),
                        horizontalArrangement = Arrangement.SpaceAround
                    ) {
                        MenuNavPillar.values().forEach { pillar ->
                            val isSelected = selectedPillar == pillar
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                modifier = Modifier.clickable { selectedPillar = pillar }
                            ) {
                                Text(pillar.icon, fontSize = 18.sp)
                                Text(
                                    text = pillar.label,
                                    color = if (isSelected) theme.bull else theme.muted,
                                    fontSize = 10.sp,
                                    fontWeight = if (isSelected) FontWeight.Black else FontWeight.Bold
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

// ==================== SUB-VIEWS (LOBBY, LOADOUT, BARRACKS, ACADEMY, STORE) ====================

@Composable
fun LobbyContent(
    theme: GameTheme,
    isLandscape: Boolean,
    activeSymbol: MarketSymbolCatalog.MarketSymbol,
    activeFormat: OpponentDuelEngine.MatchFormat,
    activeModel: TradingModelManager.CustomTradingModel,
    onSelectSymbol: (MarketSymbolCatalog.MarketSymbol) -> Unit,
    onSelectFormat: (OpponentDuelEngine.MatchFormat) -> Unit,
    onLaunch: () -> Unit
) {
    LazyColumn(
        verticalArrangement = Arrangement.spacedBy(10.dp),
        modifier = Modifier.fillMaxSize()
    ) {
        item {
            Text(
                text = "1. SELECT MATCH FORMAT",
                color = theme.muted,
                fontSize = 11.sp,
                fontWeight = FontWeight.Black
            )
            Spacer(modifier = Modifier.height(4.dp))
            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                items(OpponentDuelEngine.MATCH_FORMATS) { fmt ->
                    val isSelected = activeFormat.id == fmt.id
                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = if (isSelected) theme.bull.copy(alpha = 0.2f) else theme.grid.copy(alpha = 0.1f),
                        border = BorderStroke(1.dp, if (isSelected) theme.bull else theme.grid.copy(alpha = 0.2f)),
                        modifier = Modifier
                            .width(160.dp)
                            .clickable { onSelectFormat(fmt) }
                    ) {
                        Column(modifier = Modifier.padding(10.dp)) {
                            Text(text = fmt.badge, color = theme.bull, fontSize = 12.sp, fontWeight = FontWeight.Black)
                            Text(text = fmt.title, color = theme.text, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            Text(text = fmt.subtitle, color = theme.muted, fontSize = 9.sp)
                        }
                    }
                }
            }
        }

        item {
            Text(
                text = "2. ASSET ARENA & MARKET PAIR",
                color = theme.muted,
                fontSize = 11.sp,
                fontWeight = FontWeight.Black
            )
            Spacer(modifier = Modifier.height(4.dp))
            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                items(MarketSymbolCatalog.ALL_SYMBOLS) { sym ->
                    val isSelected = activeSymbol.id == sym.id
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = if (isSelected) theme.bull.copy(alpha = 0.2f) else theme.grid.copy(alpha = 0.1f),
                        border = BorderStroke(1.dp, if (isSelected) theme.bull else theme.grid.copy(alpha = 0.2f)),
                        modifier = Modifier.clickable { onSelectSymbol(sym) }
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Text(sym.badge, fontSize = 12.sp)
                            Column {
                                Text(sym.id, color = if (isSelected) theme.bull else theme.text, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                Text("${sym.volatilityTier} · \$${sym.basePrice.toInt()}", color = theme.muted, fontSize = 9.sp)
                            }
                        }
                    }
                }
            }
        }

        item {
            // Daily Shuffled Historical Database Regime & Market Session
            val regime = MarketSessionEngine.getDailyMarketRegime()
            var activeSession by remember { mutableStateOf(MarketSessionEngine.activeSession) }
            var activeTf by remember { mutableStateOf(MarketSessionEngine.activeTimeframe) }

            Text(
                text = "3. DAILY SHUFFLED DATABASE REGIME",
                color = theme.muted,
                fontSize = 11.sp,
                fontWeight = FontWeight.Black
            )
            Spacer(modifier = Modifier.height(4.dp))
            Surface(
                shape = RoundedCornerShape(10.dp),
                color = theme.grid.copy(alpha = 0.1f),
                border = BorderStroke(1.dp, theme.bull.copy(alpha = 0.3f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(10.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(regime.regimeName, color = theme.bull, fontSize = 11.sp, fontWeight = FontWeight.Black)
                        Text(regime.historicalDate, color = theme.text, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                    }
                    Text(regime.description, color = theme.muted, fontSize = 9.sp)
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Market Session Killzones
            Text(
                text = "4. MARKET SESSION KILLZONE",
                color = theme.muted,
                fontSize = 11.sp,
                fontWeight = FontWeight.Black
            )
            Spacer(modifier = Modifier.height(4.dp))
            LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                items(MarketSessionEngine.MarketSession.values()) { sess ->
                    val isSel = activeSession == sess
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = if (isSel) theme.bull.copy(alpha = 0.2f) else theme.grid.copy(alpha = 0.08f),
                        border = BorderStroke(1.dp, if (isSel) theme.bull else theme.grid.copy(alpha = 0.2f)),
                        modifier = Modifier.clickable {
                            activeSession = sess
                            MarketSessionEngine.activeSession = sess
                        }
                    ) {
                        Column(modifier = Modifier.padding(8.dp)) {
                            Text(sess.sessionName, color = if (isSel) theme.bull else theme.text, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                            Text("${sess.utcHours} · ${sess.volatilityMul}x Vol", color = theme.muted, fontSize = 8.sp)
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Resolution Timeframes
            Text(
                text = "5. RESOLUTION TIMEFRAME",
                color = theme.muted,
                fontSize = 11.sp,
                fontWeight = FontWeight.Black
            )
            Spacer(modifier = Modifier.height(4.dp))
            LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                items(MarketSessionEngine.Timeframe.values()) { tf ->
                    val isSel = activeTf == tf
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = if (isSel) theme.bull.copy(alpha = 0.2f) else theme.grid.copy(alpha = 0.08f),
                        border = BorderStroke(1.dp, if (isSel) theme.bull else theme.grid.copy(alpha = 0.2f)),
                        modifier = Modifier.clickable {
                            activeTf = tf
                            MarketSessionEngine.activeTimeframe = tf
                        }
                    ) {
                        Text(
                            text = "${tf.displayName} (${tf.code})",
                            color = if (isSel) theme.bull else theme.text,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp)
                        )
                    }
                }
            }
        }

        item {
            // Selected Model Loadout Card
            Text(
                text = "6. EQUIPPED TRADING MODEL LOADOUT",
                color = theme.muted,
                fontSize = 11.sp,
                fontWeight = FontWeight.Black
            )
            Spacer(modifier = Modifier.height(4.dp))
            Surface(
                shape = RoundedCornerShape(10.dp),
                color = theme.grid.copy(alpha = 0.12f),
                border = BorderStroke(1.dp, theme.grid.copy(alpha = 0.25f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(activeModel.name, color = theme.text, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        Text(
                            text = "SETUP: ${activeModel.setupKey.uppercase()} | LEV: ${activeModel.leverage.toInt()}x | ${activeModel.riskProtocol.targetMultiple}",
                            color = theme.bull,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                    Text("READY ⚡", color = theme.bull, fontSize = 11.sp, fontWeight = FontWeight.Black)
                }
            }
        }

        item {
            Spacer(modifier = Modifier.height(6.dp))
            Button(
                onClick = onLaunch,
                colors = ButtonDefaults.buttonColors(containerColor = theme.bull),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
            ) {
                Text(
                    text = "DEPLOY SESSION DRILL · ${activeFormat.badge}",
                    color = if (theme.isLight) Color.White else Color.Black,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Black,
                    letterSpacing = 1.sp
                )
            }
        }
    }
}

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
                text = "TRADING MODEL BENCH (LOADOUT CLASS BUILDER)",
                color = theme.bull,
                fontSize = 14.sp,
                fontWeight = FontWeight.Black
            )
            Text(
                text = "Equip or customize your primary setup model, confluence filter, and risk protocols",
                color = theme.muted,
                fontSize = 10.sp
            )
        }

        items(TradingModelManager.PREBUILT_MODELS) { model ->
            val isSelected = activeModel.id == model.id
            Surface(
                shape = RoundedCornerShape(10.dp),
                color = if (isSelected) theme.bull.copy(alpha = 0.18f) else theme.grid.copy(alpha = 0.08f),
                border = BorderStroke(1.dp, if (isSelected) theme.bull else theme.grid.copy(alpha = 0.2f)),
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
                        Text(model.name, color = theme.text, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        Text(
                            text = if (isSelected) "EQUIPPED" else "EQUIP",
                            color = if (isSelected) theme.bull else theme.muted,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Black
                        )
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Library: ${model.library.displayName} · Symbol: ${model.targetSymbolId}",
                        color = theme.muted,
                        fontSize = 9.sp
                    )
                    Text(
                        text = "Rules: ${model.orderType.displayName} | ${model.leverage.toInt()}x Lev | ${model.riskProtocol.displayName}",
                        color = theme.bull,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
        }
    }
}

@Composable
fun BarracksContent(theme: GameTheme) {
    val p = PlayerProfileManager.currentProfile
    LazyColumn(
        verticalArrangement = Arrangement.spacedBy(10.dp),
        modifier = Modifier.fillMaxSize()
    ) {
        item {
            // Calling Card / Dossier Banner
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = theme.grid.copy(alpha = 0.15f),
                border = BorderStroke(1.5.dp, theme.bull),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(p.avatarEmoji, fontSize = 34.sp)
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            Text(p.deskTag, color = theme.bull, fontSize = 11.sp, fontWeight = FontWeight.Black)
                            Text(p.username, color = theme.text, fontSize = 14.sp, fontWeight = FontWeight.Black)
                        }
                        Text("${p.rankTitle} · ${p.mmrRating} MMR", color = theme.muted, fontSize = 10.sp)
                    }
                }
            }
        }

        item {
            // Operator Skins Customizer
            var activeOp by remember { mutableStateOf(CharacterCustomizationManager.getEquippedOperator()) }
            var activeSuit by remember { mutableStateOf(CharacterCustomizationManager.getEquippedSuit()) }
            var activeWatch by remember { mutableStateOf(CharacterCustomizationManager.getEquippedWatch()) }

            Text(
                text = "OPERATOR SKINS & SHOW-OFF WEARABLES",
                color = theme.bull,
                fontSize = 11.sp,
                fontWeight = FontWeight.Black
            )
            Spacer(modifier = Modifier.height(4.dp))
            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                items(CharacterCustomizationManager.ALL_OPERATORS) { op ->
                    val isSel = activeOp.id == op.id
                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = if (isSel) theme.bull.copy(alpha = 0.2f) else theme.grid.copy(alpha = 0.08f),
                        border = BorderStroke(1.dp, if (isSel) theme.bull else theme.grid.copy(alpha = 0.2f)),
                        modifier = Modifier
                            .width(150.dp)
                            .clickable {
                                CharacterCustomizationManager.equipOperator(op.id)
                                activeOp = op
                            }
                    ) {
                        Column(modifier = Modifier.padding(10.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(op.avatarEmoji, fontSize = 22.sp)
                                Text(op.rarity.badge, fontSize = 12.sp)
                            }
                            Text(op.codename, color = if (isSel) theme.bull else theme.text, fontSize = 11.sp, fontWeight = FontWeight.Black)
                            Text(op.title, color = theme.muted, fontSize = 9.sp)
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(if (isSel) "EQUIPPED" else "TAP TO EQUIP", color = if (isSel) theme.bull else theme.muted, fontSize = 8.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Wearables Locker (Suit, Watch, Eyewear)
            Text(
                text = "EQUIPPED TACTICAL WEARABLES (STAT BUFFS)",
                color = theme.muted,
                fontSize = 10.sp,
                fontWeight = FontWeight.Black
            )
            Spacer(modifier = Modifier.height(4.dp))
            CharacterCustomizationManager.ALL_WEARABLES.forEach { wear ->
                val isEquipped = wear.id == activeSuit.id || wear.id == activeWatch.id
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = if (isEquipped) theme.bull.copy(alpha = 0.12f) else theme.grid.copy(alpha = 0.06f),
                    border = BorderStroke(1.dp, if (isEquipped) theme.bull.copy(alpha = 0.5f) else theme.grid.copy(alpha = 0.15f)),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 3.dp)
                        .clickable {
                            CharacterCustomizationManager.equipWearable(wear.id)
                            activeSuit = CharacterCustomizationManager.getEquippedSuit()
                            activeWatch = CharacterCustomizationManager.getEquippedWatch()
                        }
                ) {
                    Row(
                        modifier = Modifier.padding(8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Text(wear.icon, fontSize = 18.sp)
                            Column {
                                Text(wear.name, color = theme.text, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                Text(wear.statBuffDescription, color = theme.bull, fontSize = 9.sp)
                            }
                        }
                        Text(if (isEquipped) "EQUIPPED" else "EQUIP", color = if (isEquipped) theme.bull else theme.muted, fontSize = 9.sp, fontWeight = FontWeight.Black)
                    }
                }
            }
        }

        item {
            Text(
                text = "CAREER TRADING STATS (PROFIT FACTOR)",
                color = theme.muted,
                fontSize = 10.sp,
                fontWeight = FontWeight.Black
            )
            Spacer(modifier = Modifier.height(4.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                Surface(shape = RoundedCornerShape(8.dp), color = theme.grid.copy(alpha = 0.08f), modifier = Modifier.weight(1f)) {
                    Column(modifier = Modifier.padding(8.dp)) {
                        Text("WIN RATE", color = theme.muted, fontSize = 9.sp)
                        Text("${(p.careerWinRate * 100).toInt()}%", color = theme.bull, fontSize = 14.sp, fontWeight = FontWeight.Black)
                    }
                }
                Surface(shape = RoundedCornerShape(8.dp), color = theme.grid.copy(alpha = 0.08f), modifier = Modifier.weight(1f)) {
                    Column(modifier = Modifier.padding(8.dp)) {
                        Text("PROFIT FACTOR", color = theme.muted, fontSize = 9.sp)
                        Text(String.format("%.2f", p.profitFactor), color = theme.bull, fontSize = 14.sp, fontWeight = FontWeight.Black)
                    }
                }
                Surface(shape = RoundedCornerShape(8.dp), color = theme.grid.copy(alpha = 0.08f), modifier = Modifier.weight(1f)) {
                    Column(modifier = Modifier.padding(8.dp)) {
                        Text("BEST STREAK", color = theme.muted, fontSize = 9.sp)
                        Text("${p.bestWinStreak} 🔥", color = theme.text, fontSize = 14.sp, fontWeight = FontWeight.Black)
                    }
                }
            }
        }

        item {
            Text(text = "ACHIEVEMENT BADGES & COMMENDATIONS", color = theme.muted, fontSize = 10.sp, fontWeight = FontWeight.Black)
            Spacer(modifier = Modifier.height(4.dp))
            p.unlockedBadges.forEach { badge ->
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = theme.grid.copy(alpha = 0.08f),
                    modifier = Modifier.fillMaxWidth().padding(vertical = 3.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(badge.icon, fontSize = 18.sp)
                        Column {
                            Text(badge.name, color = theme.text, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            Text(badge.description, color = theme.muted, fontSize = 9.sp)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun AcademyContent(
    theme: GameTheme,
    onLaunchDrill: (StrategyCatalog.SetupCardData, Int) -> Unit
) {
    var selectedGroup by remember { mutableStateOf(StrategyCatalog.StrategyFamily.SMC) }
    val setups = remember(selectedGroup) { StrategyCatalog.getSetupsByGroup(selectedGroup) }

    LazyColumn(
        verticalArrangement = Arrangement.spacedBy(8.dp),
        modifier = Modifier.fillMaxSize()
    ) {
        item {
            Text(
                text = "STRATEGY MASTERY ACADEMY (${StrategyMasteryManager.getCertifiedStrategies().size}/${StrategyCatalog.ALL_SETUPS.size} CERTIFIED)",
                color = theme.bull,
                fontSize = 13.sp,
                fontWeight = FontWeight.Black
            )
            Spacer(modifier = Modifier.height(4.dp))
            LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                items(StrategyCatalog.StrategyFamily.values()) { grp ->
                    val isSel = selectedGroup == grp
                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = if (isSel) theme.bull.copy(alpha = 0.2f) else theme.grid.copy(alpha = 0.1f),
                        border = BorderStroke(1.dp, if (isSel) theme.bull else theme.grid.copy(alpha = 0.2f)),
                        modifier = Modifier.clickable { selectedGroup = grp }
                    ) {
                        Text(
                            text = grp.displayName,
                            color = if (isSel) theme.bull else theme.text,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }
                }
            }
        }

        items(setups) { s ->
            val prog = StrategyMasteryManager.getProgress(s.key)
            Surface(
                shape = RoundedCornerShape(10.dp),
                color = theme.grid.copy(alpha = 0.08f),
                border = BorderStroke(1.dp, theme.grid.copy(alpha = 0.2f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(10.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(s.name, color = theme.text, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        Text(
                            text = if (prog.isCertifiedForMultiplayer) "CERTIFIED 🏆" else "TIER ${prog.tier}/5",
                            color = if (prog.isCertifiedForMultiplayer) theme.bull else theme.muted,
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Black
                        )
                    }
                    Text(s.lesson, color = theme.muted, fontSize = 9.sp)
                    Spacer(modifier = Modifier.height(4.dp))
                    Button(
                        onClick = { onLaunchDrill(s, prog.tier) },
                        colors = ButtonDefaults.buttonColors(containerColor = theme.grid.copy(alpha = 0.2f)),
                        shape = RoundedCornerShape(6.dp),
                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp)
                    ) {
                        Text("LAUNCH TIER ${prog.tier} DRILL", color = theme.text, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

@Composable
fun StoreContent(theme: GameTheme) {
    var deskCredits by remember { mutableStateOf(MonetizationManager.deskCredits) }
    var activeChannel by remember { mutableStateOf(PaymentGatewayManager.activePaymentChannel) }
    var purchaseStatus by remember { mutableStateOf<String?>(null) }

    LazyColumn(
        verticalArrangement = Arrangement.spacedBy(10.dp),
        modifier = Modifier.fillMaxSize()
    ) {
        item {
            Text(
                text = "DESK VAULT & PAYMENT GATEWAYS",
                color = Color(0xFFFFD700),
                fontSize = 14.sp,
                fontWeight = FontWeight.Black
            )
            Text(
                text = MonetizationManager.GOOGLE_PLAY_COMPLIANCE_DISCLAIMER,
                color = theme.muted,
                fontSize = 8.sp
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Payment Channel Selection (Google Play vs Open Web vs Desk Credits)
            Text(
                text = "SELECT ACTIVE PAYMENT GATEWAY CHANNEL",
                color = theme.muted,
                fontSize = 10.sp,
                fontWeight = FontWeight.Black
            )
            Spacer(modifier = Modifier.height(4.dp))
            LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                items(PaymentGatewayManager.PaymentChannel.values()) { ch ->
                    val isSel = activeChannel == ch
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = if (isSel) theme.bull.copy(alpha = 0.22f) else theme.grid.copy(alpha = 0.08f),
                        border = BorderStroke(1.dp, if (isSel) theme.bull else theme.grid.copy(alpha = 0.2f)),
                        modifier = Modifier.clickable {
                            activeChannel = ch
                            PaymentGatewayManager.activePaymentChannel = ch
                        }
                    ) {
                        Column(modifier = Modifier.padding(8.dp)) {
                            Text(ch.label, color = if (isSel) theme.bull else theme.text, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                            Text(ch.badge, color = theme.muted, fontSize = 8.sp)
                        }
                    }
                }
            }

            purchaseStatus?.let { status ->
                Spacer(modifier = Modifier.height(6.dp))
                Surface(
                    shape = RoundedCornerShape(6.dp),
                    color = theme.bull.copy(alpha = 0.15f),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = status,
                        color = theme.bull,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(8.dp)
                    )
                }
            }
        }

        items(MonetizationManager.STORE_CATALOG) { item ->
            Surface(
                shape = RoundedCornerShape(10.dp),
                color = theme.grid.copy(alpha = 0.08f),
                border = BorderStroke(1.dp, theme.grid.copy(alpha = 0.2f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        Text(item.icon, fontSize = 20.sp)
                        Column {
                            Text(item.title, color = theme.text, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            Text(item.description, color = theme.muted, fontSize = 9.sp)
                            Text("Fiat: ${item.priceUsdEquivalent} · Credits: 🪙 ${item.priceCredits}", color = theme.bull, fontSize = 8.sp, fontWeight = FontWeight.SemiBold)
                        }
                    }

                    Button(
                        onClick = {
                            PaymentGatewayManager.executePurchase(
                                itemId = item.id,
                                channel = activeChannel,
                                onSuccess = { tx ->
                                    deskCredits = MonetizationManager.deskCredits
                                    purchaseStatus = "SUCCESS: Acquired ${tx.itemTitle} via ${tx.channel.badge}!"
                                },
                                onFailure = { err ->
                                    purchaseStatus = "FAILED: $err"
                                }
                            )
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (item.isPurchased) theme.grid.copy(alpha = 0.2f) else theme.bull
                        ),
                        shape = RoundedCornerShape(8.dp),
                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = if (item.isPurchased) "OWNED" else when (activeChannel) {
                                PaymentGatewayManager.PaymentChannel.DESK_CREDITS -> "🪙 ${item.priceCredits}"
                                PaymentGatewayManager.PaymentChannel.GOOGLE_PLAY_BILLING -> "BUY ${item.priceUsdEquivalent}"
                                PaymentGatewayManager.PaymentChannel.OPEN_WEB_GATEWAY -> "WEB ${item.priceUsdEquivalent}"
                            },
                            color = if (item.isPurchased) theme.muted else if (theme.isLight) Color.White else Color.Black,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Black
                        )
                    }
                }
            }
        }
    }
}
