package com.tachyonmusic.presentation.library.component

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.material.ripple.LocalRippleTheme
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.tachyonmusic.presentation.theme.NoRippleTheme
import com.tachyonmusic.presentation.theme.Theme
import com.tachyonmusic.presentation.util.copy


@Composable
fun FilterItem(textId: Int, modifier: Modifier = Modifier, selected: Boolean = false, onClick: () -> Unit) {

    val selectedBgColor = MaterialTheme.colorScheme.tertiary
    val unselectedBgColor = MaterialTheme.colorScheme.background

    val backgroundColor by animateColorAsState(
        if (selected) selectedBgColor else unselectedBgColor,
        tween(Theme.animation.medium)
    )

    val contentColor by animateColorAsState(
        if (selected) MaterialTheme.colorScheme.onTertiary else MaterialTheme.colorScheme.onBackground,
        tween(Theme.animation.medium)
    )

    CompositionLocalProvider(LocalRippleTheme provides NoRippleTheme) {
        Button(
            modifier = modifier,
            shape = Theme.shapes.medium,
            colors = ButtonDefaults.buttonColors().copy(
                containerColor = backgroundColor,
                contentColor = contentColor
            ),
            onClick = onClick,
            contentPadding = ButtonDefaults.ContentPadding.copy(
                start = Theme.padding.medium,
                end = Theme.padding.medium
            )
        ) {
            Text(stringResource(textId), fontSize = 14.sp, fontWeight = FontWeight.Bold)
        }
    }
}