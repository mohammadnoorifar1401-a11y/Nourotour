package com.yadyar.app.ui.theme

import androidx.compose.ui.graphics.Color

// ============ MONO Brand Tokens — warm, minimal, professional ============
// Source: Redesign mono-brand-identity-pack / spec
// Light is default, dark is explicit

// Primary brand
val MonoDeepTeal = Color(0xFF1B4D4E)        // سبزآبی عمیق — برند اصلی
val MonoContainerLight = Color(0xFFD3ECEC)  // ظرف اصلی روشن
val MonoAmber = Color(0xFFB86B19)           // کهربایی — CTA / accent
val MonoSecondary = Color(0xFF4A5D6A)       // خاکستری-آبی — متن ثانویه
val MonoSurfaceLight = Color(0xFFF7F8F6)    // سطح روشن — پس‌زمینه کلی
val MonoSurfaceDark = Color(0xFF0F1415)     // سطح تیره — پس‌زمینه دارک

// Dark overrides
val MonoPrimaryDark = Color(0xFF7AD0D2)     // Primary در تم تاریک
val MonoCardDark = Color(0xFF182123)        // سطح کارت تیره
val MonoCardDarkElevated = Color(0xFF1E2D2E)

// Status
val MonoSuccess = Color(0xFF1B7A4B)
val MonoSuccessContainer = Color(0xFFD6F0E2)
val MonoWarning = Color(0xFF8A5A00)
val MonoWarningContainer = Color(0xFFFFE9B5)
val MonoError = Color(0xFF8C1D18)
val MonoErrorContainer = Color(0xFFFFDAD6)
val MonoInfo = Color(0xFF155E7A)
val MonoInfoContainer = Color(0xFFCCE8F5)

// Neutral / line
val MonoLineLight = Color(0xFFE6EDEB)
val MonoLineDark = Color(0xFF2A3536)
val MonoMutedLight = Color(0xFF6B7C7D)
val MonoMutedDark = Color(0xFF8EA9AA)

// Compatibility aliases — keep old names working
val BgDeep = MonoDeepTeal
val BgMid = MonoSurfaceLight
val BgSoft = MonoContainerLight
val GoldAccent = MonoAmber
val BlueAccent = Color(0xFF2A7A8A)
val SuccessGreen = MonoSuccess
val DangerRed = MonoError
val WarningAmber = MonoWarning
val PurpleAccent = Color(0xFF6B5CE7)
val GlassFill = Color(0x14FFFFFF)
val GlassFillSoft = Color(0x0FFFFFFF)
val GlassBorder = Color(0x1E000000)
val GlassBorderSoft = Color(0x14000000)
val TextPrimary = Color(0xFF132223)
val TextSecondary = MonoSecondary

// Semantic helpers
val LightBackground = MonoSurfaceLight
val LightSurface = Color(0xFFFFFFFF)
val LightCard = Color(0xFFFFFFFF)
val LightOnBackground = Color(0xFF132223)
val LightOnSurface = Color(0xFF132223)
val LightOnSurfaceVariant = MonoSecondary

val DarkBackground = MonoSurfaceDark
val DarkSurface = Color(0xFF141E1E)
val DarkOnBackground = Color(0xFFE8F0F0)
val DarkOnSurface = Color(0xFFE8F0F0)
val DarkOnSurfaceVariant = MonoMutedDark
