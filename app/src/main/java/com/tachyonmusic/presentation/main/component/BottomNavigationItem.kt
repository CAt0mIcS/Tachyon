package com.tachyonmusic.presentation.main.component

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.compose.ui.unit.dp
import com.tachyonmusic.core.NavigationItem

abstract class BottomNavigationItem(
    @StringRes val title: Int,
    @DrawableRes val icon: Int,
    route: String
) : NavigationItem(route) {
    companion object {
        /**
         * Used to apply a padding before scaling the modifier to max size which would hide
         * the bottom of the composable because it would be hidden under the bottom navigation bar
         * TODO: May not be the same height on all systems and the height may change in the future
         */
        val HEIGHT = 40.dp
    }
}