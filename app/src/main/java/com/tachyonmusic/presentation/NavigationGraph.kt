package com.tachyonmusic.presentation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.tachyonmusic.presentation.authentication.RegisterScreen
import com.tachyonmusic.presentation.authentication.SignInScreen
import com.tachyonmusic.presentation.library.LibraryScreen
import com.tachyonmusic.presentation.main.HomeScreen
import com.tachyonmusic.presentation.player.PlayerScreen
import com.tachyonmusic.presentation.profile.ProfileScreen

@Composable
fun NavigationGraph(navController: NavHostController) {
    NavHost(navController, startDestination = HomeScreen.route) {
        composable(HomeScreen.route) {
            HomeScreen(navController)
        }
        composable(LibraryScreen.route) {
            LibraryScreen()
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