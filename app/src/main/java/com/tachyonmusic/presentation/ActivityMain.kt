package com.tachyonmusic.presentation

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.*
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.lifecycle.lifecycleScope
import com.google.accompanist.navigation.animation.rememberAnimatedNavController
import com.tachyonmusic.app.R
import com.tachyonmusic.core.SharedPrefsKeys
import com.tachyonmusic.domain.repository.MediaBrowserController
import com.tachyonmusic.logger.Log
import com.tachyonmusic.logger.domain.Logger
import com.tachyonmusic.presentation.player.PlayerScreen
import com.tachyonmusic.presentation.theme.TachyonTheme
import com.tachyonmusic.presentation.theme.Theme
import com.tachyonmusic.presentation.util.Permission
import com.tachyonmusic.presentation.util.PermissionManager
import com.tachyonmusic.presentation.util.plus
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.*
import okhttp3.internal.wait
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

                val miniPlayerHeight = remember { mutableStateOf(66.dp) }

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
                                    .fillMaxSize()
                            ) {
                                PlayerScreen(navController, sheetState, miniPlayerHeight)
                            }
                        },
                        sheetPeekHeight = 66.dp,
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

    override fun onStop() {
        super.onStop()

        // TODO: Should probably be somewhere else
        getSharedPreferences(SharedPrefsKeys.NAME, MODE_PRIVATE).edit()
            .putBoolean(SharedPrefsKeys.FIRST_APP_START, false).apply()
    }
}
