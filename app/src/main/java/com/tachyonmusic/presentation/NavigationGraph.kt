package com.tachyonmusic.presentation

import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import com.google.accompanist.navigation.animation.AnimatedNavHost
import com.google.accompanist.navigation.animation.composable
import com.tachyonmusic.presentation.authentication.RegisterScreen
import com.tachyonmusic.presentation.authentication.SignInScreen
import com.tachyonmusic.presentation.library.LibraryScreen
import com.tachyonmusic.presentation.main.HomeScreen
import com.tachyonmusic.presentation.player.PlayerScreen
import com.tachyonmusic.presentation.profile.ProfileScreen

@OptIn(ExperimentalAnimationApi::class)
@Composable
fun NavigationGraph(navController: NavHostController) {
    AnimatedNavHost(navController, startDestination = HomeScreen.route) {
        composable(HomeScreen.route) {
            HomeScreen(navController)
        }
        composable(LibraryScreen.route) {
            LibraryScreen(navController)
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
        composable(PlayerScreen.route) {
            PlayerScreen(navController)
        }
    }
}