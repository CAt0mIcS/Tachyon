package com.tachyonmusic.playback_layers.di

import android.content.Context
import com.tachyonmusic.artworkfetcher.ArtworkFetcher
import com.tachyonmusic.core.domain.SongMetadataExtractor
import com.tachyonmusic.database.domain.repository.*
import com.tachyonmusic.logger.domain.Logger
import com.tachyonmusic.playback_layers.data.*
import com.tachyonmusic.playback_layers.domain.*
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.CoroutineScope
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
class PlaybackLayerRepositoryModule {
    @Provides
    @Singleton
    fun provideArtworkFetcher() = ArtworkFetcher()


    @Provides
    @Singleton
    fun provideArtworkLoader(
        artworkFetcher: ArtworkFetcher,
        logger: Logger,
        metadataExtractor: SongMetadataExtractor
    ): ArtworkLoader = ArtworkLoaderImpl(artworkFetcher, logger, metadataExtractor)


    @Provides
    @Singleton
    fun provideArtworkCodex(artworkLoader: ArtworkLoader, logger: Logger): ArtworkCodex =
        ArtworkCodexImpl(artworkLoader, logger)

    @Provides
    @Singleton
    fun providePlaybackRepository(
        songRepository: SongRepository,
        remixRepository: RemixRepository,
        playlistRepository: PlaylistRepository,
        historyRepository: HistoryRepository,
        @ApplicationContext context: Context,
    ): PlaybackRepository = PlaybackRepositoryImpl(
        songRepository,
        remixRepository,
        playlistRepository,
        historyRepository,
        context
    )

    @Provides
    @Singleton
    fun providePredefinedPlaylistsRepository(
        playbackRepository: PlaybackRepository,
        settingsRepository: SettingsRepository,
        coroutineScope: CoroutineScope
    ): PredefinedPlaylistsRepository =
        PredefinedPlaylistsRepositoryImpl(playbackRepository, settingsRepository, coroutineScope)

    @Provides
    @Singleton
    fun provideUriPermissionRepository(@ApplicationContext context: Context): UriPermissionRepository =
        UriPermissionRepositoryImpl(context)

}


@Module
@InstallIn(SingletonComponent::class)
class PlaybackLayerUseCaseModule {
    @Provides
    @Singleton
    fun provideGetIsInternetConnectionMeteredUseCase(@ApplicationContext context: Context) =
        GetIsInternetConnectionMetered(context)

}