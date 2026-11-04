package com.setuprush.candy.ui

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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.setuprush.candy.engine.MatchPerformanceEngine
import com.setuprush.candy.theme.GameTheme
import kotlin.math.abs

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

@Composable
fun PostMatchDebriefDialog(
    theme: GameTheme,
    debrief: MatchPerformanceEngine.MatchDebrief,
    onPlayAgain: () -> Unit,
    onOpenLobby: () -> Unit,
    onDismiss: () -> Unit,
    onReplayTape: () -> Unit = {}
) {
    var selectedTab by remember { mutableIntStateOf(0) } // 0 = Chess.com Review, 1 = Stats & Scoreboard
    var selectedMoveIndex by remember { mutableIntStateOf(0) }
    val p = debrief.playerStats
    val isVictory = debrief.outcome == MatchPerformanceEngine.MatchOutcome.VICTORY ||
            debrief.outcome == MatchPerformanceEngine.MatchOutcome.SOLE_SURVIVOR ||
            debrief.outcome == MatchPerformanceEngine.MatchOutcome.QUALIFIED

    val trades = debrief.tradeReviews

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(16.dp),
            color = deskCarbonBg,
            border = BorderStroke(1.5.dp, if (isVictory) deskGreen else deskOrange),
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.95f)
                .padding(vertical = 4.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(14.dp)
            ) {
                // ==================== 1. MATCH OUTCOME BANNER ====================
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    border = BorderStroke(1.dp, if (isVictory) deskGreen.copy(alpha = 0.5f) else deskOrange.copy(alpha = 0.5f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(
                                Brush.horizontalGradient(
                                    listOf(
                                        if (isVictory) deskGreen.copy(alpha = 0.2f) else deskOrange.copy(alpha = 0.2f),
                                        deskPanelBg,
                                        deskCarbonBg
                                    )
                                )
                            )
                            .padding(12.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    Text(debrief.outcome.badge, fontSize = 16.sp)
                                    Text(
                                        text = debrief.outcome.label,
                                        color = if (isVictory) deskGreen else deskOrange,
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.Black,
                                        fontFamily = FontFamily.Monospace,
                                        letterSpacing = 1.sp
                                    )
                                }
                                Text(
                                    text = "${debrief.modeTitle.uppercase()} · ${debrief.marketSymbol}",
                                    color = Color.White,
                                    fontSize = 10.5.sp,
                                    fontWeight = FontWeight.Bold,
                                    fontFamily = FontFamily.Monospace
                                )
                                Text(
                                    text = debrief.feedbackSummary,
                                    color = deskMuted,
                                    fontSize = 8.5.sp
                                )
                            }

                            // Grade Stamp Badge
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = if (isVictory) deskGreen else deskOrange
                            ) {
                                Column(
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                                ) {
                                    Text(
                                        text = "GRADE",
                                        color = Color.Black,
                                        fontSize = 7.5.sp,
                                        fontWeight = FontWeight.Black,
                                        fontFamily = FontFamily.Monospace
                                    )
                                    Text(
                                        text = debrief.performanceGrade,
                                        color = Color.Black,
                                        fontSize = 18.sp,
                                        fontWeight = FontWeight.Black,
                                        fontFamily = FontFamily.Monospace
                                    )
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                // ==================== 2. TAB SWITCHER ====================
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = if (selectedTab == 0) deskOrange.copy(alpha = 0.2f) else deskPanelBg,
                        border = BorderStroke(1.dp, if (selectedTab == 0) deskOrange else deskBorder),
                        modifier = Modifier
                            .weight(1f)
                            .clickable { selectedTab = 0 }
                    ) {
                        Text(
                            text = "♟️ CHESS.COM REVIEW (${trades.size})",
                            color = if (selectedTab == 0) deskOrange else deskMuted,
                            fontSize = 9.5.sp,
                            fontWeight = FontWeight.Black,
                            fontFamily = FontFamily.Monospace,
                            modifier = Modifier.padding(vertical = 8.dp),
                            textAlign = TextAlign.Center
                        )
                    }

                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = if (selectedTab == 1) deskCyan.copy(alpha = 0.2f) else deskPanelBg,
                        border = BorderStroke(1.dp, if (selectedTab == 1) deskCyan else deskBorder),
                        modifier = Modifier
                            .weight(1f)
                            .clickable { selectedTab = 1 }
                    ) {
                        Text(
                            text = "📊 STATS & SCOREBOARD",
                            color = if (selectedTab == 1) deskCyan else deskMuted,
                            fontSize = 9.5.sp,
                            fontWeight = FontWeight.Black,
                            fontFamily = FontFamily.Monospace,
                            modifier = Modifier.padding(vertical = 8.dp),
                            textAlign = TextAlign.Center
                        )
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // ==================== 3. CONTENT AREA ====================
                if (selectedTab == 0) {
                    // CHESS.COM GAME REVIEW TAB
                    LazyColumn(
                        verticalArrangement = Arrangement.spacedBy(10.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f)
                    ) {
                        // Accuracy Header
                        item {
                            Surface(
                                shape = RoundedCornerShape(10.dp),
                                color = deskPanelBg,
                                border = BorderStroke(1.dp, deskOrange),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Column(modifier = Modifier.padding(12.dp)) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Column {
                                            Text(
                                                text = "MATCH ACCURACY RATING",
                                                color = deskMuted,
                                                fontSize = 9.sp,
                                                fontWeight = FontWeight.Black,
                                                fontFamily = FontFamily.Monospace
                                            )
                                            Text(
                                                text = "${String.format("%.1f", debrief.accuracyScore)}%",
                                                color = deskOrange,
                                                fontSize = 24.sp,
                                                fontWeight = FontWeight.Black,
                                                fontFamily = FontFamily.Monospace
                                            )
                                        }

                                        Column(horizontalAlignment = Alignment.End) {
                                            Text(
                                                text = "NET P&L: ${if (p.netPnl >= 0) "+$" else "-$"}${String.format("%.2f", abs(p.netPnl))}",
                                                color = if (p.netPnl >= 0) deskGreen else deskRed,
                                                fontSize = 11.5.sp,
                                                fontWeight = FontWeight.Black,
                                                fontFamily = FontFamily.Monospace
                                            )
                                            Text(
                                                text = "CAPTURED R: ${if (p.rMultiplesCaptured >= 0) "+" else ""}${String.format("%.1f", p.rMultiplesCaptured)}R",
                                                color = deskGold,
                                                fontSize = 9.5.sp,
                                                fontWeight = FontWeight.Bold,
                                                fontFamily = FontFamily.Monospace
                                            )
                                        }
                                    }

                                    Spacer(modifier = Modifier.height(6.dp))

                                    val frac = (debrief.accuracyScore / 100.0).toFloat().coerceIn(0f, 1f)
                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .height(5.dp)
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
                                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp),
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                                        ) {
                                            Text(grade.badge, fontSize = 10.sp)
                                            Text(
                                                text = "${grade.label} ($count)",
                                                color = Color(grade.colorHex),
                                                fontSize = 8.5.sp,
                                                fontWeight = FontWeight.Black,
                                                fontFamily = FontFamily.Monospace
                                            )
                                        }
                                    }
                                }
                            }
                        }

                        if (trades.isEmpty()) {
                            item {
                                Surface(
                                    shape = RoundedCornerShape(10.dp),
                                    color = deskPanelBg,
                                    border = BorderStroke(1.dp, deskBorder),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Column(
                                        modifier = Modifier.padding(20.dp),
                                        horizontalAlignment = Alignment.CenterHorizontally
                                    ) {
                                        Text("📋", fontSize = 24.sp)
                                        Spacer(modifier = Modifier.height(6.dp))
                                        Text(
                                            text = "NO EXECUTED TRADES TO REVIEW",
                                            color = Color.White,
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Bold,
                                            fontFamily = FontFamily.Monospace
                                        )
                                        Text(
                                            text = "Hold BUY or SELL to ride setups and review execution precision.",
                                            color = deskMuted,
                                            fontSize = 9.sp,
                                            textAlign = TextAlign.Center
                                        )
                                    }
                                }
                            }
                        } else {
                            val activeMove = trades.getOrElse(selectedMoveIndex) { trades[0] }

                            // Interactive Move Scrubber Card
                            item {
                                Surface(
                                    shape = RoundedCornerShape(12.dp),
                                    color = deskPanelBg,
                                    border = BorderStroke(1.dp, deskBorder),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Column(modifier = Modifier.padding(12.dp)) {
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
                                                modifier = Modifier.height(28.dp)
                                            ) {
                                                Text("< PREV", fontSize = 8.5.sp, fontFamily = FontFamily.Monospace)
                                            }

                                            Text(
                                                text = "TRADE ${selectedMoveIndex + 1} OF ${trades.size}",
                                                color = Color.White,
                                                fontSize = 10.5.sp,
                                                fontWeight = FontWeight.Black,
                                                fontFamily = FontFamily.Monospace
                                            )

                                            Button(
                                                onClick = { if (selectedMoveIndex < trades.size - 1) selectedMoveIndex++ },
                                                enabled = selectedMoveIndex < trades.size - 1,
                                                colors = ButtonDefaults.buttonColors(containerColor = deskCardBg),
                                                shape = RoundedCornerShape(6.dp),
                                                modifier = Modifier.height(28.dp)
                                            ) {
                                                Text("NEXT >", fontSize = 8.5.sp, fontFamily = FontFamily.Monospace)
                                            }
                                        }

                                        Spacer(modifier = Modifier.height(8.dp))

                                        // Move Grade Row
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Row(
                                                verticalAlignment = Alignment.CenterVertically,
                                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                                            ) {
                                                Text(activeMove.moveGrade.badge, fontSize = 14.sp)
                                                Text(
                                                    text = "${activeMove.moveGrade.label} (${activeMove.moveGrade.symbol})",
                                                    color = Color(activeMove.moveGrade.colorHex),
                                                    fontSize = 12.sp,
                                                    fontWeight = FontWeight.Black,
                                                    fontFamily = FontFamily.Monospace
                                                )
                                            }

                                            Text(
                                                text = "${if (activeMove.pnl >= 0) "+$" else "-$"}${String.format("%.2f", abs(activeMove.pnl))} (${if (activeMove.rMultiple >= 0) "+" else ""}${String.format("%.1f", activeMove.rMultiple)}R)",
                                                color = if (activeMove.pnl >= 0) deskGreen else deskRed,
                                                fontSize = 11.5.sp,
                                                fontWeight = FontWeight.Black,
                                                fontFamily = FontFamily.Monospace
                                            )
                                        }

                                        Spacer(modifier = Modifier.height(6.dp))

                                        // Trade Parameters Pill Box
                                        Surface(
                                            shape = RoundedCornerShape(8.dp),
                                            color = deskCardBg,
                                            border = BorderStroke(1.dp, deskBorder),
                                            modifier = Modifier.fillMaxWidth()
                                        ) {
                                            Row(
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .padding(horizontal = 8.dp, vertical = 6.dp),
                                                horizontalArrangement = Arrangement.SpaceBetween
                                            ) {
                                                Column {
                                                    Text("SETUP", color = deskMuted, fontSize = 7.sp, fontFamily = FontFamily.Monospace)
                                                    Text(activeMove.setupName.take(12), color = Color.White, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                                                }
                                                Column {
                                                    Text("DIRECTION", color = deskMuted, fontSize = 7.sp, fontFamily = FontFamily.Monospace)
                                                    Text(if (activeMove.direction > 0) "LONG ▲" else "SHORT ▼", color = if (activeMove.direction > 0) deskGreen else deskRed, fontSize = 9.sp, fontWeight = FontWeight.Black)
                                                }
                                                Column {
                                                    Text("ENTRY → EXIT", color = deskMuted, fontSize = 7.sp, fontFamily = FontFamily.Monospace)
                                                    Text("${String.format("%.2f", activeMove.entryPrice)} → ${String.format("%.2f", activeMove.exitPrice)}", color = Color.White, fontSize = 9.sp, fontFamily = FontFamily.Monospace)
                                                }
                                            }
                                        }

                                        Spacer(modifier = Modifier.height(6.dp))

                                        Text(
                                            text = "🧠 COACH DIAGNOSIS:",
                                            color = deskGold,
                                            fontSize = 8.5.sp,
                                            fontWeight = FontWeight.Black,
                                            fontFamily = FontFamily.Monospace
                                        )
                                        Text(
                                            text = activeMove.mistakeDiagnosis,
                                            color = Color.White,
                                            fontSize = 9.5.sp
                                        )

                                        Spacer(modifier = Modifier.height(4.dp))

                                        Text(
                                            text = "💡 PLAYBOOK RULE:",
                                            color = deskCyan,
                                            fontSize = 8.5.sp,
                                            fontWeight = FontWeight.Black,
                                            fontFamily = FontFamily.Monospace
                                        )
                                        Text(
                                            text = activeMove.actionableSolution,
                                            color = deskMuted,
                                            fontSize = 9.5.sp
                                        )
                                    }
                                }
                            }
                        }
                    }
                } else {
                    // PERFORMANCE STATS & SCOREBOARD TAB
                    LazyColumn(
                        verticalArrangement = Arrangement.spacedBy(10.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f)
                    ) {
                        // 4 Primary KPI Cards
                        item {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                MetricPill(title = "TAKES", value = "${p.takeProfits}", color = deskGreen, modifier = Modifier.weight(1f))
                                MetricPill(title = "STOPS", value = "${p.stopOuts}", color = if (p.stopOuts == 0) deskGreen else deskRed, modifier = Modifier.weight(1f))
                                MetricPill(title = "PF", value = "${String.format("%.2f", p.profitFactor)}", color = deskGold, modifier = Modifier.weight(1f))
                                MetricPill(title = "WIN %", value = "${p.winRatePct.toInt()}%", color = deskCyan, modifier = Modifier.weight(1f))
                            }
                        }

                        // Scoreboard
                        item {
                            Text(
                                text = "MATCH SCOREBOARD",
                                color = deskMuted,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Black,
                                fontFamily = FontFamily.Monospace,
                                letterSpacing = 1.sp
                            )
                        }

                        items(debrief.scoreboard) { trader ->
                            val isMe = trader.isPlayer
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = if (isMe) deskOrange.copy(alpha = 0.15f) else deskPanelBg,
                                border = BorderStroke(1.dp, if (isMe) deskOrange else deskBorder),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(horizontal = 10.dp, vertical = 8.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                                    ) {
                                        Text(
                                            text = "#${trader.placementRank}",
                                            color = if (trader.placementRank == 1) deskGold else Color.White,
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Black,
                                            fontFamily = FontFamily.Monospace
                                        )
                                        Text(
                                            text = trader.name + (if (isMe) " (YOU)" else ""),
                                            color = Color.White,
                                            fontSize = 10.5.sp,
                                            fontWeight = FontWeight.Black,
                                            fontFamily = FontFamily.Monospace
                                        )
                                    }

                                    Text(
                                        text = "${if (trader.netPnl >= 0) "+$" else "-$"}${String.format("%.2f", abs(trader.netPnl))}",
                                        color = if (trader.netPnl >= 0) deskGreen else deskRed,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Black,
                                        fontFamily = FontFamily.Monospace
                                    )
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // ==================== 4. ACTION BUTTONS ====================
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Button(
                        onClick = onReplayTape,
                        colors = ButtonDefaults.buttonColors(containerColor = deskCardBg),
                        border = BorderStroke(1.dp, deskBorder),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.weight(1f).height(44.dp)
                    ) {
                        Text(
                            text = "🔄 REPLAY",
                            color = Color.White,
                            fontSize = 10.5.sp,
                            fontWeight = FontWeight.Black,
                            fontFamily = FontFamily.Monospace
                        )
                    }

                    Button(
                        onClick = onOpenLobby,
                        colors = ButtonDefaults.buttonColors(containerColor = deskCardBg),
                        border = BorderStroke(1.dp, deskBorder),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.weight(1f).height(44.dp)
                    ) {
                        Text(
                            text = "🏛️ HUB",
                            color = Color.White,
                            fontSize = 10.5.sp,
                            fontWeight = FontWeight.Black,
                            fontFamily = FontFamily.Monospace
                        )
                    }

                    Button(
                        onClick = onPlayAgain,
                        colors = ButtonDefaults.buttonColors(containerColor = deskOrange),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.weight(1.3f).height(44.dp)
                    ) {
                        Text(
                            text = "⚡ NEXT",
                            color = Color.White,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Black,
                            fontFamily = FontFamily.Monospace
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun MetricPill(
    title: String,
    value: String,
    color: Color,
    modifier: Modifier = Modifier
) {
    Surface(
        shape = RoundedCornerShape(8.dp),
        color = deskPanelBg,
        border = BorderStroke(1.dp, deskBorder),
        modifier = modifier
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 6.dp, vertical = 6.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(title, color = deskMuted, fontSize = 7.5.sp, fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold)
            Text(value, color = color, fontSize = 12.sp, fontWeight = FontWeight.Black, fontFamily = FontFamily.Monospace)
        }
    }
}
