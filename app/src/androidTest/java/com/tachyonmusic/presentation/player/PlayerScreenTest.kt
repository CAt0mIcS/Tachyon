package com.tachyonmusic.presentation.player

import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.tachyonmusic.presentation.ActivityMain
import com.tachyonmusic.presentation.theme.TachyonTheme
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import org.junit.Before
import org.junit.Rule
import org.junit.Test


@HiltAndroidTest
class PlayerScreenTest {
    @get:Rule(order = 0)
    val hiltRule = HiltAndroidRule(this)

    @get:Rule(order = 1)
    val composeRule = createAndroidComposeRule<ActivityMain>()

    @Before
    fun setUp() {
        hiltRule.inject()
        composeRule.setContent {
            val navController = rememberNavController()
            TachyonTheme {
                NavHost(navController = navController, startDestination = PlayerScreen.route) {
                    composable(PlayerScreen.route) {
                        PlayerScreen(navController)
                    }
                }
            }
        }
    }
}
