package com.tachyonmusic.media.service

import android.os.Bundle
import android.util.Log
import androidx.media3.common.MediaItem
import androidx.media3.session.*
import com.google.common.collect.ImmutableList
import com.google.common.util.concurrent.Futures
import com.google.common.util.concurrent.ListenableFuture
import com.tachyonmusic.core.Resource
import com.tachyonmusic.core.constants.MediaAction
import com.tachyonmusic.core.constants.MetadataKeys
import com.tachyonmusic.core.domain.TimingData
import com.tachyonmusic.media.data.BrowserTree
import com.tachyonmusic.media.data.MediaNotificationProvider
import com.tachyonmusic.media.domain.CustomPlayer
import com.tachyonmusic.media.domain.use_case.ServiceUseCases
import com.tachyonmusic.util.future
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject


@AndroidEntryPoint
class MediaPlaybackService : MediaLibraryService() {

    @Inject
    lateinit var player: CustomPlayer

    @Inject
    lateinit var browserTree: BrowserTree

    @Inject
    lateinit var useCases: ServiceUseCases

    private lateinit var mediaSession: MediaLibrarySession

    override fun onCreate() {
        super.onCreate()

        setMediaNotificationProvider(MediaNotificationProvider(this))

        mediaSession =
            MediaLibrarySession.Builder(this, player, MediaLibrarySessionCallback()).build()
    }

    override fun onGetSession(controllerInfo: MediaSession.ControllerInfo): MediaLibrarySession =
        mediaSession

    override fun onDestroy() {
        player.release()
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
            Log.d("MediaPlaybackService", "Started onGetChildren")
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
                        args.getParcelable(MetadataKeys.Playback)
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
                            TimingData.fromStringArray(
                                args.getStringArray(MetadataKeys.TimingData) ?: emptyArray()
                            )
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
}
