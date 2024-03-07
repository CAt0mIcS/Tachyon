@file:OptIn(
    ExperimentalMaterial3Api::class, ExperimentalAnimationApi::class,
    ExperimentalMotionApi::class, ExperimentalMaterialApi::class
)

package com.tachyonmusic.presentation.entry

import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.FractionalThreshold
import androidx.compose.material.Scaffold
import androidx.compose.material.rememberSwipeableState
import androidx.compose.material.swipeable
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.input.nestedscroll.NestedScrollSource
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.layout.layoutId
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Velocity
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.constraintlayout.compose.ConstraintSet
import androidx.constraintlayout.compose.ExperimentalMotionApi
import androidx.constraintlayout.compose.MotionLayout
import androidx.constraintlayout.compose.MotionLayoutDebugFlags
import androidx.constraintlayout.compose.MotionLayoutScope
import androidx.hilt.navigation.compose.hiltViewModel
import com.google.accompanist.navigation.animation.rememberAnimatedNavController
import com.tachyonmusic.database.data.data_source.Database
import com.tachyonmusic.presentation.core_components.UriPermissionDialog
import com.tachyonmusic.presentation.player.PlayerScreenMat3
import com.tachyonmusic.presentation.profile.component.OpenDocumentDialog
import com.tachyonmusic.presentation.theme.TachyonTheme
import com.tachyonmusic.presentation.theme.Theme
import java.util.EnumSet

private enum class SwipingStates {
    EXPANDED,
    COLLAPSED
}

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

        Surface {
            if (isLoading) {
                Dialog(
                    onDismissRequest = { },
                    DialogProperties(dismissOnBackPress = false, dismissOnClickOutside = false)
                ) {
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier
                            .clip(Theme.shapes.large)
                            .border(
                                BorderStroke(2.dp, Theme.colors.partialOrange2),
                                Theme.shapes.large
                            )
                    ) {
                        Column {
                            CircularProgressIndicator(
                                modifier = Modifier
                                    .padding(Theme.padding.medium)
                                    .align(Alignment.CenterHorizontally)
                            )

                            Text(
                                "Loading",
                                modifier = Modifier
                                    .padding(
                                        start = 80.dp,
                                        end = 80.dp,
                                        bottom = Theme.padding.medium
                                    ),
                                textAlign = TextAlign.Center
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


            val miniPlayerHeight = remember { mutableStateOf(0.dp) }
            val navController = rememberAnimatedNavController()

            // https://www.strv.com/blog/collapsing-toolbar-using-jetpack-compose-motion-layout-engineering
            val swipingState = rememberSwipeableState(initialValue = SwipingStates.EXPANDED)


            Scaffold(
                bottomBar = {
                    BottomNavigation(navController)
                }
            ) { innerPaddingScaffold ->

                BoxWithConstraints(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPaddingScaffold)
                ) {
                    val heightInPx =
                        with(LocalDensity.current) { maxHeight.toPx() } // Get height of screen
                    val connection = remember {
                        object : NestedScrollConnection {
                            override fun onPreScroll(
                                available: Offset,
                                source: NestedScrollSource
                            ): Offset {
                                val delta = available.y
                                return if (delta < 0) {
                                    swipingState.performDrag(delta).toOffset()
                                } else
                                    Offset.Zero
                            }

                            override fun onPostScroll(
                                consumed: Offset,
                                available: Offset,
                                source: NestedScrollSource
                            ): Offset {
                                val delta = available.y
                                return swipingState.performDrag(delta).toOffset()
                            }

                            override suspend fun onPostFling(
                                consumed: Velocity,
                                available: Velocity
                            ): Velocity {
                                swipingState.performFling(velocity = available.y)
                                return super.onPostFling(consumed, available)
                            }

                            private fun Float.toOffset() = Offset(0f, this)
                        }
                    }

                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .swipeable(
                                state = swipingState,
                                thresholds = { _, _ -> FractionalThreshold(0.5f) },
                                orientation = Orientation.Vertical,
                                anchors = mapOf(
                                    // Maps anchor points (in px) to states
                                    0f to SwipingStates.COLLAPSED,
                                    heightInPx to SwipingStates.EXPANDED,
                                )
                            )
                            .nestedScroll(connection)
                    ) {
                        Column {
                            MotionLayoutHeader(
                                progress = if (swipingState.progress.to == SwipingStates.COLLAPSED)
                                    swipingState.progress.fraction else 1f - swipingState.progress.fraction,
                                mainContent = { modifier ->
                                    Box(modifier = modifier.fillMaxHeight(.9f)) {
                                        NavigationGraph(navController, miniPlayerHeight.value)
                                    }
                                },
                                scrollableBody = {
                                    PlayerScreenMat3()

//                                    PlayerLayout(
//                                        navController,
//                                        onMiniPlayerHeight = { miniPlayerHeight.value = it },
//                                        miniPlayerHeight = miniPlayerHeight.value
//                                    )
                                }
                            )
                        }

                    }
                }
            }
        }
    }
}

@Composable
private fun MotionLayoutHeader(
    progress: Float,
    mainContent: @Composable (Modifier) -> Unit,
    scrollableBody: @Composable MotionLayoutScope.() -> Unit
) {
    MotionLayout(
        start = jsonConstraintSetStart(),
        end = jsonConstraintSetEnd(),
        progress = progress,
        modifier = Modifier.fillMaxWidth(),
        debug = EnumSet.of(MotionLayoutDebugFlags.SHOW_ALL)
    ) {
        mainContent(
            Modifier
                .layoutId("homeScreen")
                .fillMaxWidth()
        )

        Box(
            Modifier
                .layoutId("content")
        ) {

        }

        scrollableBody()
    }
}


private fun jsonConstraintSetStart() = ConstraintSet(
    """ {
	homeScreen: { 
		width: "spread",
		start: ['parent', 'start', 0],
		end: ['parent', 'end', 0],
		top: ['parent', 'top', 0],
	},
	content: {
		width: "spread",
		start: ['parent', 'start', 0],
		end: ['parent', 'end', 0],
		top: ['homeScreen', 'bottom', 16],
	},

    ${LayoutId.PrimaryArtwork}: {
        width: 48,
        height: 48,
        start: ['content', 'start', 8],
        top: ['content', 'top', 8],
        bottom: ['content', 'bottom', 8]
    },
    ${LayoutId.PrimaryTitle}: {
        width: "spread",
        height: "spread",
        top: ['content', 'top', 8],
        start: ['${LayoutId.PrimaryArtwork}', 'end', 8],
        end: ['${LayoutId.PlayPauseButton}', 'start', 8],
        custom: {
            fontSize: 14
        }
    },
    ${LayoutId.PrimarySubtitle}: {
        width: "spread",
        height: "spread",
        top: ['${LayoutId.PrimaryTitle}', 'bottom', 4],
        start: ['${LayoutId.PrimaryArtwork}', 'end', 16],
        end: ['${LayoutId.PlayPauseButton}', 'start', 8],
        custom: {
            fontSize: 12
        }
    },
    ${LayoutId.PlayPauseButton}: {
        width: 48,
        height: 48,
        end: ['content', 'end', 8],
        top: ['content', 'top', 8],
        bottom: ['content', 'bottom', 8]
    }
} """
)

private fun jsonConstraintSetEnd() = ConstraintSet(
    """ {
	homeScreen: { 
		width: "spread",
		height: 0,
		start: ['parent', 'start', 0],
		end: ['parent', 'end', 0],
		top: ['parent', 'top', 0],
	},
	content: {
		width: "spread",
		start: ['parent', 'start', 0],
		end: ['parent', 'end', 0],
		top: ['homeScreen', 'bottom', 0],
	},

    ${LayoutId.PrimaryArtwork}: {
            width: "spread",
            start: ['content', 'start', 16],
            end: ['content', 'end', 16],
            top: ['content', 'top', 16]
    },
    ${LayoutId.PrimaryTitle}: {
        width: "spread",
        height: "wrap",
        start: ['content', 'start', 16],
        end: ['content', 'end', 16],
        top: ['${LayoutId.PrimaryArtwork}', 'bottom', 16],
        custom: {
            fontSize: 24
        }
    },
    ${LayoutId.PrimarySubtitle}: {
        width: "spread",
        height: "wrap",
        start: ['content', 'start', 32],
        end: ['content', 'end', 16],
        top: ['${LayoutId.PrimaryTitle}', 'bottom', 16],
        custom: {
            fontSize: 18
        }
    },
    ${LayoutId.PlayPauseButton}: {
        width: 48,
        height: 48,
        end: ['content', 'end', 8],
        top: ['content', 'top', 8],
        bottom: ['content', 'bottom', 8]
    }     
} """
)


object LayoutId {
    const val PrimaryArtwork = "PrimaryArtwork"
    const val PrimaryTitle = "PrimaryTitle"
    const val PrimarySubtitle = "PrimarySubtitle"
    const val PlayPauseButton = "MediaControlPlayPause"
}

