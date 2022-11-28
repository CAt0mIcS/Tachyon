package com.tachyonmusic.presentation.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.ui.graphics.Color

private val DarkColorPalette = darkColors(
    primary = Black,
    primaryVariant = Black,
    secondary = Black
)

private val LightColorPalette = lightColors(
    primary = LightWight,
    primaryVariant = LightSecondary,
    secondary = LightTertiary,

    background = Color.White,
    surface = LightSecondary,
    onPrimary = Color.Black,
    onSecondary = LightBlue,
    onBackground = LightContrastLow,
    onSurface = LightContrastHigh,
)


@Composable
fun TachyonTheme(darkTheme: Boolean = isSystemInDarkTheme(), content: @Composable () -> Unit) {
    val colors = if (darkTheme) {
        DarkColorPalette
    } else {
        LightColorPalette
    }

    CompositionLocalProvider(
        LocalPadding provides Padding(),
        LocalShadow provides Shadow()
    ) {
        MaterialTheme(
            colors = colors,
            typography = Typography,
            shapes = Shapes,
            content = content
        )
    }
}

object Theme {
    val colors: Colors
        @Composable
        @ReadOnlyComposable
        get() = MaterialTheme.colors

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
}


