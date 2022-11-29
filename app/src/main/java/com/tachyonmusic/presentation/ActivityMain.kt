package com.tachyonmusic.presentation

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.foundation.layout.*
import androidx.compose.material.Scaffold
import androidx.compose.ui.Modifier
import com.google.accompanist.navigation.animation.rememberAnimatedNavController
import com.tachyonmusic.app.R
import com.tachyonmusic.domain.repository.MediaBrowserController
import com.tachyonmusic.presentation.theme.TachyonTheme
import com.tachyonmusic.presentation.util.Permission
import com.tachyonmusic.presentation.util.PermissionManager
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class ActivityMain : ComponentActivity(), MediaBrowserController.EventListener {

    @Inject
    lateinit var mediaBrowser: MediaBrowserController

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        PermissionManager.from(this).apply {
            request(Permission.ReadStorage)
            rationale(getString(R.string.storage_permission_rationale))
            checkPermission { result: Boolean ->
                if (result) {
                    println("Storage permission granted")
                } else
                    println("Storage permission NOT granted")
            }
        }

        mediaBrowser.registerLifecycle(lifecycle)
        mediaBrowser.registerEventListener(this)
    }

    @OptIn(ExperimentalAnimationApi::class)
    override fun onConnected() {
        setContent {
            TachyonTheme {
                val navController = rememberAnimatedNavController()
                Scaffold(
                    bottomBar = { BottomNavigation(navController) }
                ) { innerPadding ->
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(innerPadding)
                    ) {
                        NavigationGraph(navController)
                    }
                }
            }
        }
    }
}
