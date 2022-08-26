package com.tachyonmusic.user.di

import com.tachyonmusic.user.data.FileRepositoryImpl
import com.tachyonmusic.user.domain.FileRepository
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
    fun provideFileRepository(): FileRepository = FileRepositoryImpl()
}