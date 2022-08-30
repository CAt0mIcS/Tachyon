package com.tachyonmusic.presentation

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.material.Scaffold
import androidx.lifecycle.lifecycleScope
import androidx.navigation.compose.rememberNavController
import com.tachyonmusic.app.R
import com.tachyonmusic.domain.MediaBrowserController
import com.tachyonmusic.domain.MediaPlaybackServiceMediaBrowserController
import com.tachyonmusic.presentation.main.component.BottomNavigation
import com.tachyonmusic.presentation.util.Permission
import com.tachyonmusic.presentation.util.PermissionManager
import com.tachyonmusic.ui.theme.TachyonTheme
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.guava.await
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class ActivityMain : ComponentActivity() {

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

        // TODO: Shouldn't need to cast here
//        lifecycle.addObserver(mediaBrowser as MediaPlaybackServiceMediaBrowserController)
        lifecycleScope.launch {
            (mediaBrowser as MediaPlaybackServiceMediaBrowserController).set(
                (mediaBrowser as MediaPlaybackServiceMediaBrowserController).onCreate(this@ActivityMain)
                    .await()
            )

            setContent {
                TachyonTheme {
                    val navController = rememberNavController()
                    Scaffold(
                        bottomBar = { BottomNavigation(navController) }
                    ) {
                        NavigationGraph(navController, mediaBrowser)
                    }
                }
            }
        }
    }
}