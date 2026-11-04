package com.setuprush.candy.theme

import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf

/**
 * Manages theme selection, switching, and two-way synchronization
 * between Native Compose and WebView.
 */
object ThemeManager {
    private val _currentTheme = mutableStateOf(ThemeCatalog.get("pro"))
    val currentTheme: State<GameTheme> = _currentTheme

    /** Set theme by unique key (e.g. "candy", "night", "neon", "terminal", etc.) */
    fun setTheme(id: String): GameTheme {
        val theme = ThemeCatalog.get(id)
        _currentTheme.value = theme
        return theme
    }

    /** Cycle to the next theme in the catalog */
    fun cycleNext(): GameTheme {
        val all = ThemeCatalog.all()
        val currentIndex = all.indexOfFirst { it.id == _currentTheme.value.id }
        val nextIndex = (currentIndex + 1) % all.size
        _currentTheme.value = all[nextIndex]
        return _currentTheme.value
    }

    /** Get active theme */
    fun active(): GameTheme = _currentTheme.value
}
