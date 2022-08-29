package com.tachyonmusic.media.service

import android.os.Bundle
import android.util.Log
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.session.*
import com.google.common.collect.ImmutableList
import com.google.common.util.concurrent.Futures
import com.google.common.util.concurrent.ListenableFuture
import com.tachyonmusic.core.domain.MediaId
import com.tachyonmusic.core.domain.playback.Loop
import com.tachyonmusic.core.domain.playback.Playlist
import com.tachyonmusic.core.domain.playback.Song
import com.tachyonmusic.media.data.BrowserTree
import com.tachyonmusic.media.data.MediaNotificationProvider
import com.tachyonmusic.media.domain.CustomPlayer
import com.tachyonmusic.user.domain.UserRepository
import com.tachyonmusic.util.future
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Dispatchers
import javax.inject.Inject


@AndroidEntryPoint
class MediaPlaybackService : MediaLibraryService() {

    @Inject
    lateinit var player: CustomPlayer

    @Inject
    lateinit var browserTree: BrowserTree

    @Inject
    lateinit var repository: UserRepository

    private lateinit var mediaSession: MediaLibrarySession

    override fun onCreate() {
        super.onCreate()

        repository.registerEventListener(UserEventListener())
        player.addListener(PlayerListener())

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


    private inner class UserEventListener : UserRepository.EventListener {
        override fun onSongListChanged(song: Song) {
            // TODO: Check if item count should be the count of items that changed or the count of total items in SONG_ROOT
            // TODO: Should we also notify [BrowserTree.ROOT]?
            mediaSession.notifyChildrenChanged(BrowserTree.SONG_ROOT, 1, null)
        }

        override fun onLoopListChanged(loop: Loop) {
            mediaSession.notifyChildrenChanged(BrowserTree.PLAYLIST_ROOT, 1, null)
        }

        override fun onPlaylistListChanged(playlist: Playlist) {
            mediaSession.notifyChildrenChanged(BrowserTree.PLAYLIST_ROOT, 1, null)
        }

        override fun onPlaylistChanged(playlist: Playlist) {
            mediaSession.notifyChildrenChanged(playlist.mediaId.toString(), 1, null)
        }
    }


    private inner class MediaLibrarySessionCallback : MediaLibrarySession.Callback {
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
            LibraryResult.ofError(404)
        }

        override fun onAddMediaItems(
            mediaSession: MediaSession,
            controller: MediaSession.ControllerInfo,
            mediaItems: MutableList<MediaItem>
        ): ListenableFuture<MutableList<MediaItem>> = future(Dispatchers.IO) {

            val list = mutableListOf<MediaItem>()
            for (item in mediaItems) {
                val playback = repository.find(MediaId.deserialize(item.mediaId))
                    ?: TODO("Playback ${item.mediaId} not found")
                list.add(playback.toMediaItem())
            }

            list
        }
    }


    private inner class PlayerListener : Player.Listener {

    }
}
