package com.tachyonmusic.di

import com.tachyonmusic.data.repository.MediaPlaybackServiceMediaBrowserController
import com.tachyonmusic.domain.repository.MediaBrowserController
import com.tachyonmusic.domain.use_case.*
import com.tachyonmusic.domain.use_case.authentication.RegisterUser
import com.tachyonmusic.domain.use_case.authentication.SignInUser
import com.tachyonmusic.domain.use_case.main.*
import com.tachyonmusic.domain.use_case.player.CreateNewLoop
import com.tachyonmusic.domain.use_case.player.MillisecondsToReadableString
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
    fun provideGetSongsUseCase(userRepository: UserRepository) = GetSongs(userRepository)

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
    fun provideLoadAlbumArtUseCase() = LoadPlaybackArtwork()

    @Provides
    @Singleton
    fun provideSearchStoredPlaybacksUseCase(userRepository: UserRepository) =
        SearchStoredPlaybacks(userRepository)

    @Provides
    @Singleton
    fun provideCreateNewLoopUseCase(userRepo: UserRepository, browser: MediaBrowserController) =
        CreateNewLoop(userRepo, browser)

    @Provides
    @Singleton
    fun provideMillisecondsToReadableStringUseCase() = MillisecondsToReadableString()

    @Provides
    @Singleton
    fun provideMediaBrowserController(repository: UserRepository): MediaBrowserController =
        MediaPlaybackServiceMediaBrowserController(repository)
}

