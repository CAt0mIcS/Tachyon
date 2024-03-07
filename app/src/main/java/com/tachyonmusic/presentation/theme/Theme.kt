package com.tachyonmusic.presentation.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.Typography
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.tachyonmusic.app.R

private val DarkColorPalette = darkColorScheme()

private val LightColorPalette = lightColorScheme()

private val DarkCustomColorPalette = Colors(
    primary = DarkPrimary,
    secondary = DarkSecondary,
    tertiary = DarkTertiary,
    blue = DarkBlue,
    orange = DarkOrange,
    partialOrange1 = DarkOrangePartial1,
    partialOrange2 = DarkOrangePartial2,
    contrastExtreme = Color.White,
    contrastHigh = DarkContrastHigh,
    contrastLow = DarkContrastLow,
    border = DarkBorderColor
)

private val LightCustomColorPalette = Colors(
    primary = LightPrimary,
    secondary = LightSecondary,
    tertiary = LightTertiary,
    blue = LightBlue,
    orange = LightOrange,
    partialOrange1 = LightOrangePartial1,
    partialOrange2 = LightOrangePartial2,
    contrastExtreme = Color.Black,
    contrastHigh = LightContrastHigh,
    contrastLow = LightContrastLow,
    border = LightBorderColor
)


@Composable
fun TachyonTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    settings: ComposeSettings = ComposeSettings(),
    content: @Composable () -> Unit
) {
//    val colors = if (darkTheme) DarkColorPalette else LightColorPalette TODO MAT3
    val colors = DarkColorPalette
//    val customColors = if (darkTheme) DarkCustomColorPalette else LightCustomColorPalette
    val customColors = DarkCustomColorPalette

    val typography = Typography(
        displayMedium = TextStyle(
            fontFamily = FontFamily(
                Font(R.font.microsoft_yahei),
            ),
            fontWeight = FontWeight.Normal,
            fontSize = 16.sp
        )
        /* Other default text styles to override
        button = TextStyle(
            fontFamily = FontFamily.Default,
            fontWeight = FontWeight.W500,
            fontSize = 14.sp
        ),
        caption = TextStyle(
            fontFamily = FontFamily.Default,
            fontWeight = FontWeight.Normal,
            fontSize = 12.sp
        )
        */
    )

    CompositionLocalProvider(
        LocalPadding provides Padding(),
        LocalShadow provides Shadow(),
        LocalAnimation provides Animation(),
        LocalCustomColors provides customColors,
        LocalSettings provides settings
    ) {
        MaterialTheme(
            colorScheme = colors,
            typography = typography,
            shapes = Shapes,
            content = content
        )
    }
}


class Colors(
    val primary: Color,
    val secondary: Color,
    val tertiary: Color,
    val blue: Color,
    val orange: Color,
    val partialOrange1: Color,
    val partialOrange2: Color,
    val contrastExtreme: Color,
    val contrastHigh: Color,
    val contrastLow: Color,
    val border: Color,
)

val LocalCustomColors = compositionLocalOf { LightCustomColorPalette }


object Theme {
    val colors: Colors
        @Composable
        @ReadOnlyComposable
        get() = LocalCustomColors.current

    val typography: Typography
        @Composable
        @ReadOnlyComposable
        get() = MaterialTheme.typography

    val shapes: Shapes
        @Composable
        @ReadOnlyComposable
        get() = MaterialTheme.shapes

    val padding: Padding
        @Composable
        @ReadOnlyComposable
        get() = LocalPadding.current

    val shadow: Shadow
        @Composable
        @ReadOnlyComposable
        get() = LocalShadow.current

    val animation: Animation
        @Composable
        @ReadOnlyComposable
        get() = LocalAnimation.current

    val settings: ComposeSettings
        @Composable
        @ReadOnlyComposable
        get() = LocalSettings.current
}


