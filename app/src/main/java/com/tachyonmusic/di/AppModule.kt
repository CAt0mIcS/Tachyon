package com.tachyonmusic.di

import com.tachyonmusic.domain.repository.MediaBrowserController
import com.tachyonmusic.data.repository.MediaPlaybackServiceMediaBrowserController
import com.tachyonmusic.domain.use_case.player.MillisecondsToReadableString
import com.tachyonmusic.domain.use_case.authentication.RegisterUser
import com.tachyonmusic.domain.use_case.authentication.SignInUser
import com.tachyonmusic.domain.use_case.main.ItemClicked
import com.tachyonmusic.domain.use_case.player.CreateNewLoop
import com.tachyonmusic.domain.use_case.player.PlayerUseCases
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
    fun providePlayerUseCases(repository: UserRepository) = PlayerUseCases(
        CreateNewLoop(repository),
        MillisecondsToReadableString()
    )

    @Provides
    @Singleton
    fun provideMediaBrowserController(repository: UserRepository): MediaBrowserController =
        MediaPlaybackServiceMediaBrowserController(repository)
}

