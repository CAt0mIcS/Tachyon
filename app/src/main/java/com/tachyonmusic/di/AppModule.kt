package com.tachyonmusic.di

import android.content.Context
import com.tachyonmusic.artwork.domain.ArtworkCodex
import com.tachyonmusic.artwork.domain.ArtworkMapperRepository
import com.tachyonmusic.core.domain.SongMetadataExtractor
import com.tachyonmusic.data.repository.FileRepositoryImpl
import com.tachyonmusic.data.repository.MediaPlaybackServiceMediaBrowserController
import com.tachyonmusic.database.domain.repository.*
import com.tachyonmusic.domain.repository.FileRepository
import com.tachyonmusic.domain.repository.MediaBrowserController
import com.tachyonmusic.domain.use_case.*
import com.tachyonmusic.domain.use_case.authentication.RegisterUser
import com.tachyonmusic.domain.use_case.authentication.SignInUser
import com.tachyonmusic.domain.use_case.library.AddSongToExcludedSongs
import com.tachyonmusic.domain.use_case.main.*
import com.tachyonmusic.domain.use_case.player.*
import com.tachyonmusic.domain.use_case.profile.WriteSettings
import com.tachyonmusic.domain.use_case.search.SearchStoredPlaybacks
import com.tachyonmusic.logger.domain.Logger
import com.tachyonmusic.playback_layers.PlaybackRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton


@Module
@InstallIn(SingletonComponent::class)
object AppUseCaseModule {
    @Provides
    @Singleton
    fun provideRegisterUserUseCase() = RegisterUser()

    @Provides
    @Singleton
    fun provideSignInUserUseCase() = SignInUser()

    @Provides
    @Singleton
    fun provideUpdateSongDatabaseUseCase(
        songRepository: SongRepository,
        fileRepository: FileRepository,
        metadataExtractor: SongMetadataExtractor,
        artworkCodex: ArtworkCodex,
        artworkMapperRepository: ArtworkMapperRepository,
        logger: Logger
    ) = UpdateSongDatabase(
        songRepository,
        fileRepository,
        metadataExtractor,
        artworkCodex,
        artworkMapperRepository,
        logger
    )

    @Provides
    @Singleton
    fun provideUpdateSettingsDatabaseUseCase(
        settingsRepository: SettingsRepository,
        @ApplicationContext context: Context
    ) = UpdateSettingsDatabase(settingsRepository, context)

    @Provides
    @Singleton
    fun provideObserveSettingsUseCase(settingsRepository: SettingsRepository) =
        ObserveSettings(settingsRepository)

    @Provides
    @Singleton
    fun provideWriteSettingsUseCase(settingsRepository: SettingsRepository) =
        WriteSettings(settingsRepository)

    @Provides
    @Singleton
    fun provideObserveSavedDataUseCase(dataRepository: DataRepository) =
        ObserveSavedData(dataRepository)

    @Provides
    @Singleton
    fun provideGetSavedDataUseCase(dataRepository: DataRepository) = GetSavedData(dataRepository)

    @Provides
    @Singleton
    fun provideUnloadArtworksUseCase(
        songRepository: SongRepository
    ) = UnloadArtworks(songRepository)

    @Provides
    @Singleton
    fun provideSetMusicDirectoriesUseCase(
        settingsRepository: SettingsRepository,
        @ApplicationContext context: Context
    ) = SetMusicDirectories(settingsRepository, context)

    @Provides
    @Singleton
    fun provideObserveHistoryUseCase(playbackRepository: PlaybackRepository) =
        ObserveHistory(playbackRepository)

    @Provides
    @Singleton
    fun provideSearchStoredPlaybacksUseCase(
        songRepository: SongRepository,
        loopRepository: LoopRepository,
        playlistRepository: PlaylistRepository
    ) = SearchStoredPlaybacks(songRepository, loopRepository, playlistRepository)

    @Provides
    @Singleton
    fun provideCreateNewLoopUseCase(
        songRepository: SongRepository,
        loopRepository: LoopRepository,
        browser: MediaBrowserController
    ) = CreateAndSaveNewLoop(songRepository, loopRepository, browser)

    @Provides
    @Singleton
    fun provideSavePlaybackToPlaylistUseCase(
        playlistRepository: PlaylistRepository,
        playbackRepository: PlaybackRepository
    ) = SavePlaybackToPlaylist(playlistRepository, playbackRepository)

    @Provides
    @Singleton
    fun provideRemovePlaybackFromPlaylistUseCase(
        playbackRepository: PlaybackRepository,
        playlistRepository: PlaylistRepository,
        browser: MediaBrowserController
    ) = RemovePlaybackFromPlaylist(playbackRepository, playlistRepository, browser)

    @Provides
    @Singleton
    fun provideCreateNewPlaylistUseCase(playlistRepository: PlaylistRepository) =
        CreateAndSaveNewPlaylist(playlistRepository)

    @Provides
    @Singleton
    fun providePauseResumePlaybackUseCase(browser: MediaBrowserController) =
        PauseResumePlayback(browser)

    @Provides
    @Singleton
    fun provideGetCurrentPositionUseCase(browser: MediaBrowserController) =
        GetCurrentPosition(browser)

    @Provides
    @Singleton
    fun provideNormalizeCurrentPositionUseCase(browser: MediaBrowserController) =
        NormalizeCurrentPosition(browser)

    @Provides
    @Singleton
    fun provideSeekToPositionUseCase(browser: MediaBrowserController) = SeekToPosition(browser)

    @Provides
    @Singleton
    fun provideGetRecentlyPlayedPositionUseCase(dataRepository: DataRepository) =
        GetRecentlyPlayed(dataRepository)

    @Provides
    @Singleton
    fun provideAddSongToExcludedSongsUseCase(
        settingsRepository: SettingsRepository,
        songRepository: SongRepository,
        historyRepository: HistoryRepository,
        loopRepository: LoopRepository,
        playbackRepository: PlaybackRepository,
        playlistRepository: PlaylistRepository
    ) = AddSongToExcludedSongs(
        settingsRepository,
        songRepository,
        historyRepository,
        loopRepository,
        playbackRepository,
        playlistRepository
    )

    @Provides
    @Singleton
    fun provideDeletePlaybackUseCase(
        loopRepository: LoopRepository,
        playlistRepository: PlaylistRepository,
        playbackRepository: PlaybackRepository,
        historyRepository: HistoryRepository
    ) = DeletePlayback(loopRepository, playlistRepository, playbackRepository, historyRepository)


    @Provides
    @Singleton
    fun provideGetPlaylistForPlaybackUseCase(
        settingsRepository: SettingsRepository,
        playbackRepository: PlaybackRepository,
        artworkCodex: ArtworkCodex
    ) = GetPlaylistForPlayback(settingsRepository, playbackRepository, artworkCodex)

    @Provides
    @Singleton
    fun providePlayPlaybackUseCase(
        browser: MediaBrowserController,
        getPlaylistForPlayback: GetPlaylistForPlayback,
        logger: Logger
    ) = PlayPlayback(browser, getPlaylistForPlayback, logger)

    @Provides
    @Singleton
    fun providePlayRecentlyPlayedUseCase(
        browser: MediaBrowserController,
        getRecentlyPlayed: GetRecentlyPlayed,
        playPlayback: PlayPlayback
    ) = PlayRecentlyPlayed(browser, getRecentlyPlayed, playPlayback)

    @Provides
    @Singleton
    fun provideGetMediaStatesUseCase(browser: MediaBrowserController) = GetMediaStates(browser)
}


@Module
@InstallIn(SingletonComponent::class)
object AppRepositoryModule {
    @Provides
    @Singleton
    fun provideFileRepository(@ApplicationContext context: Context): FileRepository =
        FileRepositoryImpl(context)

    @Provides
    @Singleton
    fun provideMediaBrowserController(getPlaylistForPlayback: GetPlaylistForPlayback): MediaBrowserController =
        MediaPlaybackServiceMediaBrowserController(getPlaylistForPlayback)
}
