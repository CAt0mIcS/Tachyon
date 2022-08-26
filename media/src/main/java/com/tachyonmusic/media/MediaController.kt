package com.tachyonmusic.media

import android.app.Activity
import android.content.ComponentName
import android.os.Bundle
import android.os.Parcelable
import android.support.v4.media.MediaBrowserCompat
import android.support.v4.media.MediaMetadataCompat
import android.support.v4.media.session.MediaControllerCompat
import android.support.v4.media.session.MediaSessionCompat
import android.support.v4.media.session.PlaybackStateCompat
import android.util.Log
import com.tachyonmusic.core.constants.MediaAction
import com.tachyonmusic.core.constants.MetadataKeys
import com.tachyonmusic.media.device.*
import com.tachyonmusic.media.ext.*
import com.tachyonmusic.core.domain.model.Loop
import com.tachyonmusic.core.domain.model.Playback
import com.tachyonmusic.core.domain.model.Playlist
import com.tachyonmusic.core.domain.model.Song
import com.tachyonmusic.media.service.MediaPlaybackService
import com.tachyonmusic.util.launch
import kotlinx.coroutines.*


class MediaController {
    companion object {
        const val TAG = "MediaController"
    }

    lateinit var browser: MediaBrowserCompat
        private set

    private val controllerCallback: MediaControllerCallback = MediaControllerCallback()
    private var eventListener: IEventListener? = null

    private val subscribedIds = mutableMapOf<String, (List<com.tachyonmusic.core.domain.model.Playback>) -> Unit>()

    // TODO: Why is [playback] not updating when the controller is connected
    private var playbackUpdateDone = Job()
    private var browserConnected = Job()

    private var activity: Activity? = null

    fun create(activity: Activity) {
        this.activity = activity

        browser = MediaBrowserCompat(
            activity,
            ComponentName(activity, MediaPlaybackService::class.java),
            ConnectionCallback(),
            null
        )

        Log.d(TAG, "MediaBrowserController created")
    }

    /**
     * Connects to the [MediaBrowserService]. Should be called from Activity.onStart
     */
    fun connect(activity: Activity) {
        this.activity = activity
        browser.connect()
        Log.d(TAG, "Started connecting to MediaPlaybackService")
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
        Log.d(TAG, "Started disconnecting from MediaPlaybackService")
    }

    /**
     * Registers [listener] to listen to all events sent by the [MediaBrowserServiceCompat]
     */
    fun registerEventListener(listener: IEventListener?) {
        eventListener = listener
    }

    /**
     * Suspends until the [browser] is connected to the [MediaBrowserServiceCompat]
     */
    suspend fun awaitConnection() =
        withContext(Dispatchers.IO) {
            browserConnected.join()
            browserConnected = Job()
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
    var playback: com.tachyonmusic.core.domain.model.Playback?
        get() = _playback
        set(value) {
            sendCustomAction(com.tachyonmusic.core.constants.MediaAction.SetPlaybackEvent, Bundle().apply {
                putParcelable(com.tachyonmusic.core.constants.MediaAction.Playback, value)
            })
        }

    private var _playback: com.tachyonmusic.core.domain.model.Playback? = null
        set(value) {
            field = value

            // Event to [MediaPlaybackService] to update start/end time
            field?.onStartTimeChanged = { startTime ->
                sendCustomAction(
                    com.tachyonmusic.core.constants.MediaAction.SetStartTimeEvent,
                    Bundle().apply { putLong(com.tachyonmusic.core.constants.MediaAction.StartTime, startTime) })
                field?.startTime = startTime
            }
            field?.onEndTimeChanged = { endTime ->
                sendCustomAction(
                    com.tachyonmusic.core.constants.MediaAction.SetEndTimeEvent,
                    Bundle().apply { putLong(com.tachyonmusic.core.constants.MediaAction.EndTime, endTime) })
                field?.endTime = endTime
            }
            if (field is com.tachyonmusic.core.domain.model.Playlist?)
                (field as com.tachyonmusic.core.domain.model.Playlist?)?.onCurrentPlaylistIndexChanged = { i ->
                    sendCustomAction(
                        com.tachyonmusic.core.constants.MediaAction.CurrentPlaylistIndexChangedEvent,
                        Bundle().apply { putInt(com.tachyonmusic.core.constants.MediaAction.CurrentPlaylistIndex, i) }
                    )
                    (field as com.tachyonmusic.core.domain.model.Playlist?)?.currentPlaylistIndex = i
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
        sendCustomAction(com.tachyonmusic.core.constants.MediaAction.RequestMediaSourceReloadEvent, null)
    }

    /**
     * Updates the [MediaSource] with [loops]. Overwrites previous loops
     */
    fun sendLoops(loops: ArrayList<com.tachyonmusic.core.domain.model.Loop>) {
        sendCustomAction(com.tachyonmusic.core.constants.MediaAction.SendLoopsEvent, Bundle().apply {
            putParcelableArrayList(
                com.tachyonmusic.core.constants.MediaAction.Loops,
                loops as ArrayList<out Parcelable>
            )
        })
    }

    /**
     * Updates the [MediaSource] with [playlists]. Overwrites previous playlists
     */
    fun sendPlaylists(playlists: ArrayList<com.tachyonmusic.core.domain.model.Playlist>) {
        sendCustomAction(com.tachyonmusic.core.constants.MediaAction.SendPlaylistsEvent, Bundle().apply {
            putParcelableArrayList(
                com.tachyonmusic.core.constants.MediaAction.Playlists,
                playlists as ArrayList<out Parcelable>
            )
        })
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
    fun subscribe(id: String, onChanged: (List<com.tachyonmusic.core.domain.model.Playback>) -> Unit) {
        Log.d(TAG, "Subscribing to $id")

        subscribedIds += Pair(id, onChanged)

        browser.subscribe(id, object : MediaBrowserCompat.SubscriptionCallback() {
            override fun onChildrenLoaded(
                parentId: String,
                children: MutableList<MediaBrowserCompat.MediaItem>
            ) {
                val mapped = children.map {
                    it.description.extras!!.getParcelable<com.tachyonmusic.core.domain.model.Playback>(
                        com.tachyonmusic.core.constants.MetadataKeys.Playback)!!
                }

                launch(Dispatchers.Main) {
                    onChanged(mapped)
                }

            }
        })
    }

    /**
     * Quick way to call [subscribe] with [BrowserTree.SONG_ROOT] and once the results are there call
     * [unsubscribe]. The previously subscribed callback will be restored once [action] is done
     */
    fun songs(action: (List<com.tachyonmusic.core.domain.model.Song>) -> Unit) {
        val previousOnChanged = subscribedIds[BrowserTree.SONG_ROOT]

        browser.subscribe(
            BrowserTree.SONG_ROOT,
            object : MediaBrowserCompat.SubscriptionCallback() {
                override fun onChildrenLoaded(
                    parentId: String,
                    children: MutableList<MediaBrowserCompat.MediaItem>
                ) {
                    val mapped =
                        children.map { it.description.extras!!.getParcelable<com.tachyonmusic.core.domain.model.Song>(
                            com.tachyonmusic.core.constants.MetadataKeys.Playback)!! }
                    launch(Dispatchers.Main) {
                        action(mapped)
                    }

                    if (previousOnChanged != null)
                        subscribe(BrowserTree.SONG_ROOT, previousOnChanged)
                }
            })
    }

    /**
     * Quick way to call [subscribe] with [BrowserTree.LOOP_ROOT] and once the results are there call
     * [unsubscribe]. The previously subscribed callback will be restored once [action] is done
     */
    fun loops(action: (List<com.tachyonmusic.core.domain.model.Loop>) -> Unit) {
        val previousOnChanged = subscribedIds[BrowserTree.LOOP_ROOT]

        browser.subscribe(
            BrowserTree.LOOP_ROOT,
            object : MediaBrowserCompat.SubscriptionCallback() {
                override fun onChildrenLoaded(
                    parentId: String,
                    children: MutableList<MediaBrowserCompat.MediaItem>
                ) {
                    val mapped =
                        children.map { it.description.extras!!.getParcelable<com.tachyonmusic.core.domain.model.Loop>(
                            com.tachyonmusic.core.constants.MetadataKeys.Playback)!! }
                    launch(Dispatchers.Main) {
                        action(mapped)
                    }

                    if (previousOnChanged != null)
                        subscribe(BrowserTree.SONG_ROOT, previousOnChanged)
                }
            })
    }

    /**
     * Quick way to call [subscribe] with [BrowserTree.PLAYLIST_ROOT] and once the results are there call
     * [unsubscribe]. The previously subscribed callback will be restored once [action] is done
     */
    fun playlists(action: (List<com.tachyonmusic.core.domain.model.Playlist>) -> Unit) {
        val previousOnChanged = subscribedIds[BrowserTree.PLAYLIST_ROOT]

        browser.subscribe(
            BrowserTree.PLAYLIST_ROOT,
            object : MediaBrowserCompat.SubscriptionCallback() {
                override fun onChildrenLoaded(
                    parentId: String,
                    children: MutableList<MediaBrowserCompat.MediaItem>
                ) {
                    val mapped =
                        children.map { it.description.extras!!.getParcelable<com.tachyonmusic.core.domain.model.Playlist>(
                            com.tachyonmusic.core.constants.MetadataKeys.Playback)!! }
                    launch(Dispatchers.Main) {
                        action(mapped)
                    }

                    if (previousOnChanged != null)
                        subscribe(BrowserTree.SONG_ROOT, previousOnChanged)
                }
            })
    }

    /**
     * Removes the callback which was previously subscribed to with [subscribe]
     */
    fun unsubscribe(vararg ids: String) = ids.forEach { id ->
        Log.d(TAG, "Unsubscribing from $id")
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

            browserConnected.complete()

            // Request [playback] to be updated
            sendCustomAction(com.tachyonmusic.core.constants.MediaAction.RequestPlaybackUpdateEvent)
            // TODO TODO TODO TODO
            launch(Dispatchers.Main) {
                playbackUpdateDone.join()
                playbackUpdateDone = Job()

                // Finish building the UI
                eventListener?.onConnected()
            }

            Log.d(TAG, "MediaBrowserController connection established")
        }

        override fun onConnectionSuspended() {
            // The Service has crashed. Disable transport controls until it automatically reconnects
            Log.d(TAG, "MediaControllerActivity connection suspended")
        }

        override fun onConnectionFailed() {
            // The Service has refused our connection
            Log.d(TAG, "MediaControllerActivity connection failed")
        }
    }

    private inner class MediaControllerCallback : MediaControllerCompat.Callback() {
        override fun onSessionEvent(event: String, extras: Bundle) {
            when (event) {
                com.tachyonmusic.core.constants.MediaAction.SetPlaybackEvent -> {
                    _playback = extras.getParcelable(com.tachyonmusic.core.constants.MediaAction.Playback)
                    playbackUpdateDone.complete()

                    launch(Dispatchers.Main) {
                        eventListener?.onSetPlayback()
                    }
                }
                com.tachyonmusic.core.constants.MediaAction.OnPlaybackStateChangedEvent -> {
                    launch(Dispatchers.Main) {
                        eventListener?.onPlaybackStateChanged(
                            extras.getBoolean(com.tachyonmusic.core.constants.MediaAction.IsPlaying)
                        )
                    }
                }
            }
        }

        override fun onSessionDestroyed() {
            Log.d(TAG, "MediaControllerActivity session destroyed")
            browser.disconnect()
            // maybe schedule a reconnection using a new MediaBrowser instance
            eventListener?.onDisconnected()
        }
    }

    interface IEventListener {
        fun onConnected()
        fun onDisconnected()
        fun onSetPlayback()
        fun onPlaybackStateChanged(isPlaying: Boolean)
    }

    class EventListener : IEventListener {
        override fun onConnected() {}
        override fun onDisconnected() {}
        override fun onSetPlayback() {}
        override fun onPlaybackStateChanged(isPlaying: Boolean) {}
    }
}