package com.daton.media

import android.app.Activity
import android.content.ComponentName
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.os.Parcelable
import android.support.v4.media.MediaBrowserCompat
import android.support.v4.media.MediaMetadataCompat
import android.support.v4.media.session.MediaControllerCompat
import android.support.v4.media.session.MediaSessionCompat
import android.support.v4.media.session.PlaybackStateCompat
import android.util.Log
import com.daton.media.data.MediaAction
import com.daton.media.data.MetadataKeys
import com.daton.media.device.*
import com.daton.media.ext.*
import com.daton.media.service.MediaPlaybackService
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.concurrent.CountDownLatch


class MediaController {
    companion object {
        const val TAG = "MediaController"
    }

    lateinit var browser: MediaBrowserCompat
        private set

    private val controllerCallback: MediaControllerCallback = MediaControllerCallback()

    private val subscribedIds = mutableMapOf<String, (List<Playback>) -> Unit>()

    // TODO: Why is [playback] not updating when the controller is connected
    private var firstPlaybackUpdateDone: CountDownLatch? = null

    var onConnected: (() -> Unit)? = null
    var onDisconnected: (() -> Unit)? = null

    var onPlaybackChanged: (() -> Unit)? = null
    var onPlaybackStateChanged: ((Boolean) -> Unit)? = null

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
     * Disconnects from the [MediaBrowserService] and unregisters any callbacks including callbacks
     * registered in [subscribe]. Should be called from Activity.onStop
     */
    fun disconnect() {
        if (activity != null && MediaControllerCompat.getMediaController(activity!!) != null) {
            MediaControllerCompat.getMediaController(activity!!)
                .unregisterCallback(controllerCallback)
        }
        val keys = mutableListOf<String>().apply { addAll(subscribedIds.keys) }
        if (keys.size > 0)
            for (i in keys.size - 1 downTo 0)
                unsubscribe(keys[i])

        browser.disconnect()
        activity = null
        Log.d("Mucify", "Started disconnecting from MediaPlaybackService")
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
    var playback: Playback?
        get() = _playback
        set(value) {
            val bundle = Bundle()
            bundle.putParcelable(MediaAction.Playback, value)
            sendCustomAction(MediaAction.SetPlaybackEvent, bundle)
        }

    private var _playback: Playback? = null
        set(value) {
            field = value

            // Event to [MediaPlaybackService] to update start/end time
            field?.onStartTimeChanged = { startTime ->
                sendCustomAction(
                    MediaAction.SetStartTimeEvent,
                    Bundle().apply { putLong(MediaAction.StartTime, startTime) })
                field?.startTime = startTime
            }
            field?.onEndTimeChanged = { endTime ->
                sendCustomAction(
                    MediaAction.SetEndTimeEvent,
                    Bundle().apply { putLong(MediaAction.EndTime, endTime) })
                field?.endTime = endTime
            }
            if (field is Playlist?)
                (field as Playlist?)?.onCurrentPlaylistIndexChanged = { i ->
                    sendCustomAction(
                        MediaAction.CurrentPlaylistIndexChangedEvent,
                        Bundle().apply { putInt(MediaAction.CurrentPlaylistIndex, i) }
                    )
                    (field as Playlist?)?.currentPlaylistIndex = i
                }
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
     * Current position of the currently playing song. Crashes if the Playback hasn't been started yet.
     */
    var currentPosition: Long
        get() = playbackState.position
        set(value) {
            seekTo(value)
        }

    /**
     * Requests that the [MediaSource] is (re)loaded. Requires storage permission
     */
    fun loadMediaSource() {
        sendCustomAction(MediaAction.RequestMediaSourceReloadEvent, null)
    }

    /**
     * Updates the [MediaSource] with [loops]. Overwrites previous loops
     */
    fun sendLoops(loops: ArrayList<Loop>) {
        val bundle = Bundle()
        bundle.putParcelableArrayList(
            MediaAction.Loops,
            loops as ArrayList<out Parcelable>
        )

        sendCustomAction(MediaAction.SendLoopsEvent, bundle)
    }

    /**
     * Updates the [MediaSource] with [playlists]. Overwrites previous playlists
     */
    fun sendPlaylists(playlists: ArrayList<Playlist>) {
        val bundle = Bundle()
        bundle.putParcelableArrayList(
            MediaAction.Playlists,
            playlists as ArrayList<out Parcelable>
        )

        sendCustomAction(MediaAction.SendPlaylistsEvent, bundle)
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

    fun sendCustomAction(action: String?, bundle: Bundle? = null) {
        MediaControllerCompat.getMediaController(activity!!).transportControls.sendCustomAction(
            action,
            bundle
        )
    }

    /**
     * Calls [onChanged] when the media source changes on the specified [id], e.g.
     * * subscribe(BrowserTree.PLAYLIST_ROOT) { ... } will be called whenever a playlist changes or
     *   is added/removed
     * * subscribe(BrowserTree.ExampleAlbum) { ... } will be called whenever the songs in the specified
     *   album change or songs are added/removed
     */
    fun subscribe(id: String, onChanged: (List<Playback>) -> Unit) {
        Log.d(TAG, "Subscribing to $id")

        subscribedIds += Pair(id, onChanged)

        browser.subscribe(id, object : MediaBrowserCompat.SubscriptionCallback() {
            override fun onChildrenLoaded(
                parentId: String,
                children: MutableList<MediaBrowserCompat.MediaItem>
            ) {
                Log.d(TAG, "Media source changed")
                onChanged(children.map {
                    it.description.extras!!.getParcelable(MetadataKeys.Playback)!!
                })
            }
        })
    }

    /**
     * Quick way to call [subscribe] with [BrowserTree.SONG_ROOT] and once the results are there call
     * [unsubscribe]. The previously subscribed callback will be restored once [action] is done
     */
    fun songs(action: (List<Song>) -> Unit) {
        val previousOnChanged = subscribedIds[BrowserTree.SONG_ROOT]

        browser.subscribe(
            BrowserTree.SONG_ROOT,
            object : MediaBrowserCompat.SubscriptionCallback() {
                override fun onChildrenLoaded(
                    parentId: String,
                    children: MutableList<MediaBrowserCompat.MediaItem>
                ) {
                    action(children.map { it.description.extras!!.getParcelable(MetadataKeys.Playback)!! })
                    if (previousOnChanged != null)
                        subscribe(BrowserTree.SONG_ROOT, previousOnChanged)
                }
            })
    }

    /**
     * Quick way to call [subscribe] with [BrowserTree.LOOP_ROOT] and once the results are there call
     * [unsubscribe]. The previously subscribed callback will be restored once [action] is done
     */
    fun loops(action: (List<Loop>) -> Unit) {
        val previousOnChanged = subscribedIds[BrowserTree.LOOP_ROOT]

        browser.subscribe(
            BrowserTree.LOOP_ROOT,
            object : MediaBrowserCompat.SubscriptionCallback() {
                override fun onChildrenLoaded(
                    parentId: String,
                    children: MutableList<MediaBrowserCompat.MediaItem>
                ) {
                    action(children.map { it.description.extras!!.getParcelable(MetadataKeys.Playback)!! })
                    if (previousOnChanged != null)
                        subscribe(BrowserTree.SONG_ROOT, previousOnChanged)
                }
            })
    }

    /**
     * Quick way to call [subscribe] with [BrowserTree.PLAYLIST_ROOT] and once the results are there call
     * [unsubscribe]. The previously subscribed callback will be restored once [action] is done
     */
    fun playlists(action: (List<Playlist>) -> Unit) {
        val previousOnChanged = subscribedIds[BrowserTree.PLAYLIST_ROOT]

        browser.subscribe(
            BrowserTree.PLAYLIST_ROOT,
            object : MediaBrowserCompat.SubscriptionCallback() {
                override fun onChildrenLoaded(
                    parentId: String,
                    children: MutableList<MediaBrowserCompat.MediaItem>
                ) {
                    action(children.map { it.description.extras!!.getParcelable(MetadataKeys.Playback)!! })
                    if (previousOnChanged != null)
                        subscribe(BrowserTree.SONG_ROOT, previousOnChanged)
                }
            })
    }

    /**
     * Removes the callback which was previously subscribed to with [subscribe]
     */
    fun unsubscribe(id: String) {
        Log.d(TAG, "Unsubscribing id $id")

        browser.unsubscribe(id)
        subscribedIds -= id
    }

    private inner class ConnectionCallback : MediaBrowserCompat.ConnectionCallback() {
        override fun onConnected() {
            val token: MediaSessionCompat.Token = browser.sessionToken

            // Create a MediaControllerCompat
            val mediaController = MediaControllerCompat(activity!!, token)

            // Register a Callback to stay in sync
            mediaController.registerCallback(controllerCallback)

            // Save the controller
            MediaControllerCompat.setMediaController(activity!!, mediaController)

            firstPlaybackUpdateDone = CountDownLatch(1)
            // Request [playback] to be updated
            sendCustomAction(MediaAction.RequestPlaybackUpdateEvent)
            // TODO TODO TODO TODO
            CoroutineScope(Dispatchers.IO).launch {
                firstPlaybackUpdateDone!!.await()
                firstPlaybackUpdateDone = null

                // Finish building the UI
                Handler(Looper.getMainLooper()).post {
                    onConnected?.invoke()
                }
            }

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
                MediaAction.SetPlaybackEvent -> {
                    _playback = extras.getParcelable(MediaAction.Playback)
                    firstPlaybackUpdateDone?.countDown()

                    Handler(Looper.getMainLooper()).post {
                        onPlaybackChanged?.invoke()
                    }
                }
                MediaAction.OnPlaybackStateChangedEvent -> {
                    Handler(Looper.getMainLooper()).post {
                        onPlaybackStateChanged?.invoke(
                            extras.getBoolean(
                                MediaAction.IsPlaying
                            )
                        )
                    }
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