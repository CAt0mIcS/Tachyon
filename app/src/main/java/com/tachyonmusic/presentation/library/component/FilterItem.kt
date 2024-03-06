package com.tachyonmusic.presentation.library.component

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.material.ripple.LocalRippleTheme
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.tachyonmusic.presentation.theme.NoRippleTheme
import com.tachyonmusic.presentation.theme.Theme


@Composable
fun FilterItem(textId: Int, selected: Boolean = false, onClick: () -> Unit) {

    val selectedColor = Theme.colors.blue
    val unselectedColor = Theme.colors.primary

    val color by animateColorAsState(
        if (selected) selectedColor else unselectedColor,
        tween(Theme.animation.medium)
    )

    CompositionLocalProvider(LocalRippleTheme provides NoRippleTheme) {
        Button(
            shape = Theme.shapes.medium,
            colors = ButtonDefaults.buttonColors().copy(
                containerColor = color,
                contentColor = Theme.colors.contrastHigh
            ),
            onClick = onClick
        ) {
            Text(stringResource(textId), fontSize = 14.sp, fontWeight = FontWeight.Bold)
        }
    }
}