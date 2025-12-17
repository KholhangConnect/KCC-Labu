package com.kholhang.kcclabu.ui.theme

import androidx.compose.ui.graphics.Color

// Primary colors
val MTLGreen = Color(0xFF0B6138)
val MTLGreenDark = Color(0xFF084427)
val MTLGold = Color(0xFFCD9B1D)
val MTLGoldDark = Color(0xFFA47C17)
val MTLRed = Color(0xFFCF141E)

// Neutral colors
val VillageBlack = Color(0xFF070B0E)
val VillageGrey = Color(0xFFA8B7C6)
val White = Color(0xFFFFFFFF)
val Black = Color(0xFF000000)
val DarkGrey = Color(0xFF121212)

// Background colors
val BackgroundSecondary = Color(0xFFF4F6F8)
val OnBackgroundSecondary = Color(0xFF232F3B)

// Alpha colors
val OnBackgroundAlpha4 = Color(0x0A000000)
val OnBackgroundAlpha8 = Color(0x14000000)
val OnBackgroundAlpha16 = Color(0x29000000)
val OnBackgroundAlpha32 = Color(0x52000000)
val Scrim = Color(0x36000000)

// Material 3 Light Color Scheme
val Primary = MTLGreen
val OnPrimary = White
val PrimaryContainer = MTLGreenDark
val OnPrimaryContainer = White

val Secondary = MTLGreen
val OnSecondary = White
val SecondaryContainer = MTLGreenDark
val OnSecondaryContainer = White

val Tertiary = MTLGold
val OnTertiary = White
val TertiaryContainer = MTLGoldDark
val OnTertiaryContainer = White

val Error = MTLRed
val OnError = White
val ErrorContainer = MTLRed.copy(alpha = 0.2f)
val OnErrorContainer = MTLRed

val Background = White
val OnBackground = Black
val Surface = White
val OnSurface = Black
val SurfaceVariant = BackgroundSecondary
val OnSurfaceVariant = OnBackgroundSecondary

val Outline = OnBackgroundAlpha8
val OutlineVariant = OnBackgroundAlpha4

