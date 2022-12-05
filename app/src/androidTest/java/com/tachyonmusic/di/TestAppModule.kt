package com.tachyonmusic.di

import android.app.Application
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
import com.tachyonmusic.user.data.repository.TestFileRepositoryImpl
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
        FirebaseRepository(TestFileRepositoryImpl(), localCache, gson)

    @Provides
    @Singleton
    fun provideLocalCache(app: Application, gson: Gson) = LocalCache(app, gson)

    @Provides
    @Singleton
    fun provideLoops(repository: UserRepository): MutableList<Loop> = runBlocking {
        MutableList(3) { i ->
            val song = repository.songs.value[i]

            // TODO: Don't use Impl here

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
    fun providePlaylists(repository: UserRepository): MutableList<Playlist> = runBlocking {
        MutableList(2) { i ->
            RemotePlaylistImpl(
                MediaId.ofRemotePlaylist(i.toString()),
                i.toString(),
                repository.songs.value.filter {
                    it.title == "Cosmic Storm" || it.title == "Awake" || it.title == "Last Time"
                } as MutableList<SinglePlayback>
            )
        }
    }
}