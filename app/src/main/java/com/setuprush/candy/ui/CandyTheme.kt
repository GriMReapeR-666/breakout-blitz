package com.setuprush.candy.ui

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

val CandyBackground = Color(0xFF120D18)
val CandySurface = Color(0xFF1C1524)
val CandySurfaceVariant = Color(0xFF281E34)
val CandyPink = Color(0xFFFF4081)
val CandyCyan = Color(0xFF00E5FF)
val CandyYellow = Color(0xFFFFD600)
val CandyGreen = Color(0xFF00E676)
val CandyRed = Color(0xFFFF1744)
val CandyText = Color(0xFFF8F7FA)
val CandyMuted = Color(0xFF8E8899)

val CandyDarkColorScheme = darkColorScheme(
    primary = CandyPink,
    secondary = CandyCyan,
    tertiary = CandyYellow,
    background = CandyBackground,
    surface = CandySurface,
    onPrimary = Color.White,
    onSecondary = Color.Black,
    onBackground = CandyText,
    onSurface = CandyText
)

@Composable
fun SetupCandyTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = CandyDarkColorScheme,
        content = content
    )
}
