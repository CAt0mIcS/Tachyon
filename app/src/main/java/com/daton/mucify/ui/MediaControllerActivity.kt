package com.daton.mucify.ui

import android.content.ComponentName
import android.graphics.Bitmap
import android.os.Bundle
import android.support.v4.media.MediaBrowserCompat
import android.support.v4.media.MediaMetadataCompat
import android.support.v4.media.session.MediaControllerCompat
import android.support.v4.media.session.MediaSessionCompat
import android.support.v4.media.session.PlaybackStateCompat
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import com.daton.media.MediaAction
import com.daton.media.ext.*
import com.daton.media.service.MediaPlaybackService


open class MediaControllerActivity : AppCompatActivity() {
    private lateinit var mMediaBrowser: MediaBrowserCompat
    private val mControllerCallback: MediaControllerCallback = MediaControllerCallback()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        mMediaBrowser = MediaBrowserCompat(
            this,
            ComponentName(this, MediaPlaybackService::class.java),
            ConnectionCallback(),
            null
        )

        Log.d("Mucify", "MediaBrowserController created")
    }

    override fun onStart() {
        super.onStart()
        mMediaBrowser.connect()
        Log.d("Mucify", "Started connecting to MediaPlaybackService")
    }

    override fun onStop() {
        super.onStop()
        if (MediaControllerCompat.getMediaController(this) != null) {
            MediaControllerCompat.getMediaController(this)
                .unregisterCallback(mControllerCallback)
        }
        mMediaBrowser.disconnect()
        Log.d("Mucify", "Started disconnecting from MediaPlaybackService")
    }

    /**
     * Pauses the currently playing audio. Crashes if the Playback hasn't been started yet.
     */
    fun pause() {
        MediaControllerCompat.getMediaController(this).transportControls.pause()
    }

    /**
     * Seeks the currently playing audio to the specified offset. Crashes if the Playback hasn't been started yet.
     */
    fun seekTo(millis: Long) {
        MediaControllerCompat.getMediaController(this).transportControls.seekTo(millis)
    }

    /**
     * @return media id of current playback (song/loop/playlist)
     */
    fun getMediaId(): String = getMetadata().mediaId

    /**
     * Starts playback, requires that media id was already set
     */
    fun play() {
        MediaControllerCompat.getMediaController(this).transportControls.play()
    }

    /**
     * Starts the next song in either the playlist or the next one in the sorting order after the current one.
     * Loops back to the start if we're at the end
     */
    operator fun next() {
        MediaControllerCompat.getMediaController(this).transportControls.skipToNext()
    }

    /**
     * Starts the previous song in either the playlist or the previous one in the sorting order before the current one.
     * Loops back to the end if we're at the start
     */
    fun previous() {
        MediaControllerCompat.getMediaController(this).transportControls.skipToPrevious()
    }

    /**
     * Checks if the playback state is equal to playing. Crashes if the Playback hasn't been started yet.
     */
    fun isPlaying(): Boolean {
        return MediaControllerCompat.getMediaController(this).playbackState.state == PlaybackStateCompat.STATE_PLAYING
    }

    /**
     * Checks if the playback state is equal to paused. Crashes if the Playback hasn't been started yet.
     */
    fun isPaused(): Boolean {
        return MediaControllerCompat.getMediaController(this).playbackState.state == PlaybackStateCompat.STATE_PAUSED
    }

    /**
     * Uses a custom event to call onCustomEvent in MediaPlaybackService.
     * Crashes if the Playback hasn't been started yet.
     *
     * @param millis offset from audio position zero.
     */
    fun setStartTime(millis: Long) {
        val bundle = Bundle()
        bundle.putLong(MediaAction.StartTime, millis)
        sendCustomAction(MediaAction.SetStartTime, bundle)
    }

    /**
     * Uses a custom event to call onCustomEvent in MediaPlaybackService.
     * Crashes if the Playback hasn't been started yet.
     *
     * @param millis offset from audio duration.
     */
    fun setEndTime(millis: Long) {
        val bundle = Bundle()
        bundle.putLong(MediaAction.EndTime, millis)
        sendCustomAction(MediaAction.SetEndTime, bundle)
    }

    /**
     * Gets the start time of the currently playing song. Crashes if the Playback hasn't been started yet.
     */
    fun getStartTime(): Long = getMetadata().startTime

    /**
     * Gets the end time of the currently playing song. Crashes if the Playback hasn't been started yet.
     */
    fun getEndTime(): Long = getMetadata().endTime

    /**
     * @return the image associated with the album of the current playing playback
     */
    fun getImage(): Bitmap? {
        return getMetadata().albumArt
    }

    /**
     * Gets the current position of the currently playing song. Crashes if the Playback hasn't been started yet.
     */
    fun getCurrentPosition(): Int {
        return getPlaybackState().position.toInt()
    }

    /**
     * Gets the duration of the currently playing song. Crashes if the Playback hasn't been started yet.
     */
    fun getDuration(): Long = getMetadata().duration

    /**
     * Gets the title of the currently playing song. Metadata must've been set, otherwise the
     * function will crash.
     */
    fun getSongTitle(): String = getMetadata().title

    /**
     * Gets the artist of the currently playing song. Metadata must've been set, otherwise the
     * function will crash.
     */
    fun getSongArtist(): String = getMetadata().artist

    /**
     * @return playback state which was set in MediaPlaybackService
     */
    private fun getPlaybackState(): PlaybackStateCompat {
        return MediaControllerCompat.getMediaController(this).playbackState
    }

    /**
     * @return metadata which was set in the MediaPlaybackService
     */
    private fun getMetadata(): MediaMetadataCompat {
        return MediaControllerCompat.getMediaController(this).metadata
    }

    fun sendCustomAction(action: String?, bundle: Bundle?) {
        MediaControllerCompat.getMediaController(this).transportControls.sendCustomAction(
            action,
            bundle
        )
    }

    open fun onConnected() {}
    open fun onDisconnected() {}

    private inner class ConnectionCallback : MediaBrowserCompat.ConnectionCallback() {
        override fun onConnected() {
            val token: MediaSessionCompat.Token = mMediaBrowser.sessionToken

            // Create a MediaControllerCompat
            val mediaController = MediaControllerCompat(this@MediaControllerActivity, token)

            // Save the controller
            MediaControllerCompat.setMediaController(this@MediaControllerActivity, mediaController)

            // Finish building the UI
            this@MediaControllerActivity.onConnected()

            // Register a Callback to stay in sync
            MediaControllerCompat.getMediaController(this@MediaControllerActivity)
                .registerCallback(mControllerCallback)
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

        }

        override fun onSessionDestroyed() {
            Log.d("Mucify", "MediaControllerActivity session destroyed")
            mMediaBrowser.disconnect()
            // maybe schedule a reconnection using a new MediaBrowser instance
            this@MediaControllerActivity.onDisconnected()
        }
    }
}