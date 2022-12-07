package com.tachyonmusic.domain.use_case.main

import android.os.Environment
import android.util.Log
import com.daton.database.domain.model.SongEntity
import com.daton.database.domain.repository.SettingsRepository
import com.daton.database.domain.repository.SongRepository
import com.tachyonmusic.core.data.playback.LocalSongImpl
import com.tachyonmusic.core.domain.playback.Song
import com.tachyonmusic.domain.repository.FileRepository
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.onEach
import java.io.File

/**
 * Checks if every song that is not excluded is saved in the database. If a song was removed by the
 * user or a new song was added, it removes/adds the song to the database.
 */
class UpdateSongDatabase(
    private val songRepo: SongRepository,
    private val settingsRepo: SettingsRepository,
    private val fileRepository: FileRepository
) {
    suspend operator fun invoke() = withContext(Dispatchers.IO) {

        // TODO: Shouldn't hard-code path
        // TODO: Support more extensions
        val paths = fileRepository.getFilesInDirectoryWithExtensions(
            File(Environment.getExternalStorageDirectory().absolutePath + "/Music/"),
            listOf("mp3")
        ).toMutableList()

        val settings = settingsRepo.getSettings()
        songRepo.removeIf {
            // TODO: Shouldn't use LocalSongImpl here!
            if (it is LocalSongImpl) {
                paths.remove(it.path)
                settings.excludedSongFiles.contains(it.path.absolutePath) ||
                        !it.path.exists() || !it.path.isFile
            } else TODO("It is not LocalSongImpl")
        }

        // TODO: Shouldn't use LocalSongImpl here!
        // TODO: Better async song loading?
        if (paths.isNotEmpty()) {
            Log.d("UpdateSongDatabase", "Loading ${paths.size} songs...")
            val songs = mutableListOf<Deferred<Song>>()
            for (path in paths) {
                songs += async(Dispatchers.IO) {
                    LocalSongImpl.build(path)
                }
            }

            Log.d("UpdateSongDatabase", "Loaded ${paths.size} songs")

            songRepo.addAll(
                songs.awaitAll().map { SongEntity(it.mediaId, it.title, it.artist, it.duration) })
        }
    }
}