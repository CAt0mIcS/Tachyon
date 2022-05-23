package com.daton.media.service

import android.app.Notification
import android.app.PendingIntent
import android.content.Intent
import android.net.Uri
import android.os.*
import android.support.v4.media.MediaBrowserCompat.MediaItem
import android.support.v4.media.MediaDescriptionCompat
import android.support.v4.media.session.MediaSessionCompat
import android.support.v4.media.session.PlaybackStateCompat
import android.util.Log
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.media.MediaBrowserServiceCompat
import com.daton.media.device.BrowserTree
import com.daton.media.device.MediaSource
import com.daton.media.ext.*
import com.daton.media.CustomPlayer
import com.daton.media.Playlist
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

    private lateinit var mediaSource: MediaSource

    private lateinit var mediaSession: MediaSessionCompat
    private lateinit var mediaSessionConnector: MediaSessionConnector

    private lateinit var notificationManager: MucifyNotificationManager

    private lateinit var playlist: Playlist

    private var isForegroundService = false

    /**
     * Configure ExoPlayer to handle audio focus for us.
     * See [Player.AudioComponent.setAudioAttributes] for details.
     */
    private val exoPlayer: CustomPlayer by lazy {
        CustomPlayer(ExoPlayer.Builder(this).run {
            setAudioAttributes(
                AudioAttributes.Builder()
                    .setContentType(C.CONTENT_TYPE_MUSIC)
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

            playlist = Playlist(this)
        })
    }

    private val browserTree: BrowserTree by lazy {
        BrowserTree(mediaSource)
    }


    override fun onCreate() {
        super.onCreate()

        Log.d(TAG, "Creating MediaPlaybackService")

        // Doesn't start loading files yet as we need to wait on storage permission to be granted
        mediaSource = MediaSource(this)

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
            registerCustomCommandReceiver(preparer)
            setCustomActionProviders(*preparer.getCustomActions())

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
                val children = browserTree[parentId]?.map { item ->
                    MediaItem(item.description, 0)
                }
                result.sendResult(children)
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

    private inner class QueueNavigator(
        mediaSession: MediaSessionCompat
    ) : TimelineQueueNavigator(mediaSession) {
        override fun getMediaDescription(player: Player, windowIndex: Int): MediaDescriptionCompat {
            if (windowIndex < playlist.size) {
                return MediaDescriptionCompat.Builder().apply {
                    title = playlist[windowIndex].title
                    artist = playlist[windowIndex].artist
                    iconBitmap = playlist[windowIndex].albumArt
                }.build()
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

            onSetMediaId(mediaId)
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


        override fun onSetMediaId(mediaId: String) {
            mediaSource.whenReady {
                // Find either the underlying playback or the top-level playback to play
                val itemToPlay =
                    mediaSource.find { item ->
                        if (mediaId.hasUnderlyingPlayback == true)
                            mediaId.underlyingPlayback == item.mediaId
                        else
                            item.mediaId == mediaId
                    }
                if (itemToPlay == null)
                    Log.e(TAG, "Invalid media id $mediaId")
                else {
                    currentPlayer.stop()

                    /**
                     * All songs/loops will be set as the internal playlist when playing song/loop
                     * When playing an actual playlist, all songs/loops in the playlist will be set as
                     * the internal playlist
                     * TODO: Maybe introduce setting to combine songs and loops as playlist items when playing either song or loop
                     */
                    when {
                        mediaId.isSongMediaId -> {
                            playlist.play(
                                mediaSource.filter { it.mediaId.isSongMediaId },
                                itemToPlay
                            )
                            currentPlayer.repeatMode = Player.REPEAT_MODE_ONE
                        }
                        mediaId.isLoopMediaId -> {
                            playlist.play(
                                mediaSource.filter { it.mediaId.isLoopMediaId },
                                itemToPlay
                            )
                            currentPlayer.repeatMode = Player.REPEAT_MODE_ONE
                        }
                        mediaId.isPlaylistMediaId -> {
                            playlist.play(browserTree[mediaId.basePlayback]!!, itemToPlay)
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
        }

        override fun onStoragePermissionChanged(permissionGranted: Boolean) {
            if (permissionGranted) {
                /**
                 * Starts asynchronously loading all possible media playbacks
                 */
                Log.d(MediaPlaybackService.TAG, "Loading MediaSource")
                serviceScope.launch {
                    mediaSource.loadDeviceFiles()
                }
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

        override fun onPlayerStateChanged(playWhenReady: Boolean, playbackState: Int) {
            when (playbackState) {
                Player.STATE_BUFFERING,
                Player.STATE_READY -> {
                    notificationManager.showNotificationForPlayer(currentPlayer)
                    if (playbackState == Player.STATE_READY) {

                        // When playing/paused save the current media item in persistent
                        // storage so that playback can be resumed between device reboots.
//                        saveRecentSongToStorage()

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

        }

        override fun onMediaItemTransition(
            mediaItem: com.google.android.exoplayer2.MediaItem?,
            reason: Int
        ) {
            // TODO: Loops for [CastPlayer]
            if (mediaItem != null && mediaItem.isLoop) {
                currentPlayer.seekTo(mediaItem.mediaMetadata.startTime)

                postLoopMessage(
                    mediaItem.mediaMetadata.startTime,
                    mediaItem.mediaMetadata.endTime
                )
            }
        }

        override fun onEvents(player: Player, events: Player.Events) {
            Log.d(TAG, "PlayerEventListener.onEvent with events $events")

            if (events.contains(Player.EVENT_POSITION_DISCONTINUITY)
                || events.contains(Player.EVENT_MEDIA_ITEM_TRANSITION)
                || events.contains(Player.EVENT_PLAY_WHEN_READY_CHANGED)
            ) {
                playlist.currentPlaybackIndex = if (playlist.isNotEmpty()) {
                    constrainValue(
                        player.currentMediaItemIndex,
                        0,
                        playlist.size - 1
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

    /**
     * Checks if we need to stop the foreground service when the playback is paused to make
     * the notification swipeable
     */
    private fun requiresOldNotificationHandling(): Boolean = Build.VERSION.SDK_INT <= 23

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
}
