package com.tachyonmusic.di

import android.app.Application
import com.daton.database.domain.repository.SettingsRepository
import com.daton.database.domain.repository.SongRepository
import com.tachyonmusic.data.repository.FileRepositoryImpl
import com.tachyonmusic.data.repository.MediaPlaybackServiceMediaBrowserController
import com.tachyonmusic.domain.repository.FileRepository
import com.tachyonmusic.domain.repository.MediaBrowserController
import com.tachyonmusic.domain.use_case.*
import com.tachyonmusic.domain.use_case.authentication.RegisterUser
import com.tachyonmusic.domain.use_case.authentication.SignInUser
import com.tachyonmusic.domain.use_case.main.*
import com.tachyonmusic.domain.use_case.player.*
import com.tachyonmusic.domain.use_case.search.SearchStoredPlaybacks
import com.tachyonmusic.user.domain.UserRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton


@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides
    @Singleton
    fun provideRegisterUserUseCase(repository: UserRepository) = RegisterUser(repository)

    @Provides
    @Singleton
    fun provideSignInUserUseCase(repository: UserRepository) = SignInUser(repository)

    @Provides
    @Singleton
    fun provideItemClickedUseCase(browser: MediaBrowserController) = ItemClicked(browser)

    @Provides
    @Singleton
    fun provideGetSongsUseCase(songRepository: SongRepository) = GetSongs(songRepository)

    @Provides
    @Singleton
    fun provideUpdateSongDatabaseUseCase(
        songRepository: SongRepository,
        settingsRepository: SettingsRepository,
        fileRepository: FileRepository
    ) = UpdateSongDatabase(songRepository, settingsRepository, fileRepository)

    @Provides
    @Singleton
    fun provideUpdateSettingsDatabaseUseCase(settingsRepository: SettingsRepository) =
        UpdateSettingsDatabase(settingsRepository)

    @Provides
    @Singleton
    fun provideFileRepository(): FileRepository = FileRepositoryImpl()

    @Provides
    @Singleton
    fun provideGetLoopsUseCase(userRepository: UserRepository) = GetLoops(userRepository)

    @Provides
    @Singleton
    fun provideGetPlaylistsUseCase(userRepository: UserRepository) = GetPlaylists(userRepository)

    @Provides
    @Singleton
    fun provideGetHistoryUseCase(userRepository: UserRepository) = GetHistory(userRepository)

    @Provides
    @Singleton
    fun provideSearchStoredPlaybacksUseCase(
        userRepository: UserRepository,
        songRepository: SongRepository
    ) = SearchStoredPlaybacks(userRepository, songRepository)

    @Provides
    @Singleton
    fun provideCreateNewLoopUseCase(userRepo: UserRepository, browser: MediaBrowserController) =
        CreateAndSaveNewLoop(userRepo, browser)

    @Provides
    @Singleton
    fun provideMillisecondsToReadableStringUseCase() = MillisecondsToReadableString()

    @Provides
    @Singleton
    fun providePauseResumePlaybackUseCase(browser: MediaBrowserController) =
        PauseResumePlayback(browser)

    @Provides
    fun providePlayerListenerHandlerUseCase(browser: MediaBrowserController) =
        PlayerListenerHandler(browser)

    @Provides
    @Singleton
    fun provideGetCurrentPositionUseCase(browser: MediaBrowserController) =
        GetCurrentPosition(browser)

    @Provides
    @Singleton
    fun provideGetCurrentPositionNormalizedUseCase(browser: MediaBrowserController) =
        GetCurrentPositionNormalized(browser)

    @Provides
    @Singleton
    fun provideGetAudioUpdateIntervalUseCase(userRepository: UserRepository) =
        GetAudioUpdateInterval(userRepository)

    @Provides
    fun provideHandlePlaybackStateUseCase(browser: MediaBrowserController) =
        HandlePlaybackState(browser)

    @Provides
    fun provideHandleArtworkStateUseCase(browser: MediaBrowserController) =
        HandleArtworkState(browser)

    @Provides
    fun provideHandleLoopStateUseCase(browser: MediaBrowserController) = HandleLoopState(browser)

    @Provides
    @Singleton
    fun provideSeekToPositionUseCase(browser: MediaBrowserController) = SeekToPosition(browser)

    @Provides
    @Singleton
    fun provideSetCurrentPlayback(browser: MediaBrowserController) = SetCurrentPlayback(browser)

    @Provides
    @Singleton
    fun provideMediaBrowserController(): MediaBrowserController =
        MediaPlaybackServiceMediaBrowserController()
}

