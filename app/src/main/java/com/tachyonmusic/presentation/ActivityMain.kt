package com.tachyonmusic.presentation

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.material.Scaffold
import androidx.navigation.compose.rememberNavController
import com.tachyonmusic.app.R
import com.tachyonmusic.domain.repository.MediaBrowserController
import com.tachyonmusic.data.repository.MediaPlaybackServiceMediaBrowserController
import com.tachyonmusic.presentation.main.component.BottomNavigation
import com.tachyonmusic.presentation.util.Permission
import com.tachyonmusic.presentation.util.PermissionManager
import com.tachyonmusic.presentation.theme.TachyonTheme
import dagger.hilt.android.AndroidEntryPoint
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

        // TODO: Temporary, setContent for now needs to be called after MediaBrowserController initialization
        (mediaBrowser as MediaPlaybackServiceMediaBrowserController).onConnected = {
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

        // TODO: Shouldn't need to cast here
        lifecycle.addObserver(mediaBrowser as MediaPlaybackServiceMediaBrowserController)
    }
}
