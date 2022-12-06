package com.tachyonmusic.user.di

import android.app.Application
import com.google.gson.Gson
import com.tachyonmusic.user.data.LocalCache
import com.tachyonmusic.user.data.repository.FileRepositoryImpl
import com.tachyonmusic.user.data.repository.FirebaseRepository
import com.tachyonmusic.user.domain.FileRepository
import com.tachyonmusic.user.domain.UserRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object UserModule {

    @Provides
    @Singleton
    fun provideUserRepository(
        fileRepository: FileRepository,
        localCache: LocalCache,
        gson: Gson
    ): UserRepository =
        FirebaseRepository(fileRepository, localCache, gson)

    @Provides
    @Singleton
    fun provideFileRepository(): FileRepository = FileRepositoryImpl()

    @Provides
    @Singleton
    fun provideLocalCache(app: Application, gson: Gson): LocalCache = LocalCache(app, gson)
}