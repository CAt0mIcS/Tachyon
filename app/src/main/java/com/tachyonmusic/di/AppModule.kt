package com.tachyonmusic.di

import android.content.Context
import com.tachyonmusic.core.domain.SongMetadataExtractor
import com.tachyonmusic.data.repository.FileRepositoryImpl
import com.tachyonmusic.data.repository.MediaPlaybackServiceMediaBrowserController
import com.tachyonmusic.data.repository.UriPermissionRepositoryImpl
import com.tachyonmusic.database.domain.repository.*
import com.tachyonmusic.database.domain.use_case.FindPlaybackByMediaId
import com.tachyonmusic.domain.repository.FileRepository
import com.tachyonmusic.domain.repository.MediaBrowserController
import com.tachyonmusic.domain.repository.UriPermissionRepository
import com.tachyonmusic.domain.use_case.*
import com.tachyonmusic.domain.use_case.authentication.RegisterUser
import com.tachyonmusic.domain.use_case.authentication.SignInUser
import com.tachyonmusic.domain.use_case.library.AddSongToExcludedSongs
import com.tachyonmusic.domain.use_case.library.SetSortParameters
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
    fun provideItemClickedUseCase(browser: MediaBrowserController) = PlayPlayback(browser)

    @Provides
    @Singleton
    fun provideObserveSongsUseCase(
        songRepository: SongRepository,
        @ApplicationContext context: Context
    ) = ObserveSongs(songRepository, context)

    @Provides
    @Singleton
    fun provideGetSongsUseCase(
        songRepository: SongRepository,
        @ApplicationContext context: Context
    ) = GetSongs(songRepository, context)

    @Provides
    @Singleton
    fun provideUpdateSongDatabaseUseCase(
        songRepository: SongRepository,
        fileRepository: FileRepository,
        metadataExtractor: SongMetadataExtractor,
        @ApplicationContext context: Context,
        logger: Logger
    ) = UpdateSongDatabase(
        songRepository,
        fileRepository,
        metadataExtractor,
        context,
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
    fun provideGetOrLoadArtworkUseCase(
        songRepository: SongRepository,
        settingsRepository: SettingsRepository,
        artworkCodex: ArtworkCodex,
        findPlaybackByMediaId: FindPlaybackByMediaId,
        isNetworkConnectionMetered: GetIsInternetConnectionMetered,
        @ApplicationContext context: Context
    ) = GetOrLoadArtwork(
        songRepository,
        settingsRepository,
        artworkCodex,
        findPlaybackByMediaId,
        isNetworkConnectionMetered,
        context
    )

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
    fun provideObserveLoopsUseCase(
        loopRepository: LoopRepository,
        @ApplicationContext context: Context
    ) = ObserveLoops(loopRepository, context)

    @Provides
    @Singleton
    fun provideObservePlaylistsUseCase(playlistRepository: PlaylistRepository) =
        ObservePlaylists(playlistRepository)

    @Provides
    @Singleton
    fun provideGetHistoryUseCase(
        historyRepository: HistoryRepository,
        @ApplicationContext context: Context
    ) = GetHistory(historyRepository, context)

    @Provides
    @Singleton
    fun provideObserveHistoryUseCase(
        historyRepository: HistoryRepository,
        @ApplicationContext context: Context
    ) = ObserveHistory(historyRepository, context)

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
        browser: MediaBrowserController
    ) = SavePlaybackToPlaylist(playlistRepository, browser)

    @Provides
    @Singleton
    fun provideRemovePlaybackFromPlaylistUseCase(
        playlistRepository: PlaylistRepository,
        browser: MediaBrowserController
    ) = RemovePlaybackFromPlaylist(playlistRepository, browser)

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
    fun provideSetSortParametersStateUseCase(browser: MediaBrowserController) =
        SetSortParameters(browser)

    @Provides
    @Singleton
    fun provideAddSongToExcludedSongsUseCase(
        settingsRepository: SettingsRepository,
        songRepository: SongRepository,
        historyRepository: HistoryRepository,
        loopRepository: LoopRepository,
        playlistRepository: PlaylistRepository
    ) = AddSongToExcludedSongs(
        settingsRepository,
        songRepository,
        historyRepository,
        loopRepository,
        playlistRepository
    )

    @Provides
    @Singleton
    fun provideDeletePlaybackUseCase(
        loopRepository: LoopRepository,
        playlistRepository: PlaylistRepository,
        historyRepository: HistoryRepository
    ) = DeletePlayback(loopRepository, playlistRepository, historyRepository)


    @Provides
    @Singleton
    fun providePlayRecentlyPlayedUseCase(
        browser: MediaBrowserController,
        getRecentlyPlayed: GetRecentlyPlayed
    ) = PlayRecentlyPlayed(browser, getRecentlyPlayed)

    @Provides
    @Singleton
    fun provideGetMediaStatesUseCase(
        browser: MediaBrowserController,
        @ApplicationContext context: Context
    ) = GetMediaStates(browser, context)

    @Provides
    @Singleton
    fun provideSetNewTimingDataUseCase(browser: MediaBrowserController) = SetNewTimingData(browser)

    @Provides
    @Singleton
    fun provideSetRepeatModeUseCase(
        browser: MediaBrowserController,
        dataRepository: DataRepository
    ) = SetRepeatMode(browser, dataRepository)

    @Provides
    @Singleton
    fun provideLogger(): Logger = LoggerImpl()

    @Provides
    @Singleton
    fun provideGetPlaybackChildrenUseCase(
        browser: MediaBrowserController,
        getPlaylistForPlayback: GetPlaylistForPlayback,
        @ApplicationContext context: Context
    ) = GetPlaybackChildren(browser, getPlaylistForPlayback, context)

    @Provides
    @Singleton
    fun provideObserveUriPermissionsUseCase(repository: UriPermissionRepository) =
        OnUriPermissionsChanged(repository)

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
    fun provideMediaBrowserController(): MediaBrowserController =
        MediaPlaybackServiceMediaBrowserController()

    @Provides
    @Singleton
    fun provideUriPermissionRepository(): UriPermissionRepository = UriPermissionRepositoryImpl()
}
