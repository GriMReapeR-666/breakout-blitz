package com.setuprush.candy.ui

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
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
import com.setuprush.candy.engine.MatchPerformanceEngine
import com.setuprush.candy.engine.SetupExecutionTimingEngine
import com.setuprush.candy.theme.GameTheme
import kotlin.math.abs

/**
 * Institutional Post-Match Performance Debrief Dialog.
 * Translates competitive multiplayer and solo match performance into comprehensive
 * financial metrics:
 * - Take-Profits ("Kills") & Stop-Outs ("Deaths")
 * - Profit Factor & Win/Loss Ratio
 * - Captured R-Volume ("Damage Dealt")
 * - Precision MAFE Efficiency
 * - Net Banked P&L ("Score")
 * - Match Scoreboard across all modes (1v1, 5v5, Margin Call 100, FFA, Solo)
 * - Accolade Medals & Mode Challenge Progression
 */
@Composable
fun PostMatchDebriefDialog(
    theme: GameTheme,
    debrief: MatchPerformanceEngine.MatchDebrief,
    onPlayAgain: () -> Unit,
    onOpenLobby: () -> Unit,
    onDismiss: () -> Unit,
    onReplayTape: () -> Unit = {}
) {
    var selectedTab by remember { mutableIntStateOf(0) } // 0 = Stats & Scoreboard, 1 = Trade Review
    val p = debrief.playerStats
    val isVictory = debrief.outcome == MatchPerformanceEngine.MatchOutcome.VICTORY ||
            debrief.outcome == MatchPerformanceEngine.MatchOutcome.SOLE_SURVIVOR ||
            debrief.outcome == MatchPerformanceEngine.MatchOutcome.QUALIFIED

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(20.dp),
            color = theme.bg,
            border = BorderStroke(1.dp, if (isVictory) theme.bull.copy(alpha = 0.5f) else theme.bear.copy(alpha = 0.5f)),
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
                // ==================== 1. MATCH OUTCOME BANNER ====================
                Surface(
                    shape = RoundedCornerShape(14.dp),
                    border = BorderStroke(1.dp, if (isVictory) theme.bull.copy(alpha = 0.4f) else theme.bear.copy(alpha = 0.4f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(
                                Brush.horizontalGradient(
                                    listOf(
                                        if (isVictory) theme.bull.copy(alpha = 0.22f) else theme.bear.copy(alpha = 0.22f),
                                        theme.grid.copy(alpha = 0.10f),
                                        theme.bg
                                    )
                                )
                            )
                            .padding(14.dp)
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
                                    Text(
                                        text = debrief.outcome.badge,
                                        fontSize = 16.sp
                                    )
                                    Text(
                                        text = debrief.outcome.label,
                                        color = if (isVictory) theme.bull else theme.bear,
                                        fontSize = 16.sp,
                                        fontWeight = FontWeight.Black,
                                        letterSpacing = 1.sp
                                    )
                                }

                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = "${debrief.modeTitle} · ${debrief.marketSymbol}",
                                    color = theme.text,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = debrief.feedbackSummary,
                                    color = theme.muted,
                                    fontSize = 10.sp
                                )
                            }

                            // Grade Stamp Badge
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = if (isVictory) theme.bull else theme.bear
                            ) {
                                Column(
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                                ) {
                                    Text(
                                        text = "GRADE",
                                        color = if (theme.isLight) Color.White else Color.Black,
                                        fontSize = 8.sp,
                                        fontWeight = FontWeight.Black
                                    )
                                    Text(
                                        text = debrief.performanceGrade,
                                        color = if (theme.isLight) Color.White else Color.Black,
                                        fontSize = 18.sp,
                                        fontWeight = FontWeight.Black
                                    )
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                // ==================== TAB SWITCHER ====================
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = if (selectedTab == 0) theme.bull.copy(alpha = 0.2f) else theme.grid.copy(alpha = 0.1f),
                        border = BorderStroke(1.dp, if (selectedTab == 0) theme.bull else theme.grid.copy(alpha = 0.3f)),
                        modifier = Modifier
                            .weight(1f)
                            .clickable { selectedTab = 0 }
                    ) {
                        Text(
                            text = "📊 STATS & SCOREBOARD",
                            color = if (selectedTab == 0) theme.bull else theme.muted,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Black,
                            modifier = Modifier.padding(vertical = 8.dp),
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center
                        )
                    }

                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = if (selectedTab == 1) theme.bull.copy(alpha = 0.2f) else theme.grid.copy(alpha = 0.1f),
                        border = BorderStroke(1.dp, if (selectedTab == 1) theme.bull else theme.grid.copy(alpha = 0.3f)),
                        modifier = Modifier
                            .weight(1f)
                            .clickable { selectedTab = 1 }
                    ) {
                        Text(
                            text = "📋 TRADE REVIEW (${debrief.tradeReviews.size})",
                            color = if (selectedTab == 1) theme.bull else theme.muted,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Black,
                            modifier = Modifier.padding(vertical = 8.dp),
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center
                        )
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                if (selectedTab == 1) {
                    // ==================== TRADE REVIEW & MISTAKE POST-MORTEM ====================
                    LazyColumn(
                        verticalArrangement = Arrangement.spacedBy(10.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f)
                    ) {
                        if (debrief.tradeReviews.isEmpty()) {
                            item {
                                Surface(
                                    shape = RoundedCornerShape(12.dp),
                                    color = theme.grid.copy(alpha = 0.08f),
                                    border = BorderStroke(1.dp, theme.grid.copy(alpha = 0.25f)),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Column(
                                        modifier = Modifier.padding(24.dp),
                                        horizontalAlignment = Alignment.CenterHorizontally
                                    ) {
                                        Text("📋", fontSize = 28.sp)
                                        Spacer(modifier = Modifier.height(8.dp))
                                        Text(
                                            text = "NO EXECUTED TRADES IN SESSION",
                                            color = theme.text,
                                            fontSize = 12.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                        Text(
                                            text = "Execute trades using BUY / SELL buttons to review execution quality and post-mortem leak diagnosis.",
                                            color = theme.muted,
                                            fontSize = 10.sp,
                                            textAlign = androidx.compose.ui.text.style.TextAlign.Center
                                        )
                                    }
                                }
                            }
                        } else {
                            items(debrief.tradeReviews) { tr ->
                                Surface(
                                    shape = RoundedCornerShape(12.dp),
                                    color = theme.grid.copy(alpha = 0.08f),
                                    border = BorderStroke(1.dp, if (tr.pnl >= 0) theme.bull.copy(alpha = 0.35f) else theme.bear.copy(alpha = 0.35f)),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Column(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(12.dp)
                                    ) {
                                        // Header Row: Trade #, Direction, PnL & R
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Row(
                                                verticalAlignment = Alignment.CenterVertically,
                                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                                            ) {
                                                Surface(
                                                    shape = RoundedCornerShape(4.dp),
                                                    color = if (tr.direction == 1) theme.bull.copy(alpha = 0.2f) else theme.bear.copy(alpha = 0.2f)
                                                ) {
                                                    Text(
                                                        text = if (tr.direction == 1) "LONG" else "SHORT",
                                                        color = if (tr.direction == 1) theme.bull else theme.bear,
                                                        fontSize = 9.sp,
                                                        fontWeight = FontWeight.Black,
                                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                                    )
                                                }
                                                Text(
                                                    text = "TRADE #${tr.tradeNumber} · ${tr.setupName}",
                                                    color = theme.text,
                                                    fontSize = 11.sp,
                                                    fontWeight = FontWeight.Bold
                                                )
                                            }

                                            Text(
                                                text = "${if (tr.pnl >= 0) "+$" else "-$"}${String.format("%.2f", abs(tr.pnl))} (${if (tr.rMultiple >= 0) "+" else ""}${String.format("%.1f", tr.rMultiple)}R)",
                                                color = if (tr.pnl >= 0) theme.bull else theme.bear,
                                                fontSize = 12.sp,
                                                fontWeight = FontWeight.Black
                                            )
                                        }

                                        Spacer(modifier = Modifier.height(6.dp))

                                        // Prices & Timing Badge
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Text(
                                                text = "ENTRY: ${String.format("%.2f", tr.entryPrice)} → EXIT: ${String.format("%.2f", tr.exitPrice)}",
                                                color = theme.muted,
                                                fontSize = 9.5.sp
                                            )
                                            Text(
                                                text = tr.timingBadge,
                                                color = theme.text,
                                                fontSize = 9.5.sp,
                                                fontWeight = FontWeight.Bold
                                            )
                                        }

                                        Spacer(modifier = Modifier.height(6.dp))

                                        // Mistake Diagnosis & Actionable Solution
                                        Surface(
                                            shape = RoundedCornerShape(8.dp),
                                            color = theme.bg.copy(alpha = 0.6f),
                                            border = BorderStroke(0.5.dp, theme.grid.copy(alpha = 0.25f)),
                                            modifier = Modifier.fillMaxWidth()
                                        ) {
                                            Column(
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .padding(8.dp)
                                            ) {
                                                Row(
                                                    verticalAlignment = Alignment.CenterVertically,
                                                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                                                ) {
                                                    Text(tr.mistakeCategory.badge, fontSize = 11.sp)
                                                    Text(
                                                        text = tr.mistakeCategory.label,
                                                        color = if (tr.mistakeCategory == MatchPerformanceEngine.MistakeCategory.NONE_FLAWLESS ||
                                                            tr.mistakeCategory == MatchPerformanceEngine.MistakeCategory.DISCIPLINED_STOP) theme.bull else theme.bear,
                                                        fontSize = 10.sp,
                                                        fontWeight = FontWeight.Black
                                                    )
                                                }
                                                Spacer(modifier = Modifier.height(2.dp))
                                                Text(
                                                    text = tr.mistakeDiagnosis,
                                                    color = theme.text,
                                                    fontSize = 9.5.sp
                                                )
                                                Spacer(modifier = Modifier.height(3.dp))
                                                Text(
                                                    text = "💡 SOLUTION: ${tr.actionableSolution}",
                                                    color = theme.bull,
                                                    fontSize = 9.5.sp,
                                                    fontWeight = FontWeight.Bold
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                } else {
                // Scrollable metrics, scoreboard, and challenges
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                ) {
                    // ==================== 2. FINANCIAL PERFORMANCE TELEMETRICS ====================
                    item {
                        Text(
                            text = "1. TRADER EXECUTION METRICS",
                            color = theme.muted,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Black,
                            letterSpacing = 1.sp
                        )
                        Spacer(modifier = Modifier.height(6.dp))

                        // 4 Primary KPI Cards (Wins, Losses, Profit Factor, Accuracy)
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            MetricCard(
                                title = "TAKE-PROFITS",
                                subtitle = "TARGETS (+2R)",
                                value = "${p.takeProfits}",
                                color = theme.bull,
                                theme = theme,
                                modifier = Modifier.weight(1f)
                            )
                            MetricCard(
                                title = "STOP-OUTS",
                                subtitle = "STOP-OUTS (-1R)",
                                value = "${p.stopOuts}",
                                color = if (p.stopOuts == 0) theme.bull else theme.bear,
                                theme = theme,
                                modifier = Modifier.weight(1f)
                            )
                            MetricCard(
                                title = "PROFIT FACTOR",
                                subtitle = "PROFIT FACTOR",
                                value = "${String.format("%.2f", p.profitFactor)}",
                                color = theme.bull,
                                theme = theme,
                                modifier = Modifier.weight(1f)
                            )
                            MetricCard(
                                title = "PRECISION",
                                subtitle = "MAFE ACCURACY",
                                value = "${p.precisionAccuracyPct.toInt()}%",
                                color = theme.bull,
                                theme = theme,
                                modifier = Modifier.weight(1f)
                            )
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        // Secondary Stats Row (Volume, Net PnL, Streak)
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = theme.grid.copy(alpha = 0.08f),
                            border = BorderStroke(1.dp, theme.grid.copy(alpha = 0.2f)),
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
                                    Text("CAPTURED R-VOLUME:", color = theme.muted, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                                    Text(
                                        text = "${if (p.rMultiplesCaptured >= 0) "+" else ""}${String.format("%.1f", p.rMultiplesCaptured)}R",
                                        color = if (p.rMultiplesCaptured >= 0) theme.bull else theme.bear,
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Black
                                    )
                                }
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Text("NET BANKED P&L:", color = theme.muted, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                                    Text(
                                        text = "${if (p.netPnl >= 0) "+$" else "-$"}${String.format("%.2f", abs(p.netPnl))}",
                                        color = if (p.netPnl >= 0) theme.bull else theme.bear,
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Black
                                    )
                                }
                                Column(horizontalAlignment = Alignment.End) {
                                    Text("WIN STREAK:", color = theme.muted, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                                    Text(
                                        text = "${p.bestStreak} WINS 🔥",
                                        color = theme.text,
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Black
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        // Execution Timing & Leak Detection Row
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = theme.grid.copy(alpha = 0.08f),
                            border = BorderStroke(1.dp, theme.grid.copy(alpha = 0.2f)),
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
                                    Text("🎯 SNIPER ENTRIES:", color = theme.muted, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                                    Text(
                                        text = "${SetupExecutionTimingEngine.sniperEntriesCount} SNIPERS",
                                        color = Color(0xFF00E676),
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Black
                                    )
                                }
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Text("⌛ MISSED SETUPS (+2R):", color = theme.muted, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                                    Text(
                                        text = "${SetupExecutionTimingEngine.missedSetupsCount} MISSED",
                                        color = if (SetupExecutionTimingEngine.missedSetupsCount == 0) theme.bull else Color(0xFFFF9100),
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Black
                                    )
                                }
                                Column(horizontalAlignment = Alignment.End) {
                                    Text("⚠️ CHASED TRADES (>1R):", color = theme.muted, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                                    Text(
                                        text = "${SetupExecutionTimingEngine.chasedEntriesCount} CHASED",
                                        color = if (SetupExecutionTimingEngine.chasedEntriesCount == 0) theme.bull else Color(0xFFFFB300),
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Black
                                    )
                                }
                            }
                        }
                    }

                    // ==================== 3. MEDALS & COMMENDATIONS ====================
                    if (debrief.medalsEarned.isNotEmpty()) {
                        item {
                            Text(
                                text = "2. PERFORMANCE MEDALS & COMMENDATIONS",
                                color = theme.muted,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Black,
                                letterSpacing = 1.sp
                            )
                            Spacer(modifier = Modifier.height(6.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                debrief.medalsEarned.forEach { medal ->
                                    Surface(
                                        shape = RoundedCornerShape(10.dp),
                                        color = theme.grid.copy(alpha = 0.08f),
                                        border = BorderStroke(1.dp, theme.bull.copy(alpha = 0.4f)),
                                        modifier = Modifier.weight(1f)
                                    ) {
                                        Column(
                                            horizontalAlignment = Alignment.CenterHorizontally,
                                            modifier = Modifier.padding(8.dp)
                                        ) {
                                            Text(medal.icon, fontSize = 18.sp)
                                            Spacer(modifier = Modifier.height(2.dp))
                                            Text(
                                                text = medal.title,
                                                color = theme.text,
                                                fontSize = 9.sp,
                                                fontWeight = FontWeight.Bold
                                            )
                                            Text(
                                                text = "+${medal.xpBonus} XP",
                                                color = theme.bull,
                                                fontSize = 9.sp,
                                                fontWeight = FontWeight.Black
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }

                    // ==================== 4. MATCH SCOREBOARD (ALL PARTICIPANTS) ====================
                    item {
                        Text(
                            text = "3. MATCH SCOREBOARD & RANKINGS",
                            color = theme.muted,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Black,
                            letterSpacing = 1.sp
                        )
                        Spacer(modifier = Modifier.height(6.dp))

                        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            debrief.scoreboard.forEach { entry ->
                                val isSelf = entry.isPlayer
                                Surface(
                                    shape = RoundedCornerShape(8.dp),
                                    color = if (isSelf) theme.bull.copy(alpha = 0.14f) else theme.grid.copy(alpha = 0.04f),
                                    border = BorderStroke(
                                        1.dp,
                                        if (isSelf) theme.bull else theme.grid.copy(alpha = 0.15f)
                                    ),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(horizontal = 10.dp, vertical = 6.dp),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                                        ) {
                                            Text(
                                                text = "#${entry.placementRank}",
                                                color = if (entry.placementRank == 1) theme.bull else theme.muted,
                                                fontSize = 11.sp,
                                                fontWeight = FontWeight.Black
                                            )
                                            Surface(
                                                shape = RoundedCornerShape(4.dp),
                                                color = theme.grid.copy(alpha = 0.2f)
                                            ) {
                                                Text(
                                                    text = "[${entry.deskTag}]",
                                                    color = theme.muted,
                                                    fontSize = 8.sp,
                                                    fontWeight = FontWeight.Bold,
                                                    modifier = Modifier.padding(horizontal = 3.dp, vertical = 1.dp)
                                                )
                                            }
                                            Text(
                                                text = if (isSelf) "${entry.name} (YOU)" else entry.name,
                                                color = if (isSelf) theme.bull else theme.text,
                                                fontSize = 11.sp,
                                                fontWeight = if (isSelf) FontWeight.Black else FontWeight.Bold
                                            )
                                            Text(
                                                text = entry.assetAssigned,
                                                color = theme.muted,
                                                fontSize = 9.sp
                                            )
                                        }

                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                                        ) {
                                            Text(
                                                text = "${entry.takeProfits}TP / ${entry.stopOuts}SL",
                                                color = theme.text,
                                                fontSize = 10.sp,
                                                fontWeight = FontWeight.SemiBold
                                            )
                                            Text(
                                                text = "${entry.kdEquivalent} W/L",
                                                color = theme.muted,
                                                fontSize = 9.sp
                                            )
                                            Text(
                                                text = "${if (entry.netPnl >= 0) "+$" else "-$"}${String.format("%.0f", abs(entry.netPnl))}",
                                                color = if (entry.netPnl >= 0) theme.bull else theme.bear,
                                                fontSize = 11.sp,
                                                fontWeight = FontWeight.Black
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }

                    // ==================== 5. MODE-SPECIFIC CHALLENGES & CONTRACTS ====================
                    item {
                        Text(
                            text = "4. MODE CHALLENGES & CONTRACTS PROGRESS",
                            color = theme.muted,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Black,
                            letterSpacing = 1.sp
                        )
                        Spacer(modifier = Modifier.height(6.dp))

                        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            MatchPerformanceEngine.ACTIVE_CHALLENGES.filter { it.modeId == debrief.modeId || it.modeId == "all" }.forEach { ch ->
                                Surface(
                                    shape = RoundedCornerShape(10.dp),
                                    color = theme.grid.copy(alpha = 0.06f),
                                    border = BorderStroke(1.dp, if (ch.isCompleted) theme.bull.copy(alpha = 0.6f) else theme.grid.copy(alpha = 0.15f)),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Column(modifier = Modifier.padding(10.dp)) {
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
                                                    Text(
                                                        text = ch.title,
                                                        color = theme.text,
                                                        fontSize = 11.sp,
                                                        fontWeight = FontWeight.Bold
                                                    )
                                                    if (ch.isCompleted) {
                                                        Surface(shape = RoundedCornerShape(4.dp), color = theme.bull) {
                                                            Text("COMPLETED ✅", color = if (theme.isLight) Color.White else Color.Black, fontSize = 8.sp, fontWeight = FontWeight.Black, modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp))
                                                        }
                                                    }
                                                }
                                                Text(ch.description, color = theme.muted, fontSize = 9.sp)
                                            }

                                            Text(
                                                text = "+${ch.xpReward} XP · +${ch.mmrReward} MMR",
                                                color = theme.bull,
                                                fontSize = 9.sp,
                                                fontWeight = FontWeight.Black
                                            )
                                        }

                                        Spacer(modifier = Modifier.height(6.dp))
                                        LinearProgressIndicator(
                                            progress = ch.progressRatio,
                                            color = theme.bull,
                                            trackColor = theme.grid.copy(alpha = 0.2f),
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .height(5.dp)
                                        )
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween
                                        ) {
                                            Text("${ch.modeTitle} Contract", color = theme.muted, fontSize = 8.sp)
                                            Text("${String.format("%.1f", ch.currentValue)} / ${String.format("%.1f", ch.targetValue)}", color = theme.text, fontSize = 8.sp, fontWeight = FontWeight.Bold)
                                        }
                                    }
                                }
                            }
                        }
                    }

                    // ==================== 6. PROGRESSION & MMR REWARDS ====================
                    item {
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = theme.bull.copy(alpha = 0.12f),
                            border = BorderStroke(1.dp, theme.bull.copy(alpha = 0.3f)),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 14.dp, vertical = 10.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Text("📈", fontSize = 18.sp)
                                    Column {
                                        Text("MMR RANK RATING", color = theme.muted, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                                        Text(
                                            text = "${if (debrief.mmrChange >= 0) "+" else ""}${debrief.mmrChange} MMR",
                                            color = if (debrief.mmrChange >= 0) theme.bull else theme.bear,
                                            fontSize = 14.sp,
                                            fontWeight = FontWeight.Black
                                        )
                                    }
                                }

                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Text("⭐", fontSize = 18.sp)
                                    Column(horizontalAlignment = Alignment.End) {
                                        Text("TOTAL XP EARNED", color = theme.muted, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                                        Text(
                                            text = "+${debrief.xpEarned} XP",
                                            color = theme.bull,
                                            fontSize = 14.sp,
                                            fontWeight = FontWeight.Black
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                } // End of tab 0 (Stats & Scoreboard)

                // ==================== 7. ACTION BUTTONS ====================
                Spacer(modifier = Modifier.height(10.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Button(
                        onClick = {
                            onDismiss()
                            onOpenLobby()
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = theme.grid.copy(alpha = 0.2f)),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier
                            .weight(1f)
                            .height(44.dp),
                        contentPadding = PaddingValues(horizontal = 4.dp)
                    ) {
                        Text("DESK LOBBY", color = theme.text, fontSize = 9.5.sp, fontWeight = FontWeight.Bold)
                    }

                    Button(
                        onClick = {
                            onDismiss()
                            onReplayTape()
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = theme.grid.copy(alpha = 0.35f)),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier
                            .weight(1.1f)
                            .height(44.dp),
                        contentPadding = PaddingValues(horizontal = 4.dp)
                    ) {
                        Text("⏪ REPLAY TAPE", color = theme.text, fontSize = 9.5.sp, fontWeight = FontWeight.Bold)
                    }

                    Button(
                        onClick = {
                            onDismiss()
                            onPlayAgain()
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = theme.bull),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier
                            .weight(1.3f)
                            .height(44.dp),
                        contentPadding = PaddingValues(horizontal = 4.dp)
                    ) {
                        Text(
                            text = "NEXT MATCH ⚡",
                            color = if (theme.isLight) Color.White else Color.Black,
                            fontSize = 9.5.sp,
                            fontWeight = FontWeight.Black
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun MetricCard(
    title: String,
    subtitle: String,
    value: String,
    color: Color,
    theme: GameTheme,
    modifier: Modifier = Modifier
) {
    Surface(
        shape = RoundedCornerShape(10.dp),
        color = theme.grid.copy(alpha = 0.08f),
        border = BorderStroke(1.dp, color.copy(alpha = 0.35f)),
        modifier = modifier
    ) {
        Column(
            modifier = Modifier.padding(8.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = title,
                color = theme.muted,
                fontSize = 8.sp,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = value,
                color = color,
                fontSize = 15.sp,
                fontWeight = FontWeight.Black
            )
            Text(
                text = subtitle,
                color = theme.muted,
                fontSize = 7.sp
            )
        }
    }
}
