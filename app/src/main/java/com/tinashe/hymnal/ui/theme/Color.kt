package com.tinashe.hymnal.ui.theme

import androidx.compose.ui.graphics.Color

// Primary colors
val CISGreen = Color(0xFF0B6138)
val CISGreenDark = Color(0xFF084427)
val CISGold = Color(0xFFCD9B1D)
val CISGoldDark = Color(0xFFA47C17)
val CISRed = Color(0xFFCF141E)

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
val Primary = CISGreen
val OnPrimary = White
val PrimaryContainer = CISGreenDark
val OnPrimaryContainer = White

val Secondary = CISGreen
val OnSecondary = White
val SecondaryContainer = CISGreenDark
val OnSecondaryContainer = White

val Tertiary = CISGold
val OnTertiary = White
val TertiaryContainer = CISGoldDark
val OnTertiaryContainer = White

val Error = CISRed
val OnError = White
val ErrorContainer = CISRed.copy(alpha = 0.2f)
val OnErrorContainer = CISRed

val Background = White
val OnBackground = Black
val Surface = White
val OnSurface = Black
val SurfaceVariant = BackgroundSecondary
val OnSurfaceVariant = OnBackgroundSecondary

val Outline = OnBackgroundAlpha8
val OutlineVariant = OnBackgroundAlpha4

