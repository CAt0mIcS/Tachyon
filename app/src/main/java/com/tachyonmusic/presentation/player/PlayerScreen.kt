package com.tachyonmusic.presentation.player

import androidx.compose.runtime.Composable
import androidx.media3.session.MediaBrowser
import androidx.navigation.NavController
import com.tachyonmusic.core.NavigationItem

object PlayerScreen : NavigationItem("player_screen") {

    @Composable
    operator fun invoke(
        navController: NavController,
        browser: MediaBrowser
    ) {
    }
}