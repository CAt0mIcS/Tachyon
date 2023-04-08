package com.tachyonmusic.presentation.entry

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import com.tachyonmusic.domain.repository.MediaBrowserController
import com.tachyonmusic.logger.domain.Logger
import com.tachyonmusic.permission.domain.UriPermissionRepository
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject


@AndroidEntryPoint
class ActivityMain : ComponentActivity(), MediaBrowserController.EventListener {

    @Inject
    lateinit var log: Logger

    @Inject
    lateinit var mediaBrowser: MediaBrowserController

    @Inject
    lateinit var uriPermissionRepository: UriPermissionRepository


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        mediaBrowser.registerLifecycle(lifecycle)
        mediaBrowser.registerEventListener(this)
    }

    override fun onResume() {
        super.onResume()
        uriPermissionRepository.dispatchUpdate()
    }

    override fun onConnected() {
        setupUi()
    }


    private fun setupUi() {
        setContent {
            MainScreen()
        }
    }
}
