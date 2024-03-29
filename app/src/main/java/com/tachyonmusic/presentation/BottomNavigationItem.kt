package com.tachyonmusic.presentation

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes

abstract class BottomNavigationItem(
    @StringRes val title: Int,
    @DrawableRes val icon: Int,
    route: String
) : NavigationItem(route)