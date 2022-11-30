package com.tachyonmusic.presentation.theme

import androidx.compose.ui.graphics.Color

val LightWhite = Color.White
val LightSecondary = Color(0xFFDDDDDD)

//val WhiteTertiary = Color(0xFF797979)
val LightTertiary = Color(0xFFA2A2A2)

val LightBlue = Color(0xFF3498DB)
val LightOrange = Color(0xFFFA7000)
val LightOrangePartial1 = LightOrange.copy(alpha = 0.42f)
val LightOrangePartial2 = LightOrange.copy(alpha = 0.2f)

val LightContrastHigh = Color(0xFF252525)
val LightContrastLow = Color(0xB3252525)

val LightBorderColor = LightTertiary.copy(alpha = LightTertiary.alpha * .7f)


// TODO: Dark theme
val Black = Color.Black
