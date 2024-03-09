package com.tachyonmusic.presentation.entry

import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.gestures.AnchoredDraggableState
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.SwipeableState
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.Dp
import androidx.navigation.NavHostController
import com.google.accompanist.navigation.animation.AnimatedNavHost
import com.google.accompanist.navigation.animation.composable
import com.tachyonmusic.presentation.authentication.RegisterScreen
import com.tachyonmusic.presentation.authentication.SignInScreen
import com.tachyonmusic.presentation.home.HomeScreen
import com.tachyonmusic.presentation.library.LibraryScreen
import com.tachyonmusic.presentation.profile.ProfileScreen

@OptIn(ExperimentalAnimationApi::class, ExperimentalMaterialApi::class,
    ExperimentalFoundationApi::class
)
@Composable
fun NavigationGraph(
    navController: NavHostController,
    miniPlayerHeight: Dp,
    swipeableState: AnchoredDraggableState<SwipingStates>
) {
    AnimatedNavHost(navController, startDestination = HomeScreen.route) {
        composable(HomeScreen.route) {
            HomeScreen(navController, miniPlayerHeight, swipeableState)
        }
        composable(LibraryScreen.route) {
            LibraryScreen(swipeableState)
        }
        composable(ProfileScreen.route) {
            ProfileScreen()
        }
        composable(SignInScreen.route) {
            SignInScreen(navController)
        }
        composable(RegisterScreen.route) {
            RegisterScreen(navController)
        }
    }
}