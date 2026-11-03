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
import com.setuprush.candy.engine.IndicatorCatalog
import com.setuprush.candy.theme.GameTheme

data class IndicatorItem(
    val id: String,
    val name: String,
    val short: String,
    val tag: String,
    val signalsDescription: String,
    val enabled: Boolean
)

/**
 * Complete Indicator Library & Signal Picker Dialog.
 * Houses all 68 technical indicators from Setup Rush with:
 * - Live real-time search across all 68 indicators
 * - Category filter chips (All, Trend, Smart Money, Volume, Levels, Volatility, Momentum, Look)
 * - 4-indicator ceiling enforcement (MAX_IND: 4) matching Pine Script v6 runtime
 * - Pane indicator badge (Main Chart Overlay vs Separate Sub-Pane)
 */
@Composable
fun IndicatorPickerDialog(
    theme: GameTheme,
    activeIds: Set<String>,
    onToggle: (String) -> Unit,
    onDismiss: () -> Unit
) {
    var searchQuery by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf(IndicatorCatalog.IndicatorCategory.ALL) }

    val filteredIndicators = remember(searchQuery, selectedCategory) {
        IndicatorCatalog.search(searchQuery, selectedCategory)
    }

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(18.dp),
            color = theme.bg,
            border = BorderStroke(1.dp, theme.grid.copy(alpha = 0.5f)),
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.92f)
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
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Text(
                                text = "INDICATOR LIBRARY",
                                color = theme.text,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Black
                            )
                            Surface(
                                shape = RoundedCornerShape(4.dp),
                                color = if (activeIds.size >= 4) theme.bear.copy(alpha = 0.2f) else theme.bull.copy(alpha = 0.2f)
                            ) {
                                Text(
                                    text = "${activeIds.size}/4 ACTIVE",
                                    color = if (activeIds.size >= 4) theme.bear else theme.bull,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Black,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                            }
                        }
                        Text(
                            text = "68 Technical Indicators · Pine Script v6 Parity",
                            color = theme.muted,
                            fontSize = 11.sp
                        )
                    }

                    Button(
                        onClick = onDismiss,
                        colors = ButtonDefaults.buttonColors(containerColor = theme.grid.copy(alpha = 0.2f)),
                        shape = RoundedCornerShape(8.dp),
                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp)
                    ) {
                        Text("DONE", color = theme.text, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Search Bar
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    placeholder = { Text("Search 68 indicators (e.g. FVG, RSI, Supertrend, MACD)...", color = theme.muted, fontSize = 11.sp) },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = theme.bull,
                        unfocusedBorderColor = theme.grid.copy(alpha = 0.3f),
                        focusedTextColor = theme.text,
                        unfocusedTextColor = theme.text,
                        cursorColor = theme.bull
                    ),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp)
                )

                Spacer(modifier = Modifier.height(10.dp))

                // Category Filter Chips Row
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    items(IndicatorCatalog.IndicatorCategory.values()) { category ->
                        val isSelected = selectedCategory == category
                        val count = IndicatorCatalog.getByCategory(category).size
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = if (isSelected) theme.bull.copy(alpha = 0.22f) else theme.grid.copy(alpha = 0.08f),
                            border = BorderStroke(1.dp, if (isSelected) theme.bull else theme.grid.copy(alpha = 0.2f)),
                            modifier = Modifier.clickable { selectedCategory = category }
                        ) {
                            Text(
                                text = "${category.displayName} ($count)",
                                color = if (isSelected) theme.bull else theme.text,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // 68 Indicators List
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                ) {
                    items(filteredIndicators) { ind ->
                        val isEnabled = activeIds.contains(ind.id)
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = if (isEnabled) theme.bull.copy(alpha = 0.09f) else theme.grid.copy(alpha = 0.04f),
                            border = BorderStroke(
                                1.dp,
                                if (isEnabled) theme.bull.copy(alpha = 0.45f) else theme.grid.copy(alpha = 0.15f)
                            ),
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { onToggle(ind.id) }
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(10.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                                    modifier = Modifier.weight(1f)
                                ) {
                                    // Short Tag Pill
                                    Surface(
                                        shape = RoundedCornerShape(6.dp),
                                        color = if (isEnabled) theme.bull else theme.grid.copy(alpha = 0.2f)
                                    ) {
                                        Text(
                                            text = ind.short,
                                            color = if (isEnabled) (if (theme.isLight) Color.White else Color.Black) else theme.muted,
                                            fontSize = 9.sp,
                                            fontWeight = FontWeight.Black,
                                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                        )
                                    }

                                    Column {
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                                        ) {
                                            Text(
                                                text = ind.name,
                                                color = if (isEnabled) theme.bull else theme.text,
                                                fontSize = 12.sp,
                                                fontWeight = FontWeight.Bold
                                            )
                                            Surface(
                                                shape = RoundedCornerShape(4.dp),
                                                color = theme.grid.copy(alpha = 0.18f)
                                            ) {
                                                Text(
                                                    text = ind.category.badge,
                                                    color = theme.muted,
                                                    fontSize = 8.sp,
                                                    fontWeight = FontWeight.Bold,
                                                    modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
                                                )
                                            }
                                            Surface(
                                                shape = RoundedCornerShape(4.dp),
                                                color = if (ind.isPane) theme.bear.copy(alpha = 0.15f) else theme.bull.copy(alpha = 0.15f)
                                            ) {
                                                Text(
                                                    text = if (ind.isPane) "SUB-PANE" else "OVERLAY",
                                                    color = if (ind.isPane) theme.bear else theme.bull,
                                                    fontSize = 8.sp,
                                                    fontWeight = FontWeight.Bold,
                                                    modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
                                                )
                                            }
                                        }

                                        Text(
                                            text = ind.description,
                                            color = theme.muted,
                                            fontSize = 10.sp,
                                            modifier = Modifier.padding(top = 2.dp)
                                        )
                                    }
                                }

                                Switch(
                                    checked = isEnabled,
                                    onCheckedChange = { onToggle(ind.id) },
                                    colors = SwitchDefaults.colors(
                                        checkedThumbColor = theme.bull,
                                        checkedTrackColor = theme.bull.copy(alpha = 0.3f),
                                        uncheckedThumbColor = theme.muted,
                                        uncheckedTrackColor = theme.grid.copy(alpha = 0.2f)
                                    ),
                                    modifier = Modifier.padding(start = 6.dp)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

/**
 * Backward compatibility overload for legacy items list.
 */
@Composable
fun IndicatorPickerDialog(
    theme: GameTheme,
    items: List<IndicatorItem>,
    onToggle: (String) -> Unit,
    onDismiss: () -> Unit
) {
    val activeSet = items.filter { it.enabled }.map { it.id }.toSet()
    IndicatorPickerDialog(
        theme = theme,
        activeIds = activeSet,
        onToggle = onToggle,
        onDismiss = onDismiss
    )
}
