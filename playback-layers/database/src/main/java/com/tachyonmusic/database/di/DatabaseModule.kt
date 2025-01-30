package com.tachyonmusic.database.di

import android.app.Application
import androidx.room.Room
import com.tachyonmusic.database.data.data_source.Database
import com.tachyonmusic.database.data.data_source.room.RoomDatabase
import com.tachyonmusic.database.data.repository.RoomDataRepository
import com.tachyonmusic.database.data.repository.RoomHistoryRepository
import com.tachyonmusic.database.data.repository.RoomPlaylistRepository
import com.tachyonmusic.database.data.repository.RoomRemixRepository
import com.tachyonmusic.database.data.repository.RoomSettingsRepository
import com.tachyonmusic.database.data.repository.RoomSongRepository
import com.tachyonmusic.database.domain.repository.DataRepository
import com.tachyonmusic.database.domain.repository.HistoryRepository
import com.tachyonmusic.database.domain.repository.PlaylistRepository
import com.tachyonmusic.database.domain.repository.RemixRepository
import com.tachyonmusic.database.domain.repository.SettingsRepository
import com.tachyonmusic.database.domain.repository.SongRepository
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
    ).addMigrations().build()
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
    fun provideRemixRepository(
        database: Database,
    ): RemixRepository = RoomRemixRepository(database.remixDao)

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