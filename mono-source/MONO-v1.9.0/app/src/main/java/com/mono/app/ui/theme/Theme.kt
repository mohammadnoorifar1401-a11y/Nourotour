package com.mono.app.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.unit.LayoutDirection

private val MonoColors = darkColorScheme(
    primary = GoldAccent,
    onPrimary = Color(0xFF2B2203),
    secondary = BlueAccent,
    onSecondary = TextPrimary,
    background = BgDeep,
    onBackground = TextPrimary,
    surface = BgMid,
    onSurface = TextPrimary,
    surfaceVariant = BgSoft,
    onSurfaceVariant = TextSecondary,
    error = DangerRed,
    onError = TextPrimary
)

@Composable
fun MonoTheme(content: @Composable () -> Unit) {
    CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
        MaterialTheme(
            colorScheme = MonoColors,
            typography = MonoTypography,
            content = content
        )
    }
}
