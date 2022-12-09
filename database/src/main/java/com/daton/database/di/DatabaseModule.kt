package com.daton.database.di

import android.app.Application
import androidx.room.Room
import com.daton.database.data.data_source.*
import com.daton.database.data.repository.*
import com.daton.database.data.repository.shared_action.*
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
        convertEntityToSong: ConvertEntityToSong
    ): SongRepository =
        SongRepositoryImpl(database.songDao, convertEntityToSong)

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
    fun provideHistoryRepository(
        database: Database,
        convertEntityToPlayback: ConvertEntityToPlayback,
        findPlaybackByMediaId: FindPlaybackByMediaId
    ): HistoryRepository =
        HistoryRepositoryImpl(database.historyDao, convertEntityToPlayback, findPlaybackByMediaId)

    @Provides
    @Singleton
    fun provideArtworkSource(): ArtworkSource = ArtworkSourceImpl()

    @Provides
    @Singleton
    fun provideGetArtworkForPlayback() = GetArtworkForPlayback()

    @Provides
    @Singleton
    fun provideConvertEntityToPlayback(
        convertEntityToSong: ConvertEntityToSong,
        convertEntityToLoop: ConvertEntityToLoop,
        convertEntityToPlaylist: ConvertEntityToPlaylist
    ) = ConvertEntityToPlayback(convertEntityToSong, convertEntityToLoop, convertEntityToPlaylist)

    @Provides
    @Singleton
    fun provideConvertEntityToSong(getArtworkForPlayback: GetArtworkForPlayback) =
        ConvertEntityToSong(getArtworkForPlayback)

    @Provides
    @Singleton
    fun provideConvertEntityToLoop(getArtworkForPlayback: GetArtworkForPlayback) =
        ConvertEntityToLoop(getArtworkForPlayback)

    @Provides
    @Singleton
    fun provideFindPlaybackByMediaId(
        songRepository: SongRepository,
        loopRepository: LoopRepository,
        playlistRepository: PlaylistRepository
    ) = FindPlaybackByMediaId(songRepository, loopRepository, playlistRepository)

    @Provides
    @Singleton
    fun provideConvertEntityToPlaylist(
        songRepository: SongRepository,
        loopRepository: LoopRepository,
        convertEntityToSong: ConvertEntityToSong,
        convertEntityToLoop: ConvertEntityToLoop
    ) = ConvertEntityToPlaylist(
        songRepository,
        loopRepository,
        convertEntityToSong,
        convertEntityToLoop
    )

    @Provides
    @Singleton
    fun provideLoadArtwork(artworkSource: ArtworkSource, updateArtwork: UpdateArtwork) =
        LoadArtwork(artworkSource, updateArtwork)

    @Provides
    @Singleton
    fun provideUpdateArtwork(
        songRepository: SongRepository,
        loopRepository: LoopRepository,
        historyRepository: HistoryRepository
    ) = UpdateArtwork(songRepository, loopRepository, historyRepository)
}