package com.daton.database.di

import android.app.Application
import androidx.room.Room
import com.daton.artworkfetcher.ArtworkFetcher
import com.daton.database.data.data_source.*
import com.daton.database.data.repository.*
import com.daton.database.domain.ArtworkSource
import com.daton.database.domain.repository.*
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
        Database::class.java,
        Database::class.java.name
    ).build()


    @Provides
    @Singleton
    fun provideSettingsRepository(database: Database): SettingsRepository =
        SettingsRepositoryImpl(database.settingsDao)

    @Provides
    @Singleton
    fun provideSongRepository(
        database: Database,
        artworkSource: ArtworkSource
    ): SongRepository = SongRepositoryImpl(database.songDao, artworkSource, ArtworkFetcher())

    @Provides
    @Singleton
    fun provideLoopRepository(database: Database): LoopRepository =
        LoopRepositoryImpl(database.loopDao)

    @Provides
    @Singleton
    fun providePlaylistRepository(database: Database): PlaylistRepository =
        PlaylistRepositoryImpl(database.playlistDao)

    @Provides
    @Singleton
    fun provideHistoryRepository(database: Database): HistoryRepository =
        HistoryRepositoryImpl(database.historyDao)

    @Provides
    @Singleton
    fun provideArtworkSource(): ArtworkSource = ArtworkSourceImpl()
}