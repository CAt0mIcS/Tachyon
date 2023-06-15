package com.tachyonmusic.presentation.entry

import android.content.Intent
import android.os.Bundle
import android.view.Menu
import androidx.activity.compose.setContent
import androidx.appcompat.app.AppCompatActivity
import com.google.android.gms.cast.framework.CastButtonFactory
import com.google.android.gms.cast.framework.CastContext
import com.spotify.android.appremote.api.SpotifyAppRemote
import com.tachyonmusic.app.R
import com.tachyonmusic.domain.repository.MediaBrowserController
import com.tachyonmusic.domain.repository.SpotifyInterfacer
import com.tachyonmusic.logger.domain.Logger
import com.tachyonmusic.permission.domain.UriPermissionRepository
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
    lateinit var spotifyInterfacer: SpotifyInterfacer

    private var castContext: CastContext? = null

    private var spotifyAppRemote: SpotifyAppRemote? = null


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

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

    override fun onStart() {
        super.onStart()
        spotifyInterfacer.authorize(this)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, intent: Intent?) {
        super.onActivityResult(requestCode, resultCode, intent)
        spotifyInterfacer.onAuthorization(requestCode, resultCode, intent)
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
