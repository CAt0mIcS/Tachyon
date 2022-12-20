package com.tachyonmusic.database.di

import android.app.Application
import androidx.room.Room
import com.daton.database.data.data_source.Database
import com.daton.database.data.data_source.room.RoomDatabase
import com.daton.database.data.repository.RoomDataRepository
import com.daton.database.data.repository.RoomHistoryRepository
import com.daton.database.data.repository.RoomLoopRepository
import com.daton.database.data.repository.RoomPlaylistRepository
import com.daton.database.data.repository.RoomSettingsRepository
import com.daton.database.data.repository.RoomSongRepository
import com.daton.database.domain.repository.DataRepository
import com.daton.database.domain.repository.HistoryRepository
import com.daton.database.domain.repository.LoopRepository
import com.daton.database.domain.repository.PlaylistRepository
import com.daton.database.domain.repository.SettingsRepository
import com.daton.database.domain.repository.SongRepository
import com.daton.database.domain.use_case.FindPlaybackByMediaId
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
class DatabaseTestModule {
    @Provides
    @Singleton
    fun provideDatabase(app: Application): Database = Room.inMemoryDatabaseBuilder(
        app,
        RoomDatabase::class.java,
    ).build()

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
        songRepository: SongRepository,
        loopRepository: LoopRepository,
        playlistRepository: PlaylistRepository,
    ): HistoryRepository =
        RoomHistoryRepository(
            database.historyDao,
            FindPlaybackByMediaId(
                songRepository,
                loopRepository,
                playlistRepository
            ),
            songRepository,
            loopRepository
        )

    @Provides
    @Singleton
    fun provideDataRepository(
        database: Database,
    ): DataRepository = RoomDataRepository(database.dataDao)
}