@file:OptIn(ExperimentalMaterialApi::class, ExperimentalAnimationApi::class)

package com.tachyonmusic.presentation.entry

import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.google.accompanist.navigation.animation.rememberAnimatedNavController
import com.tachyonmusic.presentation.core_components.UriPermissionDialog
import com.tachyonmusic.presentation.player.PlayerLayout
import com.tachyonmusic.presentation.theme.TachyonTheme
import com.tachyonmusic.presentation.theme.Theme

@OptIn(ExperimentalMaterialApi::class, ExperimentalAnimationApi::class)
@Composable
fun MainScreen(
    viewModel: MainViewModel = hiltViewModel()
) {
    val settings by viewModel.composeSettings.collectAsState()
    val requiresMusicPathSelection by viewModel.requiresMusicPathSelection.collectAsState()

    var showUriPermissionDialog by remember { mutableStateOf(false) }

    TachyonTheme(settings = settings) {

        if (requiresMusicPathSelection) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(Theme.padding.medium),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text("Please select a directory with all your music to continue")
                Button(onClick = { showUriPermissionDialog = true }) {
                    Text("Select...")
                }
            }

            UriPermissionDialog(showUriPermissionDialog) {
                viewModel.setNewMusicDirectory(it)
                showUriPermissionDialog = false
            }

            return@TachyonTheme
        }

        val sheetAnimationSpec = tween<Float>(
            durationMillis = Theme.animation.medium, easing = LinearEasing
        )
        val sheetState = rememberBottomSheetState(
            initialValue = BottomSheetValue.Collapsed,
            animationSpec = sheetAnimationSpec,
        )

        var targetSheetFraction by remember { mutableStateOf(0f) }
        val sheetFraction by animateFloatAsState(
            targetValue = targetSheetFraction,
            animationSpec = sheetAnimationSpec
        )

        val scaffoldState =
            rememberBottomSheetScaffoldState(bottomSheetState = sheetState)

        val miniPlayerHeight = remember { mutableStateOf(0.dp) }
        val navController = rememberAnimatedNavController()

        Scaffold(
            bottomBar = {
                BottomNavigation(
                    navController,
                    sheetState,
                    onSheetStateFraction = { targetSheetFraction = it })
            }
        ) { innerPaddingScaffold ->

            BottomSheetScaffold(
                modifier = Modifier.padding(innerPaddingScaffold),
                scaffoldState = scaffoldState,
                sheetContent = {
                    Box(
                        modifier = Modifier
                            .fillMaxSize(),
                    ) {
                        PlayerLayout(
                            navController,
                            sheetState,
                            onMiniPlayerHeight = { miniPlayerHeight.value = it },
                            miniPlayerHeight = miniPlayerHeight.value,
                            onTargetSheetFraction = { targetSheetFraction = it },
                            sheetFraction = sheetFraction
                        )
                    }
                },
                sheetPeekHeight = miniPlayerHeight.value,
                sheetBackgroundColor = Theme.colors.primary,
                sheetGesturesEnabled = false
            ) { innerPaddingSheet ->
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPaddingSheet)
                ) {
                    NavigationGraph(
                        navController,
                        sheetState,
                        miniPlayerHeight.value,
                        onTargetSheetFraction = {
                            targetSheetFraction = it
                        }
                    )
                }
            }
        }
    }
}
