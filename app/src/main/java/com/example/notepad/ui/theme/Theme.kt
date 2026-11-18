package com.example.notepad.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import com.example.notepad.NotePadViewModel

private val LightColors = lightColorScheme(
    primary = Color(0xFF1976D2),
    onPrimary = Color.White,
    primaryContainer = Color(0xFFBBDEFB),
    secondary = Color(0xFFFF9800),
    background = Color(0xFFF5F7FA),
    surface = Color.White
)

private val DarkColors = darkColorScheme(
    primary = Color(0xFF90CAF9),
    onPrimary = Color(0xFF003258),
    primaryContainer = Color(0xFF0065B0),
    secondary = Color(0xFFFFB74D)
)

private val BgMilk = Color(0xFFFDF6E3)
private val BgLavender = Color(0xFFF3E5F5)
private val BgMint = Color(0xFFE8F5E9)
private val BgSand = Color(0xFFFBE9E7)

@Composable
fun NotePadTheme(
    bg: String = "default",
    content: @Composable () -> Unit
) {
    val isDark = isSystemInDarkTheme()
    val base = if (isDark) DarkColors else LightColors
    val bgColor = when (bg) {
        NotePadViewModel.BG_MILK -> BgMilk
        NotePadViewModel.BG_LAVENDER -> BgLavender
        NotePadViewModel.BG_MINT -> BgMint
        NotePadViewModel.BG_SAND -> BgSand
        else -> base.background
    }
    val colors = base.copy(background = bgColor, surface = if (bg == "default" && !isDark) Color.White else bgColor)
    MaterialTheme(colorScheme = colors, content = content)
}
