package com.daton.media

import android.app.Activity
import android.content.ComponentName
import android.graphics.Bitmap
import android.os.Bundle
import android.support.v4.media.MediaBrowserCompat
import android.support.v4.media.MediaMetadataCompat
import android.support.v4.media.session.MediaControllerCompat
import android.support.v4.media.session.MediaSessionCompat
import android.support.v4.media.session.PlaybackStateCompat
import android.util.Log
import com.daton.media.data.MediaAction
import com.daton.media.data.MediaId
import com.daton.media.device.Loop
import com.daton.media.device.Playlist
import com.daton.media.ext.*
import com.daton.media.service.MediaPlaybackService
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json


class MediaController {
    lateinit var browser: MediaBrowserCompat
        private set

    private val controllerCallback: MediaControllerCallback = MediaControllerCallback()

    var onConnected: (() -> Unit)? = null
    var onDisconnected: (() -> Unit)? = null
    var onMediaSourceChanged: (() -> Unit)? = null

    var onMediaIdChanged: (() -> Unit)? = null

    private var activity: Activity? = null

    fun create(activity: Activity) {
        this.activity = activity

        browser = MediaBrowserCompat(
            activity,
            ComponentName(activity, MediaPlaybackService::class.java),
            ConnectionCallback(),
            null
        )

        Log.d("Mucify", "MediaBrowserController created")
    }

    /**
     * Connects to the [MediaBrowserService]. Should be called from Activity.onStart
     */
    fun connect(activity: Activity) {
        this.activity = activity
        browser.connect()
        Log.d("Mucify", "Started connecting to MediaPlaybackService")
    }

    /**
     * Disconnects from the [MediaBrowserService] and unregisters any callbacks. Should be called from Activity.onStop
     */
    fun disconnect() {
        if (activity != null && MediaControllerCompat.getMediaController(activity!!) != null) {
            MediaControllerCompat.getMediaController(activity!!)
                .unregisterCallback(controllerCallback)
        }
        browser.disconnect()
        activity = null
        Log.d("Mucify", "Started disconnecting from MediaPlaybackService")
    }

    /**
     * Passes the new media id to the service, after this, all media operations are supported
     */
    fun setPlayback(mediaId: MediaId) {
        val bundle = Bundle()
        bundle.putString(MediaAction.MediaId, mediaId.serialize())
        sendCustomAction(MediaAction.SetMediaId, bundle)
    }

    /**
     * Pauses the currently playing audio. Crashes if the Playback hasn't been started yet.
     */
    fun pause() {
        MediaControllerCompat.getMediaController(activity!!).transportControls.pause()
    }

    /**
     * Seeks the currently playing audio to the specified offset. Crashes if the Playback hasn't been started yet.
     */
    fun seekTo(millis: Long) {
        MediaControllerCompat.getMediaController(activity!!).transportControls.seekTo(millis)
    }

    /**
     * @return media id of current playback (song/loop/playlist)
     */
    var mediaId: MediaId
        get() = metadata.mediaId
        set(value) {
            setPlayback(value)
        }

    /**
     * Starts playback, requires that media id was already set
     */
    fun play() {
        MediaControllerCompat.getMediaController(activity!!).transportControls.play()
    }

    /**
     * Starts the next song in either the playlist or the next one in the sorting order after the current one.
     * Loops back to the start if we're at the end
     */
    operator fun next() {
        MediaControllerCompat.getMediaController(activity!!).transportControls.skipToNext()
    }

    /**
     * Starts the previous song in either the playlist or the previous one in the sorting order before the current one.
     * Loops back to the end if we're at the start
     */
    fun previous() {
        MediaControllerCompat.getMediaController(activity!!).transportControls.skipToPrevious()
    }

    /**
     * Checks if the service already has a media id to play. If true all media operations will be supported
     */
    val isCreated: Boolean
        get() = activity != null && playbackState.state != PlaybackStateCompat.STATE_NONE

    /**
     * Checks if the playback state is equal to playing. Crashes if the Playback hasn't been started yet.
     */
    var isPlaying: Boolean
        get() = playbackState.state == PlaybackStateCompat.STATE_PLAYING
        set(value) {
            if (value) play() else pause()
        }

    /**
     * Checks if the playback state is equal to paused. Crashes if the Playback hasn't been started yet.
     */
    var isPaused: Boolean
        get() = playbackState.state == PlaybackStateCompat.STATE_PAUSED
        set(value) {
            if (value) pause() else play()
        }

    /**
     * Start time of the currently playing song. Crashes if the Playback hasn't been started yet.
     */
    var startTime: Long
        get() = metadata.startTime
        set(value) {
            val bundle = Bundle()
            bundle.putLong(MediaAction.StartTime, value)
            sendCustomAction(MediaAction.SetStartTime, bundle)
        }

    /**
     * End time of the currently playing song. Crashes if the Playback hasn't been started yet.
     */
    var endTime: Long
        get() = metadata.endTime
        set(value) {
            val bundle = Bundle()
            bundle.putLong(MediaAction.EndTime, value)
            sendCustomAction(MediaAction.SetEndTime, bundle)
        }

    /**
     * @return the image associated with the album of the current playing playback
     */
    val albumArt: Bitmap?
        get() = metadata.albumArt

    /**
     * Current position of the currently playing song. Crashes if the Playback hasn't been started yet.
     */
    var currentPosition: Long
        get() = playbackState.position
        set(value) {
            seekTo(value)
        }

    /**
     * Duration of the currently playing song. Crashes if the Playback hasn't been started yet.
     */
    val duration: Long
        get() = metadata.duration

    /**
     * Title of the currently playing song. Metadata must've been set, otherwise the
     * function will crash.
     */
    val title: String
        get() = metadata.title

    /**
     * Artist of the currently playing song. Metadata must've been set, otherwise the
     * function will crash.
     */
    val artist: String
        get() = metadata.artist

    /**
     * Updates the [MediaSource] with [loops]. Overwrites previous loops
     */
    fun sendLoops(loops: List<Loop>) {
        if (loops.isEmpty())
            return

        val bundle = Bundle()
        bundle.putStringArray(
            MediaAction.Loops,
            Array(loops.size) { i -> Json.encodeToString(loops[i]) })

        sendCustomAction(MediaAction.SendLoops, bundle)
    }

    /**
     * Updates the [MediaSource] with [playlists]. Overwrites previous playlists
     */
    fun sendPlaylists(playlists: List<Playlist>) {
        if (playlists.isEmpty())
            return

        val bundle = Bundle()
        bundle.putStringArray(
            MediaAction.Playlists,
            Array(playlists.size) { i -> Json.encodeToString(playlists[i]) })

        sendCustomAction(MediaAction.SendPlaylists, bundle)
    }

    /**
     * @return playback state which was set in MediaPlaybackService
     */
    val playbackState: PlaybackStateCompat
        get() = MediaControllerCompat.getMediaController(activity!!).playbackState

    /**
     * @return metadata which was set in the MediaPlaybackService
     */
    val metadata: MediaMetadataCompat
        get() = MediaControllerCompat.getMediaController(activity!!).metadata

    fun sendCustomAction(action: String?, bundle: Bundle?) {
        MediaControllerCompat.getMediaController(activity!!).transportControls.sendCustomAction(
            action,
            bundle
        )
    }

    private inner class ConnectionCallback : MediaBrowserCompat.ConnectionCallback() {
        override fun onConnected() {
            val token: MediaSessionCompat.Token = browser.sessionToken

            // Create a MediaControllerCompat
            val mediaController = MediaControllerCompat(activity!!, token)

            // Save the controller
            MediaControllerCompat.setMediaController(activity!!, mediaController)

            // Finish building the UI
            onConnected?.invoke()

            // Register a Callback to stay in sync
            MediaControllerCompat.getMediaController(activity!!)
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

    private inner class MediaControllerCallback : MediaControllerCompat.Callback() {
        override fun onSessionEvent(event: String, extras: Bundle) {
            when (event) {
                MediaAction.MediaSourceChanged -> {
                    onMediaSourceChanged?.invoke()
                }
                MediaAction.MediaIdChanged -> {
                    onMediaIdChanged?.invoke()
                }
            }
        }

        override fun onSessionDestroyed() {
            Log.d("Mucify", "MediaControllerActivity session destroyed")
            browser.disconnect()
            // maybe schedule a reconnection using a new MediaBrowser instance
            onDisconnected?.invoke()
        }
    }
}