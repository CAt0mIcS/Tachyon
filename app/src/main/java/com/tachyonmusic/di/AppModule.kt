package com.tachyonmusic.di

import com.tachyonmusic.domain.use_case.RegisterUser
import com.tachyonmusic.domain.use_case.SignInUser
import com.tachyonmusic.user.data.FirebaseRepository
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
    fun provideUserRepository(): UserRepository = FirebaseRepository()

    @Provides
    @Singleton
    fun provideRegisterUserUseCase(repository: UserRepository) = RegisterUser(repository)

    @Provides
    @Singleton
    fun provideSignInUserUseCase(repository: UserRepository) = SignInUser(repository)
}