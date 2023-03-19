package com.tachyonmusic.media.service

import android.os.Bundle
import androidx.media3.common.AudioAttributes
import androidx.media3.common.C
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.exoplayer.util.EventLogger
import androidx.media3.session.*
import com.google.common.collect.ImmutableList
import com.google.common.util.concurrent.Futures
import com.google.common.util.concurrent.ListenableFuture
import com.tachyonmusic.core.ArtworkType
import com.tachyonmusic.core.data.RemoteArtwork
import com.tachyonmusic.core.domain.TimingDataController
import com.tachyonmusic.database.domain.repository.DataRepository
import com.tachyonmusic.database.domain.repository.RecentlyPlayed
import com.tachyonmusic.database.domain.repository.SettingsRepository
import com.tachyonmusic.logger.domain.Logger
import com.tachyonmusic.media.core.*
import com.tachyonmusic.media.data.BrowserTree
import com.tachyonmusic.media.data.CustomPlayerImpl
import com.tachyonmusic.media.data.MediaNotificationProvider
import com.tachyonmusic.media.domain.CustomPlayer
import com.tachyonmusic.media.domain.use_case.AddNewPlaybackToHistory
import com.tachyonmusic.media.domain.use_case.SaveRecentlyPlayed
import com.tachyonmusic.media.util.coreRepeatMode
import com.tachyonmusic.media.util.playback
import com.tachyonmusic.media.util.supportedCommands
import com.tachyonmusic.util.future
import com.tachyonmusic.util.ms
import com.tachyonmusic.util.runOnUiThread
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.*
import javax.inject.Inject

@AndroidEntryPoint
class MediaPlaybackService : MediaLibraryService(), Player.Listener {

    private lateinit var exoPlayer: CustomPlayer
    private lateinit var currentPlayer: CustomPlayer

    @Inject
    lateinit var castPlayer: CustomPlayer

    @Inject
    lateinit var browserTree: BrowserTree

    @Inject
    lateinit var settingsRepository: SettingsRepository

    @Inject
    lateinit var saveRecentlyPlayed: SaveRecentlyPlayed

    @Inject
    lateinit var addNewPlaybackToHistory: AddNewPlaybackToHistory

    @Inject
    lateinit var dataRepository: DataRepository

    @Inject
    lateinit var log: Logger

    private val ioScope = CoroutineScope(Dispatchers.IO + SupervisorJob())

    private lateinit var mediaSession: MediaLibrarySession


    override fun onCreate() {
        super.onCreate()

        runBlocking {
            exoPlayer = buildExoPlayer(!settingsRepository.getSettings().ignoreAudioFocus)
            currentPlayer = exoPlayer
        }

//        settingsRepository.observe().onEach {
//            switchPlayer(exoPlayer, buildExoPlayer(!it.ignoreAudioFocus))
//        }.launchIn(ioScope)

        setMediaNotificationProvider(MediaNotificationProvider(this))

        exoPlayer.addListener(this)
        castPlayer.addListener(this)

        mediaSession =
            MediaLibrarySession.Builder(this, exoPlayer, MediaLibrarySessionCallback()).build()
    }

    /**
     * TODO: Seamlessly switch between old and new exo player instances
     */
    fun switchPlayer(oldPlayer: CustomPlayer, newPlayer: CustomPlayer) {
        val pos = oldPlayer.currentPosition
        val items = oldPlayer.mediaItems
        val currentIndex = oldPlayer.currentMediaItemIndex
        val playWhenReady = oldPlayer.playWhenReady

        if (items.isNotEmpty()) {
            newPlayer.setMediaItems(items)
            newPlayer.seekTo(currentIndex, pos)
            newPlayer.playWhenReady = playWhenReady
            newPlayer.prepare()
        }

        currentPlayer = newPlayer
    }

    override fun onGetSession(controllerInfo: MediaSession.ControllerInfo): MediaLibrarySession =
        mediaSession

    override fun onDestroy() {
        super.onDestroy()
        ioScope.coroutineContext.cancelChildren()

        exoPlayer.release()
        castPlayer.release()
        mediaSession.release()
        // TODO: Make [mediaSession] nullable and set to null?
    }


    private inner class MediaLibrarySessionCallback : MediaLibrarySession.Callback {
        override fun onConnect(
            session: MediaSession,
            controller: MediaSession.ControllerInfo
        ): MediaSession.ConnectionResult = supportedCommands

        override fun onPostConnect(session: MediaSession, controller: MediaSession.ControllerInfo) {
            val playback = currentPlayer.mediaMetadata.playback
            log.info("Dispatching StateUpdateEvent with $playback and playWhenReady=${currentPlayer.playWhenReady}")
            mediaSession.dispatchMediaEvent(
                StateUpdateEvent(
                    playback,
                    currentPlayer.playWhenReady
                )
            )
        }

        override fun onGetLibraryRoot(
            session: MediaLibrarySession,
            browser: MediaSession.ControllerInfo,
            params: LibraryParams?
        ): ListenableFuture<LibraryResult<MediaItem>> =
            Futures.immediateFuture(LibraryResult.ofItem(browserTree.root, null))

        override fun onGetChildren(
            session: MediaLibrarySession,
            browser: MediaSession.ControllerInfo,
            parentId: String,
            page: Int,
            pageSize: Int,
            params: LibraryParams?
        ): ListenableFuture<LibraryResult<ImmutableList<MediaItem>>> = future(Dispatchers.IO) {
            log.debug("Started onGetChildren")
            val items = browserTree.get(parentId, page, pageSize)
            if (items != null)
                return@future LibraryResult.ofItemList(items, null)
            LibraryResult.ofError(LibraryResult.RESULT_ERROR_BAD_VALUE)
        }

        override fun onAddMediaItems(
            mediaSession: MediaSession,
            controller: MediaSession.ControllerInfo,
            mediaItems: MutableList<MediaItem>
        ): ListenableFuture<List<MediaItem>> = future(Dispatchers.IO) {
            mediaItems.mapNotNull {
                it.buildUpon()
                    .setUri(it.mediaMetadata.playback?.uri ?: return@mapNotNull null)
                    .build()
            }
        }

        override fun onCustomCommand(
            session: MediaSession,
            controller: MediaSession.ControllerInfo,
            customCommand: SessionCommand,
            args: Bundle
        ): ListenableFuture<SessionResult> = future(Dispatchers.IO) {
            runOnUiThread {
                when (val event = customCommand.toMediaBrowserEvent(args)) {
                    is SetRepeatModeEvent -> handleSetRepeatModeEvent(event)
                    else -> SessionResult(SessionResult.RESULT_SUCCESS)
                }
            }
        }


        private fun handleSetRepeatModeEvent(event: SetRepeatModeEvent): SessionResult {
            currentPlayer.coreRepeatMode = event.repeatMode
            return SessionResult(SessionResult.RESULT_SUCCESS)
        }
    }


    /**************************************************************************
     ********** [Player.Listener]
     *************************************************************************/
    override fun onMediaItemTransition(mediaItem: MediaItem?, reason: Int) {
        if (reason != Player.MEDIA_ITEM_TRANSITION_REASON_PLAYLIST_CHANGED) {
            val newPlayback = mediaItem?.mediaMetadata?.playback
            ioScope.launch {
                addNewPlaybackToHistory(newPlayback)
            }
        }
    }

    override fun onIsPlayingChanged(isPlaying: Boolean) {
        if (!isPlaying) {
            val playback = currentPlayer.mediaMetadata.playback ?: return
            val currentPos = currentPlayer.currentPosition.ms
            ioScope.launch {
                saveRecentlyPlayed(
                    RecentlyPlayed(
                        playback.mediaId,
                        currentPos,
                        playback.duration,
                        ArtworkType.getType(playback),
                        if (playback.artwork is RemoteArtwork)
                            (playback.artwork as RemoteArtwork).uri.toURL()
                                .toString() else null
                    )
                )
            }
        }
    }

    override fun onPositionDiscontinuity(
        oldPosition: Player.PositionInfo,
        newPosition: Player.PositionInfo,
        reason: Int
    ) {
        if (reason == Player.DISCONTINUITY_REASON_SEEK || reason == Player.DISCONTINUITY_REASON_SEEK_ADJUSTMENT) {
            ioScope.launch {
                val data = dataRepository.getData()
                dataRepository.update(
                    recentlyPlayed = RecentlyPlayed(
                        mediaId = data.recentlyPlayedMediaId ?: return@launch,
                        position = newPosition.contentPositionMs.ms,
                        duration = data.recentlyPlayedDuration,
                        artworkType = data.recentlyPlayedArtworkType,
                        artworkUrl = data.recentlyPlayedArtworkUrl
                    )
                )
            }
        }
    }


    /**************************************************************************
     ********** HELPER FUNCTIONS
     *************************************************************************/
    private fun buildExoPlayer(handleAudioFocus: Boolean): CustomPlayer =
        CustomPlayerImpl(ExoPlayer.Builder(this).apply {
            setAudioAttributes(
                AudioAttributes.Builder()
                    .setContentType(C.AUDIO_CONTENT_TYPE_MUSIC)
                    .setUsage(C.USAGE_MEDIA)
                    .build(),
                handleAudioFocus
            )
            setHandleAudioBecomingNoisy(true)
        }.build().apply {
            // TODO: Debug only
            addAnalyticsListener(EventLogger())
        }, log).apply {
            registerEventListener(CustomPlayerEventListener())
        }

    private inner class CustomPlayerEventListener : CustomPlayer.Listener {
        override fun onTimingDataUpdated(controller: TimingDataController?) {
            mediaSession.dispatchMediaEvent(TimingDataUpdatedEvent(controller))
        }
    }
}
