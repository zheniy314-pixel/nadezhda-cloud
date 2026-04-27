package ru.criticaldays.app.ui.theme

import android.app.Activity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

private val Primary = Color(0xFF7B1FA2)
private val Secondary = Color(0xFFCE93D8)

private val LightColors = lightColorScheme(
    primary = Primary,
    secondary = Secondary,
    tertiary = Color(0xFF4A148C)
)

private val DarkColors = darkColorScheme(
    primary = Secondary,
    secondary = Primary,
    tertiary = Color(0xFFE1BEE7)
)

@Composable
fun CriticalDaysTheme(content: @Composable () -> Unit) {
    val dark = isSystemInDarkTheme()
    val scheme = if (dark) DarkColors else LightColors
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = scheme.primary.toArgb()
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = !dark
        }
    }
    MaterialTheme(colorScheme = scheme, content = content)
}
