package com.beaunolten.rise.ui.theme

import android.app.Activity
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

private val RiseColorScheme = lightColorScheme(
    primary = Green,
    onPrimary = Card,
    secondary = Teal,
    onSecondary = Card,
    tertiary = Orange,
    onTertiary = Card,
    background = Bg,
    onBackground = Ink,
    surface = Card,
    onSurface = Ink,
    surfaceVariant = CardMint,
    onSurfaceVariant = InkSoft,
    outline = InkFaint,
)

@Composable
fun RiseTheme(content: @Composable () -> Unit) {
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = true
        }
    }
    MaterialTheme(
        colorScheme = RiseColorScheme,
        typography = RiseTypography,
        shapes = RiseShapes,
        content = content,
    )
}
