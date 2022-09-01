package com.tachyonmusic.di

import com.tachyonmusic.domain.MediaBrowserController
import com.tachyonmusic.domain.MediaPlaybackServiceMediaBrowserController
import com.tachyonmusic.domain.use_case.MillisecondsToReadableString
import com.tachyonmusic.domain.use_case.RegisterUser
import com.tachyonmusic.domain.use_case.SignInUser
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
    fun provideCurrentPlaybackPositionToReadableStringUseCase() =
        MillisecondsToReadableString()

    @Provides
    @Singleton
    fun provideMediaBrowserController(repository: UserRepository): MediaBrowserController =
        MediaPlaybackServiceMediaBrowserController(repository)
}

