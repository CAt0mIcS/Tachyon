package com.tachyonmusic.media.service

import android.os.Bundle
import androidx.media3.common.C
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.session.*
import com.google.common.collect.ImmutableList
import com.google.common.util.concurrent.Futures
import com.google.common.util.concurrent.ListenableFuture
import com.tachyonmusic.core.data.constants.MediaAction
import com.tachyonmusic.core.data.constants.MetadataKeys
import com.tachyonmusic.database.domain.repository.RecentlyPlayed
import com.tachyonmusic.logger.Log
import com.tachyonmusic.logger.domain.Logger
import com.tachyonmusic.media.CAST_PLAYER_NAME
import com.tachyonmusic.media.EXO_PLAYER_NAME
import com.tachyonmusic.media.data.BrowserTree
import com.tachyonmusic.media.data.MediaNotificationProvider
import com.tachyonmusic.media.data.ext.parcelable
import com.tachyonmusic.media.data.ext.playback
import com.tachyonmusic.media.domain.CustomPlayer
import com.tachyonmusic.media.domain.use_case.ServiceUseCases
import com.tachyonmusic.util.Resource
import com.tachyonmusic.util.future
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.*
import javax.inject.Inject
import javax.inject.Named


@AndroidEntryPoint
class MediaPlaybackService(
    private val log: Logger = Log()
) : MediaLibraryService(), Player.Listener {

    @Inject
    @Named(EXO_PLAYER_NAME)
    lateinit var exoPlayer: CustomPlayer

    @Inject
    @Named(CAST_PLAYER_NAME)
    lateinit var castPlayer: CustomPlayer

    @Inject
    lateinit var browserTree: BrowserTree

    @Inject
    lateinit var useCases: ServiceUseCases

    private val supervisor = SupervisorJob()
    private val ioScope = CoroutineScope(supervisor + Dispatchers.IO)

    private lateinit var mediaSession: MediaLibrarySession

    override fun onCreate() {
        super.onCreate()

        setMediaNotificationProvider(MediaNotificationProvider(this))

        exoPlayer.addListener(this)
        castPlayer.addListener(this)

        mediaSession =
            MediaLibrarySession.Builder(this, exoPlayer, MediaLibrarySessionCallback()).build()
    }

    override fun onGetSession(controllerInfo: MediaSession.ControllerInfo): MediaLibrarySession =
        mediaSession

    override fun onDestroy() {
        exoPlayer.release()
        castPlayer.release()
        mediaSession.release()
        // TODO: Make [mediaSession] nullable and set to null?
    }


    private inner class MediaLibrarySessionCallback : MediaLibrarySession.Callback {
        override fun onConnect(
            session: MediaSession,
            controller: MediaSession.ControllerInfo
        ): MediaSession.ConnectionResult = useCases.getSupportedCommands()

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
            useCases.confirmAddedMediaItems(mediaItems)
        }

        override fun onCustomCommand(
            session: MediaSession,
            controller: MediaSession.ControllerInfo,
            customCommand: SessionCommand,
            args: Bundle
        ): ListenableFuture<SessionResult> = future(Dispatchers.IO) {
            return@future when (customCommand) {
                MediaAction.setPlaybackCommand -> {
                    val loadingRes = useCases.loadPlaylistForPlayback(
                        args.parcelable(MetadataKeys.Playback)
                    )

                    if (loadingRes is Resource.Error)
                        return@future SessionResult(SessionResult.RESULT_ERROR_BAD_VALUE)

                    withContext(Dispatchers.Main) {
                        val prepareRes =
                            useCases.preparePlayer(loadingRes.data?.first, loadingRes.data?.second)
                        if (prepareRes is Resource.Error)
                            return@withContext SessionResult(SessionResult.RESULT_ERROR_BAD_VALUE)
                        SessionResult(SessionResult.RESULT_SUCCESS)
                    }
                }

                MediaAction.updateTimingDataCommand -> {
                    val res = withContext(Dispatchers.Main) {
                        useCases.updateTimingDataOfCurrentPlayback(
                            args.parcelable(MetadataKeys.TimingData)
                        )
                    }

                    if (res is Resource.Error)
                        return@future SessionResult(SessionResult.RESULT_ERROR_BAD_VALUE)
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
                useCases.addNewPlaybackToHistory(mediaItem?.mediaMetadata?.playback)
            }
    }

    // TODO: Check if the position is saved every time, e.g. when the playback is terminated another way
    override fun onIsPlayingChanged(isPlaying: Boolean) {
        if (!isPlaying) {
            val currentPos = exoPlayer.currentPosition
            val duration = exoPlayer.duration
            if (duration != C.TIME_UNSET)
                ioScope.launch {
                    useCases.saveRecentlyPlayed(RecentlyPlayed(currentPos, duration))
                }
        }

    }
}
