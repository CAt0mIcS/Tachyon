package com.tachyonmusic.media.di

import android.app.Service
import android.content.Context
import androidx.media3.cast.CastPlayer
import com.google.android.gms.cast.framework.CastContext
import com.tachyonmusic.artworkfetcher.ArtworkFetcher
import com.tachyonmusic.core.domain.SongMetadataExtractor
import com.tachyonmusic.database.domain.repository.DataRepository
import com.tachyonmusic.database.domain.repository.HistoryRepository
import com.tachyonmusic.database.domain.repository.LoopRepository
import com.tachyonmusic.database.domain.repository.PlaylistRepository
import com.tachyonmusic.database.domain.repository.SettingsRepository
import com.tachyonmusic.database.domain.repository.SongRepository
import com.tachyonmusic.database.domain.use_case.FindPlaybackByMediaId
import com.tachyonmusic.logger.domain.Logger
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
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.android.scopes.ServiceScoped
import dagger.hilt.components.SingletonComponent
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
    fun provideCastPlayer(context: CastContext): CustomPlayer =
        CustomPlayerImpl(CastPlayer(context))
}


@Module
@InstallIn(SingletonComponent::class)
class MediaPlaybackUseCaseModule {
    @Provides
    @Singleton
    fun provideGetPlaylistForPlaybackUseCase(
        songRepository: SongRepository,
        loopRepository: LoopRepository,
        settingsRepository: SettingsRepository,
        getOrLoadArtwork: GetOrLoadArtwork
    ) = GetPlaylistForPlayback(
        songRepository,
        loopRepository,
        settingsRepository,
        getOrLoadArtwork
    )

    @Provides
    @Singleton
    fun provideConfirmAddedMediaItemsUseCase(
        songRepository: SongRepository,
        loopRepository: LoopRepository, findPlaybackByMediaId: FindPlaybackByMediaId
    ) = ConfirmAddedMediaItems(songRepository, loopRepository, findPlaybackByMediaId)

    @Provides
    @Singleton
    fun provideAddNewPlaybackToHistoryUseCase(
        historyRepository: HistoryRepository,
        settingsRepository: SettingsRepository
    ) = AddNewPlaybackToHistory(historyRepository, settingsRepository)

    @Provides
    @Singleton
    fun provideSaveRecentlyPlayedUseCase(dataRepository: DataRepository) =
        SaveRecentlyPlayed(dataRepository)

    @Provides
    @Singleton
    fun provideGetSettingsUseCase(settingsRepository: SettingsRepository) =
        GetSettings(settingsRepository)

    @Provides
    @Singleton
    fun provideGetIsInternetConnectionMetered(@ApplicationContext context: Context) =
        GetIsInternetConnectionMetered(context)
}


@Module
@InstallIn(SingletonComponent::class)
class MediaPlaybackSingletonRepositoryModule {
    @Provides
    @Singleton
    fun provideArtworkFetcher() = ArtworkFetcher()

    @Provides
    @Singleton
    internal fun provideArtworkLoader(
        artworkFetcher: ArtworkFetcher,
        log: Logger,
        metadataExtractor: SongMetadataExtractor
    ): ArtworkLoader = ArtworkLoaderImpl(artworkFetcher, log, metadataExtractor)

    @Provides
    @Singleton
    fun provideArtworkCodex(
        artworkLoader: ArtworkLoader,
        log: Logger
    ): ArtworkCodex = ArtworkCodexImpl(artworkLoader, log)
}