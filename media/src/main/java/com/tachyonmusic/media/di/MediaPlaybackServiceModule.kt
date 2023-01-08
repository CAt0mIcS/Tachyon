package com.tachyonmusic.media.di

import android.app.Service
import androidx.media3.cast.CastPlayer
import androidx.media3.common.AudioAttributes
import androidx.media3.common.C
import androidx.media3.common.Player
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.exoplayer.util.EventLogger
import com.google.android.gms.cast.framework.CastContext
import com.tachyonmusic.artworkfetcher.ArtworkFetcher
import com.tachyonmusic.database.domain.repository.DataRepository
import com.tachyonmusic.database.domain.repository.HistoryRepository
import com.tachyonmusic.database.domain.repository.LoopRepository
import com.tachyonmusic.database.domain.repository.PlaylistRepository
import com.tachyonmusic.database.domain.repository.SettingsRepository
import com.tachyonmusic.database.domain.repository.SongRepository
import com.tachyonmusic.database.domain.use_case.FindPlaybackByMediaId
import com.tachyonmusic.logger.domain.Logger
import com.tachyonmusic.media.CAST_PLAYER_NAME
import com.tachyonmusic.media.EXO_PLAYER_NAME
import com.tachyonmusic.media.data.ArtworkCodexImpl
import com.tachyonmusic.media.data.ArtworkLoaderImpl
import com.tachyonmusic.media.data.BrowserTree
import com.tachyonmusic.media.data.CustomPlayerImpl
import com.tachyonmusic.media.domain.ArtworkCodex
import com.tachyonmusic.media.domain.ArtworkLoader
import com.tachyonmusic.media.domain.CustomPlayer
import com.tachyonmusic.media.domain.use_case.*
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ServiceComponent
import dagger.hilt.android.scopes.ServiceScoped
import dagger.hilt.components.SingletonComponent
import javax.inject.Named
import javax.inject.Singleton

@Module
@InstallIn(ServiceComponent::class)
class MediaPlaybackServiceRepositoryModule {

    @Provides
    @ServiceScoped
    fun provideCastContext(service: Service): CastContext =
        CastContext.getSharedInstance(service)

    @Provides
    @ServiceScoped
    fun provideBrowserTree(
        songRepository: SongRepository,
        loopRepository: LoopRepository,
        playlistRepository: PlaylistRepository
    ): BrowserTree = BrowserTree(songRepository, loopRepository, playlistRepository)

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
}


@Module
@InstallIn(ServiceComponent::class)
class MediaPlaybackUseCaseModule {
    @Provides
    @ServiceScoped
    fun provideServiceUseCases(
        historyRepository: HistoryRepository,
        settingsRepository: SettingsRepository,
        @Named(EXO_PLAYER_NAME) player: CustomPlayer,
        songRepository: SongRepository,
        loopRepository: LoopRepository,
        findPlaybackByMediaId: FindPlaybackByMediaId,
        dataRepository: DataRepository,
        getOrLoadArtwork: GetOrLoadArtwork
    ) = ServiceUseCases(
        LoadPlaylistForPlayback(
            songRepository,
            loopRepository,
            settingsRepository,
            getOrLoadArtwork
        ),
        ConfirmAddedMediaItems(songRepository, loopRepository, findPlaybackByMediaId),
        PreparePlayer(player),
        GetSupportedCommands(),
        UpdateTimingDataOfCurrentPlayback(player),
        AddNewPlaybackToHistory(historyRepository, settingsRepository),
        SaveRecentlyPlayed(dataRepository)
    )
}


@Module
@InstallIn(SingletonComponent::class)
class MediaPlaybackSingletonRepositoryModule {
    @Provides
    @Singleton
    fun provideArtworkFetcher() = ArtworkFetcher()

    @Provides
    @Singleton
    internal fun provideArtworkLoader(artworkFetcher: ArtworkFetcher, log: Logger): ArtworkLoader =
        ArtworkLoaderImpl(artworkFetcher, log)

    @Provides
    @Singleton
    fun provideArtworkCodex(
        artworkLoader: ArtworkLoader,
        log: Logger
    ): ArtworkCodex = ArtworkCodexImpl(artworkLoader, log)
}