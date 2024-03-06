@file:OptIn(ExperimentalMaterial3Api::class, ExperimentalAnimationApi::class)

package com.tachyonmusic.presentation.entry

import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.BottomSheetScaffold
import androidx.compose.material3.SheetState
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SheetValue
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberBottomSheetScaffoldState
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.material3.rememberStandardBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.hilt.navigation.compose.hiltViewModel
import com.google.accompanist.navigation.animation.rememberAnimatedNavController
import com.tachyonmusic.database.data.data_source.Database
import com.tachyonmusic.presentation.core_components.UriPermissionDialog
import com.tachyonmusic.presentation.player.PlayerLayout
import com.tachyonmusic.presentation.profile.component.OpenDocumentDialog
import com.tachyonmusic.presentation.theme.TachyonTheme
import com.tachyonmusic.presentation.theme.Theme

@Composable
fun MainScreen(
    viewModel: MainViewModel = hiltViewModel()
) {
    val settings by viewModel.composeSettings.collectAsState()
    val requiresMusicPathSelection by viewModel.requiresMusicPathSelection.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()

    var showUriPermissionDialog by remember { mutableStateOf(false) }
    var showImportDbDialog by remember { mutableStateOf(false) }
    var databaseImported by remember { mutableStateOf(false) }

    TachyonTheme(settings = settings) {

        Surface(
            modifier = Modifier.fillMaxSize()
        ) {
            if (isLoading) {
                Dialog(
                    onDismissRequest = { },
                    DialogProperties(dismissOnBackPress = false, dismissOnClickOutside = false)
                ) {
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier
                            .background(Theme.colors.primary, Theme.shapes.large)
                            .border(
                                BorderStroke(2.dp, Theme.colors.partialOrange2),
                                Theme.shapes.large
                            )
                    ) {
                        Column {
                            CircularProgressIndicator(
                                modifier = Modifier
                                    .padding(Theme.padding.medium)
                                    .align(Alignment.CenterHorizontally),
                                color = Theme.colors.contrastHigh
                            )

                            Text(
                                "Loading",
                                modifier = Modifier
                                    .padding(
                                        start = 80.dp,
                                        end = 80.dp,
                                        bottom = Theme.padding.medium
                                    ),
                                textAlign = TextAlign.Center,
                                color = Theme.colors.contrastHigh
                            )
                        }
                    }
                }
            }

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

                    if (!databaseImported) // TODO: Ask user to import all required directories
                        Button(onClick = { showImportDbDialog = true }) {
                            Text("Import Database")
                        }
                }

                UriPermissionDialog(showUriPermissionDialog) {
                    viewModel.setNewMusicDirectory(it)
                    showUriPermissionDialog = false
                }

                OpenDocumentDialog(showImportDbDialog, Database.ZIP_MIME_TYPE) {
                    viewModel.onImportDatabase(it)
                    databaseImported = it != null
                    showImportDbDialog = false
                }

                return@Surface
            }

            val sheetAnimationSpec = tween<Float>(
                durationMillis = Theme.animation.medium, easing = LinearEasing
            )
            val sheetState = rememberStandardBottomSheetState(
                initialValue = SheetValue.PartiallyExpanded,
//                animationSpec = sheetAnimationSpec, TODO MAT3
            )

            var targetSheetFraction by remember { mutableFloatStateOf(0f) }
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
                    containerColor = Theme.colors.primary,
//                    sheetGesturesEnabled = false TODO MAT3
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
}
