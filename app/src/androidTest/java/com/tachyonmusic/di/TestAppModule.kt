package com.tachyonmusic.di

import android.app.Application
import com.tachyonmusic.user.data.LocalCache
import com.tachyonmusic.user.data.repository.FileRepositoryImpl
import com.tachyonmusic.user.data.repository.FirebaseRepository
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
    fun provideUserRepository(localCache: LocalCache): UserRepository =
        FirebaseRepository(FileRepositoryImpl(), localCache)

    @Provides
    @Singleton
    fun provideLocalCache(app: Application) = LocalCache(app)
}