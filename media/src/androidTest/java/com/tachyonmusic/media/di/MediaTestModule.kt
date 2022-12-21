package com.tachyonmusic.media.di

import android.app.Application
import androidx.room.Room
import com.tachyonmusic.database.data.data_source.Database
import com.tachyonmusic.database.data.data_source.room.RoomDatabase
import com.tachyonmusic.database.data.repository.RoomLoopRepository
import com.tachyonmusic.database.data.repository.RoomPlaylistRepository
import com.tachyonmusic.database.data.repository.RoomSongRepository
import com.tachyonmusic.database.domain.repository.LoopRepository
import com.tachyonmusic.database.domain.repository.PlaylistRepository
import com.tachyonmusic.database.domain.repository.SongRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object MediaTestModule {
    @Provides
    @Singleton
    fun provideDatabase(app: Application): Database = Room.inMemoryDatabaseBuilder(
        app,
        RoomDatabase::class.java,
    ).build()

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
}