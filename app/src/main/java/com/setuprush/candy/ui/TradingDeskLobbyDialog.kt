package com.setuprush.candy.ui

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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.setuprush.candy.engine.MarketSymbolCatalog
import com.setuprush.candy.engine.PlayerProfileManager
import com.setuprush.candy.engine.StrategyCatalog
import com.setuprush.candy.engine.TradingModelManager
import com.setuprush.candy.theme.GameTheme

enum class CompetitiveTradingFormat(val id: String, val title: String, val badge: String, val desc: String) {
    PROP_DUEL_1V1("1v1", "1v1 Head-to-Head Prop Duel", "1v1", "Best of 3 rounds · Identical chart seed · Ranked MMR"),
    PORTFOLIO_SQUAD_5V5("5v5", "5v5 Multi-Asset Portfolio Clash", "5v5", "Asset assignment · Pooled team equity · 5 market desks"),
    MARGIN_CALL_TOURNAMENT("tournament", "Margin Call: 100-Trader Tournament", "TOURNAMENT", "100 Traders · Dynamic margin threshold · Last solvent trader standing")
}

@Composable
fun TradingDeskLobbyDialog(
    theme: GameTheme,
    onDeployModel: (CompetitiveTradingFormat, TradingModelManager.CustomTradingModel, MarketSymbolCatalog.MarketSymbol) -> Unit,
    onDismiss: () -> Unit
) {
    val profile = PlayerProfileManager.currentProfile
    var selectedFormat by remember { mutableStateOf(CompetitiveTradingFormat.PORTFOLIO_SQUAD_5V5) }
    var selectedSymbol by remember { mutableStateOf(MarketSymbolCatalog.activeSymbol) }
    var activeModel by remember { mutableStateOf(TradingModelManager.activeTradingModel) }

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(20.dp),
            color = theme.bg,
            border = androidx.compose.foundation.BorderStroke(1.dp, theme.grid.copy(alpha = 0.5f)),
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.95f)
                .padding(vertical = 8.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(18.dp)
            ) {
                // ==================== 1. TRADER PROFILE & ACCREDITATION BANNER ====================
                Surface(
                    shape = RoundedCornerShape(14.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, theme.bull.copy(alpha = 0.4f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(
                                Brush.horizontalGradient(
                                    listOf(
                                        theme.bull.copy(alpha = 0.20f),
                                        theme.grid.copy(alpha = 0.12f),
                                        theme.bg
                                    )
                                )
                            )
                            .padding(14.dp)
                    ) {
                        Column {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Surface(
                                        shape = RoundedCornerShape(6.dp),
                                        color = theme.bull
                                    ) {
                                        Text(
                                            text = "[${profile.deskTag}]",
                                            color = if (theme.isLight) Color.White else Color.Black,
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Black,
                                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                        )
                                    }
                                    Text(
                                        text = profile.username,
                                        color = theme.text,
                                        fontSize = 16.sp,
                                        fontWeight = FontWeight.Black
                                    )
                                }

                                Surface(
                                    shape = RoundedCornerShape(6.dp),
                                    color = theme.grid.copy(alpha = 0.25f)
                                ) {
                                    Text(
                                        text = "LV ${profile.deskLevel} · ${profile.rankingTier.uppercase()}",
                                        color = theme.muted,
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                    )
                                }
                            }

                            Text(
                                text = "“${profile.traderTitle}”",
                                color = theme.bull,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.SemiBold,
                                modifier = Modifier.padding(top = 2.dp)
                            )

                            Text(
                                text = profile.bio,
                                color = theme.muted,
                                fontSize = 10.sp,
                                modifier = Modifier.padding(top = 2.dp)
                            )

                            // Verified Accreditation Badges Row
                            Spacer(modifier = Modifier.height(8.dp))
                            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                profile.showcaseBadges.forEach { badge ->
                                    Surface(
                                        shape = RoundedCornerShape(8.dp),
                                        color = theme.bg.copy(alpha = 0.7f),
                                        border = androidx.compose.foundation.BorderStroke(1.dp, theme.bull.copy(alpha = 0.3f))
                                    ) {
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(4.dp),
                                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp)
                                        ) {
                                            Text(badge.icon, fontSize = 11.sp)
                                            Text(badge.title, color = theme.text, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Scrollable content
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                ) {
                    // ==================== 2. COMPETITIVE TRADING FORMAT ====================
                    item {
                        Text(
                            text = "1. COMPETITIVE TRADING FORMAT",
                            color = theme.muted,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Black,
                            letterSpacing = 1.sp
                        )
                        Spacer(modifier = Modifier.height(6.dp))

                        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            CompetitiveTradingFormat.values().forEach { format ->
                                val isSelected = selectedFormat == format
                                Surface(
                                    shape = RoundedCornerShape(10.dp),
                                    color = if (isSelected) theme.bull.copy(alpha = 0.14f) else theme.grid.copy(alpha = 0.05f),
                                    border = androidx.compose.foundation.BorderStroke(
                                        1.dp,
                                        if (isSelected) theme.bull else theme.grid.copy(alpha = 0.2f)
                                    ),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable { selectedFormat = format }
                                ) {
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(10.dp),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Column(modifier = Modifier.weight(1f)) {
                                            Row(
                                                verticalAlignment = Alignment.CenterVertically,
                                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                                            ) {
                                                Text(
                                                    text = format.title,
                                                    color = if (isSelected) theme.bull else theme.text,
                                                    fontSize = 12.sp,
                                                    fontWeight = FontWeight.Black
                                                )
                                                Surface(
                                                    shape = RoundedCornerShape(4.dp),
                                                    color = theme.grid.copy(alpha = 0.2f)
                                                ) {
                                                    Text(
                                                        text = format.badge,
                                                        color = theme.muted,
                                                        fontSize = 9.sp,
                                                        fontWeight = FontWeight.Black,
                                                        modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
                                                    )
                                                }
                                            }
                                            Text(
                                                text = format.desc,
                                                color = theme.muted,
                                                fontSize = 10.sp
                                            )
                                        }

                                        if (isSelected) {
                                            Text("SELECTED", color = theme.bull, fontSize = 10.sp, fontWeight = FontWeight.Black)
                                        }
                                    }
                                }
                            }
                        }
                    }

                    // ==================== 3. SYMBOL / TRADING PAIR SELECTION ====================
                    item {
                        Text(
                            text = "2. MARKET SYMBOL / ASSET SELECTION",
                            color = theme.muted,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Black,
                            letterSpacing = 1.sp
                        )
                        Spacer(modifier = Modifier.height(6.dp))

                        LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            items(MarketSymbolCatalog.ALL_SYMBOLS) { sym ->
                                val isSelected = selectedSymbol.id == sym.id
                                Surface(
                                    shape = RoundedCornerShape(10.dp),
                                    color = if (isSelected) theme.bull.copy(alpha = 0.2f) else theme.grid.copy(alpha = 0.08f),
                                    border = androidx.compose.foundation.BorderStroke(
                                        1.dp,
                                        if (isSelected) theme.bull else theme.grid.copy(alpha = 0.2f)
                                    ),
                                    modifier = Modifier.clickable {
                                        selectedSymbol = sym
                                        MarketSymbolCatalog.selectSymbol(sym.id)
                                    }
                                ) {
                                    Column(modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)) {
                                        Text(
                                            text = sym.pair,
                                            color = if (isSelected) theme.bull else theme.text,
                                            fontSize = 12.sp,
                                            fontWeight = FontWeight.Black
                                        )
                                        Text(
                                            text = "$${String.format("%,.1f", sym.basePrice)}",
                                            color = theme.text.copy(alpha = 0.8f),
                                            fontSize = 10.sp,
                                            fontWeight = FontWeight.SemiBold
                                        )
                                        Text(
                                            text = sym.role,
                                            color = theme.muted,
                                            fontSize = 9.sp
                                        )
                                    }
                                }
                            }
                        }
                    }

                    // ==================== 4. TRADING MODEL PRESETS & SPECIFICATION ====================
                    item {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "3. TRADING MODEL PRESETS",
                                color = theme.muted,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Black,
                                letterSpacing = 1.sp
                            )

                            Text(
                                text = "ACTIVE: ${activeModel.name.uppercase()}",
                                color = theme.bull,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        Spacer(modifier = Modifier.height(6.dp))

                        // Preset Switcher Row
                        LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            items(TradingModelManager.PREBUILT_MODELS) { model ->
                                val isSelected = activeModel.id == model.id
                                Surface(
                                    shape = RoundedCornerShape(8.dp),
                                    color = if (isSelected) theme.bull.copy(alpha = 0.18f) else theme.grid.copy(alpha = 0.08f),
                                    border = androidx.compose.foundation.BorderStroke(
                                        1.dp,
                                        if (isSelected) theme.bull else theme.grid.copy(alpha = 0.2f)
                                    ),
                                    modifier = Modifier.clickable {
                                        activeModel = model
                                        TradingModelManager.selectModel(model.id)
                                    }
                                ) {
                                    Text(
                                        text = model.name,
                                        color = if (isSelected) theme.bull else theme.text,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        // Model Details Card
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = theme.grid.copy(alpha = 0.06f),
                            border = androidx.compose.foundation.BorderStroke(1.dp, theme.grid.copy(alpha = 0.2f)),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                val setup = StrategyCatalog.findSetup(activeModel.setupKey)
                                val conf = TradingModelManager.getConfluence(activeModel.confluenceIndicatorId)

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Column {
                                        Text("PRIMARY STRATEGY SETUP:", color = theme.muted, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                                        Text(setup?.name ?: activeModel.setupKey.uppercase(), color = theme.text, fontSize = 12.sp, fontWeight = FontWeight.Black)
                                        Text("“${setup?.lesson ?: ""}”", color = theme.muted, fontSize = 10.sp)
                                    }
                                    Surface(shape = RoundedCornerShape(4.dp), color = theme.bull.copy(alpha = 0.2f)) {
                                        Text(activeModel.library.tag, color = theme.bull, fontSize = 8.sp, fontWeight = FontWeight.Black, modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp))
                                    }
                                }

                                Divider(color = theme.grid.copy(alpha = 0.15f), modifier = Modifier.padding(vertical = 8.dp))

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Column {
                                        Text("CONFLUENCE FILTER:", color = theme.muted, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                                        Text(conf?.displayName ?: "None", color = theme.bull, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                        Text(activeModel.orderType.displayName, color = theme.muted, fontSize = 10.sp)
                                    }
                                    Column(horizontalAlignment = Alignment.End) {
                                        Text("MARGIN & RISK RULE:", color = theme.muted, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                                        Text("${activeModel.leverage.toInt()}x LEVERAGE", color = theme.text, fontSize = 11.sp, fontWeight = FontWeight.Black)
                                        Text(activeModel.riskProtocol.displayName, color = theme.bull, fontSize = 10.sp, fontWeight = FontWeight.SemiBold)
                                    }
                                }
                            }
                        }
                    }
                }

                // ==================== 5. DEPLOY ACTION BUTTON ====================
                Spacer(modifier = Modifier.height(10.dp))
                Button(
                    onClick = {
                        onDeployModel(selectedFormat, activeModel, selectedSymbol)
                        onDismiss()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = theme.bull),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                ) {
                    Text(
                        text = "DEPLOY MODEL TO MARKET (${selectedSymbol.id} · ${selectedFormat.badge})",
                        color = if (theme.isLight) Color.White else Color.Black,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Black,
                        letterSpacing = 1.sp
                    )
                }
            }
        }
    }
}
