package com.tachyonmusic.database.di

import android.app.Application
import androidx.room.Room
import com.tachyonmusic.database.data.data_source.*
import com.tachyonmusic.database.data.data_source.room.RoomDatabase
import com.tachyonmusic.database.data.repository.*
import com.tachyonmusic.database.domain.repository.*
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
        Database.NAME
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
    fun provideCustomizedSongRepository(
        database: Database,
    ): CustomizedSongRepository = RoomCustomizedSongRepository(database.customizedSongDao)

    @Provides
    @Singleton
    fun providePlaylistRepository(database: Database): PlaylistRepository =
        RoomPlaylistRepository(database.playlistDao)

    @Provides
    @Singleton
    fun provideHistoryRepository(database: Database): HistoryRepository =
        RoomHistoryRepository(database.historyDao)

    @Provides
    @Singleton
    fun provideDataRepository(
        database: Database,
    ): DataRepository = RoomDataRepository(database.dataDao)
}


@Module
@InstallIn(SingletonComponent::class)
object DatabaseUseCaseModule {

}