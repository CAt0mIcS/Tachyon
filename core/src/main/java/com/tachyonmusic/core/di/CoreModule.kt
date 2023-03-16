package com.tachyonmusic.core.di

import android.content.Context
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.google.gson.ToNumberPolicy
import com.tachyonmusic.core.data.FileSongMetadataExtractor
import com.tachyonmusic.core.domain.MediaId
import com.tachyonmusic.core.domain.SongMetadataExtractor
import com.tachyonmusic.core.domain.TimingData
import com.tachyonmusic.logger.LoggerImpl
import com.tachyonmusic.logger.data.ConsoleLogger
import com.tachyonmusic.logger.data.ConsoleUiTextLogger
import com.tachyonmusic.logger.domain.Logger
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
class CoreModule {

    @Provides
    @Singleton
    fun provideGSON(): Gson = GsonBuilder().apply {
        registerTypeAdapter(MediaId::class.java, MediaId.Serializer())
        registerTypeAdapter(TimingData::class.java, TimingData.Serializer())
        setObjectToNumberStrategy(ToNumberPolicy.LONG_OR_DOUBLE)
    }.create()

    @Provides
    @Singleton
    fun provideSongMetadataExtractor(
        @ApplicationContext context: Context,
        logger: Logger
    ): SongMetadataExtractor = FileSongMetadataExtractor(context.contentResolver, logger)

    @Provides
    @Singleton
    fun provideLogger(@ApplicationContext context: Context): Logger = LoggerImpl(
        listOf(
            ConsoleLogger(),
            ConsoleUiTextLogger(context)
        )
    )
}