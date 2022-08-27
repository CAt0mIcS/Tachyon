package com.tachyonmusic.presentation

import android.content.ComponentName
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.material.Scaffold
import androidx.lifecycle.lifecycleScope
import androidx.media3.session.MediaBrowser
import androidx.media3.session.SessionToken
import androidx.navigation.compose.rememberNavController
import com.tachyonmusic.app.R
import com.tachyonmusic.media.data.BrowserTree
import com.tachyonmusic.media.service.MediaPlaybackService
import com.tachyonmusic.presentation.main.component.BottomNavigation
import com.tachyonmusic.presentation.util.Permission
import com.tachyonmusic.presentation.util.PermissionManager
import com.tachyonmusic.ui.theme.TachyonTheme
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.guava.await
import kotlinx.coroutines.launch

@AndroidEntryPoint
class ActivityMain : ComponentActivity() {

    private lateinit var mediaBrowser: MediaBrowser

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            TachyonTheme {
                val navController = rememberNavController()
                Scaffold(
                    bottomBar = { BottomNavigation(navController) }
                ) {
                    NavigationGraph(navController)
                }
            }
        }

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

        lifecycleScope.launch {
            val sessionToken = SessionToken(
                this@ActivityMain,
                ComponentName(this@ActivityMain, MediaPlaybackService::class.java)
            )

            mediaBrowser = MediaBrowser.Builder(this@ActivityMain, sessionToken)
                .buildAsync()
                .await()

            val items =
                mediaBrowser.getChildren(BrowserTree.ROOT, 3, 20, null).await().value!!
            Log.d("ActivityMain", "Finished onGetChildren")
            for (item in items) {
                println("Item ${item.mediaId}")
            }
        }
    }
}