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
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import com.google.accompanist.navigation.animation.rememberAnimatedNavController
import com.tachyonmusic.app.R
import com.tachyonmusic.core.SharedPrefsKeys
import com.tachyonmusic.domain.repository.MediaBrowserController
import com.tachyonmusic.presentation.player.PlayerScreen
import com.tachyonmusic.presentation.theme.TachyonTheme
import com.tachyonmusic.presentation.theme.Theme
import com.tachyonmusic.presentation.util.Permission
import com.tachyonmusic.presentation.util.PermissionManager
import com.tachyonmusic.presentation.util.plus
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class ActivityMain : ComponentActivity(), MediaBrowserController.EventListener {

    @Inject
    lateinit var mediaBrowser: MediaBrowserController

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        PermissionManager.from(this).apply {
            request(Permission.ReadExternalStorage)
            request(Permission.ReadMediaAudio)
            rationale(getString(R.string.storage_permission_rationale))
            checkPermission { result: Boolean ->
                if (result) println("Storage permission granted")
                else {
                    println("Storage permission NOT granted")
                }

            }
        }

        mediaBrowser.registerLifecycle(lifecycle)
        mediaBrowser.registerEventListener(this)
    }

    @OptIn(ExperimentalAnimationApi::class, ExperimentalMaterialApi::class)
    override fun onConnected() {
        setContent {
            TachyonTheme {

                val sheetState = rememberBottomSheetState(
                    initialValue = BottomSheetValue.Collapsed, animationSpec = tween(
                        durationMillis = Theme.animation.medium, easing = LinearEasing
                    )
                )
                val scaffoldState = rememberBottomSheetScaffoldState(bottomSheetState = sheetState)
                val navController = rememberAnimatedNavController()

                Scaffold(bottomBar = {
                    val animationSpec = tween<IntOffset>(
                        durationMillis = Theme.animation.long, easing = LinearEasing
                    )

                    // TODO: Shouldn't be hard-coded: Used to make BottomNavigation disappear
                    val offset = Int.MAX_VALUE

                    AnimatedVisibility(
                        visible = sheetState.progress.fraction == 1.0f && sheetState.isCollapsed,
                        enter = slideInVertically(animationSpec, initialOffsetY = { offset }),
                        exit = slideOutVertically(animationSpec, targetOffsetY = { offset })
                    ) {
                        BottomNavigation(navController)
                    }
                }) { innerPaddingScaffold ->

                    BottomSheetScaffold(
                        scaffoldState = scaffoldState, sheetContent = {
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(innerPaddingScaffold)
                                    // BottomSheetScaffold sets color, overwrite with Theme.colors.primary
                                    .background(Theme.colors.primary)
                            ) {
                                PlayerScreen(navController, sheetState)
                            }
                        }, sheetPeekHeight = 0.dp
                    ) { innerPaddingSheet ->

                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(innerPaddingScaffold + innerPaddingSheet)
                        ) {
                            NavigationGraph(navController, sheetState)
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
