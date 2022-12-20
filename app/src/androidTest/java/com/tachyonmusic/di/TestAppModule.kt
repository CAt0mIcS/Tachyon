package com.tachyonmusic.di

import android.app.Application
import androidx.room.Room
import com.tachyonmusic.core.data.playback.*
import com.tachyonmusic.core.domain.MediaId
import com.tachyonmusic.core.domain.TimingData
import com.tachyonmusic.core.domain.TimingDataController
import com.tachyonmusic.core.domain.playback.Loop
import com.tachyonmusic.core.domain.playback.Playlist
import com.tachyonmusic.core.domain.playback.SinglePlayback
import com.tachyonmusic.database.data.data_source.Database
import com.tachyonmusic.database.data.data_source.room.RoomDatabase
import com.tachyonmusic.database.data.repository.RoomHistoryRepository
import com.tachyonmusic.database.domain.repository.HistoryRepository
import com.tachyonmusic.database.domain.repository.LoopRepository
import com.tachyonmusic.database.domain.repository.SongRepository
import com.tachyonmusic.database.domain.use_case.FindPlaybackByMediaId
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.runBlocking
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object TestAppModule {
    @Provides
    @Singleton
    fun provideHistoryRepository(
        db: Database,
        findPlaybackByMediaId: FindPlaybackByMediaId,
        songRepository: SongRepository,
        loopRepository: LoopRepository
    ): HistoryRepository =
        RoomHistoryRepository(db.historyDao, findPlaybackByMediaId, songRepository, loopRepository)

    @Provides
    @Singleton
    fun provideDatabase(app: Application): Database =
        Room.inMemoryDatabaseBuilder(app, RoomDatabase::class.java).build()

    @Provides
    @Singleton
    fun provideLoops(repository: SongRepository): MutableList<Loop> = runBlocking {
        MutableList(3) { i ->
            val song = repository.getSongs()[i]
            RemoteLoopImpl(
                MediaId.ofRemoteLoop(i.toString(), song.mediaId),
                i.toString(),
                TimingDataController(
                    listOf(
                        TimingData(1, 10),
                        TimingData(100, 1000)
                    )
                ),
                song
            )
        }
    }

    @Provides
    @Singleton
    fun providePlaylists(repository: SongRepository): MutableList<Playlist> = runBlocking {
        MutableList(2) { i ->
            RemotePlaylistImpl(
                MediaId.ofRemotePlaylist(i.toString()),
                i.toString(),
                repository.getSongs().filter {
                    it.title == "Cosmic Storm" || it.title == "Awake" || it.title == "Last Time"
                } as MutableList<SinglePlayback>
            )
        }
    }
}