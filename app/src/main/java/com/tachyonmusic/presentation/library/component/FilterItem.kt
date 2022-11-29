package com.tachyonmusic.presentation.library.component

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.material.Button
import androidx.compose.material.ButtonDefaults
import androidx.compose.material.Text
import androidx.compose.material.ripple.LocalRippleTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.tachyonmusic.presentation.theme.NoRippleTheme
import com.tachyonmusic.presentation.theme.Theme


@Composable
fun FilterItem(text: String, selected: Boolean = false, onClick: () -> Unit) {

    val selectedColor = Theme.colors.blue
    val unselectedColor = Theme.colors.primary

    val color by animateColorAsState(
        if (selected) selectedColor else unselectedColor,
        tween(Theme.animation.medium)
    )

    CompositionLocalProvider(LocalRippleTheme provides NoRippleTheme) {
        Button(
            shape = Theme.shapes.medium,
            colors = ButtonDefaults.buttonColors(
                backgroundColor = color,
                contentColor = Theme.colors.contrastHigh
            ),
            onClick = {
                onClick()
            }
        ) {
            Text(text = text, fontSize = 14.sp, fontWeight = FontWeight.Bold)
        }
    }
}