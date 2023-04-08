package com.tachyonmusic.presentation.entry

import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.material.BottomSheetState
import androidx.compose.material.ExperimentalMaterialApi
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

@OptIn(ExperimentalAnimationApi::class, ExperimentalMaterialApi::class)
@Composable
fun NavigationGraph(
    navController: NavHostController,
    sheetState: BottomSheetState,
    miniPlayerHeight: Dp
) {
    AnimatedNavHost(navController, startDestination = HomeScreen.route) {
        composable(HomeScreen.route) {
            HomeScreen(navController, sheetState, miniPlayerHeight)
        }
        composable(LibraryScreen.route) {
            LibraryScreen(sheetState)
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