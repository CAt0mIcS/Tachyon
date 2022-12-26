package com.tachyonmusic.presentation.player

import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.material.*
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import com.google.accompanist.navigation.animation.AnimatedNavHost
import com.google.accompanist.navigation.animation.composable
import com.google.accompanist.navigation.animation.rememberAnimatedNavController
import com.tachyonmusic.presentation.ActivityMain
import com.tachyonmusic.presentation.main.HomeScreen
import com.tachyonmusic.presentation.theme.TachyonTheme
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import org.junit.Before
import org.junit.Rule


@HiltAndroidTest
internal class PlayerScreenTest {
    @get:Rule(order = 0)
    val hiltRule = HiltAndroidRule(this)

    @get:Rule(order = 1)
    val composeRule = createAndroidComposeRule<ActivityMain>()

    @OptIn(ExperimentalAnimationApi::class, ExperimentalMaterialApi::class)
    @Before
    fun setUp() {
        hiltRule.inject()
        composeRule.setContent {
            val navController = rememberAnimatedNavController()
            val sheetState = rememberBottomSheetState(initialValue = BottomSheetValue.Expanded)
            val scaffoldState = rememberBottomSheetScaffoldState(bottomSheetState = sheetState)

            TachyonTheme {
                BottomSheetScaffold(
                    scaffoldState = scaffoldState,
                    sheetContent = { PlayerScreen(navController, sheetState) },
                ) {
                    AnimatedNavHost(navController, startDestination = HomeScreen.route) {
                        composable(HomeScreen.route) {
                            HomeScreen(navController, sheetState)
                        }
                    }
                }
            }
        }
    }
}
