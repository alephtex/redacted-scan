package org.fairscan.app.ui.theme

import androidx.compose.ui.graphics.Color

// CIA Red Theme - AMOLED Black with Red accents

// Pure AMOLED Black
val PureBlack = Color(0xFF000000)
val PureWhite = Color(0xFFFFFFFF)

// CIA Red shades
val CIARed = Color(0xFFCC0000)
val CIARedBright = Color(0xFFFF0000)
val CIARedDark = Color(0xFF990000)
val CIARedMuted = Color(0xFF660000)

// Dark theme colors (primary theme - always dark for AMOLED)
val PrimaryDark = CIARedBright
val OnPrimaryDark = PureWhite
val PrimaryContainerDark = CIARedDark
val OnPrimaryContainerDark = PureWhite
val SecondaryDark = PureWhite
val OnSecondaryDark = PureBlack
val SecondaryContainerDark = Color(0xFF1A1A1A)
val OnSecondaryContainerDark = PureWhite
val TertiaryDark = CIARed
val OnTertiaryDark = PureWhite
val TertiaryContainerDark = CIARedMuted
val OnTertiaryContainerDark = PureWhite
val ErrorDark = CIARedBright
val OnErrorDark = PureWhite
val ErrorContainerDark = CIARedDark
val OnErrorContainerDark = PureWhite
val BackgroundDark = PureBlack
val OnBackgroundDark = PureWhite
val SurfaceDark = PureBlack
val OnSurfaceDark = PureWhite
val SurfaceVariantDark = Color(0xFF1A1A1A)
val OnSurfaceVariantDark = Color(0xFFCCCCCC)
val OutlineDark = CIARed

// Light theme colors (fallback - also dark styled)
val Primary = CIARed
val OnPrimary = PureWhite
val PrimaryContainer = CIARedDark
val OnPrimaryContainer = PureWhite
val Secondary = PureWhite
val OnSecondary = PureBlack
val SecondaryContainer = Color(0xFF1A1A1A)
val OnSecondaryContainer = PureWhite
val Tertiary = CIARed
val OnTertiary = PureWhite
val TertiaryContainer = CIARedMuted
val OnTertiaryContainer = PureWhite
val Error = CIARedBright
val OnError = PureWhite
val ErrorContainer = CIARedDark
val OnErrorContainer = PureWhite
val Background = PureBlack
val OnBackground = PureWhite
val Surface = PureBlack
val OnSurface = PureWhite
val SurfaceVariant = Color(0xFF1A1A1A)
val OnSurfaceVariant = Color(0xFFCCCCCC)
val Outline = CIARed
