package com.tachyonmusic.presentation.entry

import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
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
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.layoutId
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.constraintlayout.compose.ConstraintSet
import androidx.constraintlayout.compose.MotionLayout
import androidx.hilt.navigation.compose.hiltViewModel
import com.google.accompanist.navigation.animation.rememberAnimatedNavController
import com.tachyonmusic.app.R
import com.tachyonmusic.database.data.data_source.Database
import com.tachyonmusic.presentation.core_components.UriPermissionDialog
import com.tachyonmusic.presentation.player.PlayerLayout
import com.tachyonmusic.presentation.profile.component.OpenDocumentDialog
import com.tachyonmusic.presentation.theme.TachyonTheme
import com.tachyonmusic.presentation.theme.Theme
import com.tachyonmusic.presentation.util.asString
import com.tachyonmusic.util.EventSeverity

enum class SwipingStates {
    EXPANDED,
    COLLAPSED
}

@Composable
fun MainScreen(
    viewModel: MainViewModel = hiltViewModel()
) {
    val settings by viewModel.composeSettings.collectAsState()
    val requiresMusicPathSelection by viewModel.requiresMusicPathSelection.collectAsState()
    val requiredMusicPathsAfterDatabaseImport by viewModel.requiredMusicDirectoriesAfterDatabaseImport.collectAsState()
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
                            .background(
                                MaterialTheme.colorScheme.background,
                                Theme.shapes.large
                            )
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
                                stringResource(R.string.loading),
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

                    if (requiredMusicPathsAfterDatabaseImport.isNotEmpty()) {
                        Text("The following directories are required by the imported database")
                        for (dir in requiredMusicPathsAfterDatabaseImport)
                            Text(dir)
                    }

                    Button(onClick = { showUriPermissionDialog = true }) {
                        Text("Select...")
                    }

                    if (!databaseImported)
                        Button(onClick = { showImportDbDialog = true }) {
                            Text("Import Database")
                        }
                }

                UriPermissionDialog(showUriPermissionDialog) {
                    viewModel.setNewMusicDirectory(it)
                    showUriPermissionDialog = false
                }

                OpenDocumentDialog(showImportDbDialog, Database.JSON_MIME_TYPE) {
                    viewModel.onImportDatabase(it)
                    databaseImported = it != null
                    showImportDbDialog = false
                }

                return@Surface
            }


            var miniPlayerHeight by remember { mutableStateOf(0.dp) }
            val navController = rememberAnimatedNavController()
            val localDensity = LocalDensity.current

            val anchoredDraggableState = remember {
                AnchoredDraggableState(
                    initialValue = SwipingStates.COLLAPSED,
                    anchors = DraggableAnchors {},
                    positionalThreshold = { distance: Float -> distance * 0.5f },
                    velocityThreshold = { with(localDensity) { 100.dp.toPx() } },
                    animationSpec = tween(),
                )
            }

            /***************************************************************************************
             * Show Messages from [EventChannel]
             **************************************************************************************/
            val snackbarHostState = remember { SnackbarHostState() }

            val context = LocalContext.current
            LaunchedEffect(key1 = true) {
                viewModel.eventChannel.collect { event ->
                    snackbarHostState.showSnackbar(
                        event.message.asString(context),
                        withDismissAction = true,
                        duration = when (event.severity) {
                            EventSeverity.Error, EventSeverity.Fatal -> SnackbarDuration.Long
                            else -> SnackbarDuration.Short
                        }
                    )
                }
            }

            Scaffold(
                snackbarHost = { SnackbarHost(snackbarHostState) },
                bottomBar = {
                    BottomNavigation(anchoredDraggableState, navController)
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

                    anchoredDraggableState.updateAnchors(anchors)
                    val offset =
                        if (anchoredDraggableState.offset.isNaN()) 0f else anchoredDraggableState.offset
                    val progress =
                        (1 - (offset / heightInPx)).coerceIn(0f, 1f)

                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(MaterialTheme.colorScheme.background)
                    ) {
                        Column {
                            MotionLayoutHeader(
                                progress,
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
                                    ) {
                                        // TODO: When MiniPlayer is collapsed [progress] is still something like 0.079...
                                        //      we're correcting it here and it seems to be missing [miniPlayerHeight]
                                        //      and extra small padding to make it 0 when collapsed and 1 when expanded.
                                        //      This may not be true for all devices and densities and paddings and size and ...
                                        val extraSmallPaddingInPx = with(LocalDensity.current) {
                                            Theme.padding.extraSmall.toPx()
                                        }

                                        val correctedProgress =
                                            (1 - (offset / (heightInPx - miniPlayerHeightInPx - extraSmallPaddingInPx)))
                                                .coerceIn(0f, 1f)

                                        PlayerLayout(
                                            miniPlayerHeight,
                                            { miniPlayerHeight = it },
                                            anchoredDraggableState,
                                            correctedProgress,
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

@Composable
private fun MotionLayoutHeader(
    progress: Float,
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

