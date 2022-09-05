package com.tachyonmusic.user.data.repository

import android.os.Environment
import android.util.Log
import com.tachyonmusic.core.data.playback.LocalSong
import com.tachyonmusic.core.domain.playback.Song
import com.tachyonmusic.user.domain.FileRepository
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.Deferred
import java.io.File

class FileRepositoryImpl : FileRepository {
    override val songs: Deferred<ArrayList<Song>>
        get() = _songs

    private val _songs = CompletableDeferred<ArrayList<Song>>()

    init {
        val files =
            File(Environment.getExternalStorageDirectory().absolutePath + "/Music/").listFiles()!!
        Log.d("FirebaseRepository", "Started loading songs")
        val songs = arrayListOf<Song>()
        for (file in files) {
            if (file.extension == "mp3") {
                songs += LocalSong.build(file)
            }
        }
        Log.d("FirebaseRepository", "Finished loading songs")
        _songs.complete(songs)
    }
}