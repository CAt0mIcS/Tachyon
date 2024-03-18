@file:OptIn(
    ExperimentalMaterial3Api::class, ExperimentalAnimationApi::class,
    ExperimentalMotionApi::class, ExperimentalMaterialApi::class, ExperimentalFoundationApi::class
)

package com.tachyonmusic.presentation.entry

import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.AnchoredDraggableState
import androidx.compose.foundation.gestures.DraggableAnchors
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.anchoredDraggable
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
import androidx.compose.material.SwipeProgress
import androidx.compose.material.SwipeableState
import androidx.compose.material.rememberSwipeableState
import androidx.compose.material.swipeable
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.input.nestedscroll.NestedScrollSource
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.layout.layoutId
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
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
import com.tachyonmusic.presentation.player.PlayerLayout
import com.tachyonmusic.presentation.profile.component.OpenDocumentDialog
import com.tachyonmusic.presentation.theme.TachyonTheme
import com.tachyonmusic.presentation.theme.Theme
import java.lang.IllegalArgumentException
import java.util.EnumSet

enum class SwipingStates {
    EXPANDED,
    COLLAPSED
}

@OptIn(ExperimentalFoundationApi::class)
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
                                BorderStroke(2.dp, MaterialTheme.colorScheme.surfaceContainer),
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


            var miniPlayerHeight by remember { mutableStateOf(0.dp) }
            val navController = rememberAnimatedNavController()


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
                    val miniPlayerHeightInPx =
                        with(LocalDensity.current) { if (miniPlayerHeight <= 0.dp) 70.dp.toPx() else miniPlayerHeight.toPx() }

                    // https://www.strv.com/blog/collapsing-toolbar-using-jetpack-compose-motion-layout-engineering
                    // https://medium.com/@AungThiha3/jetpack-compose-anchored-draggable-item-in-motionlayout-part-1-8d5a1cde880f

                    val anchors = DraggableAnchors {
                        SwipingStates.COLLAPSED at heightInPx - miniPlayerHeightInPx
                        SwipingStates.EXPANDED at 0f
                    }
                    val density = LocalDensity.current
                    val anchoredDraggableState = remember {
                        AnchoredDraggableState(
                            initialValue = SwipingStates.COLLAPSED,
                            anchors = anchors,
                            positionalThreshold = { distance: Float -> distance * 0.5f },
                            velocityThreshold = { with(density) { 100.dp.toPx() } },
                            animationSpec = tween(),
                        )
                    }
                    val offset =
                        if (anchoredDraggableState.offset.isNaN()) 0f else anchoredDraggableState.offset
                    val progress = (1 - (offset / heightInPx)).coerceIn(0f, 1f)

                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(MaterialTheme.colorScheme.background)
                    ) {
                        Column {
                            MotionLayoutHeader(
                                progress,
                                anchoredDraggableState,
                                mainContent = { modifier ->
                                    Box(modifier = modifier.fillMaxHeight()) {
                                        NavigationGraph(
                                            navController,
                                            miniPlayerHeight,
                                            anchoredDraggableState
                                        )
                                    }
                                },
                                scrollableBody = {
                                    // TODO: Animate properly: Image moving from left corner to big screen, text moving, ...

                                    Box(
                                        modifier = Modifier
                                            .fillMaxSize()
                                            .anchoredDraggable(
                                                anchoredDraggableState,
                                                Orientation.Vertical
                                            )
                                            .padding(top = Theme.padding.extraSmall)
                                    ) {
                                        PlayerLayout(
                                            navController,
                                            miniPlayerHeight,
                                            { miniPlayerHeight = it },
                                            anchoredDraggableState,
                                            progress,
                                        )
                                    }
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMotionApi::class)
@Composable
private fun MotionLayoutHeader(
    progress: Float,
    draggableState: AnchoredDraggableState<SwipingStates>,
    mainContent: @Composable (Modifier) -> Unit,
    scrollableBody: @Composable () -> Unit
) {
    MotionLayout(
        start = jsonConstraintSetStart(),
        end = jsonConstraintSetEnd(),
        progress = progress,
        modifier = Modifier.fillMaxWidth()
    ) {
        mainContent(
            Modifier
                .layoutId("homeScreen")
                .fillMaxWidth()
        )

        Box(Modifier.layoutId("content")) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(Theme.shapes.large)
                    .background(MaterialTheme.colorScheme.background, Theme.shapes.large)
            ) {
                scrollableBody()
            }
        }
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
		top: ['homeScreen', 'bottom', 0],
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
	}  
} """
)

