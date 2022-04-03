package com.de.mucify.ui

import android.content.ComponentName
import android.content.Intent
import android.os.Bundle
import android.os.PersistableBundle
import android.support.v4.media.MediaBrowserCompat
import android.support.v4.media.session.MediaControllerCompat
import android.support.v4.media.session.MediaSessionCompat
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import com.de.common.service.MediaPlaybackService
import com.de.mucify.R


class ActivityLibrary : AppCompatActivity() {

    private val mediaBrowser: MediaBrowserCompat by lazy {
        MediaBrowserCompat(
            this,
            ComponentName(this, MediaPlaybackService::class.java),
            ConnectionCallback(),
            null
        )
    }

    override fun onCreate(savedInstanceState: Bundle?, persistentState: PersistableBundle?) {
        super.onCreate(savedInstanceState, persistentState)
        setContentView(R.layout.activity_player)

        startService(Intent(this, MediaPlaybackService::class.java))
    }

    override fun onStart() {
        super.onStart()
        mediaBrowser.connect()
        Log.d("Mucify", "Started connecting to MediaPlaybackService")
    }

    override fun onStop() {
        super.onStop()
        if (MediaControllerCompat.getMediaController(this) != null) {
            MediaControllerCompat.getMediaController(this)
                .unregisterCallback(controllerCallback)
        }
        mediaBrowser.disconnect()
        Log.d("Mucify", "Started disconnecting from MediaPlaybackService")
    }


    fun onConnected() {
        Log.d("Mucify", "Connected to media browser service")
    }


    private inner class ConnectionCallback : MediaBrowserCompat.ConnectionCallback() {
        override fun onConnected() {
            val token: MediaSessionCompat.Token = mediaBrowser.sessionToken

            // Create a MediaControllerCompat
            val mediaController = MediaControllerCompat(this@ActivityLibrary, token)

            // Save the controller
            MediaControllerCompat.setMediaController(this@ActivityLibrary, mediaController)

            // Finish building the UI
            this@ActivityLibrary.onConnected()

            // Register a Callback to stay in sync
            MediaControllerCompat.getMediaController(this@ActivityLibrary)
                .registerCallback(controllerCallback)
            Log.d("Mucify", "MediaBrowserController connection established")
        }

        override fun onConnectionSuspended() {
            // The Service has crashed. Disable transport controls until it automatically reconnects
            Log.d("Mucify", "MediaControllerActivity connection suspended")
        }

        override fun onConnectionFailed() {
            // The Service has refused our connection
            Log.d("Mucify", "MediaControllerActivity connection failed")
        }
    }


    private val controllerCallback = object : MediaControllerCompat.Callback() {
        override fun onSessionDestroyed() {
            Log.d("Mucify", "MediaControllerActivity session destroyed")
            mediaBrowser.disconnect()
            // maybe schedule a reconnection using a new MediaBrowser instance
        }
    }

}