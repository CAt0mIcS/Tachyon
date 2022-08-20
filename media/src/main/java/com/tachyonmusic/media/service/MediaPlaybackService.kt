package com.tachyonmusic.media.service

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
import com.tachyonmusic.media.CustomPlayer
import com.tachyonmusic.media.data.MediaAction
import com.tachyonmusic.media.data.MediaId
import com.tachyonmusic.media.data.MetadataKeys
import com.tachyonmusic.media.device.*
import com.tachyonmusic.media.ext.*
import com.tachyonmusic.media.playback.Loop
import com.tachyonmusic.media.playback.Playback
import com.tachyonmusic.media.playback.Playlist
import com.tachyonmusic.media.playback.Song
import com.google.android.exoplayer2.*
import com.google.android.exoplayer2.audio.AudioAttributes
import com.google.android.exoplayer2.ext.mediasession.MediaSessionConnector
import com.google.android.exoplayer2.ext.mediasession.TimelineQueueNavigator
import com.google.android.exoplayer2.ui.PlayerNotificationManager
import com.google.android.exoplayer2.util.EventLogger
import com.google.android.exoplayer2.util.Util.constrainValue
import kotlinx.coroutines.*


class MediaPlaybackService : MediaBrowserServiceCompat(),
    MediaSource.IEventListener by MediaSource.EventListener() {

    companion object {
        const val TAG = "MediaPlaybackService"

        // Doesn't start loading files yet as we need to wait on storage permission to be granted
        // TODO: Make not static:
        // TODO:   When going into another activity that doesn't require a connection to
        // TODO:   the [MediaBrowserController] and then switching back to the Activity that does,
        // TODO:   e.g. ActivityMain will recreate the [MediaPlaybackService] which will have been destroyed
        // TODO:   because there were no more browsers connected to it. This might be solvable using ViewBinding
        // TODO:   The problem is that the [mediaSource] will be recreated, too and would need to be reloaded
        // TODO:   unnecessarily. Thus we make the [mediaSource] static for now, because we're not using ViewBinding yet.
        // TODO:   Once we are we can store [ActivityMain.mediaLoaded] in the ViewBinding, meaning the MediaSource
        // TODO:   will be reloaded if the Activity completely restarts (Would still be an unnecessary reload of MediaSource)
        private val mediaSource = MediaSource()

        /**
         * Checks if we need to stop the foreground service when the playback is paused to make
         * the notification swipeable
         */
        private val REQUIRES_OLD_NOTIFICATION_HANDLING: Boolean = Build.VERSION.SDK_INT <= 26
    }

    // The current player will either be an ExoPlayer (for local playback) or a CastPlayer (for
    // remote playback through a Cast device).
    private lateinit var currentPlayer: CustomPlayer

    private lateinit var mediaSession: MediaSessionCompat
    private lateinit var mediaSessionConnector: MediaSessionConnector

    private lateinit var notificationManager: NotificationManager

    private var currentMediaItems = listOf<com.google.android.exoplayer2.MediaItem>()
    private var currentPlayback: Playback? = null
        set(value) {
            field = value
            mediaSession.sendSessionEvent(
                MediaAction.SetPlaybackEvent,
                Bundle().apply {
                    putParcelable(MediaAction.Playback, field)
                })
        }

    private var isPlayingPlaylist = false

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
            addAnalyticsListener(EventLogger())
            repeatMode = Player.REPEAT_MODE_ONE
        })
    }

    private lateinit var browserTree: BrowserTree


    override fun onCreate() {
        super.onCreate()

        Log.d(TAG, "Creating MediaPlaybackService")

        mediaSource.registerEventListener(this)
        mediaSource.whenReady { successfullyInitialized ->
            if (successfullyInitialized)
                browserTree = BrowserTree(mediaSource)
            else
                TODO("Handle unsuccessful initialization")
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
        mediaSession = MediaSessionCompat(this, "com.tachyonmusic.media")
            .apply {
                setSessionActivity(sessionActivityPendingIntent)
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
        notificationManager = NotificationManager(
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
             * TODO: What to return if [player.currentMediaItem] == null
             */
            setMediaMetadataProvider { player ->
                /**
                 * Update Playback on UI side whenever new metadata is requested
                 */
                if (currentPlayback != null) {
                    val playback = currentPlayback!!

                    mediaSession.sendSessionEvent(
                        MediaAction.SetPlaybackEvent,
                        Bundle().apply {
                            putParcelable(MediaAction.Playback, playback)
                        })

                    return@setMediaMetadataProvider playback.toMediaMetadata()
                }

                val playback =
                    player.mediaMetadata.extras?.getParcelable<Playback>(MetadataKeys.Playback)

                if (playback != null) {
                    mediaSession.sendSessionEvent(
                        MediaAction.SetPlaybackEvent,
                        Bundle().apply {
                            putParcelable(MediaAction.Playback, playback)
                        })

                    return@setMediaMetadataProvider playback.toMediaMetadata()
                }
                MediaMetadataCompat.Builder().build()
            }

            registerCustomCommandReceiver(preparer)
            setCustomActionProviders(*preparer.getCustomActions())

            /**
             * Required because when connecting phone via Bluetooth and playing the next
             * media item through the Bluetooth interface (e.g. pressing next button on headphones)
             * the next item won't be played if we're at the end of the playlist
             */
            setMediaButtonEventHandler(MediaButtonEventHandler())

            /**
             * If no next playback is present in queue we still want to dispatch ACTION_SKIP_TO_NEXT
             * to loop back to the beginning of the playlist (Logic in CustomPlayer)
             */
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
                result.sendResult(children)
            } else {
                TODO("Handle error that MediaSource wasn't initialized properly")
            }
        }

        /**
         * If the results are not ready, the service must "detach" the results before
         * the method returns. After the source is ready, the lambda above will run,
         * and the caller will be notified that the results are ready.
         */
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
        if (!currentPlayer.isPlaying && REQUIRES_OLD_NOTIFICATION_HANDLING)
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

    override fun onMediaSourceChanged(root: String, mediaId: String?) {
        Log.d(TAG, "onMediaSourceChanged with root $root and mediaId $mediaId")
        browserTree = BrowserTree(mediaSource)
        // TODO: Performance of that?
        if (root != BrowserTree.ROOT)
            notifyChildrenChanged(BrowserTree.ROOT)
        // END-TODO
        notifyChildrenChanged(root)

        if (mediaId != null) {
            notifyChildrenChanged(mediaId)
        }
    }

    fun preparePlayer(
        items: List<com.google.android.exoplayer2.MediaItem>,
        initialWindowIndex: Int
    ) {
        currentMediaItems = items
        currentPlayer.setMediaItems(items)

        currentPlayer.seekTo(initialWindowIndex, C.TIME_UNSET)
    }

    private inner class QueueNavigator(
        mediaSession: MediaSessionCompat
    ) : TimelineQueueNavigator(mediaSession) {
        override fun getMediaDescription(player: Player, windowIndex: Int): MediaDescriptionCompat {
            if (currentPlayback is Playlist && windowIndex < (currentPlayback as Playlist).playbacks.size)
                return (currentPlayback as Playlist).playbacks[windowIndex].toMediaDescriptionCompat()

            return currentPlayback?.toMediaDescriptionCompat() ?: MediaDescriptionCompat.Builder()
                .build()
        }
    }


    /***********************************************************************************************
     ***************************** PLAYBACK PREPARER ***********************************************
     **********************************************************************************************/
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

                if (currentPlayback is Loop) {
                    currentPlayer.seekTo((currentPlayback as Loop).startTime)
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

            mediaSource.whenReady { successfullyInitialized ->
                if (successfullyInitialized) {
                    onSetPlayback(
                        mediaSource.findById(MediaId.deserialize(mediaId))
                            ?: TODO("Playback for media id $mediaId not found")
                    )
                    onPrepare(playWhenReady)
                } else
                    TODO("MediaSource initialization unsuccessful")
            }
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


        override fun onSetPlayback(playback: Playback) {
            mediaSource.whenReady { successfullyInitialized ->
                if (!successfullyInitialized)
                    TODO("MediaSource initialization unsuccessful")

                /**
                 * All songs/loops will be set as the internal playlist when playing song/loop
                 * When playing an actual playlist, all songs/loops in the playlist will be set as
                 * the internal playlist
                 */
                when (playback) {
                    is Song -> {
                        currentPlayer.stop()

                        val initialWindowIndex = mediaSource.songs.indexOf(playback)
                        if (initialWindowIndex == -1)
                            TODO("Invalid initial window index for $playback")

                        isPlayingPlaylist = false
                        preparePlayer(
                            if (combinePlaybackTypes)
                                mediaSource.songs.map { it.toExoPlayerMediaItem() } +
                                        mediaSource.loops.map { it.toExoPlayerMediaItem() }
                            else
                                mediaSource.songs.map { it.toExoPlayerMediaItem() },
                            initialWindowIndex
                        )
                        currentPlayer.repeatMode = Player.REPEAT_MODE_ONE
                    }
                    is Loop -> {
                        currentPlayer.stop()
                        val initialWindowIndex = mediaSource.loops.indexOf(playback)
                        if (initialWindowIndex == -1)
                            TODO("Invalid initial window index for $playback")

                        isPlayingPlaylist = false
                        preparePlayer(
                            if (combinePlaybackTypes)
                                mediaSource.loops.map { it.toExoPlayerMediaItem() } +
                                        mediaSource.songs.map { it.toExoPlayerMediaItem() }
                            else
                                mediaSource.loops.map { it.toExoPlayerMediaItem() },
                            initialWindowIndex
                        )
                        currentPlayer.repeatMode = Player.REPEAT_MODE_ONE
                    }
                    is Playlist -> {
                        // Request for a specific song in playlist
                        isPlayingPlaylist = true
                        currentPlayback = playback

                        if (playback.currentPlaylistIndex != -1) {
                            currentPlayer.stop()

                            preparePlayer(
                                playback.toExoPlayerMediaItemList(),
                                playback.currentPlaylistIndex
                            )

                        }
                        currentPlayer.repeatMode = Player.REPEAT_MODE_ALL
                    }
                }
            }
        }

        override fun onSetStartTime(startTime: Long) {
            val playback = currentPlayback ?: return

            val endTime = playback.endTime

            // Start time and end time are back to default: Delete message
            if (startTime == 0L && endTime == currentPlayer.duration) {
                currentPlayer.cancelLoopMessage()
                return
            }

            if (currentPlayer.currentPosition < startTime || currentPlayer.currentPosition > endTime)
                currentPlayer.seekTo(startTime)
            currentPlayer.postLoopMessage(startTime, endTime)

            playback.startTime = startTime
        }

        override fun onSetEndTime(endTime: Long) {
            val playback = currentPlayback ?: return

            val startTime = playback.startTime

            // Start time and end time are back to default: Delete message
            if (startTime == 0L && endTime == currentPlayer.duration) {
                currentPlayer.cancelLoopMessage()
                return
            }

            if (currentPlayer.currentPosition < startTime || currentPlayer.currentPosition > endTime)
                currentPlayer.seekTo(startTime)
            currentPlayer.postLoopMessage(startTime, endTime)

            playback.endTime = endTime
        }

        override fun onRequestMediaSourceReload() {
            /**
             * Starts asynchronously loading all playbacks in shared storage (only songs atm)
             */
            Log.d(MediaPlaybackService.TAG, "Loading MediaSource")
            mediaSource.loadSharedDeviceFiles()
        }

        override fun onCombinePlaybackTypesChanged(combine: Boolean) {
            combinePlaybackTypes = combine
            // TODO: Immediately reload player to have instant results (maybe not because this is called quite often)
        }

        override fun onLoopsReceived(loops: MutableList<Loop>) {
            mediaSource.loops = loops
        }

        override fun onPlaylistsReceived(playlists: MutableList<Playlist>) {
            mediaSource.playlists = playlists
        }

        override fun onRequestPlaybackUpdate() {
            mediaSession.sendSessionEvent(MediaAction.SetPlaybackEvent, Bundle().apply {
                putParcelable(MediaAction.Playback, currentPlayback)
            })
        }

        override fun onCurrentPlaylistIndexChanged(currentPlaylistIndex: Int) {
            currentPlayer.seekToDefaultPosition(currentPlaylistIndex)
            onRequestPlaybackUpdate()
        }
    }

    /***********************************************************************************************
     ************************ PLAYER NOTIFICATION LISTENER *****************************************
     **********************************************************************************************/
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


    /***********************************************************************************************
     **************************** PLAYER EVENT LISTENER ********************************************
     **********************************************************************************************/
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
                        if (!playWhenReady && REQUIRES_OLD_NOTIFICATION_HANDLING) {
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
            mediaSession.sendSessionEvent(MediaAction.OnPlaybackStateChangedEvent, Bundle().apply {
                putBoolean(MediaAction.IsPlaying, isPlaying)
            })
        }

        override fun onMediaItemTransition(
            mediaItem: com.google.android.exoplayer2.MediaItem?,
            reason: Int
        ) {
            // TODO: Loops for [CastPlayer]
            if (isPlayingPlaylist)
                (currentPlayback as Playlist).currentPlaylistIndex =
                    currentPlayer.currentMediaItemIndex
            else
                currentPlayback = mediaItem!!.mediaMetadata.playback
            val playback = currentPlayback

            if (playback != null) {
                if (playback is Loop) {
                    // Single loop
                    currentPlayer.seekTo(playback.startTime)
                    currentPlayer.postLoopMessage(playback.startTime, playback.endTime)

                } else if (playback is Playlist && playback.current != null && playback.current!! is Loop) {
                    // Loop in playlist
                    // TODO: Loops in playlist not seeking to beginning
                    val loop = playback.current!! as Loop

                    currentPlayer.seekTo(loop.startTime)
                    currentPlayer.postLoopMessageForPlaylist(loop.endTime)
                }
            }
        }

        override fun onEvents(player: Player, events: Player.Events) {
            Log.d(TAG, "PlayerEventListener.onEvent")

            if (events.contains(Player.EVENT_POSITION_DISCONTINUITY)
                || events.contains(Player.EVENT_MEDIA_ITEM_TRANSITION)
                || events.contains(Player.EVENT_PLAY_WHEN_READY_CHANGED)
            ) {
                if (currentPlayback is Playlist) {
                    (currentPlayback as Playlist).currentPlaylistIndex =
                        if (currentMediaItems.isNotEmpty()) {
                            constrainValue(
                                player.currentMediaItemIndex,
                                0,
                                currentMediaItems.size - 1
                            )
                        } else 0
                }
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
}