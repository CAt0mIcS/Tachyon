package com.tachyonmusic.presentation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.tachyonmusic.presentation.authentication.RegisterScreen
import com.tachyonmusic.presentation.authentication.SignInScreen
import com.tachyonmusic.presentation.main.LibraryScreen
import com.tachyonmusic.presentation.main.RecommendedScreen
import com.tachyonmusic.presentation.main.SearchScreen

@Composable
fun NavigationGraph(navController: NavHostController) {
    NavHost(navController, startDestination = LibraryScreen.route) {
        composable(LibraryScreen.route) {
            LibraryScreen()
        }
        composable(SearchScreen.route) {
            SearchScreen()
        }
        composable(RecommendedScreen.route) {
            RecommendedScreen()
        }
        composable(SignInScreen.route) {
            SignInScreen(navController)
        }
        composable(RegisterScreen.route) {
            RegisterScreen(navController)
        }

    }
}