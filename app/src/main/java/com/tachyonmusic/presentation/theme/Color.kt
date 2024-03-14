package com.tachyonmusic.presentation.theme

import androidx.compose.ui.graphics.Color

val LightPrimary = Color(0xFFEEEEEE)
val LightSecondary = Color(0xFFDDDDDD)

//val WhiteTertiary = Color(0xFF797979)
val LightTertiary = Color(0xFFA2A2A2)

val LightBlue = Color(0xFF3498DB)
val LightOrange = Color(0xFFFA7000)
val LightOrangePartial1 = LightOrange.copy(alpha = 0.42f)
val LightOrangePartial2 = LightOrange.copy(alpha = 0.2f)

val LightContrastExtreme = Color.Black
val LightContrastHigh = Color(0xFF252525)
val LightContrastLow = Color(0xB3252525)

val LightBorderColor = LightTertiary.copy(alpha = LightTertiary.alpha * .7f)

val LightSurfaceContainerLowest = Color.White
val LightSurfaceContainerLow = Color(0xFFEEEEDD)
val LightSurfaceContainer = LightSecondary
val LightSurfaceContainerHigh = Color(0xFFC7C7C7)
val LightSurfaceContainerHighest = LightTertiary


/*********************************************************************
 * DARK THEME
 */
val DarkPrimary = Color(0xFF151515)
val DarkSecondary = LightSecondary.inverse()

val DarkTertiary = Color(0xFF3A3A3A)

val DarkBlue = Color(0xFF2A7AAF)
val DarkOrange = LightOrange
val DarkOrangePartial1 = LightOrangePartial1
val DarkOrangePartial2 = LightOrangePartial2

val DarkContrastExtreme = Color.White
val DarkContrastHigh = Color(0xFFBDBDBD)
val DarkContrastLow = LightContrastLow.inverse()

val DarkBorderColor = LightBorderColor.inverse()

val DarkSurfaceContainerLow = Color(0xFF181818)


internal fun Color.inverse() = Color(
    alpha - red,
    alpha - green,
    alpha - blue
)