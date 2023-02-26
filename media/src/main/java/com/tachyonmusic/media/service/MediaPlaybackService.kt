package com.tachyonmusic.media.service

import android.os.Bundle
import androidx.annotation.OptIn
import androidx.media3.common.AudioAttributes
import androidx.media3.common.C
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.exoplayer.util.EventLogger
import androidx.media3.session.*
import com.google.common.collect.ImmutableList
import com.google.common.util.concurrent.Futures
import com.google.common.util.concurrent.ListenableFuture
import com.tachyonmusic.core.data.RemoteArtwork
import com.tachyonmusic.core.domain.TimingDataController
import com.tachyonmusic.core.domain.playback.SinglePlayback
import com.tachyonmusic.database.domain.ArtworkType
import com.tachyonmusic.database.domain.repository.RecentlyPlayed
import com.tachyonmusic.logger.domain.Logger
import com.tachyonmusic.media.core.*
import com.tachyonmusic.media.data.BrowserTree
import com.tachyonmusic.media.data.CustomPlayerImpl
import com.tachyonmusic.media.data.MediaNotificationProvider
import com.tachyonmusic.media.domain.CustomPlayer
import com.tachyonmusic.media.domain.use_case.*
import com.tachyonmusic.media.util.playback
import com.tachyonmusic.media.util.prepare
import com.tachyonmusic.media.util.supportedCommands
import com.tachyonmusic.media.util.updateTimingDataOfCurrentPlayback
import com.tachyonmusic.util.*
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.*
import javax.inject.Inject

@OptIn(UnstableApi::class)
@AndroidEntryPoint
class MediaPlaybackService : MediaLibraryService(), Player.Listener {

    private lateinit var exoPlayer: CustomPlayer
    private lateinit var currentPlayer: CustomPlayer

    @Inject
    lateinit var castPlayer: CustomPlayer

    @Inject
    lateinit var browserTree: BrowserTree

    @Inject
    lateinit var getPlaylistForPlayback: GetPlaylistForPlayback

    @Inject
    lateinit var confirmAddedMediaItems: ConfirmAddedMediaItems

    @Inject
    lateinit var addNewPlaybackToHistory: AddNewPlaybackToHistory

    @Inject
    lateinit var saveRecentlyPlayed: SaveRecentlyPlayed

    @Inject
    lateinit var getSettings: GetSettings

    @Inject
    lateinit var log: Logger

    private val ioScope = CoroutineScope(Dispatchers.IO + SupervisorJob())

    private lateinit var mediaSession: MediaLibrarySession

    private var queuedPlayback: SinglePlayback? = null

    private var sortParams = SortParameters()


    override fun onCreate() {
        super.onCreate()

        runBlocking {
            val settings = getSettings()
            exoPlayer = buildExoPlayer(!settings.ignoreAudioFocus)
        }
        currentPlayer = exoPlayer

        setMediaNotificationProvider(MediaNotificationProvider(this))

        exoPlayer.addListener(this)
        castPlayer.addListener(this)

        mediaSession =
            MediaLibrarySession.Builder(this, exoPlayer, MediaLibrarySessionCallback()).build()
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
        ): ListenableFuture<MutableList<MediaItem>> = future(Dispatchers.IO) {
            confirmAddedMediaItems(mediaItems)
        }

        override fun onCustomCommand(
            session: MediaSession,
            controller: MediaSession.ControllerInfo,
            customCommand: SessionCommand,
            args: Bundle
        ): ListenableFuture<SessionResult> = future(Dispatchers.IO) {
            return@future when (val event = customCommand.toMediaBrowserEvent(args)) {
                is SetPlaybackEvent -> {
                    mediaSession.dispatchMediaEvent(TimingDataUpdatedEvent(event.playback?.timingData))

                    if (event.playback == null) {
                        runOnUiThread {
                            currentPlayer.stop()
                            currentPlayer.clearMediaItems()
                        }
                        return@future SessionResult(SessionResult.RESULT_SUCCESS)
                    }

                    if (event.playback is SinglePlayback) {
                        val playbackChanged = runOnUiThread {
                            val idx = currentPlayer.indexOfMediaItem(event.playback.mediaId)

                            if (idx >= 0) {
                                queuedPlayback = event.playback
                                currentPlayer.seekTo(idx, 0)
                                return@runOnUiThread true
                            }
                            false
                        }

                        if (playbackChanged)
                            return@future SessionResult(SessionResult.RESULT_SUCCESS)
                    }

                    val loadingRes = getPlaylistForPlayback(event.playback, sortParams)

                    if (loadingRes is Resource.Error)
                        return@future SessionResult(SessionResult.RESULT_ERROR_BAD_VALUE)

                    queuedPlayback = event.playback.underlyingSinglePlayback
                    runOnUiThread {
                        val prepareRes =
                            currentPlayer.prepare(
                                loadingRes.data?.mediaItems,
                                loadingRes.data?.initialWindowIndex
                            )

                        if (prepareRes is Resource.Error)
                            return@runOnUiThread SessionResult(SessionResult.RESULT_ERROR_BAD_VALUE)

                        SessionResult(SessionResult.RESULT_SUCCESS)
                    }
                }

                is SetTimingDataEvent -> {
                    val res = withContext(Dispatchers.Main) {
                        currentPlayer.updateTimingDataOfCurrentPlayback(event.timingData)
                    }

                    if (res is Resource.Error)
                        return@future SessionResult(SessionResult.RESULT_ERROR_BAD_VALUE)
                    SessionResult(SessionResult.RESULT_SUCCESS)
                }

                is SetRepeatModeEvent -> {
                    runOnUiThreadAsync {
                        when (event.repeatMode) {
                            RepeatMode.All -> {
                                currentPlayer.shuffleModeEnabled = false
                                currentPlayer.repeatMode = Player.REPEAT_MODE_ALL
                            }

                            RepeatMode.One -> {
                                currentPlayer.shuffleModeEnabled = false
                                currentPlayer.repeatMode = Player.REPEAT_MODE_ONE
                            }

                            RepeatMode.Shuffle -> {
                                currentPlayer.repeatMode = Player.REPEAT_MODE_ALL
                                currentPlayer.shuffleModeEnabled = true
                            }
                        }
                    }
                    SessionResult(SessionResult.RESULT_SUCCESS)
                }

                is SetSortingParamsEvent -> {
                    sortParams = event.sortParameters
                    SessionResult(SessionResult.RESULT_SUCCESS)
                }
            }
        }
    }

    override fun onMediaItemTransition(mediaItem: MediaItem?, reason: Int) {
        /**
         * When changing playlist [onMediaItemTransition] is also called with the bellow reason
         * this would mean having the first item in the playlist in history as well as the one
         * we actually want to play
         */
        if (reason != Player.MEDIA_ITEM_TRANSITION_REASON_PLAYLIST_CHANGED || queuedPlayback != null) {
            ioScope.launch {
                addNewPlaybackToHistory(queuedPlayback ?: mediaItem?.mediaMetadata?.playback)
                queuedPlayback = null
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
                        if (playback.artwork.value is RemoteArtwork)
                            (playback.artwork.value as RemoteArtwork).uri.toURL()
                                .toString() else null
                    )
                )
            }
        }
    }

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

            repeatMode = Player.REPEAT_MODE_ALL
        }, log).apply {
            registerEventListener(CustomPlayerEventListener())
        }

    private inner class CustomPlayerEventListener : CustomPlayer.Listener {
        override fun onTimingDataUpdated(controller: TimingDataController?) {
            mediaSession.dispatchMediaEvent(TimingDataUpdatedEvent(controller))
        }
    }
}
