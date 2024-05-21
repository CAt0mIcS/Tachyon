package com.tachyonmusic.presentation.entry

import android.os.Bundle
import android.view.Menu
import androidx.activity.compose.setContent
import androidx.appcompat.app.AppCompatActivity
import com.google.android.gms.ads.MobileAds
import com.google.android.gms.ads.RequestConfiguration
import com.google.android.gms.cast.framework.CastButtonFactory
import com.google.android.gms.cast.framework.CastContext
import com.tachyonmusic.app.R
import com.tachyonmusic.database.domain.repository.DataRepository
import com.tachyonmusic.domain.repository.MediaBrowserController
import com.tachyonmusic.logger.domain.Logger
import com.tachyonmusic.playback_layers.domain.UriPermissionRepository
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject


// https://developer.spotify.com/documentation/android/tutorials/getting-started#introduction

@AndroidEntryPoint
class ActivityMain : AppCompatActivity(), MediaBrowserController.EventListener {

    @Inject
    lateinit var log: Logger

    @Inject
    lateinit var mediaBrowser: MediaBrowserController

    @Inject
    lateinit var uriPermissionRepository: UriPermissionRepository

    @Inject
    lateinit var dataRepository: DataRepository

    private var castContext: CastContext? = null


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        MobileAds.initialize(this)
        MobileAds.setRequestConfiguration(
            RequestConfiguration.Builder().setTestDeviceIds(listOf("TEST_EMULATOR")).build()
        )

        // Initialize the Cast context. This is required so that the media route button can be
        // created in the AppBar
        castContext = CastContext.getSharedInstance(this)


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


    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        super.onCreateOptionsMenu(menu)
        menuInflater.inflate(R.menu.main_activity_menu, menu)

        /**
         * Set up a MediaRouteButton to allow the user to control the current media playback route
         */
        menu?.let {
            CastButtonFactory.setUpMediaRouteButton(this, menu, R.id.media_route_menu_item)
        }
        return true
    }

    private fun setupUi() {
        setContent {
            MainScreen()
        }
    }
}
