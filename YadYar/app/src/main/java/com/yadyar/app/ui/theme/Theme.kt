package com.yadyar.app.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.unit.LayoutDirection
import com.yadyar.app.data.preferences.ThemePreference

private val MonoLightColors = lightColorScheme(
    primary = MonoDeepTeal,
    onPrimary = Color.White,
    primaryContainer = MonoContainerLight,
    onPrimaryContainer = MonoDeepTeal,
    secondary = MonoAmber,
    onSecondary = Color.White,
    secondaryContainer = Color(0xFFFFE9C8),
    onSecondaryContainer = Color(0xFF3A2100),
    tertiary = MonoSecondary,
    onTertiary = Color.White,
    background = MonoSurfaceLight,
    onBackground = LightOnBackground,
    surface = LightSurface,
    onSurface = LightOnSurface,
    surfaceVariant = MonoContainerLight,
    onSurfaceVariant = MonoSecondary,
    surfaceContainer = Color.White,
    surfaceContainerHigh = Color(0xFFF0F4F3),
    outline = MonoLineLight,
    outlineVariant = Color(0xFFD9E3E3),
    error = MonoError,
    onError = Color.White,
    errorContainer = MonoErrorContainer,
    onErrorContainer = Color(0xFF4A0A07),
    scrim = Color(0x52000000)
)

private val MonoDarkColors = darkColorScheme(
    primary = MonoPrimaryDark,
    onPrimary = Color(0xFF003334),
    primaryContainer = Color(0xFF204F50),
    onPrimaryContainer = Color(0xFFBCEBEC),
    secondary = Color(0xFFF0B86A),
    onSecondary = Color(0xFF3A2100),
    secondaryContainer = Color(0xFF5A3A00),
    onSecondaryContainer = Color(0xFFFFE9C8),
    tertiary = Color(0xFFB8CBD0),
    onTertiary = Color(0xFF223338),
    background = MonoSurfaceDark,
    onBackground = DarkOnBackground,
    surface = MonoCardDark,
    onSurface = DarkOnSurface,
    surfaceVariant = Color(0xFF253132),
    onSurfaceVariant = MonoMutedDark,
    surfaceContainer = MonoCardDark,
    surfaceContainerHigh = MonoCardDarkElevated,
    outline = MonoLineDark,
    outlineVariant = Color(0xFF32403F),
    error = Color(0xFFFFB4AB),
    onError = Color(0xFF690005),
    errorContainer = Color(0xFF93000A),
    onErrorContainer = Color(0xFFFFDAD6)
)

// Legacy alias for existing screens
private val YadYarColors = MonoLightColors

@Composable
fun YadYarTheme(
    darkTheme: Boolean? = null,
    content: @Composable () -> Unit
) {
    val context = LocalContext.current
    val themePref = ThemePreference.get(context)
    val isDark by themePref.isDark.collectAsState(initial = false)
    val useSystem = darkTheme == null
    val dark = when {
        darkTheme != null -> darkTheme
        useSystem -> isDark || isSystemInDarkTheme()
        else -> false
    }
    val scheme = if (dark) MonoDarkColors else MonoLightColors
    CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
        MaterialTheme(
            colorScheme = scheme,
            typography = YadYarTypography,
            shapes = MonoShapes,
            content = content
        )
    }
}

@Composable
fun MonoTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    YadYarTheme(darkTheme = darkTheme, content = content)
}
