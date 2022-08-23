package com.tachyonmusic.presentation.main

import androidx.compose.material.Scaffold
import androidx.compose.runtime.Composable
import androidx.navigation.NavController
import com.tachyonmusic.app.R
import com.tachyonmusic.presentation.main.component.BottomNavigation
import com.tachyonmusic.presentation.main.component.BottomNavigationItem

object SearchScreen : BottomNavigationItem(R.string.btmNav_search, R.drawable.ic_search, "search") {
    @Composable
    operator fun invoke() {

    }
}