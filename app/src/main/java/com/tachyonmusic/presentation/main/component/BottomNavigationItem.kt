package com.tachyonmusic.presentation.main.component

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import com.tachyonmusic.app.R
import com.tachyonmusic.presentation.util.NavigationItem

abstract class BottomNavigationItem(
    @StringRes val title: Int,
    @DrawableRes val icon: Int,
    route: String
) : NavigationItem(route)