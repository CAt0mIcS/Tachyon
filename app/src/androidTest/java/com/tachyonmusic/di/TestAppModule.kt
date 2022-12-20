package com.tachyonmusic.di

import android.app.Application
import androidx.room.Room
import com.daton.database.data.data_source.Database
import com.daton.database.data.data_source.room.RoomDatabase
import com.daton.database.data.repository.RoomHistoryRepository
import com.daton.database.data.repository.shared_action.ConvertEntityToPlayback
import com.daton.database.data.repository.shared_action.FindPlaybackByMediaId
import com.daton.database.domain.repository.HistoryRepository
import com.daton.database.domain.repository.SongRepository
import com.google.gson.Gson
import com.tachyonmusic.core.data.playback.*
import com.tachyonmusic.core.domain.MediaId
import com.tachyonmusic.core.domain.TimingData
import com.tachyonmusic.core.domain.TimingDataController
import com.tachyonmusic.core.domain.playback.Loop
import com.tachyonmusic.core.domain.playback.Playlist
import com.tachyonmusic.core.domain.playback.SinglePlayback
import com.tachyonmusic.user.data.LocalCache
import com.tachyonmusic.user.data.repository.FirebaseRepository
import com.tachyonmusic.user.domain.UserRepository
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
    fun provideUserRepository(localCache: LocalCache, gson: Gson): UserRepository =
        FirebaseRepository(localCache, gson)

    @Provides
    @Singleton
    fun provideHistoryRepository(
        db: Database,
        convertEntityToPlayback: ConvertEntityToPlayback,
        findPlaybackByMediaId: FindPlaybackByMediaId
    ): HistoryRepository =
        RoomHistoryRepository(db.historyDao, convertEntityToPlayback, findPlaybackByMediaId)

    @Provides
    @Singleton
    fun provideDatabase(app: Application): Database =
        Room.inMemoryDatabaseBuilder(app, RoomDatabase::class.java).build()

    @Provides
    @Singleton
    fun provideLocalCache(app: Application, gson: Gson) = LocalCache(app, gson)

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