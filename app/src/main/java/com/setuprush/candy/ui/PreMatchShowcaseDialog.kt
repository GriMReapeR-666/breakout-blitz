package com.setuprush.candy.ui

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
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
import com.setuprush.candy.engine.*
import com.setuprush.candy.theme.GameTheme
import kotlinx.coroutines.delay

/**
 * AAA Pre-Match Operator Intro & Tactical Calling Card Showcase Dialog.
 * Displays before each match deployment:
 * - Animated Operator Avatar & Entry Quote
 * - Equipped Wearables (Suit, Luxury Tourbillon, AR Goggles, Holo-Dock)
 * - Equipped Trading Model (Style Class, Leverage, Risk Armor)
 * - Trader Stats: Profit Factor, Net R-Volume, MAFE Accuracy
 * - Facing matched Rival Desk Trader with a 3-second deployment countdown.
 */
@Composable
fun PreMatchShowcaseDialog(
    theme: GameTheme,
    formatTitle: String,
    symbol: MarketSymbolCatalog.MarketSymbol,
    model: TradingModelManager.CustomTradingModel,
    onDeployMatch: () -> Unit
) {
    val p = PlayerProfileManager.currentProfile
    val operator = CharacterCustomizationManager.getEquippedOperator()
    val suit = CharacterCustomizationManager.getEquippedSuit()
    val watch = CharacterCustomizationManager.getEquippedWatch()
    val eyewear = CharacterCustomizationManager.getEquippedEyewear()
    val rival = OpponentDuelEngine.activeRival ?: OpponentDuelEngine.RivalTrader(
        id = "rival_default",
        name = "Satoshi_Sniper",
        deskTag = "TITAN",
        mmr = p.mmrRating + 25,
        targetSetup = "smc_fvg",
        winRate = 0.74,
        avgRMultiple = 2.4,
        preferredStyle = "SCALPER",
        avatar = "🥷"
    )

    var countdownSeconds by remember { mutableIntStateOf(3) }

    LaunchedEffect(Unit) {
        while (countdownSeconds > 0) {
            delay(1000L)
            countdownSeconds--
        }
        onDeployMatch()
    }

    Dialog(onDismissRequest = onDeployMatch) {
        Surface(
            shape = RoundedCornerShape(20.dp),
            color = theme.bg,
            border = BorderStroke(2.dp, theme.bull),
            modifier = Modifier
                .fillMaxWidth()
                .padding(10.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(18.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Top Format Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "⚡ PRE-MATCH OPERATOR INTRO",
                        color = theme.bull,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Black,
                        letterSpacing = 1.sp
                    )
                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = theme.bull.copy(alpha = 0.2f)
                    ) {
                        Text(
                            text = "STARTING IN ${countdownSeconds}S",
                            color = theme.bull,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Black,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Versus Banner (Player vs Rival)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Player Side
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.weight(1f)
                    ) {
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = theme.bull.copy(alpha = 0.15f),
                            border = BorderStroke(1.5.dp, theme.bull),
                            modifier = Modifier.size(64.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Text(operator.avatarEmoji, fontSize = 34.sp)
                            }
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "[${p.deskTag}] ${p.username}",
                            color = theme.text,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Black
                        )
                        Text(
                            text = "${p.mmrRating} MMR · ${operator.codename}",
                            color = theme.bull,
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    // VS Emblem
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = "VS",
                            color = Color(0xFFFFD700),
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Black
                        )
                        Text(
                            text = symbol.id,
                            color = theme.muted,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    // Rival Side
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.weight(1f)
                    ) {
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = theme.bear.copy(alpha = 0.15f),
                            border = BorderStroke(1.5.dp, theme.bear),
                            modifier = Modifier.size(64.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Text(rival.avatar, fontSize = 34.sp)
                            }
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "[${rival.deskTag}] ${rival.name}",
                            color = theme.text,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Black
                        )
                        Text(
                            text = "${rival.mmr} MMR · ${rival.preferredStyle}",
                            color = theme.bear,
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Operator Intro Quote
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = theme.grid.copy(alpha = 0.12f),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "\"${operator.introQuote}\"",
                        color = theme.text,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Medium,
                        modifier = Modifier.padding(10.dp)
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Equipped Wearables Showcase
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Surface(shape = RoundedCornerShape(6.dp), color = theme.grid.copy(alpha = 0.08f), modifier = Modifier.weight(1f)) {
                        Row(modifier = Modifier.padding(6.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                            Text(suit.icon, fontSize = 12.sp)
                            Text(suit.name.take(12), color = theme.text, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                    Surface(shape = RoundedCornerShape(6.dp), color = theme.grid.copy(alpha = 0.08f), modifier = Modifier.weight(1f)) {
                        Row(modifier = Modifier.padding(6.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                            Text(watch.icon, fontSize = 12.sp)
                            Text(watch.name.take(12), color = theme.text, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                    Surface(shape = RoundedCornerShape(6.dp), color = theme.grid.copy(alpha = 0.08f), modifier = Modifier.weight(1f)) {
                        Row(modifier = Modifier.padding(6.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                            Text(eyewear.icon, fontSize = 12.sp)
                            Text(eyewear.name.take(12), color = theme.text, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Equipped Trading Model Loadout
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = theme.bull.copy(alpha = 0.12f),
                    border = BorderStroke(1.dp, theme.bull.copy(alpha = 0.4f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(10.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "ACTIVE MODEL: ${model.name.uppercase()}",
                                color = theme.text,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Black
                            )
                            Text(
                                text = "SETUP: ${model.setupKey.uppercase()} | ${model.leverage.toInt()}x LEV | ${model.riskProtocol.targetMultiple}",
                                color = theme.bull,
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        Text("ACTIVE ⚡", color = theme.bull, fontSize = 10.sp, fontWeight = FontWeight.Black)
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Instant Deploy Button
                Button(
                    onClick = onDeployMatch,
                    colors = ButtonDefaults.buttonColors(containerColor = theme.bull),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(44.dp)
                ) {
                    Text(
                        text = "DEPLOY INTO TRADING NOW",
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
