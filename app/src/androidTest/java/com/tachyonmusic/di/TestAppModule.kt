package com.tachyonmusic.di

import android.app.Application
import androidx.room.Room
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.google.gson.ToNumberPolicy
import com.tachyonmusic.core.data.playback.RemoteLoopImpl
import com.tachyonmusic.core.data.playback.RemotePlaylistImpl
import com.tachyonmusic.core.di.CoreModule
import com.tachyonmusic.core.domain.MediaId
import com.tachyonmusic.core.domain.SongMetadataExtractor
import com.tachyonmusic.core.domain.TimingData
import com.tachyonmusic.core.domain.TimingDataController
import com.tachyonmusic.core.domain.playback.Loop
import com.tachyonmusic.core.domain.playback.Playlist
import com.tachyonmusic.core.domain.playback.SinglePlayback
import com.tachyonmusic.data.repository.FileRepositoryImpl
import com.tachyonmusic.database.data.data_source.Database
import com.tachyonmusic.database.data.data_source.room.RoomDatabase
import com.tachyonmusic.database.di.DatabaseModule
import com.tachyonmusic.database.domain.repository.SongRepository
import com.tachyonmusic.domain.repository.FileRepository
import com.tachyonmusic.domain.repository.MediaBrowserController
import com.tachyonmusic.util.TestMediaBrowserController
import com.tachyonmusic.util.TestSongMetadataExtractor
import dagger.Module
import dagger.Provides
import dagger.hilt.components.SingletonComponent
import dagger.hilt.testing.TestInstallIn
import kotlinx.coroutines.runBlocking
import javax.inject.Singleton

@Module
@TestInstallIn(
    components = [SingletonComponent::class],
    replaces = [DatabaseModule::class, AppRepositoryModule::class, CoreModule::class]
)
object TestAppModule {
    @Provides
    @Singleton
    fun provideGSON(): Gson = GsonBuilder().apply {
        registerTypeAdapter(MediaId::class.java, MediaId.Serializer())
        registerTypeAdapter(TimingData::class.java, TimingData.Serializer())
        setObjectToNumberStrategy(ToNumberPolicy.LONG_OR_DOUBLE)
    }.create()

    @Provides
    @Singleton
    fun provideFileRepository(): FileRepository = FileRepositoryImpl()

    @Provides
    @Singleton
    fun provideDatabase(app: Application): Database =
        Room.inMemoryDatabaseBuilder(app, RoomDatabase::class.java).build()

    @Provides
    @Singleton
    fun provideMediaBrowserController(): MediaBrowserController =
        TestMediaBrowserController()

    @Provides
    @Singleton
    fun provideSongMetadataExtractor(): SongMetadataExtractor = TestSongMetadataExtractor()

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