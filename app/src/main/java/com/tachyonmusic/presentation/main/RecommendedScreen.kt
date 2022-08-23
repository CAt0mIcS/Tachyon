package com.tachyonmusic.presentation.main

import androidx.compose.runtime.Composable
import com.tachyonmusic.app.R
import com.tachyonmusic.presentation.main.component.BottomNavigationItem

object RecommendedScreen :
    BottomNavigationItem(R.string.btmNav_recommended, R.drawable.ic_recommended, "recommended") {

    @Composable
    operator fun invoke() {

    }
}