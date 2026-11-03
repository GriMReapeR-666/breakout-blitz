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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.setuprush.candy.engine.StrategyCatalog
import com.setuprush.candy.engine.StrategyMasteryManager
import com.setuprush.candy.theme.GameTheme

/**
 * 5-Tier Strategy Mastery & Tutorial Academy Dialog.
 * Teaches traders how to visually identify, execute, and master all 24 setups
 * across 6 strategy families:
 * - Tier 1: Visual Anatomy & Identification
 * - Tier 2: Precision Entry & Invalidation Stop
 * - Tier 3: Volume & Confluence Filter (Trap Avoidance)
 * - Tier 4: Turbo Speed Reaction Drill
 * - Tier 5: Grandmaster Certification Trial (Unlocks Setup for Multiplayer Matches)
 */
@Composable
fun StrategyMasteryTutorialDialog(
    theme: GameTheme,
    onLaunchMasteryDrill: (StrategyCatalog.SetupCardData, Int) -> Unit,
    onDismiss: () -> Unit
) {
    var selectedGroup by remember { mutableStateOf(StrategyCatalog.StrategyGroup.SMC) }
    val groupSetups = remember(selectedGroup) {
        StrategyCatalog.getSetupsByGroup(selectedGroup)
    }
    var selectedSetup by remember { mutableStateOf(groupSetups.firstOrNull() ?: StrategyCatalog.ALL_SETUPS.first()) }
    var masteryProgress by remember(selectedSetup.key) {
        mutableStateOf(StrategyMasteryManager.getProgress(selectedSetup.key))
    }

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(20.dp),
            color = theme.bg,
            border = BorderStroke(1.dp, theme.grid.copy(alpha = 0.5f)),
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
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "🎓 STRATEGY MASTERY ACADEMY",
                            color = theme.bull,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Black,
                            letterSpacing = 1.sp
                        )
                        Text(
                            text = "Master each setup to unlock it for competitive multiplayer lobbies",
                            color = theme.muted,
                            fontSize = 10.sp
                        )
                    }

                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = theme.grid.copy(alpha = 0.2f)
                    ) {
                        Text(
                            text = "${StrategyMasteryManager.getCertifiedStrategies().size}/${StrategyCatalog.ALL_SETUPS.size} UNLOCKED",
                            color = theme.text,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Strategy Family Tabs
                LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    items(StrategyCatalog.StrategyGroup.values()) { group ->
                        val isSelected = selectedGroup == group
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = if (isSelected) theme.bull.copy(alpha = 0.22f) else theme.grid.copy(alpha = 0.08f),
                            border = BorderStroke(1.dp, if (isSelected) theme.bull else theme.grid.copy(alpha = 0.2f)),
                            modifier = Modifier.clickable {
                                selectedGroup = group
                                val first = StrategyCatalog.getSetupsByGroup(group).firstOrNull()
                                if (first != null) {
                                    selectedSetup = first
                                    masteryProgress = StrategyMasteryManager.getProgress(first.key)
                                }
                            }
                        ) {
                            Text(
                                text = group.displayName,
                                color = if (isSelected) theme.bull else theme.text,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Setup Selector in Family
                LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    items(groupSetups) { setup ->
                        val isSelected = selectedSetup.key == setup.key
                        val progress = StrategyMasteryManager.getProgress(setup.key)
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = if (isSelected) theme.bull.copy(alpha = 0.18f) else theme.grid.copy(alpha = 0.06f),
                            border = BorderStroke(1.dp, if (isSelected) theme.bull else theme.grid.copy(alpha = 0.15f)),
                            modifier = Modifier.clickable {
                                selectedSetup = setup
                                masteryProgress = progress
                            }
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp),
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 5.dp)
                            ) {
                                Text(
                                    text = if (progress.isCertifiedForMultiplayer) "👑" else "T${progress.tier}",
                                    fontSize = 10.sp
                                )
                                Text(
                                    text = setup.name,
                                    color = if (isSelected) theme.bull else theme.text,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Scrollable Tutorial & Mastery Content
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                ) {
                    // Selected Setup Overview Card
                    item {
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = theme.grid.copy(alpha = 0.08f),
                            border = BorderStroke(1.dp, theme.bull.copy(alpha = 0.35f)),
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
                                            text = selectedSetup.name.uppercase(),
                                            color = theme.text,
                                            fontSize = 14.sp,
                                            fontWeight = FontWeight.Black
                                        )
                                        Text(
                                            text = "${selectedSetup.group.displayName} · ${selectedSetup.bias}",
                                            color = if (selectedSetup.bias == "BULLISH") theme.bull else theme.bear,
                                            fontSize = 10.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }

                                    Surface(
                                        shape = RoundedCornerShape(6.dp),
                                        color = if (masteryProgress.isCertifiedForMultiplayer) theme.bull else theme.grid.copy(alpha = 0.25f)
                                    ) {
                                        Text(
                                            text = if (masteryProgress.isCertifiedForMultiplayer) "MULTIPLAYER READY 👑" else "TIER ${masteryProgress.tier}/5",
                                            color = if (masteryProgress.isCertifiedForMultiplayer) (if (theme.isLight) Color.White else Color.Black) else theme.muted,
                                            fontSize = 10.sp,
                                            fontWeight = FontWeight.Black,
                                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp)
                                        )
                                    }
                                }

                                Spacer(modifier = Modifier.height(6.dp))
                                Text(
                                    text = "“${selectedSetup.lesson}”",
                                    color = theme.muted,
                                    fontSize = 11.sp
                                )

                                Spacer(modifier = Modifier.height(8.dp))
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text("BENCHMARK WIN RATE: ${(selectedSetup.winRate * 100).toInt()}%", color = theme.bull, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                                    Text("YOUR STATS: ${masteryProgress.winCount}W / ${masteryProgress.lossCount}L (${(masteryProgress.winRate * 100).toInt()}%)", color = theme.text, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                                }

                                // XP Bar
                                Spacer(modifier = Modifier.height(6.dp))
                                LinearProgressIndicator(
                                    progress = (masteryProgress.xp.toFloat() / masteryProgress.xpRequired.toFloat()).coerceIn(0f, 1f),
                                    color = theme.bull,
                                    trackColor = theme.grid.copy(alpha = 0.2f),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(6.dp)
                                )
                                Text(
                                    text = "XP: ${masteryProgress.xp} / ${masteryProgress.xpRequired} (Next Tier at ${masteryProgress.xpRequired} XP)",
                                    color = theme.muted,
                                    fontSize = 9.sp,
                                    modifier = Modifier.padding(top = 2.dp)
                                )
                            }
                        }
                    }

                    // 5 Mastery Tiers Breakdown
                    item {
                        Text(
                            text = "5-TIER MASTERY CURRICULUM",
                            color = theme.muted,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Black,
                            letterSpacing = 1.sp
                        )
                    }

                    // TIER 1
                    item {
                        MasteryTierCard(
                            tierNumber = 1,
                            tierName = "Visual Anatomy & Identification",
                            isUnlocked = masteryProgress.tier >= 1,
                            theme = theme,
                            content = "• Core Anatomy: ${selectedSetup.anatomy}\n• Recognition Rule: Look for ${selectedSetup.trigger.split(";").firstOrNull() ?: selectedSetup.trigger}."
                        )
                    }

                    // TIER 2
                    item {
                        MasteryTierCard(
                            tierNumber = 2,
                            tierName = "Precision Entry & Invalidation",
                            isUnlocked = masteryProgress.tier >= 2,
                            theme = theme,
                            content = "• Entry Trigger: ${selectedSetup.trigger}\n• Invalidation Stop: ${selectedSetup.invalidation}\n• Target: Minimum +2.0R to +3.0R expansion."
                        )
                    }

                    // TIER 3
                    item {
                        MasteryTierCard(
                            tierNumber = 3,
                            tierName = "Volume & Confluence Filter",
                            isUnlocked = masteryProgress.tier >= 3,
                            theme = theme,
                            content = "• Trap Avoidance: ${selectedSetup.trap}\n• Confluence: Supertrend alignment + structural break verification."
                        )
                    }

                    // TIER 4
                    item {
                        MasteryTierCard(
                            tierNumber = 4,
                            tierName = "Turbo Speed Execution Drill",
                            isUnlocked = masteryProgress.tier >= 4,
                            theme = theme,
                            content = "• High-speed tape simulation at Desk 3 (1.5x speed multiplier).\n• Enforces sub-second reaction time and disciplined finger hold execution."
                        )
                    }

                    // TIER 5
                    item {
                        MasteryTierCard(
                            tierNumber = 5,
                            tierName = "Grandmaster Certification Trial",
                            isUnlocked = masteryProgress.tier >= 5,
                            theme = theme,
                            content = "• Blind chart trial without indicator aids.\n• Passing certifies this setup and PERMANENTLY UNLOCKS it in 1v1 Prop Duels, 5v5 Squad Clashes, and 100-trader Margin Call Tournaments!"
                        )
                    }
                }

                // Action Buttons
                Spacer(modifier = Modifier.height(10.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Button(
                        onClick = onDismiss,
                        colors = ButtonDefaults.buttonColors(containerColor = theme.grid.copy(alpha = 0.2f)),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier
                            .weight(1f)
                            .height(46.dp)
                    ) {
                        Text("CLOSE", color = theme.text, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }

                    Button(
                        onClick = {
                            // Award mastery XP on launching trial drill
                            masteryProgress = StrategyMasteryManager.recordTrade(selectedSetup.key, isWin = true, rMultiple = 2.0)
                            onLaunchMasteryDrill(selectedSetup, masteryProgress.tier)
                            onDismiss()
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = theme.bull),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier
                            .weight(2f)
                            .height(46.dp)
                    ) {
                        Text(
                            text = "START TIER ${masteryProgress.tier} TRIAL DRILL ⚡",
                            color = if (theme.isLight) Color.White else Color.Black,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Black
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun MasteryTierCard(
    tierNumber: Int,
    tierName: String,
    isUnlocked: Boolean,
    theme: GameTheme,
    content: String
) {
    Surface(
        shape = RoundedCornerShape(10.dp),
        color = if (isUnlocked) theme.grid.copy(alpha = 0.07f) else theme.grid.copy(alpha = 0.02f),
        border = BorderStroke(1.dp, if (isUnlocked) theme.bull.copy(alpha = 0.3f) else theme.grid.copy(alpha = 0.15f)),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(10.dp)) {
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
                        color = if (isUnlocked) theme.bull else theme.grid.copy(alpha = 0.2f)
                    ) {
                        Text(
                            text = "TIER $tierNumber",
                            color = if (isUnlocked) (if (theme.isLight) Color.White else Color.Black) else theme.muted,
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Black,
                            modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
                        )
                    }
                    Text(
                        text = tierName,
                        color = if (isUnlocked) theme.text else theme.muted,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                Text(
                    text = if (isUnlocked) "UNLOCKED 🔓" else "LOCKED 🔒",
                    color = if (isUnlocked) theme.bull else theme.muted,
                    fontSize = 9.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = content,
                color = if (isUnlocked) theme.text.copy(alpha = 0.85f) else theme.muted.copy(alpha = 0.6f),
                fontSize = 10.sp,
                lineHeight = 14.sp
            )
        }
    }
}
