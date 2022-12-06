package com.tachyonmusic.media.di

import android.app.Service
import androidx.media3.cast.CastPlayer
import androidx.media3.common.AudioAttributes
import androidx.media3.common.C
import androidx.media3.common.Player
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.exoplayer.util.EventLogger
import com.daton.database.domain.repository.SongRepository
import com.google.android.gms.cast.framework.CastContext
import com.tachyonmusic.media.CAST_PLAYER_NAME
import com.tachyonmusic.media.EXO_PLAYER_NAME
import com.tachyonmusic.media.data.BrowserTree
import com.tachyonmusic.media.data.CustomPlayerImpl
import com.tachyonmusic.media.domain.CustomPlayer
import com.tachyonmusic.media.domain.use_case.*
import com.tachyonmusic.user.domain.UserRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ServiceComponent
import dagger.hilt.android.scopes.ServiceScoped
import javax.inject.Named
import javax.inject.Singleton

@Module
@InstallIn(ServiceComponent::class)
class MediaPlaybackServiceModule {

    @Provides
    @ServiceScoped
    fun provideCastContext(service: Service): CastContext =
        CastContext.getSharedInstance(service)

    @Provides
    @ServiceScoped
    fun provideBrowserTree(
        repository: UserRepository,
        songRepository: SongRepository
    ): BrowserTree = BrowserTree(repository, songRepository)

    @Provides
    @ServiceScoped
    @Named(EXO_PLAYER_NAME)
    fun provideExoPlayer(service: Service): CustomPlayer =
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
    @Named(CAST_PLAYER_NAME)
    fun provideCastPlayer(context: CastContext): CustomPlayer =
        CustomPlayerImpl(CastPlayer(context))

    @Provides
    @ServiceScoped
    fun provideServiceUseCases(
        repository: UserRepository,
        @Named(EXO_PLAYER_NAME) player: CustomPlayer,
        songRepository: SongRepository,
    ) = ServiceUseCases(
        LoadPlaylistForPlayback(repository, songRepository),
        ConfirmAddedMediaItems(repository),
        PreparePlayer(player),
        GetSupportedCommands(),
        UpdateTimingDataOfCurrentPlayback(player),
        AddNewPlaybackToHistory(repository)
    )
}