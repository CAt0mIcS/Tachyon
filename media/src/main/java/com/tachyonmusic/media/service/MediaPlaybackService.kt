package com.tachyonmusic.media.service

import android.os.Bundle
import androidx.media3.common.*
import androidx.media3.common.text.Cue
import androidx.media3.common.text.CueGroup
import androidx.media3.session.*
import com.google.common.collect.ImmutableList
import com.google.common.util.concurrent.ListenableFuture
import com.tachyonmusic.media.CustomPlayer
import com.tachyonmusic.media.data.BrowserTree
import com.tachyonmusic.user.domain.FileRepository
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject


@AndroidEntryPoint
class MediaPlaybackService : MediaLibraryService(),
    MediaLibraryService.MediaLibrarySession.Callback,
    Player.Listener {

    @Inject
    lateinit var player: CustomPlayer

    @Inject
    lateinit var browserTree: BrowserTree

    @Inject
    lateinit var repository: FileRepository

    private lateinit var mediaSession: MediaLibrarySession

    override fun onCreate() {
        super.onCreate()

        mediaSession =
            MediaLibrarySession.Builder(this, player, this).build()
    }

    override fun onGetSession(controllerInfo: MediaSession.ControllerInfo): MediaLibrarySession =
        mediaSession
}
