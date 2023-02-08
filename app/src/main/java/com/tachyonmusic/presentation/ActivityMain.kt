package com.tachyonmusic.presentation

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.BottomSheetScaffold
import androidx.compose.material.BottomSheetValue
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.Scaffold
import androidx.compose.material.rememberBottomSheetScaffoldState
import androidx.compose.material.rememberBottomSheetState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.lifecycleScope
import com.google.accompanist.navigation.animation.rememberAnimatedNavController
import com.tachyonmusic.app.R
import com.tachyonmusic.domain.repository.MediaBrowserController
import com.tachyonmusic.logger.domain.Logger
import com.tachyonmusic.presentation.player.Player
import com.tachyonmusic.presentation.theme.TachyonTheme
import com.tachyonmusic.presentation.theme.Theme
import com.tachyonmusic.presentation.util.Permission
import com.tachyonmusic.presentation.util.PermissionManager
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class ActivityMain : ComponentActivity(), MediaBrowserController.EventListener {

    @Inject
    lateinit var log: Logger

    @Inject
    lateinit var mediaBrowser: MediaBrowserController

    // TODO: Better way of awaiting two events
    private val permissionJob = CompletableDeferred<Boolean>()
    private val mediaControllerConnectionJob = Job()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        PermissionManager.from(this).apply {
            request(Permission.ReadExternalStorage)
            request(Permission.ReadMediaAudio)
            rationale(getString(R.string.storage_permission_rationale))
            checkPermission { result: Boolean ->
                permissionJob.complete(result)

                if (result) log.info("Storage permission granted")
                else log.info("Storage permission NOT granted")
            }
        }

        mediaBrowser.registerLifecycle(lifecycle)
        mediaBrowser.registerEventListener(this)

        lifecycleScope.launch {
            mediaControllerConnectionJob.join()
            permissionJob.await()

            setupUi()
        }
    }

    override fun onConnected() {
        mediaControllerConnectionJob.complete()
    }

    @OptIn(ExperimentalAnimationApi::class, ExperimentalMaterialApi::class)
    private fun setupUi() {
        setContent {
            TachyonTheme {

                val sheetState = rememberBottomSheetState(
                    initialValue = BottomSheetValue.Collapsed, animationSpec = tween(
                        durationMillis = Theme.animation.medium, easing = LinearEasing
                    )
                )
                val scaffoldState = rememberBottomSheetScaffoldState(bottomSheetState = sheetState)

                val miniPlayerHeight = remember { mutableStateOf(0.dp) }

                val navController = rememberAnimatedNavController()

                Scaffold(bottomBar = {
                    BottomNavigation(navController)
                }) { innerPaddingScaffold ->

                    BottomSheetScaffold(
                        modifier = Modifier.padding(innerPaddingScaffold),
                        scaffoldState = scaffoldState,
                        sheetContent = {
                            Box(
                                modifier = Modifier
                                    .fillMaxSize(),
                            ) {
                                Player(sheetState, miniPlayerHeight)
                            }
                        },
                        sheetPeekHeight = miniPlayerHeight.value,
                        sheetBackgroundColor = Theme.colors.primary
                    ) { innerPaddingSheet ->

                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(innerPaddingSheet)
                        ) {
                            NavigationGraph(navController, sheetState, miniPlayerHeight)
                        }
                    }
                }
            }
        }
    }
}
