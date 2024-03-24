package com.tachyonmusic.presentation.theme

import androidx.compose.ui.graphics.Color
import kotlin.math.max
import kotlin.math.min

////val LightPrimary = Color(0xFFEEEEEE)
//val LightSecondary = Color(0xFFDDDDDD)
//
////val WhiteTertiary = Color(0xFF797979)
//val LightTertiary = Color(0xFFA2A2A2)
//
//val LightBlue = Color(0xFF3498DB)
//val LightOrange = Color(0xFFFA7000)
//val LightPrimary = LightOrange
//val LightOrangePartial1 = LightOrange.copy(alpha = 0.42f)
//val LightOrangePartial2 = LightOrange.copy(alpha = 0.2f)
//
//val LightContrastExtreme = Color.Black
//val LightContrastHigh = Color(0xFF252525)
//val LightContrastLow = Color(0xB3252525)
//
//val LightBorderColor = LightTertiary.copy(alpha = LightTertiary.alpha * .7f)
//
//val LightSurfaceContainerLowest = Color.White
//val LightSurfaceContainerLow = Color(0xFFEEEEDD)
//val LightSurfaceContainer = LightSecondary
//val LightSurfaceContainerHigh = Color(0xFFC7C7C7)
//val LightSurfaceContainerHighest = LightTertiary
//
//
///*********************************************************************
// * DARK THEME
// */
////val DarkPrimary = Color(0xFF151515)
//val DarkSecondary = LightSecondary.inverse()
//
//val DarkTertiary = Color(0xFF3A3A3A)
//
//val DarkBlue = Color(0xFF2A7AAF)
//val DarkOrange = LightOrange
//val DarkPrimary = DarkOrange
//val DarkOrangePartial1 = LightOrangePartial1
//val DarkOrangePartial2 = LightOrangePartial2
//
//val DarkContrastExtreme = Color.White
//val DarkContrastHigh = Color(0xFFBDBDBD)
//val DarkContrastLow = LightContrastLow.inverse()
//
//val DarkBorderColor = LightBorderColor.inverse()
//
//val DarkSurfaceContainerLow = Color(0xFF181818)

private const val containerDarkeningFactor = .15f
private const val surfaceDarkeningFactor = .07f

/********************************************
 * LIGHT COLOR THEME
 ********************************************/

private val LightContrastExtreme = Color.Black
private val LightContrastHigh = Color(0xFF252525)
private val LightContrastLow = Color(0xB3252525)

val LightPrimary = Color(0xFFdb6b0f)
val LightBackground = Color(0xFFEEEEEE)
val LightOnBackground = LightContrastHigh

val LightOnPrimary = LightOnBackground

val LightSecondary = Color(0xFF565656)
val LightOnSecondary = LightBackground

val LightTertiary = Color(0xFF3498DB)
val LightOnTertiary = LightOnBackground

val LightError = Color.Red // TODO
val LightOnError = LightContrastExtreme // TODO
val LightOnErrorContainer = LightError - containerDarkeningFactor
val LightErrorContainer =
    LightTertiary.interpolate(LightOnErrorContainer) + 3 * containerDarkeningFactor

val LightOnPrimaryContainer = LightPrimary - containerDarkeningFactor
val LightPrimaryContainer =
    LightPrimary.interpolate(LightOnPrimaryContainer) + 3 * containerDarkeningFactor

val LightOnSecondaryContainer = LightSecondary - containerDarkeningFactor
val LightSecondaryContainer =
    LightSecondary.interpolate(LightOnSecondaryContainer) + 3 * containerDarkeningFactor

val LightOnTertiaryContainer = LightTertiary - containerDarkeningFactor
val LightTertiaryContainer =
    LightTertiary.interpolate(LightOnTertiaryContainer) + 3 * containerDarkeningFactor

val LightSurfaceDim = LightContrastLow.copy(alpha = .3f)
val LightSurface = LightOnPrimary
val LightSurfaceBright = Color(0xFFf9f9ff)
val LightSurfaceVariant = LightPrimary.copy(alpha = 0.2f)

val LightSurfaceContainerLowest = LightSurfaceBright
val LightSurfaceContainerLow = LightSurfaceContainerLowest - surfaceDarkeningFactor
val LightSurfaceContainer = LightSurfaceContainerLow - surfaceDarkeningFactor
val LightSurfaceContainerHigh = LightSurfaceContainer - surfaceDarkeningFactor
val LightSurfaceContainerHighest = LightSurfaceContainerHigh - surfaceDarkeningFactor

val LightOnSurface = Color(0xFF191c20)
val LightOnSurfaceVariant = Color(0xFF44474e)
val LightOutline = Color(0xFF74777f)
val LightOutlineVariant = Color(0xFFc4c6d0)

val LightInverseSurface = LightSurfaceContainer.inverse()
val LightInverseOnSurface = LightOnSurface.inverse()
val LightInversePrimary = LightPrimary.inverse()

val LightScrim = LightBackground - .1f


/********************************************
 * DARK COLOR THEME
 ********************************************/

private val DarkContrastExtreme = Color.White
private val DarkContrastHigh = Color(0xFFBDBDBD)
private val DarkContrastLow = LightContrastLow.inverse()

val DarkPrimary = LightPrimary
val DarkBackground = Color(0xFF151515)
val DarkOnBackground = DarkContrastHigh

val DarkOnPrimary = DarkOnBackground

val DarkSecondary = LightSecondary.inverse()
val DarkOnSecondary = DarkBackground

val DarkTertiary = Color(0xFF2A7AAF)
val DarkOnTertiary = DarkOnBackground

val DarkError = Color.Red // TODO
val DarkOnError = LightContrastExtreme // TODO
val DarkOnErrorContainer = DarkError + containerDarkeningFactor
val DarkErrorContainer =
    DarkTertiary.interpolate(DarkOnErrorContainer) - 3 * containerDarkeningFactor

val DarkOnPrimaryContainer = DarkPrimary + containerDarkeningFactor
val DarkPrimaryContainer =
    DarkPrimary.interpolate(DarkOnPrimaryContainer) - 3 * containerDarkeningFactor

val DarkOnSecondaryContainer = DarkSecondary + containerDarkeningFactor
val DarkSecondaryContainer =
    DarkSecondary.interpolate(DarkOnSecondaryContainer) - 3 * containerDarkeningFactor

val DarkOnTertiaryContainer = DarkTertiary + containerDarkeningFactor
val DarkTertiaryContainer =
    DarkTertiary.interpolate(DarkOnTertiaryContainer) - 3 * containerDarkeningFactor

val DarkSurfaceDim = DarkContrastLow.copy(alpha = .3f)
val DarkSurface = DarkOnPrimary
val DarkSurfaceBright = DarkPrimary
val DarkSurfaceVariant = DarkPrimary.copy(alpha = 0.2f)

val DarkSurfaceContainerLowest = DarkSurfaceBright
val DarkSurfaceContainerLow = DarkSurfaceContainerLowest + surfaceDarkeningFactor
val DarkSurfaceContainer = DarkSurfaceContainerLow + surfaceDarkeningFactor
val DarkSurfaceContainerHigh = DarkSurfaceContainer + surfaceDarkeningFactor
val DarkSurfaceContainerHighest = DarkSurfaceContainerHigh + surfaceDarkeningFactor

val DarkOnSurface = Color(0xFF191c20) // TODO
val DarkOnSurfaceVariant = Color(0xFF44474e) // TODO
val DarkOutline = Color(0xFF74777f) // TODO
val DarkOutlineVariant = Color(0xFFc4c6d0) // TODO

val DarkInverseSurface = DarkSurfaceContainer.inverse()
val DarkInverseOnSurface = DarkOnSurface.inverse()
val DarkInversePrimary = DarkPrimary.inverse()

val DarkScrim = DarkBackground + .1f


fun Color.inverse() = Color(
    alpha - red,
    alpha - green,
    alpha - blue
)

fun Color.interpolate(other: Color) = Color(
    (red + other.red) / 2f,
    (green + other.green) / 2f,
    (blue + other.blue) / 2f,
    (alpha + other.alpha) / 2f
)

operator fun Color.plus(rgb: Float) =
    Color(min(red + rgb, 1f), min(green + rgb, 1f), min(blue + rgb, 1f), alpha)

operator fun Color.minus(rgb: Float) =
    Color(max(red - rgb, 0f), max(green - rgb, 0f), max(blue - rgb, 0f), alpha)



val primaryLight = Color(0xFF8C4F26).interpolate(Color(0xFFdb6b0f))
//val primaryLight = Color(0xFF8C4F26) // generator
//val primaryLight = Color(0xFFdb6b0f) // original
val onPrimaryLight = Color(0xFFFFFFFF)
val primaryContainerLight = Color(0xFFFFDBC8)
val onPrimaryContainerLight = Color(0xFF321300)
val secondaryLight = Color(0xFF006874)
val onSecondaryLight = Color(0xFFFFFFFF)
val secondaryContainerLight = Color(0xFF9EEFFD)
val onSecondaryContainerLight = Color(0xFF001F24)
val tertiaryLight = Color(0xFF2C638B)
val onTertiaryLight = Color(0xFFFFFFFF)
val tertiaryContainerLight = Color(0xFFCCE5FF)
val onTertiaryContainerLight = Color(0xFF001D31)
val errorLight = Color(0xFFBA1A1A)
val onErrorLight = Color(0xFFFFFFFF)
val errorContainerLight = Color(0xFFFFDAD6)
val onErrorContainerLight = Color(0xFF410002)
val backgroundLight = Color(0xFFFFF8F5)
val onBackgroundLight = Color(0xFF221A15)
val surfaceLight = Color(0xFFFFF8F5)
val onSurfaceLight = Color(0xFF221A15)
val surfaceVariantLight = Color(0xFFF4DED3)
val onSurfaceVariantLight = Color(0xFF52443C)
val outlineLight = Color(0xFF85746B)
val outlineVariantLight = Color(0xFFD7C2B8)
val scrimLight = Color(0xFF000000)
val inverseSurfaceLight = Color(0xFF382E29)
val inverseOnSurfaceLight = Color(0xFFFFEDE5)
val inversePrimaryLight = Color(0xFFFFB68B)
val surfaceDimLight = Color(0xFFE7D7CF)
val surfaceBrightLight = Color(0xFFFFF8F5)
val surfaceContainerLowestLight = Color(0xFFFFFFFF)
val surfaceContainerLowLight = Color(0xFFFFF1EA)
val surfaceContainerLight = Color(0xFFFCEAE2)
val surfaceContainerHighLight = Color(0xFFF6E5DD)
val surfaceContainerHighestLight = Color(0xFFF0DFD7)

val primaryLightMediumContrast = Color(0xFF6A340D)
val onPrimaryLightMediumContrast = Color(0xFFFFFFFF)
val primaryContainerLightMediumContrast = Color(0xFFA6643A)
val onPrimaryContainerLightMediumContrast = Color(0xFFFFFFFF)
val secondaryLightMediumContrast = Color(0xFF004A53)
val onSecondaryLightMediumContrast = Color(0xFFFFFFFF)
val secondaryContainerLightMediumContrast = Color(0xFF25808C)
val onSecondaryContainerLightMediumContrast = Color(0xFFFFFFFF)
val tertiaryLightMediumContrast = Color(0xFF00476D)
val onTertiaryLightMediumContrast = Color(0xFFFFFFFF)
val tertiaryContainerLightMediumContrast = Color(0xFF4579A3)
val onTertiaryContainerLightMediumContrast = Color(0xFFFFFFFF)
val errorLightMediumContrast = Color(0xFF8C0009)
val onErrorLightMediumContrast = Color(0xFFFFFFFF)
val errorContainerLightMediumContrast = Color(0xFFDA342E)
val onErrorContainerLightMediumContrast = Color(0xFFFFFFFF)
val backgroundLightMediumContrast = Color(0xFFFFF8F5)
val onBackgroundLightMediumContrast = Color(0xFF221A15)
val surfaceLightMediumContrast = Color(0xFFFFF8F5)
val onSurfaceLightMediumContrast = Color(0xFF221A15)
val surfaceVariantLightMediumContrast = Color(0xFFF4DED3)
val onSurfaceVariantLightMediumContrast = Color(0xFF4E4038)
val outlineLightMediumContrast = Color(0xFF6C5C53)
val outlineVariantLightMediumContrast = Color(0xFF88776E)
val scrimLightMediumContrast = Color(0xFF000000)
val inverseSurfaceLightMediumContrast = Color(0xFF382E29)
val inverseOnSurfaceLightMediumContrast = Color(0xFFFFEDE5)
val inversePrimaryLightMediumContrast = Color(0xFFFFB68B)
val surfaceDimLightMediumContrast = Color(0xFFE7D7CF)
val surfaceBrightLightMediumContrast = Color(0xFFFFF8F5)
val surfaceContainerLowestLightMediumContrast = Color(0xFFFFFFFF)
val surfaceContainerLowLightMediumContrast = Color(0xFFFFF1EA)
val surfaceContainerLightMediumContrast = Color(0xFFFCEAE2)
val surfaceContainerHighLightMediumContrast = Color(0xFFF6E5DD)
val surfaceContainerHighestLightMediumContrast = Color(0xFFF0DFD7)

val primaryLightHighContrast = Color(0xFF3C1800)
val onPrimaryLightHighContrast = Color(0xFFFFFFFF)
val primaryContainerLightHighContrast = Color(0xFF6A340D)
val onPrimaryContainerLightHighContrast = Color(0xFFFFFFFF)
val secondaryLightHighContrast = Color(0xFF00272C)
val onSecondaryLightHighContrast = Color(0xFFFFFFFF)
val secondaryContainerLightHighContrast = Color(0xFF004A53)
val onSecondaryContainerLightHighContrast = Color(0xFFFFFFFF)
val tertiaryLightHighContrast = Color(0xFF00243B)
val onTertiaryLightHighContrast = Color(0xFFFFFFFF)
val tertiaryContainerLightHighContrast = Color(0xFF00476D)
val onTertiaryContainerLightHighContrast = Color(0xFFFFFFFF)
val errorLightHighContrast = Color(0xFF4E0002)
val onErrorLightHighContrast = Color(0xFFFFFFFF)
val errorContainerLightHighContrast = Color(0xFF8C0009)
val onErrorContainerLightHighContrast = Color(0xFFFFFFFF)
val backgroundLightHighContrast = Color(0xFFFFF8F5)
val onBackgroundLightHighContrast = Color(0xFF221A15)
val surfaceLightHighContrast = Color(0xFFFFF8F5)
val onSurfaceLightHighContrast = Color(0xFF000000)
val surfaceVariantLightHighContrast = Color(0xFFF4DED3)
val onSurfaceVariantLightHighContrast = Color(0xFF2D211B)
val outlineLightHighContrast = Color(0xFF4E4038)
val outlineVariantLightHighContrast = Color(0xFF4E4038)
val scrimLightHighContrast = Color(0xFF000000)
val inverseSurfaceLightHighContrast = Color(0xFF382E29)
val inverseOnSurfaceLightHighContrast = Color(0xFFFFFFFF)
val inversePrimaryLightHighContrast = Color(0xFFFFE7DC)
val surfaceDimLightHighContrast = Color(0xFFE7D7CF)
val surfaceBrightLightHighContrast = Color(0xFFFFF8F5)
val surfaceContainerLowestLightHighContrast = Color(0xFFFFFFFF)
val surfaceContainerLowLightHighContrast = Color(0xFFFFF1EA)
val surfaceContainerLightHighContrast = Color(0xFFFCEAE2)
val surfaceContainerHighLightHighContrast = Color(0xFFF6E5DD)
val surfaceContainerHighestLightHighContrast = Color(0xFFF0DFD7)

val primaryDark = Color(0xFFFFB68B).interpolate(Color(0xFFdb6b0f))
//val primaryDark = Color(0xFFFFB68B) // generator
//val primaryDark = Color(0xFFdb6b0f) // original
val onPrimaryDark = Color(0xFF522300)
val primaryContainerDark = Color(0xFF6F3811)
val onPrimaryContainerDark = Color(0xFFFFDBC8)
val secondaryDark = Color(0xFF82D3E0)
val onSecondaryDark = Color(0xFF00363D)
val secondaryContainerDark = Color(0xFF004F58)
val onSecondaryContainerDark = Color(0xFF9EEFFD)
val tertiaryDark = Color(0xFF2C638B)
val onTertiaryDark = Color(0xFFFFFFFF)
val tertiaryContainerDark = Color(0xFF074B72)
val onTertiaryContainerDark = Color(0xFFCCE5FF)
val errorDark = Color(0xFFFFB4AB)
val onErrorDark = Color(0xFF690005)
val errorContainerDark = Color(0xFF93000A)
val onErrorContainerDark = Color(0xFFFFDAD6)
val backgroundDark = Color(0xFF1A120D)
val onBackgroundDark = Color(0xFFF0DFD7)
val surfaceDark = Color(0xFF1A120D)
val onSurfaceDark = Color(0xFFF0DFD7)
val surfaceVariantDark = Color(0xFF52443C)
val onSurfaceVariantDark = Color(0xFFD7C2B8)
val outlineDark = Color(0xFF9F8D84)
val outlineVariantDark = Color(0xFF52443C)
val scrimDark = Color(0xFF000000)
val inverseSurfaceDark = Color(0xFFF0DFD7)
val inverseOnSurfaceDark = Color(0xFF382E29)
val inversePrimaryDark = Color(0xFF8C4F26)
val surfaceDimDark = Color(0xFF1A120D)
val surfaceBrightDark = Color(0xFF413732)
val surfaceContainerLowestDark = Color(0xFF140D08)
val surfaceContainerLowDark = Color(0xFF221A15)
val surfaceContainerDark = Color(0xFF261E19)
val surfaceContainerHighDark = Color(0xFF312823)
val surfaceContainerHighestDark = Color(0xFF3D332D)

val primaryDarkMediumContrast = Color(0xFFFFBC95)
val onPrimaryDarkMediumContrast = Color(0xFF2A0E00)
val primaryContainerDarkMediumContrast = Color(0xFFC78053)
val onPrimaryContainerDarkMediumContrast = Color(0xFF000000)
val secondaryDarkMediumContrast = Color(0xFF86D7E5)
val onSecondaryDarkMediumContrast = Color(0xFF001A1D)
val secondaryContainerDarkMediumContrast = Color(0xFF499CA9)
val onSecondaryContainerDarkMediumContrast = Color(0xFF000000)
val tertiaryDarkMediumContrast = Color(0xFF9DD0FE)
val onTertiaryDarkMediumContrast = Color(0xFF001829)
val tertiaryContainerDarkMediumContrast = Color(0xFF6296C1)
val onTertiaryContainerDarkMediumContrast = Color(0xFF000000)
val errorDarkMediumContrast = Color(0xFFFFBAB1)
val onErrorDarkMediumContrast = Color(0xFF370001)
val errorContainerDarkMediumContrast = Color(0xFFFF5449)
val onErrorContainerDarkMediumContrast = Color(0xFF000000)
val backgroundDarkMediumContrast = Color(0xFF1A120D)
val onBackgroundDarkMediumContrast = Color(0xFFF0DFD7)
val surfaceDarkMediumContrast = Color(0xFF1A120D)
val onSurfaceDarkMediumContrast = Color(0xFFFFFAF8)
val surfaceVariantDarkMediumContrast = Color(0xFF52443C)
val onSurfaceVariantDarkMediumContrast = Color(0xFFDBC7BC)
val outlineDarkMediumContrast = Color(0xFFB29F95)
val outlineVariantDarkMediumContrast = Color(0xFF918076)
val scrimDarkMediumContrast = Color(0xFF000000)
val inverseSurfaceDarkMediumContrast = Color(0xFFF0DFD7)
val inverseOnSurfaceDarkMediumContrast = Color(0xFF312823)
val inversePrimaryDarkMediumContrast = Color(0xFF703912)
val surfaceDimDarkMediumContrast = Color(0xFF1A120D)
val surfaceBrightDarkMediumContrast = Color(0xFF413732)
val surfaceContainerLowestDarkMediumContrast = Color(0xFF140D08)
val surfaceContainerLowDarkMediumContrast = Color(0xFF221A15)
val surfaceContainerDarkMediumContrast = Color(0xFF261E19)
val surfaceContainerHighDarkMediumContrast = Color(0xFF312823)
val surfaceContainerHighestDarkMediumContrast = Color(0xFF3D332D)

val primaryDarkHighContrast = Color(0xFFFFFAF8)
val onPrimaryDarkHighContrast = Color(0xFF000000)
val primaryContainerDarkHighContrast = Color(0xFFFFBC95)
val onPrimaryContainerDarkHighContrast = Color(0xFF000000)
val secondaryDarkHighContrast = Color(0xFFF1FDFF)
val onSecondaryDarkHighContrast = Color(0xFF000000)
val secondaryContainerDarkHighContrast = Color(0xFF86D7E5)
val onSecondaryContainerDarkHighContrast = Color(0xFF000000)
val tertiaryDarkHighContrast = Color(0xFFF9FBFF)
val onTertiaryDarkHighContrast = Color(0xFF000000)
val tertiaryContainerDarkHighContrast = Color(0xFF9DD0FE)
val onTertiaryContainerDarkHighContrast = Color(0xFF000000)
val errorDarkHighContrast = Color(0xFFFFF9F9)
val onErrorDarkHighContrast = Color(0xFF000000)
val errorContainerDarkHighContrast = Color(0xFFFFBAB1)
val onErrorContainerDarkHighContrast = Color(0xFF000000)
val backgroundDarkHighContrast = Color(0xFF1A120D)
val onBackgroundDarkHighContrast = Color(0xFFF0DFD7)
val surfaceDarkHighContrast = Color(0xFF1A120D)
val onSurfaceDarkHighContrast = Color(0xFFFFFFFF)
val surfaceVariantDarkHighContrast = Color(0xFF52443C)
val onSurfaceVariantDarkHighContrast = Color(0xFFFFFAF8)
val outlineDarkHighContrast = Color(0xFFDBC7BC)
val outlineVariantDarkHighContrast = Color(0xFFDBC7BC)
val scrimDarkHighContrast = Color(0xFF000000)
val inverseSurfaceDarkHighContrast = Color(0xFFF0DFD7)
val inverseOnSurfaceDarkHighContrast = Color(0xFF000000)
val inversePrimaryDarkHighContrast = Color(0xFF481E00)
val surfaceDimDarkHighContrast = Color(0xFF1A120D)
val surfaceBrightDarkHighContrast = Color(0xFF413732)
val surfaceContainerLowestDarkHighContrast = Color(0xFF140D08)
val surfaceContainerLowDarkHighContrast = Color(0xFF221A15)
val surfaceContainerDarkHighContrast = Color(0xFF261E19)
val surfaceContainerHighDarkHighContrast = Color(0xFF312823)
val surfaceContainerHighestDarkHighContrast = Color(0xFF3D332D)