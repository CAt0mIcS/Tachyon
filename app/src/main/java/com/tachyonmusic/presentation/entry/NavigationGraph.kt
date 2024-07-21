package com.tachyonmusic.presentation.entry

import androidx.compose.foundation.gestures.AnchoredDraggableState
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.Dp
import androidx.navigation.NavHostController
import com.google.accompanist.navigation.animation.AnimatedNavHost
import com.google.accompanist.navigation.animation.composable
import com.tachyonmusic.presentation.home.HomeScreen
import com.tachyonmusic.presentation.library.LibraryScreen
import com.tachyonmusic.presentation.library.search.PlaybackSearchScreen
import com.tachyonmusic.presentation.profile.ProfileScreen

@Composable
fun NavigationGraph(
    navController: NavHostController,
    miniPlayerHeight: Dp,
    swipeableState: AnchoredDraggableState<SwipingStates>
) {
    AnimatedNavHost(navController, startDestination = HomeScreen.route()) {
        composable(HomeScreen.route()) {
            HomeScreen(miniPlayerHeight, swipeableState)
        }
        composable(LibraryScreen.route()) {
            LibraryScreen(swipeableState, navController)
        }
        composable(ProfileScreen.route()) {
            ProfileScreen()
        }
        composable(
            route = PlaybackSearchScreen.route(),
            arguments = PlaybackSearchScreen.arguments
        ) { backStackEntry ->
            val arguments = requireNotNull(backStackEntry.arguments)
            PlaybackSearchScreen(arguments, swipeableState)
        }
    }
}