package com.tachyonmusic.di

import android.app.Application
import androidx.room.Room
import com.tachyonmusic.core.data.playback.LocalPlaylist
import com.tachyonmusic.core.data.playback.LocalRemix
import com.tachyonmusic.core.domain.MediaId
import com.tachyonmusic.core.domain.TimingData
import com.tachyonmusic.core.domain.TimingDataController
import com.tachyonmusic.core.domain.playback.Playlist
import com.tachyonmusic.core.domain.playback.Remix
import com.tachyonmusic.core.domain.playback.SinglePlayback
import com.tachyonmusic.data.repository.FileRepositoryImpl
import com.tachyonmusic.data.repository.MediaPlaybackServiceMediaBrowserController
import com.tachyonmusic.database.data.data_source.Database
import com.tachyonmusic.database.data.data_source.room.RoomDatabase
import com.tachyonmusic.database.di.DatabaseModule
import com.tachyonmusic.database.domain.repository.SongRepository
import com.tachyonmusic.domain.repository.FileRepository
import com.tachyonmusic.domain.repository.MediaBrowserController
import dagger.Module
import dagger.Provides
import dagger.hilt.components.SingletonComponent
import dagger.hilt.testing.TestInstallIn
import kotlinx.coroutines.runBlocking
import javax.inject.Singleton

@Module
@TestInstallIn(
    components = [SingletonComponent::class],
    replaces = [DatabaseModule::class, AppRepositoryModule::class]
)
object TestAppModule {
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
        MediaPlaybackServiceMediaBrowserController()

    @Provides
    @Singleton
    fun provideCustomizedSongs(repository: SongRepository): MutableList<Remix> = runBlocking {
        MutableList(3) { i ->
            val song = repository.getSongs()[i]
            LocalRemix(
                MediaId.ofLocalRemix(i.toString(), song.mediaId),
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
            LocalPlaylist(
                MediaId.ofLocalPlaylist(i.toString()),
                i.toString(),
                repository.getSongs().filter {
                    it.title == "Cosmic Storm" || it.title == "Awake" || it.title == "Last Time"
                } as MutableList<SinglePlayback>
            )
        }
    }
}