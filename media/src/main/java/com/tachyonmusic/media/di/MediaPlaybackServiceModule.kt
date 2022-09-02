package com.tachyonmusic.media.di

import android.app.Service
import androidx.media3.common.AudioAttributes
import androidx.media3.common.C
import androidx.media3.common.Player
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.exoplayer.util.EventLogger
import com.tachyonmusic.media.domain.CustomPlayer
import com.tachyonmusic.media.data.BrowserTree
import com.tachyonmusic.media.data.CustomPlayerImpl
import com.tachyonmusic.media.domain.use_case.*
import com.tachyonmusic.user.domain.UserRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ServiceComponent
import dagger.hilt.android.scopes.ServiceScoped
import javax.inject.Singleton

@Module
@InstallIn(ServiceComponent::class)
class MediaPlaybackServiceModule {

    @Provides
    @ServiceScoped
    fun provideBrowserTree(repository: UserRepository): BrowserTree = BrowserTree(repository)

    @Provides
    @ServiceScoped
    fun providePlayer(service: Service): CustomPlayer =
        CustomPlayerImpl(ExoPlayer.Builder(service).apply {
            setAudioAttributes(
                AudioAttributes.Builder()
                    .setContentType(C.AUDIO_CONTENT_TYPE_MUSIC)
                    .setUsage(C.USAGE_MEDIA)
                    .build(), true
            )
            setHandleAudioBecomingNoisy(true)
        }.build().apply {
            // TODO: Debug only
            addAnalyticsListener(EventLogger())
            repeatMode = Player.REPEAT_MODE_ONE
        })

    @Provides
    @ServiceScoped
    fun provideServiceUseCases(repository: UserRepository, player: CustomPlayer) = ServiceUseCases(
        LoadPlaylistForPlayback(repository),
        ConfirmAddedMediaItems(repository),
        PreparePlayer(player),
        GetSupportedCommands(),
        AddTimingDataToCurrentPlayback(player)
    )
}