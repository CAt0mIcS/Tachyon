package com.tachyonmusic.di

import com.tachyonmusic.user.data.repository.FirebaseRepository
import com.tachyonmusic.user.data.repository.TestFileRepository
import com.tachyonmusic.user.domain.UserRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object TestAppModule {
    @Provides
    @Singleton
    fun provideUserRepository(): UserRepository = FirebaseRepository(TestFileRepository())
}