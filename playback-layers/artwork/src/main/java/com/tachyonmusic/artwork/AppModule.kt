package com.tachyonmusic.artwork

import android.content.Context
import com.tachyonmusic.artwork.data.ArtworkCodexImpl
import com.tachyonmusic.artwork.data.ArtworkLoaderImpl
import com.tachyonmusic.artwork.data.ArtworkMapperRepositoryImpl
import com.tachyonmusic.artwork.domain.ArtworkCodex
import com.tachyonmusic.artwork.domain.ArtworkLoader
import com.tachyonmusic.artwork.domain.ArtworkMapperRepository
import com.tachyonmusic.artwork.domain.GetIsInternetConnectionMetered
import com.tachyonmusic.artworkfetcher.ArtworkFetcher
import com.tachyonmusic.core.domain.SongMetadataExtractor
import com.tachyonmusic.database.domain.repository.SettingsRepository
import com.tachyonmusic.database.domain.repository.SongRepository
import com.tachyonmusic.logger.domain.Logger
import com.tachyonmusic.permission.domain.PermissionMapperRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
class AppModule {

    @Provides
    @Singleton
    fun provideArtworkLoader(
        artworkFetcher: ArtworkFetcher,
        @ApplicationContext context: Context,
        log: Logger,
        metadataExtractor: SongMetadataExtractor
    ): ArtworkLoader = ArtworkLoaderImpl(artworkFetcher, context, log, metadataExtractor)

    @Provides
    @Singleton
    fun provideArtworkCodex(artworkLoader: ArtworkLoader, log: Logger): ArtworkCodex =
        ArtworkCodexImpl(artworkLoader, log)


    @Provides
    @Singleton
    fun provideGetIsInternetConnectionMeteredUseCase(@ApplicationContext context: Context) =
        GetIsInternetConnectionMetered(context)


    @Provides
    @Singleton
    fun provideArtworkMapperRepository(
        permissionMapperRepository: PermissionMapperRepository,
        songRepository: SongRepository,
        settingsRepository: SettingsRepository,
        artworkCodex: ArtworkCodex,
        isInternetConnectionMetered: GetIsInternetConnectionMetered
    ): ArtworkMapperRepository = ArtworkMapperRepositoryImpl(
        permissionMapperRepository,
        songRepository,
        settingsRepository,
        artworkCodex,
        isInternetConnectionMetered
    )
}