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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.setuprush.candy.engine.StrategyCatalog
import com.setuprush.candy.theme.GameTheme

enum class GameMode(val id: String, val displayName: String, val shortDesc: String) {
    DAILY("daily", "DAILY TAPE", "Global 24H Seed · Standardized Tape"),
    RUSH("rush", "RUSH", "2:00 · Long Only · Max Cash"),
    PRO("pro", "PRO", "2:00 · Long & Short · Full Freedom"),
    BLOWUP("blowup", "BLOW-UP", "Endless · Climbing Leverage · Survive"),
    SNIPER("sniper", "SNIPER", "3 Bullets · 3x Size · High Conviction"),
    PRACTICE("practice", "PRACTICE", "Infinite Tape · Zero Stress · Pure Training")
}

enum class DeskLevel(val id: String, val displayName: String, val speedMul: Float) {
    DESK_1("desk1", "DESK 1: JUNIOR", 0.85f),
    DESK_2("desk2", "DESK 2: PROP DESK", 1.0f),
    DESK_3("desk3", "DESK 3: SCALPER", 1.35f),
    BLIND("blind", "BLIND: MASTER", 1.0f)
}

@Composable
fun ModeStrategyPickerDialog(
    theme: GameTheme,
    currentMode: GameMode,
    currentLevel: DeskLevel,
    selectedStrategyKey: String?, // null = All / Random
    onModeSelected: (GameMode) -> Unit,
    onLevelSelected: (DeskLevel) -> Unit,
    onStrategySelected: (String?) -> Unit,
    onDismiss: () -> Unit
) {
    var selectedFamily by remember { mutableStateOf<StrategyCatalog.StrategyFamily?>(null) } // null = All
    var activeMode by remember { mutableStateOf(currentMode) }
    var activeLevel by remember { mutableStateOf(currentLevel) }
    var activeStrategy by remember { mutableStateOf(selectedStrategyKey) }

    val filteredSetups = remember(selectedFamily) {
        if (selectedFamily == null) StrategyCatalog.ALL_SETUPS
        else StrategyCatalog.getByFamily(selectedFamily!!)
    }

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(20.dp),
            color = theme.bg,
            border = androidx.compose.foundation.BorderStroke(1.dp, theme.grid.copy(alpha = 0.5f)),
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.94f)
                .padding(vertical = 10.dp)
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
                            text = "STRATEGY GROUPS & SETUP CARDS",
                            color = theme.text,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Black
                        )
                        Text(
                            text = "Learn, Configure & Practice Any Strategy Group",
                            color = theme.muted,
                            fontSize = 11.sp
                        )
                    }

                    Button(
                        onClick = {
                            onModeSelected(activeMode)
                            onLevelSelected(activeLevel)
                            onStrategySelected(activeStrategy)
                            onDismiss()
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = theme.bull),
                        shape = RoundedCornerShape(10.dp),
                        contentPadding = PaddingValues(horizontal = 14.dp, vertical = 6.dp)
                    ) {
                        Text(
                            text = "START RIDE",
                            color = if (theme.isLight) Color.White else Color.Black,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Black
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Section 1: Game Modes
                Text(
                    text = "1. INTERACTIVE & COMPETITIVE MODES",
                    color = theme.muted,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Black,
                    letterSpacing = 1.sp
                )
                Spacer(modifier = Modifier.height(6.dp))

                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    items(GameMode.values()) { mode ->
                        val isSelected = activeMode == mode
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = if (isSelected) theme.bull.copy(alpha = 0.2f) else theme.grid.copy(alpha = 0.08f),
                            border = androidx.compose.foundation.BorderStroke(
                                1.dp,
                                if (isSelected) theme.bull else theme.grid.copy(alpha = 0.2f)
                            ),
                            modifier = Modifier.clickable { activeMode = mode }
                        ) {
                            Column(modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)) {
                                Text(
                                    text = mode.displayName,
                                    color = if (isSelected) theme.bull else theme.text,
                                    fontSize = 11.sp,
                                    fontWeight = if (isSelected) FontWeight.Black else FontWeight.Bold
                                )
                                Text(
                                    text = mode.shortDesc.split("·").firstOrNull()?.trim() ?: "",
                                    color = theme.muted,
                                    fontSize = 9.sp
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Section 2: Desk Level / Difficulty
                Text(
                    text = "2. DESK LEVEL & ASSISTANCE",
                    color = theme.muted,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Black,
                    letterSpacing = 1.sp
                )
                Spacer(modifier = Modifier.height(6.dp))

                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    items(DeskLevel.values()) { level ->
                        val isSelected = activeLevel == level
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = if (isSelected) theme.text.copy(alpha = 0.15f) else theme.grid.copy(alpha = 0.06f),
                            border = androidx.compose.foundation.BorderStroke(
                                1.dp,
                                if (isSelected) theme.text else theme.grid.copy(alpha = 0.2f)
                            ),
                            modifier = Modifier.clickable { activeLevel = level }
                        ) {
                            Text(
                                text = level.displayName,
                                color = if (isSelected) theme.text else theme.muted,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 5.dp)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Section 3: Strategy Family Filters
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "3. STRATEGY GROUP CARDS",
                        color = theme.muted,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Black,
                        letterSpacing = 1.sp
                    )

                    Text(
                        text = if (activeStrategy == null) "Active: ALL GROUPS" else "Locked: 1 Setup",
                        color = if (activeStrategy == null) theme.muted else theme.bull,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                Spacer(modifier = Modifier.height(6.dp))

                // Group Pills
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    item {
                        FilterChip(
                            selected = selectedFamily == null,
                            onClick = { selectedFamily = null },
                            label = { Text("ALL GROUPS (24+)", fontSize = 9.sp, fontWeight = FontWeight.Bold) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = theme.bull.copy(alpha = 0.2f),
                                selectedLabelColor = theme.bull,
                                containerColor = theme.grid.copy(alpha = 0.08f),
                                labelColor = theme.muted
                            )
                        )
                    }

                    items(StrategyCatalog.StrategyFamily.values()) { fam ->
                        FilterChip(
                            selected = selectedFamily == fam,
                            onClick = { selectedFamily = fam },
                            label = { Text(fam.displayName, fontSize = 9.sp, fontWeight = FontWeight.Bold) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = theme.bull.copy(alpha = 0.2f),
                                selectedLabelColor = theme.bull,
                                containerColor = theme.grid.copy(alpha = 0.08f),
                                labelColor = theme.muted
                            )
                        )
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Setup Cards List
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                ) {
                    // Option 0: ALL SETUPS (Random Mix)
                    item {
                        val isAllSelected = activeStrategy == null
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = if (isAllSelected) theme.bull.copy(alpha = 0.15f) else theme.grid.copy(alpha = 0.04f),
                            border = androidx.compose.foundation.BorderStroke(
                                1.dp,
                                if (isAllSelected) theme.bull else theme.grid.copy(alpha = 0.15f)
                            ),
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { activeStrategy = null }
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(12.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = "⚡ FULL MARKET MIX (ALL 6 GROUPS)",
                                        color = if (isAllSelected) theme.bull else theme.text,
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.Black
                                    )
                                    Text(
                                        text = "Candle Vision scans dynamically for any Price Action, Candlestick, SMC, CRT, or Trend setup.",
                                        color = theme.muted,
                                        fontSize = 11.sp
                                    )
                                }
                                if (isAllSelected) {
                                    Text("✓ ACTIVE", color = theme.bull, fontSize = 11.sp, fontWeight = FontWeight.Black)
                                }
                            }
                        }
                    }

                    // Setup Cards
                    items(filteredSetups) { setup ->
                        val isSelected = activeStrategy == setup.key
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = if (isSelected) theme.bull.copy(alpha = 0.12f) else theme.grid.copy(alpha = 0.04f),
                            border = androidx.compose.foundation.BorderStroke(
                                1.dp,
                                if (isSelected) theme.bull else theme.grid.copy(alpha = 0.15f)
                            ),
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { activeStrategy = setup.key }
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(12.dp)
                            ) {
                                // Header: Name + Group Badge + Win Rate
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
                                            text = setup.name,
                                            color = if (isSelected) theme.bull else theme.text,
                                            fontSize = 13.sp,
                                            fontWeight = FontWeight.Bold
                                        )

                                        Surface(
                                            shape = RoundedCornerShape(4.dp),
                                            color = theme.grid.copy(alpha = 0.2f)
                                        ) {
                                            Text(
                                                text = setup.family.badge,
                                                color = theme.muted,
                                                fontSize = 9.sp,
                                                fontWeight = FontWeight.Black,
                                                modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
                                            )
                                        }

                                        Surface(
                                            shape = RoundedCornerShape(4.dp),
                                            color = if (setup.bias == "BULLISH") theme.bull.copy(alpha = 0.2f) else theme.bear.copy(alpha = 0.2f)
                                        ) {
                                            Text(
                                                text = setup.bias,
                                                color = if (setup.bias == "BULLISH") theme.bull else theme.bear,
                                                fontSize = 9.sp,
                                                fontWeight = FontWeight.Bold,
                                                modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
                                            )
                                        }
                                    }

                                    Text(
                                        text = "${(setup.winRate * 100).toInt()}% WIN",
                                        color = if (isSelected) theme.bull else theme.muted,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }

                                // Setup Lesson
                                Text(
                                    text = "“${setup.lesson}”",
                                    color = theme.text.copy(alpha = 0.9f),
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Medium,
                                    modifier = Modifier.padding(top = 4.dp)
                                )

                                // Identification & Entry
                                Text(
                                    text = "• Rule: ${setup.identificationRule}",
                                    color = theme.muted,
                                    fontSize = 10.sp,
                                    modifier = Modifier.padding(top = 2.dp)
                                )
                                Text(
                                    text = "• Trigger: ${setup.entryTrigger} · Stop: ${setup.invalidationStop}",
                                    color = theme.muted,
                                    fontSize = 10.sp,
                                    modifier = Modifier.padding(top = 1.dp)
                                )
                                Text(
                                    text = "⚠️ Trap: ${setup.commonTrap}",
                                    color = theme.bear.copy(alpha = 0.85f),
                                    fontSize = 10.sp,
                                    modifier = Modifier.padding(top = 2.dp)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
