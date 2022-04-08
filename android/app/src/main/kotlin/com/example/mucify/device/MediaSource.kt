package com.example.mucify.device

import android.content.Context
import android.os.Environment
import android.support.v4.media.MediaMetadataCompat
import com.example.mucify.Util
import com.example.mucify.ext.id
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import java.io.File
import java.util.*

/**
 * Class which manages and holds access to the entire media library. Once loaded, all songs, loops
 * and playlists available on the device as device files will be accessible through this class.
 */
class MediaSource(context: Context) : Iterable<MediaMetadataCompat> {

    companion object {
        /**
         * State indicating the source was created, but no initialization has performed.
         */
        const val STATE_CREATED = 1

        /**
         * State indicating initialization of the source is in progress.
         */
        const val STATE_INITIALIZING = 2

        /**
         * State indicating the source has been initialized and is ready to be used.
         */
        const val STATE_INITIALIZED = 3

        /**
         * State indicating an error has occurred.
         */
        const val STATE_ERROR = 4

        val LoopFileExtension = ".loop"
        val PlaylistFileExtension = ".playlist"

        // TODO: Does MediaPlayer support more audio formats? Does it support all of the listed ones? Do all of them work?
        val SupportedAudioExtensions: List<String> = listOf(
            ".3gp",
            ".mp4",
            ".m4a",
            ".aac",
            ".ts",
            ".flac",
            ".imy",
            ".mp3",
            ".mkv",
            ".ogg",
            ".wav"
        )
    }

    /**
     * Path to the directory where loops and playlists are stored
     */
    val DataDirectory: File = context.filesDir

    /**
     * Path to the external storage music directory
     */
    lateinit var MusicDirectory: File
        private set


    var state: Int = STATE_CREATED
        private set(value) {
            if (value == STATE_INITIALIZED || value == STATE_ERROR) {
                synchronized(onReadyListeners) {
                    field = value
                    onReadyListeners.forEach { listener ->
                        listener(state == STATE_INITIALIZED)
                    }
                }
            } else {
                field = value;
            }
        }

    /**
     * List of all available media items
     * TODO Should have separate classes for Song, Playlist, ...
     */
    private var catalog = emptyList<MediaMetadataCompat>()

    private var onReadyListeners = mutableListOf<(Boolean) -> Unit>()

    private val serviceJob = SupervisorJob()
    private val serviceScope = CoroutineScope(Dispatchers.Main + serviceJob)


    init {
        /**
         * Music directory may not be available, if so we'll set the state to STATE_ERROR
         */
        val musicDir = context.getExternalFilesDir(Environment.DIRECTORY_MUSIC)
        if (musicDir != null)
            MusicDirectory = musicDir
        else
            state = STATE_ERROR

        // TODO: Two coroutines for loading songs and loading loops and playlists
        serviceScope.launch {
            loadSongs(MusicDirectory)
            loadLoopsAndPlaylists()
            state = STATE_INITIALIZED
        }
    }


    fun whenReady(performAction: (Boolean) -> Unit): Boolean {
        return when (state) {
            STATE_CREATED, STATE_INITIALIZING -> {
                onReadyListeners += performAction
                false
            }
            else -> {
                performAction(state != STATE_ERROR)
                true
            }
        }
    }

    override fun iterator(): Iterator<MediaMetadataCompat> {
        return catalog.iterator()
    }


    private fun loadSongs(path: File?) {
        if (path == null || !path.exists()) return
        val files = path.listFiles() ?: return
        for (file in files) {
            if (file.isDirectory) loadSongs(file) else {
                if (Util.isSongFile(file)) {
                    catalog = catalog + MediaMetadataCompat.Builder().apply {
                        id = "Song_" + file.absolutePath
                    }.build()
                }
            }
        }
    }

    private fun loadLoopsAndPlaylists() {
        if (!DataDirectory.exists()) return
        val files = DataDirectory.listFiles() ?: return
        for (file in files) {
            if (Util.isLoopFile(file)) {
                catalog = catalog + MediaMetadataCompat.Builder().apply {
                    id = "Loop_" + file.absolutePath
                }.build()
            } else if (Util.isPlaylistFile(file)) {
                catalog = catalog + MediaMetadataCompat.Builder().apply {
                    id = "Playlist_" + file.absolutePath
                }.build()
            }
        }
    }

}