package com.tachyonmusic.presentation.entry

import android.content.Intent
import android.os.Bundle
import android.view.Menu
import androidx.activity.compose.setContent
import androidx.appcompat.app.AppCompatActivity
import com.google.android.gms.cast.framework.CastButtonFactory
import com.google.android.gms.cast.framework.CastContext
import com.spotify.android.appremote.api.ConnectionParams
import com.spotify.android.appremote.api.Connector
import com.spotify.android.appremote.api.SpotifyAppRemote
import com.spotify.sdk.android.auth.AuthorizationClient
import com.spotify.sdk.android.auth.AuthorizationRequest
import com.spotify.sdk.android.auth.AuthorizationResponse
import com.tachyonmusic.app.R
import com.tachyonmusic.domain.repository.MediaBrowserController
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

    private var castContext: CastContext? = null

    val REQUEST_CODE = 1337
    private val clientId = "2a708447488345f3b1d0452821e269af"

    //    private val redirectUri = Uri.Builder()
//        .scheme("spotify-sdk")
//        .authority("auth")
//        .build()
    private val redirectUri = "yourcustomprotocol://callback"
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

        val builder = AuthorizationRequest.Builder(
            clientId,
            AuthorizationResponse.Type.TOKEN,
            redirectUri.toString()
        )

        builder.setScopes(
            arrayOf(
                "app-remote-control",
                "user-read-playback-state",
                "user-modify-playback-state",
                "user-read-currently-playing",
                "playlist-read-private",
                "playlist-read-collaborative"
            )
        )
        val request = builder.build()

        AuthorizationClient.openLoginActivity(this, REQUEST_CODE, request)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, intent: Intent?) {
        super.onActivityResult(requestCode, resultCode, intent)

        // Check if result comes from the correct activity
        if (requestCode == REQUEST_CODE) {
            val response = AuthorizationClient.getResponse(resultCode, intent)
            when (response.type) {
                AuthorizationResponse.Type.TOKEN -> {
                    connectToSpotify()
                }
                AuthorizationResponse.Type.ERROR -> {
                    log.error(response.error)
                }
                else -> {}
            }
        }
    }

    fun connectToSpotify() {
        val connectionParams = ConnectionParams.Builder(clientId)
            .setRedirectUri(redirectUri.toString())
            .showAuthView(true)
            .build()

        SpotifyAppRemote.connect(this, connectionParams, object : Connector.ConnectionListener {
            override fun onConnected(appRemote: SpotifyAppRemote) {
                spotifyAppRemote = appRemote
                log.info("Spotify connected successfully")
                onSpotifyConnected()
            }

            override fun onFailure(throwable: Throwable) {
                log.error(throwable.message.toString())
            }
        })
    }

    fun onSpotifyConnected() {
        spotifyAppRemote!!.playerApi.play("spotify:playlist:37i9dQZF1DX2sUQwD7tbmL")
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
