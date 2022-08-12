package com.daton.media.service

import android.app.Notification
import android.app.PendingIntent
import android.content.Intent
import android.net.Uri
import android.os.*
import android.support.v4.media.MediaBrowserCompat.MediaItem
import android.support.v4.media.MediaDescriptionCompat
import android.support.v4.media.MediaMetadataCompat
import android.support.v4.media.session.MediaSessionCompat
import android.support.v4.media.session.PlaybackStateCompat
import android.util.Log
import android.view.KeyEvent
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.media.MediaBrowserServiceCompat
import com.daton.media.CustomPlayer
import com.daton.media.data.MediaAction
import com.daton.media.data.MediaId
import com.daton.media.device.BrowserTree
import com.daton.media.device.Loop
import com.daton.media.device.MediaSource
import com.daton.media.device.Playlist
import com.daton.media.ext.*
import com.google.android.exoplayer2.*
import com.google.android.exoplayer2.audio.AudioAttributes
import com.google.android.exoplayer2.ext.mediasession.MediaSessionConnector
import com.google.android.exoplayer2.ext.mediasession.TimelineQueueNavigator
import com.google.android.exoplayer2.ui.PlayerNotificationManager
import com.google.android.exoplayer2.util.EventLogger
import com.google.android.exoplayer2.util.Util.constrainValue
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch


class MediaPlaybackService : MediaBrowserServiceCompat() {

    companion object {
        const val TAG = "MediaPlaybackService"
    }

    private val serviceJob = SupervisorJob()
    private val serviceScope = CoroutineScope(Dispatchers.Main + serviceJob)

    // The current player will either be an ExoPlayer (for local playback) or a CastPlayer (for
    // remote playback through a Cast device).
    private lateinit var currentPlayer: CustomPlayer
    private var playerMessage: PlayerMessage? = null

    // Doesn't start loading files yet as we need to wait on storage permission to be granted
    private val mediaSource = MediaSource()

    /**
     * When media id is set to a playlist without specifying any underlying media id to play we need to
     * update metadata with the base media id of the playlist
     */
    private var basePlaylistMediaId: MediaId? = null
        set(value) {
            field = value
            if (field != null)
                mediaSessionConnector.invalidateMediaSessionMetadata()
        }

    private lateinit var mediaSession: MediaSessionCompat
    private lateinit var mediaSessionConnector: MediaSessionConnector

    private lateinit var notificationManager: MucifyNotificationManager

    private var currentMediaItems = listOf<MediaMetadataCompat>()
    private var currentPlaybackIndex: Int = 0

    /**
     * Controls if songs and loops should be combined into one playlist when playing a song/loop
     */
    private var combinePlaybackTypes = false

    private var isForegroundService = false

    /**
     * Configure ExoPlayer to handle audio focus for us.
     * See [Player.AudioComponent.setAudioAttributes] for details.
     */
    private val exoPlayer: CustomPlayer by lazy {
        CustomPlayer(ExoPlayer.Builder(this).run {
            setAudioAttributes(
                AudioAttributes.Builder()
                    .setContentType(C.AUDIO_CONTENT_TYPE_MUSIC)
                    .setUsage(C.USAGE_MEDIA)
                    .build(), true
            )
            setHandleAudioBecomingNoisy(true)
            return@run build()
        }.apply {
            addListener(PlayerEventListener())

            // TODO: Debug only
            addAnalyticsListener(EventLogger(null))
            repeatMode = Player.REPEAT_MODE_ONE
        })
    }

    private lateinit var browserTree: BrowserTree


    override fun onCreate() {
        super.onCreate()

        Log.d(TAG, "Creating MediaPlaybackService")

        mediaSource.onChangedListener = { parentId, mediaId ->
            browserTree = BrowserTree(mediaSource)
            notifyChildrenChanged(BrowserTree.ROOT)
            notifyChildrenChanged(parentId)

            if (mediaId != null) {
                notifyChildrenChanged(mediaId)
            }
        }

        // Build a PendingIntent that can be used to launch the UI.
        val sessionActivityPendingIntent =
            packageManager?.getLaunchIntentForPackage(packageName)?.let { sessionIntent ->
                PendingIntent.getActivity(
                    this,
                    0,
                    sessionIntent,
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) PendingIntent.FLAG_IMMUTABLE else 0
                )
            }

        // Create a new MediaSession.
        mediaSession = MediaSessionCompat(this, "com.daton.mucify")
            .apply {
                setSessionActivity(sessionActivityPendingIntent)
                setFlags(
                    MediaSessionCompat.FLAG_HANDLES_MEDIA_BUTTONS or
                            MediaSessionCompat.FLAG_HANDLES_TRANSPORT_CONTROLS
                )
                isActive = true
            }

        /**
         * In order for [MediaBrowserCompat.ConnectionCallback.onConnected] to be called,
         * a [MediaSessionCompat.Token] needs to be set on the [MediaBrowserServiceCompat].
         *
         * It is possible to wait to set the session token, if required for a specific use-case.
         * However, the token *must* be set by the time [MediaBrowserServiceCompat.onGetRoot]
         * returns, or the connection will fail silently. (The system will not even call
         * [MediaBrowserCompat.ConnectionCallback.onConnectionFailed].)
         */
        sessionToken = mediaSession.sessionToken

        /**
         * The notification manager will use our player and media session to decide when to post
         * notifications. When notifications are posted or removed our listener will be called, this
         * allows us to promote the service to foreground (required so that we're not killed if
         * the main UI is not visible).
         */
        notificationManager = MucifyNotificationManager(
            this,
            mediaSession.sessionToken,
            PlayerNotificationListener()
        )

        // ExoPlayer will manage the MediaSession for us.
        mediaSessionConnector = MediaSessionConnector(mediaSession)
        mediaSessionConnector.apply {
            val preparer = PlaybackPreparer()

            setPlaybackPreparer(preparer)
            setQueueNavigator(QueueNavigator(mediaSession))

            /**
             * When updating e.g. the endTime in [PlaybackPreparer.onSetEndTime] the metadata of the [MediaController]
             * needs to be updated with the new one
             * TODO: We now need to maintain [MediaMetadata.toMediaMetadataCompat] and update it when [MediaMetadataCompat] changes
             * TODO: What to return if [player.currentMediaItem] == null
             */
            setMediaMetadataProvider { player ->
                /**
                 * [player.currentMediaItem] is null if media id is a playlist without any underlying
                 * playback. [basePlaylistMediaId] will be set to the playlist media id if this is the
                 * case and the metadata can be updated with [basePlaylistMediaId]
                 */
                if (basePlaylistMediaId != null) {
                    MediaMetadataCompat.Builder().apply {
                        mediaId = basePlaylistMediaId!!
                    }.build()
                } else if (player.currentTimeline.isEmpty)
                    MediaMetadataCompat.Builder().build()
                else if (player.currentMediaItem != null)
                    player.mediaMetadata.toMediaMetadataCompat(player.currentMediaItem!!.mediaId.toMediaId())
                else
                // TODO: Might not work
                    MediaMetadataCompat.Builder().build()
            }

            registerCustomCommandReceiver(preparer)
            setCustomActionProviders(*preparer.getCustomActions())

            setMediaButtonEventHandler(MediaButtonEventHandler())

            // If not next playback is present in queue we still want to dispatch ACTION_SKIP_TO_NEXT
            // to loop back to the beginning of the playlist (Logic in CustomPlayer)
            setDispatchUnsupportedActionsEnabled(true)
        }

        switchToPlayer(null, exoPlayer)
        notificationManager.showNotificationForPlayer(currentPlayer)
    }


    override fun onGetRoot(
        clientPackageName: String,
        clientUid: Int,
        rootHints: Bundle?
    ): BrowserRoot {
        /**
         * We'll for now allow everyone to connect
         */
        Log.d(TAG, "MediaPlaybackService.onGetRoot")
        return BrowserRoot(BrowserTree.ROOT, null)
    }

    override fun onLoadChildren(
        parentId: String,
        result: Result<List<MediaItem>>
    ) {
        Log.d(TAG, "MediaPlaybackService.onLoadChildren with parentId: $parentId")
        val resultsSent = mediaSource.whenReady { successfullyInitialized ->
            if (successfullyInitialized) {
                val children = browserTree[parentId]
                try {
                    result.sendResult(children)
                } catch (_: IllegalStateException) {
                    Log.e(
                        TAG,
                        "java.lang.IllegalStateException: sendResult() called when either sendResult() or sendError() had already been called for: /"
                    )
                }

            } else {
                TODO("Handle error that MediaSource wasn't initialized properly")
            }
        }

        // If the results are not ready, the service must "detach" the results before
        // the method returns. After the source is ready, the lambda above will run,
        // and the caller will be notified that the results are ready.
        if (!resultsSent) {
            Log.d(TAG, "MediaPlaybackService.onLoadChildren: results not sent")
            result.detach()
        }
    }

    /**
     * TODO: [MediaBrowserServiceCompat.onSearch] to allow e.g. Google Assistant to search and play music
     */

    /**
     * Called when swiping activity away from recents. We might want to pause the audio here
     */
    override fun onTaskRemoved(rootIntent: Intent) {
        super.onTaskRemoved(rootIntent)

        // TODO: Notification not swipeable on some devices if foreground service is still attached to it
        // TODO: Figure out the exact build numbers where this is the case and only call
        // TODO: hideNotification if necessary
        if (!currentPlayer.isPlaying && requiresOldNotificationHandling())
            notificationManager.hideNotification()
    }

    private fun switchToPlayer(previousPlayer: CustomPlayer?, newPlayer: CustomPlayer) {
        if (previousPlayer == newPlayer) {
            return
        }
        currentPlayer = newPlayer
//        if (previousPlayer != null) {
//            val playbackState = previousPlayer.playbackState
//            if (currentPlaylistItems.isEmpty()) {
//                // We are joining a playback session. Loading the session from the new player is
//                // not supported, so we stop playback.
//                currentPlayer.clearMediaItems()
//                currentPlayer.stop()
//            } else if (playbackState != Player.STATE_IDLE && playbackState != Player.STATE_ENDED) {
//                preparePlaylist(
//                    metadataList = currentPlaylistItems,
//                    itemToPlay = currentPlaylistItems[currentMediaItemIndex],
//                    playWhenReady = previousPlayer.playWhenReady,
//                    playbackStartPositionMs = previousPlayer.currentPosition
//                )
//            }
//        }
        mediaSessionConnector.setPlayer(newPlayer)
        previousPlayer?.run {
            stop()
            clearMediaItems()
        }
    }

    fun preparePlayer(
        items: List<MediaMetadataCompat>,
        initialWindowIndex: Int
    ) {
        currentMediaItems = items
        currentPlayer.setMediaItems(items.map { it.toExoMediaItem() })

        currentPlayer.seekTo(initialWindowIndex, C.TIME_UNSET)
    }

    private inner class QueueNavigator(
        mediaSession: MediaSessionCompat
    ) : TimelineQueueNavigator(mediaSession) {
        override fun getMediaDescription(player: Player, windowIndex: Int): MediaDescriptionCompat {
            if (windowIndex < currentMediaItems.size) {
                return currentMediaItems[windowIndex].toMediaDescriptionCompat()
            }
            return MediaDescriptionCompat.Builder().build()
        }
    }

    private inner class PlaybackPreparer : MediaSessionConnectorPlaybackPreparer() {
        override fun onCommand(
            player: Player,
            command: String,
            extras: Bundle?,
            cb: ResultReceiver?
        ): Boolean {
            Log.d(TAG, "PlaybackPreparer.onCommand with command $command")
            return false
        }

        override fun getSupportedPrepareActions(): Long =
            PlaybackStateCompat.ACTION_PREPARE_FROM_MEDIA_ID or
                    PlaybackStateCompat.ACTION_PLAY_FROM_MEDIA_ID or
                    PlaybackStateCompat.ACTION_PREPARE_FROM_SEARCH or
                    PlaybackStateCompat.ACTION_PLAY_FROM_SEARCH

        override fun onPrepare(playWhenReady: Boolean) {
            Log.d(TAG, "PlaybackPreparer.onPrepare with playWhenReady = $playWhenReady")

            // Handles the case where [MediaBrowserController.setMediaId] is called just before
            // [MediaBrowserController.play] and the media source hasn't been initialized yet in [onSetMediaId]
            mediaSource.whenReady {
                currentPlayer.playWhenReady
                currentPlayer.prepare()

                if (currentPlayer.currentMediaItem!!.isLoop) {
                    currentPlayer.seekTo(currentPlayer.currentMediaItem!!.mediaMetadata.startTime)
                }
            }
        }

        override fun onPrepareFromMediaId(
            mediaId: String,
            playWhenReady: Boolean,
            extras: Bundle?
        ) {
            Log.d(
                TAG,
                "PlaybackPreparer.onPrepareFromMediaId with mediaId = $mediaId and playWhenReady = $playWhenReady"
            )

            onSetMediaId(mediaId.toMediaId())
            onPrepare(playWhenReady)
        }

        override fun onPrepareFromSearch(query: String, playWhenReady: Boolean, extras: Bundle?) {
            Log.d(
                TAG,
                "PlaybackPreparer.onPrepareFromSearch with query = $query and playWhenReady = $playWhenReady"
            )
        }

        override fun onPrepareFromUri(uri: Uri, playWhenReady: Boolean, extras: Bundle?) {
            Log.d(
                TAG,
                "PlaybackPreparer.onPrepareFromUri with uri = $uri and playWhenReady = $playWhenReady"
            )
        }


        override fun onSetMediaId(mediaId: MediaId) {
            mediaSource.whenReady {
                // Find either the underlying playback or the top-level playback to play
                val initialWindowIndex: Int = if (mediaId.isSong)
                    mediaSource.indexOfSong { song ->
                        song.mediaId == mediaId
                    }
                else if (mediaId.isLoop)
                    mediaSource.indexOfLoop { loop ->
                        loop.mediaId == mediaId
                    }
                else
                    mediaSource.indexOfPlaylist { playlist ->
                        // Base playback is the same and playlist contains song/loop
                        mediaId.source == playlist.mediaId.source && (mediaId.underlyingMediaId == null ||
                                playlist.playbacks.contains(
                                    mediaId.underlyingMediaId
                                ))
                    }

                if (initialWindowIndex == -1)
                    TODO("Invalid media id $mediaId")
                else {
                    /**
                     * All songs/loops will be set as the internal playlist when playing song/loop
                     * When playing an actual playlist, all songs/loops in the playlist will be set as
                     * the internal playlist
                     */
                    when {
                        mediaId.isSong -> {
                            currentPlayer.stop()
                            preparePlayer(
                                if (combinePlaybackTypes)
                                    mediaSource.songs.map { it.toMediaMetadata() } +
                                            mediaSource.loops.map { it.toMediaMetadata(mediaSource) }
                                else
                                    mediaSource.songs.map { it.toMediaMetadata() },
                                initialWindowIndex
                            )
                            currentPlayer.repeatMode = Player.REPEAT_MODE_ONE
                        }
                        mediaId.isLoop -> {
                            currentPlayer.stop()
                            preparePlayer(
                                if (combinePlaybackTypes)
                                    mediaSource.loops.map { it.toMediaMetadata(mediaSource) } +
                                            mediaSource.songs.map { it.toMediaMetadata() }
                                else
                                    mediaSource.loops.map { it.toMediaMetadata(mediaSource) },
                                initialWindowIndex
                            )
                            currentPlayer.repeatMode = Player.REPEAT_MODE_ONE
                        }
                        mediaId.isPlaylist -> {
                            val playlist: Playlist = mediaSource.playlists[initialWindowIndex]
                            // Request for a specific song in playlist
                            if (mediaId.underlyingMediaId != null) {
                                currentPlayer.stop()
                                basePlaylistMediaId = null

                                val indexToPlay: Int =
                                    playlist.playbacks.indexOf(mediaId.underlyingMediaId)

                                preparePlayer(
                                    playlist.toMediaMetadataList(mediaSource),
                                    indexToPlay
                                )

                            } else {
                                basePlaylistMediaId = mediaId
                            }
                            currentPlayer.repeatMode = Player.REPEAT_MODE_ALL
                        }
                    }
                }
            }
        }

        override fun onSetStartTime(startTime: Long) {
            val endTime = currentPlayer.mediaMetadata.endTime

            // Start time and end time are back to default: Delete message
            if (startTime == 0L && endTime == currentPlayer.duration) {
                playerMessage?.cancel()
                return
            }

            if (currentPlayer.currentPosition < startTime || currentPlayer.currentPosition > endTime)
                currentPlayer.seekTo(startTime)
            postLoopMessage(startTime, endTime)

            currentPlayer.mediaMetadata.startTime = startTime
            mediaSessionConnector.invalidateMediaSessionMetadata()
        }

        override fun onSetEndTime(endTime: Long) {
            val startTime = currentPlayer.mediaMetadata.startTime

            // Start time and end time are back to default: Delete message
            if (startTime == 0L && endTime == currentPlayer.duration) {
                playerMessage?.cancel()
                return
            }

            if (currentPlayer.currentPosition < startTime || currentPlayer.currentPosition > endTime)
                currentPlayer.seekTo(startTime)
            postLoopMessage(startTime, endTime)

            currentPlayer.mediaMetadata.endTime = endTime
            mediaSessionConnector.invalidateMediaSessionMetadata()
        }

        override fun onStoragePermissionChanged(permissionGranted: Boolean) {
            // Only load device files if they haven't already been loaded.
            if (permissionGranted && mediaSource.state != MediaSource.STATE_INITIALIZED) {
                /**
                 * Starts asynchronously loading all playbacks in shared storage (only songs atm)
                 */
                Log.d(MediaPlaybackService.TAG, "Loading MediaSource")
                serviceScope.launch {
                    mediaSource.loadSharedDeviceFiles()
                }
            } else if (!permissionGranted)
                mediaSource.clearSongs()
        }

        override fun onCombinePlaybackTypesChanged(combine: Boolean) {
            combinePlaybackTypes = combine
            // TODO: Immediately reload player to have instant results (maybe because this is called quite often)
        }

        override fun onLoopsReceived(loops: List<Loop>) {
            mediaSource.whenReady { successfullyInitialized ->
                if (successfullyInitialized)
                    mediaSource.loops = loops as MutableList<Loop>
                else
                    TODO("Media Source not initialized properly")
            }
        }

        override fun onPlaylistsReceived(playlists: List<Playlist>) {
            mediaSource.whenReady { successfullyInitialized ->
                if (successfullyInitialized)
                    mediaSource.playlists = playlists as MutableList<Playlist>
                else
                    TODO("Media Source not initialized properly")
            }
        }
    }

    /**
     * Listen for notification events.
     */
    private inner class PlayerNotificationListener :
        PlayerNotificationManager.NotificationListener {
        override fun onNotificationPosted(
            notificationId: Int,
            notification: Notification,
            ongoing: Boolean
        ) {
            if (ongoing && !isForegroundService) {
                ContextCompat.startForegroundService(
                    applicationContext,
                    Intent(applicationContext, this@MediaPlaybackService.javaClass)
                )

                startForeground(notificationId, notification)
                isForegroundService = true
            }
        }

        override fun onNotificationCancelled(notificationId: Int, dismissedByUser: Boolean) {
            stopForeground(true)
            isForegroundService = false
            stopSelf()
        }
    }


    /**
     * Listen for events from ExoPlayer.
     */
    private inner class PlayerEventListener : Player.Listener {

        @Deprecated("Deprecated in Java")
        override fun onPlayerStateChanged(playWhenReady: Boolean, playbackState: Int) {
            when (playbackState) {
                Player.STATE_BUFFERING,
                Player.STATE_READY -> {
                    notificationManager.showNotificationForPlayer(currentPlayer)
                    if (playbackState == Player.STATE_READY) {
                        // TODO: Notification not swipeable on some devices if foreground service is still attached to it
                        // TODO: Figure out the exact build numbers where this is the case and limit it

                        //TODO: Maybe introduce a close button whenever [requiresOldNotificationHandling] is true
                        if (!playWhenReady && requiresOldNotificationHandling()) {
                            // If playback is paused we remove the foreground state which allows the
                            // notification to be dismissed.
                            stopForeground(false)
                            isForegroundService = false
                        }
                    }
                }
                else -> {
                    notificationManager.hideNotification()
                }
            }
        }

        override fun onIsPlayingChanged(isPlaying: Boolean) {
            val bundle = Bundle()
            bundle.putBoolean(MediaAction.IsPlaying, isPlaying)
            mediaSession.sendSessionEvent(MediaAction.OnPlaybackStateChanged, bundle)
        }

        override fun onMediaItemTransition(
            mediaItem: com.google.android.exoplayer2.MediaItem?,
            reason: Int
        ) {
            // TODO: Loops for [CastPlayer]

            if (mediaItem != null) {
                val mediaId = mediaItem.mediaId.toMediaId()

                if (mediaId.isLoop) {
                    // Single loop
                    currentPlayer.seekTo(mediaItem.mediaMetadata.startTime)
                    postLoopMessage(
                        mediaItem.mediaMetadata.startTime,
                        mediaItem.mediaMetadata.endTime
                    )

                } else if (mediaId.isPlaylist && mediaId.underlyingMediaId?.isLoop == true) {
                    // Loop in playlist
                    // TODO: Loops in playlist not seeking to beginning
                    val loop =
                        mediaSource.findLoop { it.mediaId == mediaId.underlyingMediaId }
                            ?: TODO("Loop ${mediaId.underlyingMediaId} not found")

                    currentPlayer.seekTo(loop.startTime)
                    postLoopMessageForPlaylist(loop.endTime)
                }
            }

            // Notify [MediaController] which notifies subscribed activities
            mediaSession.sendSessionEvent(MediaAction.MediaIdChanged, null)
        }

        override fun onEvents(player: Player, events: Player.Events) {
            Log.d(TAG, "PlayerEventListener.onEvent")

            if (events.contains(Player.EVENT_POSITION_DISCONTINUITY)
                || events.contains(Player.EVENT_MEDIA_ITEM_TRANSITION)
                || events.contains(Player.EVENT_PLAY_WHEN_READY_CHANGED)
            ) {
                currentPlaybackIndex = if (currentMediaItems.isNotEmpty()) {
                    constrainValue(
                        player.currentMediaItemIndex,
                        0,
                        currentMediaItems.size - 1
                    )
                } else 0
            }
        }

        override fun onPlayerError(error: PlaybackException) {
            var message = "Unknown error"
            Log.e(TAG, "Player error: " + error.errorCodeName + " (" + error.errorCode + ")")
            if (error.errorCode == PlaybackException.ERROR_CODE_IO_BAD_HTTP_STATUS
                || error.errorCode == PlaybackException.ERROR_CODE_IO_FILE_NOT_FOUND
            ) {
                message = "Media not found"
            }
            Toast.makeText(
                applicationContext,
                message,
                Toast.LENGTH_LONG
            ).show()
        }
    }

    private inner class MediaButtonEventHandler : MediaSessionConnector.MediaButtonEventHandler {
        override fun onMediaButtonEvent(player: Player, mediaButtonIntent: Intent): Boolean {
            if (Intent.ACTION_MEDIA_BUTTON == mediaButtonIntent.action) {
                val key = mediaButtonIntent.getParcelableExtra<KeyEvent>(Intent.EXTRA_KEY_EVENT)
                if (key != null && key.action == KeyEvent.ACTION_DOWN) {
                    // TODO: Debug if action might be handled twice
                    if (key.keyCode == KeyEvent.KEYCODE_MEDIA_NEXT) {
                        currentPlayer.seekToNext()
                        return true
                    } else if (key.keyCode == KeyEvent.KEYCODE_MEDIA_PREVIOUS) {
                        currentPlayer.seekToPrevious()
                        return true
                    }
                }
            }
            return false
        }
    }


    /**
     * Checks if we need to stop the foreground service when the playback is paused to make
     * the notification swipeable
     */
    private fun requiresOldNotificationHandling(): Boolean = Build.VERSION.SDK_INT <= 26

    private fun postLoopMessage(startTime: Long, endTime: Long) {
        // Cancel any previous messages
        playerMessage?.cancel()

        playerMessage = currentPlayer.createMessage { _, payload ->
            currentPlayer.seekTo(payload as Long)
        }.apply {
            looper = Looper.getMainLooper()
            deleteAfterDelivery = false
            payload = startTime
            setPosition(endTime)
            send()
        }
    }

    private fun postLoopMessageForPlaylist(endTime: Long) {
        // Cancel any previous messages
        playerMessage?.cancel()

        playerMessage = currentPlayer.createMessage { _, _ ->
            currentPlayer.seekToNext()
        }.apply {
            looper = Looper.getMainLooper()
            deleteAfterDelivery = true
            setPosition(endTime)
            send()
        }
    }
}
