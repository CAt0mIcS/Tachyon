package com.tachyonmusic.media.service

import android.os.Bundle
import androidx.media3.common.AudioAttributes
import androidx.media3.common.C
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.exoplayer.util.EventLogger
import androidx.media3.session.LibraryResult
import androidx.media3.session.MediaLibraryService
import androidx.media3.session.MediaSession
import androidx.media3.session.SessionCommand
import androidx.media3.session.SessionResult
import com.google.common.collect.ImmutableList
import com.google.common.util.concurrent.Futures
import com.google.common.util.concurrent.ListenableFuture
import com.tachyonmusic.core.data.RemoteArtwork
import com.tachyonmusic.core.data.constants.MediaAction
import com.tachyonmusic.core.data.constants.MetadataKeys
import com.tachyonmusic.core.data.constants.RepeatMode
import com.tachyonmusic.core.domain.playback.SinglePlayback
import com.tachyonmusic.database.domain.ArtworkType
import com.tachyonmusic.database.domain.repository.RecentlyPlayed
import com.tachyonmusic.logger.LoggerImpl
import com.tachyonmusic.logger.domain.Logger
import com.tachyonmusic.media.data.BrowserTree
import com.tachyonmusic.media.data.CustomPlayerImpl
import com.tachyonmusic.media.data.MediaNotificationProvider
import com.tachyonmusic.media.data.ext.parcelable
import com.tachyonmusic.media.data.ext.playback
import com.tachyonmusic.media.domain.CustomPlayer
import com.tachyonmusic.media.domain.use_case.AddNewPlaybackToHistory
import com.tachyonmusic.media.domain.use_case.ConfirmAddedMediaItems
import com.tachyonmusic.media.domain.use_case.GetPlaylistForPlayback
import com.tachyonmusic.media.domain.use_case.GetSettings
import com.tachyonmusic.media.domain.use_case.GetSupportedCommands
import com.tachyonmusic.media.domain.use_case.PreparePlayer
import com.tachyonmusic.media.domain.use_case.SaveRecentlyPlayed
import com.tachyonmusic.media.domain.use_case.UpdateTimingDataOfCurrentPlayback
import com.tachyonmusic.util.Resource
import com.tachyonmusic.util.future
import com.tachyonmusic.util.runOnUiThreadAsync
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import javax.inject.Inject


@AndroidEntryPoint
class MediaPlaybackService(
    private val log: Logger = LoggerImpl()
) : MediaLibraryService(), Player.Listener {

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
    lateinit var preparePlayer: PreparePlayer

    @Inject
    lateinit var getSupportedCommands: GetSupportedCommands

    @Inject
    lateinit var updateTimingDataOfCurrentPlayback: UpdateTimingDataOfCurrentPlayback

    @Inject
    lateinit var addNewPlaybackToHistory: AddNewPlaybackToHistory

    @Inject
    lateinit var saveRecentlyPlayed: SaveRecentlyPlayed

    @Inject
    lateinit var getSettings: GetSettings

    private val supervisor = SupervisorJob()
    private val ioScope = CoroutineScope(supervisor + Dispatchers.IO)

    private lateinit var mediaSession: MediaLibrarySession

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
        exoPlayer.release()
        castPlayer.release()
        mediaSession.release()
        // TODO: Make [mediaSession] nullable and set to null?
    }


    private inner class MediaLibrarySessionCallback : MediaLibrarySession.Callback {
        override fun onConnect(
            session: MediaSession,
            controller: MediaSession.ControllerInfo
        ): MediaSession.ConnectionResult = getSupportedCommands()

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
            return@future when (customCommand) {
                MediaAction.setPlaybackCommand -> {
                    val loadingRes = getPlaylistForPlayback(
                        args.parcelable(MetadataKeys.Playback)
                    )

                    if (loadingRes is Resource.Error)
                        return@future SessionResult(SessionResult.RESULT_ERROR_BAD_VALUE)

                    withContext(Dispatchers.Main) {
                        val prepareRes =
                            preparePlayer(
                                currentPlayer,
                                loadingRes.data?.mediaItems,
                                loadingRes.data?.initialWindowIndex
                            )

                        if (prepareRes is Resource.Error)
                            return@withContext SessionResult(SessionResult.RESULT_ERROR_BAD_VALUE)
                        SessionResult(SessionResult.RESULT_SUCCESS)
                    }
                }

                MediaAction.updateTimingDataCommand -> {
                    val res = withContext(Dispatchers.Main) {
                        updateTimingDataOfCurrentPlayback(
                            currentPlayer,
                            args.parcelable(MetadataKeys.TimingData)
                        )
                    }

                    if (res is Resource.Error)
                        return@future SessionResult(SessionResult.RESULT_ERROR_BAD_VALUE)
                    SessionResult(SessionResult.RESULT_SUCCESS)
                }

                MediaAction.repeatModeChangedCommand -> {
                    runOnUiThreadAsync {
                        when (RepeatMode.fromId(args.getInt(MetadataKeys.RepeatMode))) {
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

                else -> SessionResult(SessionResult.RESULT_ERROR_NOT_SUPPORTED)
            }
        }
    }

    override fun onMediaItemTransition(mediaItem: MediaItem?, reason: Int) {
        /**
         * When changing playlist [onMediaItemTransition] is also called with the bellow reason
         * this would mean having the first item in the playlist in history as well as the one
         * we actually want to play
         */
        if (reason != Player.MEDIA_ITEM_TRANSITION_REASON_PLAYLIST_CHANGED)
            ioScope.launch {
                addNewPlaybackToHistory(mediaItem?.mediaMetadata?.playback)
            }
    }

    override fun onIsPlayingChanged(isPlaying: Boolean) {
        if (!isPlaying) {
            val playback = currentPlayer.mediaMetadata.playback ?: return
            val currentPos = currentPlayer.currentPosition
            ioScope.launch {
                saveRecentlyPlayed(
                    RecentlyPlayed(
                        playback.mediaId,
                        currentPos,
                        playback.duration ?: return@launch,
                        ArtworkType.getType(playback as SinglePlayback), // TODO: Playlists
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
            repeatMode = Player.REPEAT_MODE_ONE
        })
}
