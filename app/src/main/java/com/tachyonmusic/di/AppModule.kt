package com.tachyonmusic.di

import com.tachyonmusic.core.domain.SongMetadataExtractor
import com.tachyonmusic.data.repository.FileRepositoryImpl
import com.tachyonmusic.data.repository.MediaPlaybackServiceMediaBrowserController
import com.tachyonmusic.database.domain.repository.*
import com.tachyonmusic.database.domain.use_case.FindPlaybackByMediaId
import com.tachyonmusic.domain.repository.FileRepository
import com.tachyonmusic.domain.repository.MediaBrowserController
import com.tachyonmusic.domain.use_case.*
import com.tachyonmusic.domain.use_case.authentication.RegisterUser
import com.tachyonmusic.domain.use_case.authentication.SignInUser
import com.tachyonmusic.domain.use_case.main.*
import com.tachyonmusic.domain.use_case.player.*
import com.tachyonmusic.domain.use_case.profile.WriteSettings
import com.tachyonmusic.domain.use_case.search.SearchStoredPlaybacks
import com.tachyonmusic.logger.LoggerImpl
import com.tachyonmusic.logger.domain.Logger
import com.tachyonmusic.media.domain.ArtworkCodex
import com.tachyonmusic.media.domain.use_case.GetIsInternetConnectionMetered
import com.tachyonmusic.media.domain.use_case.GetOrLoadArtwork
import com.tachyonmusic.media.domain.use_case.GetPlaylistForPlayback
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
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
    fun provideItemClickedUseCase(browser: MediaBrowserController) = PlayPlayback(browser)

    @Provides
    @Singleton
    fun provideObserveSongsUseCase(songRepository: SongRepository) = ObserveSongs(songRepository)

    @Provides
    @Singleton
    fun provideGetSongsUseCase(songRepository: SongRepository) = GetSongs(songRepository)

    @Provides
    @Singleton
    fun provideUpdateSongDatabaseUseCase(
        songRepository: SongRepository,
        settingsRepository: SettingsRepository,
        fileRepository: FileRepository,
        metadataExtractor: SongMetadataExtractor
    ) = UpdateSongDatabase(songRepository, settingsRepository, fileRepository, metadataExtractor)

    @Provides
    @Singleton
    fun provideUpdateSettingsDatabaseUseCase(settingsRepository: SettingsRepository) =
        UpdateSettingsDatabase(settingsRepository)

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
    fun provideGetOrLoadArtworkUseCase(
        songRepository: SongRepository,
        settingsRepository: SettingsRepository,
        artworkCodex: ArtworkCodex,
        findPlaybackByMediaId: FindPlaybackByMediaId,
        isNetworkConnectionMetered: GetIsInternetConnectionMetered
    ) = GetOrLoadArtwork(
        songRepository,
        settingsRepository,
        artworkCodex,
        findPlaybackByMediaId,
        isNetworkConnectionMetered
    )

    @Provides
    @Singleton
    fun provideUnloadArtworksUseCase(
        songRepository: SongRepository
    ) = UnloadArtworks(songRepository)

    @Provides
    @Singleton
    fun provideObserveLoopsUseCase(loopRepository: LoopRepository) = ObserveLoops(loopRepository)

    @Provides
    @Singleton
    fun provideObservePlaylistsUseCase(playlistRepository: PlaylistRepository) =
        ObservePlaylists(playlistRepository)

    @Provides
    @Singleton
    fun provideGetHistoryUseCase(historyRepository: HistoryRepository) =
        GetHistory(historyRepository)

    @Provides
    @Singleton
    fun provideObserveHistoryUseCase(historyRepository: HistoryRepository) =
        ObserveHistory(historyRepository)

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
    fun provideSavePlaybackToPlaylistUseCase(playlistRepository: PlaylistRepository) =
        SavePlaybackToPlaylist(playlistRepository)

    @Provides
    @Singleton
    fun provideRemovePlaybackFromPlaylistUseCase(playlistRepository: PlaylistRepository) =
        RemovePlaybackFromPlaylist(playlistRepository)

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
    fun provideSetCurrentPlaybackUseCase(browser: MediaBrowserController) =
        SetCurrentPlayback(browser)

    @Provides
    @Singleton
    fun provideGetRecentlyPlayedPositionUseCase(dataRepository: DataRepository) =
        GetRecentlyPlayed(dataRepository)

    @Provides
    @Singleton
    fun provideGetAssociatedPlaylistStateUseCase(browser: MediaBrowserController) =
        GetAssociatedPlaylistState(browser)

    @Provides
    @Singleton
    fun providePlayRecentlyPlayedUseCase(
        browser: MediaBrowserController,
        getRecentlyPlayed: GetRecentlyPlayed
    ) = PlayRecentlyPlayed(browser, getRecentlyPlayed)

    @Provides
    @Singleton
    fun provideGetIsPlayingStateUseCase(browser: MediaBrowserController) =
        GetIsPlayingState(browser)

    @Provides
    @Singleton
    fun provideGetPlaybackStateStateUseCase(browser: MediaBrowserController) =
        GetCurrentPlaybackState(browser)

    @Provides
    @Singleton
    fun provideGetTimingDataStateUseCase(browser: MediaBrowserController) =
        GetTimingDataState(browser)

    @Provides
    @Singleton
    fun provideSetNewTimingDataUseCase(browser: MediaBrowserController) = SetNewTimingData(browser)

    @Provides
    @Singleton
    fun provideSetRepeatModeUseCase(browser: MediaBrowserController) = SetRepeatMode(browser)

    @Provides
    @Singleton
    fun provideLogger(): Logger = LoggerImpl()

    @Provides
    @Singleton
    fun provideGetPlaybackChildrenUseCase(
        browser: MediaBrowserController,
        getPlaylistForPlayback: GetPlaylistForPlayback
    ) = GetPlaybackChildren(browser, getPlaylistForPlayback)
}


@Module
@InstallIn(SingletonComponent::class)
object AppRepositoryModule {
    @Provides
    @Singleton
    fun provideFileRepository(): FileRepository = FileRepositoryImpl()

    @Provides
    @Singleton
    fun provideMediaBrowserController(): MediaBrowserController =
        MediaPlaybackServiceMediaBrowserController()
}
