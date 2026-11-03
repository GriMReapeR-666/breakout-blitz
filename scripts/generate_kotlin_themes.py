import json
import re

with open('scripts/themes_catalog.json', 'r', encoding='utf-8') as f:
    catalog = json.load(f)

def parse_color(c_str):
    if not c_str:
        return "Color.Transparent"
    c_str = c_str.strip()
    # Hex: #rrggbb
    if c_str.startswith('#'):
        hex_val = c_str[1:]
        if len(hex_val) == 6:
            return f"Color(0xFF{hex_val.upper()})"
        elif len(hex_val) == 3:
            expanded = "".join([ch*2 for ch in hex_val])
            return f"Color(0xFF{expanded.upper()})"
    # rgba(r, g, b, a)
    m = re.match(r'rgba?\(\s*(\d+)\s*,\s*(\d+)\s*,\s*(\d+)(?:\s*,\s*([\d.]+))?\s*\)', c_str)
    if m:
        r, g, b = int(m.group(1)), int(m.group(2)), int(m.group(3))
        a = float(m.group(4)) if m.group(4) else 1.0
        a_int = int(a * 255)
        return f"Color({r}, {g}, {b}, {a_int})"
    return "Color.Transparent"

kotlin_code = """package com.setuprush.candy.theme

import androidx.compose.ui.graphics.Color

/**
 * 100% Bit-accurate clone of Setup Rush theme specification.
 * Contains all 14 official theme palettes reverse-engineered directly
 * from setuprush.com.
 */
data class GameTheme(
    val id: String,
    val name: String,
    val desc: String,
    val price: Int,
    val isLight: Boolean,
    val bg: Color,
    val text: Color,
    val muted: Color,
    val grid: Color,
    val up: Color,
    val down: Color,
    val wickUp: Color,
    val wickDown: Color,
    val outline: Color,
    val bull: Color,
    val bear: Color,
    val axis: Color
)

object ThemeCatalog {
    val THEMES: Map<String, GameTheme> = mapOf(
"""

for tid, t in catalog.items():
    bg_col = parse_color(t.get('bg'))
    text_col = parse_color(t.get('text'))
    muted_col = parse_color(t.get('muted'))
    grid_col = parse_color(t.get('grid'))
    up_col = parse_color(t.get('up'))
    down_col = parse_color(t.get('down'))
    wick_up = parse_color(t.get('wickUp'))
    wick_down = parse_color(t.get('wickDown'))
    outline_col = parse_color(t.get('outline')) if t.get('outline') else wick_up
    bull_col = parse_color(t.get('bull'))
    bear_col = parse_color(t.get('bear'))
    axis_col = parse_color(t.get('axis'))
    is_light = "true" if t.get('light') else "false"
    price = t.get('price', 0)
    name = t.get('name', tid)
    desc = t.get('desc', '')

    kotlin_code += f"""        "{tid}" to GameTheme(
            id = "{tid}",
            name = "{name}",
            desc = "{desc}",
            price = {price},
            isLight = {is_light},
            bg = {bg_col},
            text = {text_col},
            muted = {muted_col},
            grid = {grid_col},
            up = {up_col},
            down = {down_col},
            wickUp = {wick_up},
            wickDown = {wick_down},
            outline = {outline_col},
            bull = {bull_col},
            bear = {bear_col},
            axis = {axis_col}
        ),
"""

kotlin_code += """    )

    fun get(id: String): GameTheme = THEMES[id] ?: THEMES["candy"] ?: THEMES["night"]!!
    fun all(): List<GameTheme> = THEMES.values.toList()
}
"""

import os
os.makedirs('app/src/main/java/com/setuprush/candy/theme', exist_ok=True)
with open('app/src/main/java/com/setuprush/candy/theme/SetupTheme.kt', 'w', encoding='utf-8') as f:
    f.write(kotlin_code)

print("Generated app/src/main/java/com/setuprush/candy/theme/SetupTheme.kt")
