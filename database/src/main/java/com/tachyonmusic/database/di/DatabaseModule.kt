package com.tachyonmusic.database.di

import android.app.Application
import android.content.Context
import androidx.room.Room
import com.tachyonmusic.database.data.data_source.*
import com.tachyonmusic.database.data.data_source.room.RoomDatabase
import com.tachyonmusic.database.data.repository.*
import com.tachyonmusic.database.domain.ArtworkSource
import com.tachyonmusic.database.domain.repository.*
import com.tachyonmusic.database.domain.use_case.FindPlaybackByMediaId
import com.tachyonmusic.database.domain.use_case.LoadArtwork
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
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
        findPlaybackByMediaId: FindPlaybackByMediaId,
        songRepository: SongRepository,
        loopRepository: LoopRepository
    ): HistoryRepository =
        RoomHistoryRepository(
            database.historyDao,
            findPlaybackByMediaId,
            songRepository,
            loopRepository
        )

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
    fun provideArtworkSource(): ArtworkSource = ArtworkSourceImpl()

    @Provides
    @Singleton
    fun provideFindPlaybackByMediaIdUseCase(
        songRepository: SongRepository,
        loopRepository: LoopRepository,
        playlistRepository: PlaylistRepository
    ) = FindPlaybackByMediaId(songRepository, loopRepository, playlistRepository)


    @Provides
    @Singleton
    fun provideLoadArtworkUseCase(
        artworkSource: ArtworkSource,
        songRepository: SongRepository,
        loopRepository: LoopRepository,
        @ApplicationContext context: Context
    ) = LoadArtwork(artworkSource, context, songRepository, loopRepository)
}