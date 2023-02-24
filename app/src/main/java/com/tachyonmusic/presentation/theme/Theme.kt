package com.tachyonmusic.presentation.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.ui.graphics.Color

private val DarkColorPalette = darkColors(
    primary = DarkPrimary,
    primaryVariant = DarkSecondary,
    secondary = DarkTertiary,

    background = DarkPrimary,
    surface = DarkSecondary,
    onPrimary = DarkContrastHigh,
    onSecondary = DarkBlue,
    onBackground = DarkContrastLow,
    onSurface = DarkContrastHigh,
)

private val LightColorPalette = lightColors(
    primary = LightPrimary,
    primaryVariant = LightSecondary,
    secondary = LightTertiary,

    background = Color.White,
    surface = LightSecondary,
    onPrimary = LightContrastHigh,
    onSecondary = LightBlue,
    onBackground = LightContrastLow,
    onSurface = LightContrastHigh,
)

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
    val colors = if (darkTheme) DarkColorPalette else LightColorPalette
    val customColors = if (darkTheme) DarkCustomColorPalette else LightCustomColorPalette

    CompositionLocalProvider(
        LocalPadding provides Padding(),
        LocalShadow provides Shadow(),
        LocalAnimation provides Animation(),
        LocalCustomColors provides customColors,
        LocalSettings provides settings
    ) {
        MaterialTheme(
            colors = colors,
            typography = Typography,
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


