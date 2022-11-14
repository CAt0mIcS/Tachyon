package com.tachyonmusic.user.data.repository

import android.os.Environment
import android.util.Log
import com.tachyonmusic.core.data.playback.LocalSongImpl
import com.tachyonmusic.core.domain.playback.Song
import com.tachyonmusic.user.domain.FileRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import java.io.File

class FileRepositoryImpl : FileRepository {
    override val songs: StateFlow<List<Song>>
        get() = _songs

    private val _songs = MutableStateFlow<List<Song>>(listOf())

    init {
        val files =
            File(Environment.getExternalStorageDirectory().absolutePath + "/Music/").listFiles()!!
        Log.d("FirebaseRepository", "Started loading songs")
        val songs = arrayListOf<Song>()
        for (file in files) {
            if (file.extension == "mp3") {
                // TODO: Don't use Impl here
                songs += LocalSongImpl.build(file)
            }
        }
        Log.d("FirebaseRepository", "Finished loading songs")
        songs.sortBy { it.title + it.artist }
        _songs.value = songs
    }

    override operator fun plusAssign(song: Song) {
        val newList = songs.value + song
        newList.sortedBy { it.title + it.artist }
        _songs.value = newList
    }

    override fun minusAssign(song: Song) {
        _songs.value -= song
    }
}