package com.tachyonmusic.di

import android.app.Application
import com.google.gson.Gson
import com.tachyonmusic.core.data.playback.RemoteLoop
import com.tachyonmusic.core.data.playback.RemotePlaylist
import com.tachyonmusic.core.domain.MediaId
import com.tachyonmusic.core.domain.TimingData
import com.tachyonmusic.core.domain.TimingDataController
import com.tachyonmusic.core.data.playback.AbstractLoop
import com.tachyonmusic.core.data.playback.Playlist
import com.tachyonmusic.core.data.playback.SinglePlayback
import com.tachyonmusic.user.data.LocalCache
import com.tachyonmusic.user.data.repository.FileRepositoryImpl
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
        FirebaseRepository(FileRepositoryImpl(), localCache, gson)

    @Provides
    @Singleton
    fun provideLocalCache(app: Application, gson: Gson) = LocalCache(app, gson)

    @Provides
    @Singleton
    fun provideLoops(repository: UserRepository): MutableList<AbstractLoop> = runBlocking {
        MutableList(3) { i ->
            val song = repository.songs.await()[i]

            RemoteLoop(
                MediaId.ofRemoteLoop(i.toString(), song.mediaId),
                i.toString(),
                TimingDataController(listOf(TimingData(1, 10), TimingData(100, 1000))),
                song
            )
        }
    }

    @Provides
    @Singleton
    fun providePlaylists(repository: UserRepository): MutableList<Playlist> = runBlocking {
        MutableList(2) { i ->
            RemotePlaylist(
                MediaId.ofRemotePlaylist(i.toString()),
                i.toString(),
                repository.songs.await().filter {
                    it.title == "Cosmic Storm" || it.title == "Awake" || it.title == "Last Time"
                } as MutableList<SinglePlayback>
            )
        }
    }
}