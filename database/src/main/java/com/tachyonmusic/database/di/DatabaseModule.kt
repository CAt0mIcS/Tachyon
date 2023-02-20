package com.tachyonmusic.database.di

import android.app.Application
import androidx.room.Room
import com.tachyonmusic.database.data.data_source.*
import com.tachyonmusic.database.data.data_source.room.RoomDatabase
import com.tachyonmusic.database.data.repository.*
import com.tachyonmusic.database.domain.repository.*
import com.tachyonmusic.database.domain.use_case.ConvertHistoryEntityToPlayback
import com.tachyonmusic.database.domain.use_case.FindPlaybackByMediaId
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {
    @Provides
    @Singleton
    fun provideDatabase(app: Application): Database = Room.databaseBuilder(
        app,
        RoomDatabase::class.java,
        RoomDatabase::class.java.name
    ).build()
}

@Module
@InstallIn(SingletonComponent::class)
object DatabaseRepositoryModule {

    @Provides
    @Singleton
    fun provideSettingsRepository(database: Database): SettingsRepository =
        RoomSettingsRepository(database.settingsDao)

    @Provides
    @Singleton
    fun provideSongRepository(
        database: Database,
    ): SongRepository = RoomSongRepository(database.songDao)

    @Provides
    @Singleton
    fun provideLoopRepository(
        database: Database,
    ): LoopRepository = RoomLoopRepository(database.loopDao)

    @Provides
    @Singleton
    fun providePlaylistRepository(
        database: Database,
        songRepository: SongRepository,
        loopRepository: LoopRepository
    ): PlaylistRepository =
        RoomPlaylistRepository(database.playlistDao, songRepository, loopRepository)

    @Provides
    @Singleton
    fun provideHistoryRepository(
        database: Database,
        convertHistoryEntityToPlayback: ConvertHistoryEntityToPlayback
    ): HistoryRepository =
        RoomHistoryRepository(database.historyDao, convertHistoryEntityToPlayback)

    @Provides
    @Singleton
    fun provideDataRepository(
        database: Database,
    ): DataRepository = RoomDataRepository(database.dataDao)
}


@Module
@InstallIn(SingletonComponent::class)
object DatabaseUseCaseModule {

    @Provides
    @Singleton
    fun provideFindPlaybackByMediaIdUseCase(
        songRepository: SongRepository,
        loopRepository: LoopRepository,
        playlistRepository: PlaylistRepository
    ) = FindPlaybackByMediaId(songRepository, loopRepository, playlistRepository)

    @Provides
    @Singleton
    fun provideConvertHistoryEntityToPlaybackUseCase(findPlaybackByMediaId: FindPlaybackByMediaId) =
        ConvertHistoryEntityToPlayback(findPlaybackByMediaId)
}