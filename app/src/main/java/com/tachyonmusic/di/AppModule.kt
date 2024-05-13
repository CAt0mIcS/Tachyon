package com.tachyonmusic.di

import android.app.Application
import android.content.Context
import com.tachyonmusic.TachyonApplication
import com.tachyonmusic.core.domain.SongMetadataExtractor
import com.tachyonmusic.data.repository.FileRepositoryImpl
import com.tachyonmusic.data.repository.MediaPlaybackServiceMediaBrowserController
import com.tachyonmusic.data.repository.StateRepository
import com.tachyonmusic.database.data.data_source.Database
import com.tachyonmusic.database.domain.repository.CustomizedSongRepository
import com.tachyonmusic.database.domain.repository.DataRepository
import com.tachyonmusic.database.domain.repository.HistoryRepository
import com.tachyonmusic.database.domain.repository.PlaylistRepository
import com.tachyonmusic.database.domain.repository.SettingsRepository
import com.tachyonmusic.database.domain.repository.SongRepository
import com.tachyonmusic.domain.repository.FileRepository
import com.tachyonmusic.domain.repository.MediaBrowserController
import com.tachyonmusic.domain.repository.StateRepositoryImpl
import com.tachyonmusic.domain.use_case.DeletePlayback
import com.tachyonmusic.domain.use_case.GetRecentlyPlayed
import com.tachyonmusic.domain.use_case.LoadArtworkForPlayback
import com.tachyonmusic.domain.use_case.PlayPlayback
import com.tachyonmusic.domain.use_case.RegisterNewUriPermission
import com.tachyonmusic.domain.use_case.authentication.RegisterUser
import com.tachyonmusic.domain.use_case.authentication.SignInUser
import com.tachyonmusic.domain.use_case.home.NormalizeCurrentPosition
import com.tachyonmusic.domain.use_case.home.UnloadArtworks
import com.tachyonmusic.domain.use_case.home.UpdateSettingsDatabase
import com.tachyonmusic.domain.use_case.home.UpdateSongDatabase
import com.tachyonmusic.domain.use_case.library.AddSongToExcludedSongs
import com.tachyonmusic.domain.use_case.library.AssignArtworkToPlayback
import com.tachyonmusic.domain.use_case.library.QueryArtworkForPlayback
import com.tachyonmusic.domain.use_case.library.UpdatePlaybackMetadata
import com.tachyonmusic.domain.use_case.player.CreateAndSaveNewPlaylist
import com.tachyonmusic.domain.use_case.player.CreateCustomizedSong
import com.tachyonmusic.domain.use_case.player.GetCurrentPosition
import com.tachyonmusic.domain.use_case.player.GetPlaybackChildren
import com.tachyonmusic.domain.use_case.player.PauseResumePlayback
import com.tachyonmusic.domain.use_case.player.RemovePlaybackFromPlaylist
import com.tachyonmusic.domain.use_case.player.SavePlaybackToPlaylist
import com.tachyonmusic.domain.use_case.player.SeekToPosition
import com.tachyonmusic.domain.use_case.profile.ExportDatabase
import com.tachyonmusic.domain.use_case.profile.ImportDatabase
import com.tachyonmusic.domain.use_case.profile.WriteSettings
import com.tachyonmusic.logger.LoggerImpl
import com.tachyonmusic.logger.data.ConsoleLogger
import com.tachyonmusic.logger.data.ConsoleUiTextLogger
import com.tachyonmusic.logger.domain.Logger
import com.tachyonmusic.media.domain.AudioEffectController
import com.tachyonmusic.media.domain.use_case.AddNewPlaybackToHistory
import com.tachyonmusic.media.domain.use_case.SearchStoredPlaybacks
import com.tachyonmusic.playback_layers.domain.ArtworkCodex
import com.tachyonmusic.playback_layers.domain.ArtworkLoader
import com.tachyonmusic.playback_layers.domain.GetPlaylistForPlayback
import com.tachyonmusic.playback_layers.domain.PlaybackRepository
import com.tachyonmusic.playback_layers.domain.PredefinedPlaylistsRepository
import com.tachyonmusic.playback_layers.domain.UriPermissionRepository
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
        assignArtworkToPlayback: AssignArtworkToPlayback,
        stateRepository: StateRepository,
        logger: Logger
    ) = UpdateSongDatabase(
        songRepository,
        fileRepository,
        metadataExtractor,
        artworkCodex,
        assignArtworkToPlayback,
        stateRepository,
        logger
    )

    @Provides
    @Singleton
    fun provideUpdatePlaybackMetadataUseCase(
        songRepository: SongRepository,
        customizedSongRepository: CustomizedSongRepository,
        playlistRepository: PlaylistRepository
    ) = UpdatePlaybackMetadata(songRepository, customizedSongRepository, playlistRepository)

    @Provides
    @Singleton
    fun provideUpdateSettingsDatabaseUseCase(
        settingsRepository: SettingsRepository,
        @ApplicationContext context: Context
    ) = UpdateSettingsDatabase(settingsRepository, context)

    @Provides
    @Singleton
    fun provideWriteSettingsUseCase(settingsRepository: SettingsRepository) =
        WriteSettings(settingsRepository)

    @Provides
    @Singleton
    fun provideExportDatabaseUseCase(database: Database, @ApplicationContext context: Context) =
        ExportDatabase(database, context)

    @Provides
    @Singleton
    fun provideImportDatabaseUseCase(
        database: Database,
        @ApplicationContext context: Context,
        settingsRepository: SettingsRepository,
        uriPermissionRepository: UriPermissionRepository
    ) = ImportDatabase(database, context, settingsRepository, uriPermissionRepository)

    @Provides
    @Singleton
    fun provideUnloadArtworksUseCase(
        songRepository: SongRepository,
        assignArtworkToPlayback: AssignArtworkToPlayback
    ) = UnloadArtworks(songRepository, assignArtworkToPlayback)

    @Provides
    @Singleton
    fun provideLoadArtworkForPlaybackUseCase(
        metadataExtractor: SongMetadataExtractor,
        logger: Logger
    ) = LoadArtworkForPlayback(metadataExtractor, logger)

    @Provides
    @Singleton
    fun provideSearchStoredPlaybacksUseCase(
        predefinedPlaylistsRepository: PredefinedPlaylistsRepository,
        playbackRepository: PlaybackRepository
    ) = SearchStoredPlaybacks(predefinedPlaylistsRepository, playbackRepository)


    @Provides
    @Singleton
    fun provideCreateNewCustomizedSongUseCase(audioEffectController: AudioEffectController) =
        CreateCustomizedSong(audioEffectController)

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
        songRepository: SongRepository
    ) = AddSongToExcludedSongs(settingsRepository, songRepository)

    @Provides
    @Singleton
    fun provideDeletePlaybackUseCase(
        customizedSongRepository: CustomizedSongRepository,
        playlistRepository: PlaylistRepository,
        playbackRepository: PlaybackRepository,
        historyRepository: HistoryRepository
    ) = DeletePlayback(
        customizedSongRepository,
        playlistRepository,
        playbackRepository,
        historyRepository
    )


    @Provides
    @Singleton
    fun provideGetPlaylistForPlaybackUseCase(
        predefinedPlaylistsRepository: PredefinedPlaylistsRepository,
        artworkCodex: ArtworkCodex
    ) = GetPlaylistForPlayback(predefinedPlaylistsRepository, artworkCodex)

    @Provides
    @Singleton
    fun providePlayPlaybackUseCase(
        browser: MediaBrowserController,
        getPlaylistForPlayback: GetPlaylistForPlayback,
        addNewPlaybackToHistory: AddNewPlaybackToHistory,
        logger: Logger
    ) = PlayPlayback(
        browser,
        getPlaylistForPlayback,
        addNewPlaybackToHistory,
        logger
    )

    @Provides
    @Singleton
    fun provideGetPlaybackChildrenUseCase(
        browser: MediaBrowserController,
        predefinedPlaylistsRepository: PredefinedPlaylistsRepository,
        logger: Logger
    ) = GetPlaybackChildren(browser, predefinedPlaylistsRepository, logger)

    @Provides
    @Singleton
    fun provideRegisterNewUriPermissionUseCase(
        uriPermissionRepository: UriPermissionRepository,
        settingsRepository: SettingsRepository
    ) = RegisterNewUriPermission(uriPermissionRepository, settingsRepository)

    @Provides
    @Singleton
    fun provideQueryArtworkForPlayback(artworkLoader: ArtworkLoader) =
        QueryArtworkForPlayback(artworkLoader)

    @Provides
    @Singleton
    fun provideAssignArtworkToPlayback(songRepository: SongRepository) =
        AssignArtworkToPlayback(songRepository)
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
    fun provideStateRepository(logger: Logger): StateRepository = StateRepositoryImpl(logger)

    @Provides
    @Singleton
    fun provideMediaBrowserController(
        getPlaylistForPlayback: GetPlaylistForPlayback,
        predefinedPlaylistsRepository: PredefinedPlaylistsRepository,
        logger: Logger
    ): MediaBrowserController =
        MediaPlaybackServiceMediaBrowserController(
            getPlaylistForPlayback,
            predefinedPlaylistsRepository,
            logger
        )

    @Provides
    @Singleton
    fun provideLogger(@ApplicationContext context: Context): Logger = LoggerImpl(
        setOf(
            ConsoleLogger(),
            ConsoleUiTextLogger(context)
        )
    )

    @Provides
    @Singleton
    fun provideApplicationCoroutineScope(app: Application) =
        (app as TachyonApplication).coroutineScope
}
