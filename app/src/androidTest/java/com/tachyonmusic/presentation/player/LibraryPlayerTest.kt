package com.tachyonmusic.presentation.player

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.unit.dp
import com.tachyonmusic.presentation.entry.ActivityMain
import com.tachyonmusic.presentation.library.LibraryScreen
import com.tachyonmusic.presentation.theme.TachyonTheme
import com.tachyonmusic.presentation.theme.Theme
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import org.junit.Before
import org.junit.Rule
import org.junit.Test


@HiltAndroidTest
internal class LibraryPlayerTest {
    @get:Rule(order = 0)
    val hiltRule = HiltAndroidRule(this)

    @get:Rule(order = 1)
    val composeRule = createAndroidComposeRule<ActivityMain>()

    @Before
    fun setUp() {
        hiltRule.inject()
        composeRule.setContent {
            TachyonTheme {
                val sheetState = rememberBottomSheetState(
                    initialValue = BottomSheetValue.Collapsed, animationSpec = tween(
                        durationMillis = Theme.animation.medium, easing = LinearEasing
                    )
                )
                val scaffoldState = rememberBottomSheetScaffoldState(bottomSheetState = sheetState)
                val scope = rememberCoroutineScope()

                var miniPlayerHeight by remember { mutableStateOf(0.dp) }

                Scaffold(bottomBar = {
                    // Simulate [BottomNavigation] bar
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp)
                            .background(Theme.colors.contrastHigh)
                    )
                }) { innerPaddingScaffold ->

                    BottomSheetScaffold(
                        modifier = Modifier.padding(innerPaddingScaffold),
                        scaffoldState = scaffoldState,
                        sheetContent = {
                            Box(
                                modifier = Modifier
                                    .fillMaxSize(),
                            ) {
                                PlayerLayout(
                                    sheetState,
                                    { miniPlayerHeight = it },
                                    miniPlayerHeight
                                )
                            }
                        },
                        sheetPeekHeight = miniPlayerHeight,
                        sheetBackgroundColor = Theme.colors.primary
                    ) { innerPaddingSheet ->

                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(innerPaddingSheet)
                        ) {
                            LibraryScreen(sheetState)
                        }
                    }
                }
            }
        }
    }

    @Test
    fun clickingOnPlaybackWhileArtworkLoading_ArtworkAppearsInPlayerAfterLoaded() {

    }
}
