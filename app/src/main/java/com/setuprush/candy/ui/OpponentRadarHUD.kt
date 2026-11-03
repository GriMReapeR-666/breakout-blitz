package com.setuprush.candy.ui

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.setuprush.candy.engine.OpponentDuelEngine
import com.setuprush.candy.theme.GameTheme
import kotlin.math.abs

/**
 * Live Opponent Radar & Dueling HUD.
 * Renders the rival prop trader's real-time position, live dollar P&L,
 * target progress bar towards +2R cash out, and match score in head-to-head duels.
 */
@Composable
fun OpponentRadarHUD(
    theme: GameTheme,
    modifier: Modifier = Modifier
) {
    val rival = OpponentDuelEngine.activeRival ?: return
    val animatedProgress by animateFloatAsState(
        targetValue = rival.targetProgress,
        label = "rivalProgress"
    )

    Surface(
        color = theme.bg.copy(alpha = 0.94f),
        shape = RoundedCornerShape(12.dp),
        border = androidx.compose.foundation.BorderStroke(
            1.dp,
            if (rival.status == OpponentDuelEngine.RivalTradeStatus.HOLDING_LONG) theme.bull.copy(alpha = 0.6f)
            else if (rival.status == OpponentDuelEngine.RivalTradeStatus.HOLDING_SHORT) theme.bear.copy(alpha = 0.6f)
            else theme.grid.copy(alpha = 0.35f)
        ),
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 14.dp, vertical = 4.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 8.dp)
        ) {
            // Top Row: Duel Score & Round Status
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Text(
                        text = "⚔️ 1v1 PROP DUEL",
                        color = theme.bull,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Black,
                        letterSpacing = 1.sp
                    )
                    Surface(
                        shape = RoundedCornerShape(4.dp),
                        color = theme.grid.copy(alpha = 0.25f)
                    ) {
                        Text(
                            text = "R${OpponentDuelEngine.matchRound}/3",
                            color = theme.text,
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
                        )
                    }
                }

                Text(
                    text = "SCORE: YOU ${OpponentDuelEngine.playerRoundsWon} - ${rival.roundsWon} RIVAL",
                    color = theme.text,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Black
                )
            }

            Spacer(modifier = Modifier.height(6.dp))

            // Middle Row: Rival Identification & Live Position Status
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
                        color = theme.bull.copy(alpha = 0.2f)
                    ) {
                        Text(
                            text = "[${rival.deskTag}]",
                            color = theme.bull,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Black,
                            modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
                        )
                    }
                    Text(
                        text = rival.name,
                        color = theme.text,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "(${rival.mmr} MMR)",
                        color = theme.muted,
                        fontSize = 10.sp
                    )
                }

                // Rival live trade status tag
                val statusColor = when (rival.status) {
                    OpponentDuelEngine.RivalTradeStatus.HOLDING_LONG -> theme.bull
                    OpponentDuelEngine.RivalTradeStatus.HOLDING_SHORT -> theme.bear
                    OpponentDuelEngine.RivalTradeStatus.CASHED_OUT -> theme.bull
                    OpponentDuelEngine.RivalTradeStatus.STOPPED_OUT, OpponentDuelEngine.RivalTradeStatus.MARGIN_CALLED -> theme.bear
                    else -> theme.muted
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Text(
                        text = "${rival.status.badge} ${rival.status.label}",
                        color = statusColor,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.ExtraBold
                    )

                    if (rival.status == OpponentDuelEngine.RivalTradeStatus.HOLDING_LONG ||
                        rival.status == OpponentDuelEngine.RivalTradeStatus.HOLDING_SHORT) {
                        val pnlSign = if (rival.livePnl >= 0) "+$" else "-$"
                        val pnlColor = if (rival.livePnl >= 0) theme.bull else theme.bear
                        Text(
                            text = "$pnlSign${String.format("%.2f", abs(rival.livePnl))}",
                            color = pnlColor,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Black
                        )
                    }
                }
            }

            // Bottom Row: Target Meter Bar (Towards +2R Cash Out)
            if (rival.status == OpponentDuelEngine.RivalTradeStatus.HOLDING_LONG ||
                rival.status == OpponentDuelEngine.RivalTradeStatus.HOLDING_SHORT) {
                Spacer(modifier = Modifier.height(6.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = "RIVAL +2R METER:",
                        color = theme.muted,
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Bold
                    )
                    LinearProgressIndicator(
                        progress = animatedProgress,
                        color = if (rival.status == OpponentDuelEngine.RivalTradeStatus.HOLDING_LONG) theme.bull else theme.bear,
                        trackColor = theme.grid.copy(alpha = 0.2f),
                        modifier = Modifier
                            .weight(1f)
                            .height(5.dp)
                    )
                    Text(
                        text = "${(animatedProgress * 100).toInt()}%",
                        color = theme.text,
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Black
                    )
                }
            }

            // Event Message ticker if any
            OpponentDuelEngine.matchStatusMessage?.let { msg ->
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = msg,
                    color = if (msg.contains("CASHED") || msg.contains("WON") || msg.contains("VICTORY")) theme.bull
                    else if (msg.contains("STOPPED") || msg.contains("DEFEAT")) theme.bear
                    else theme.muted,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }
    }
}
