package com.daton.media.device

import android.content.Context
import android.graphics.BitmapFactory
import android.media.MediaMetadataRetriever
import android.os.Environment
import android.support.v4.media.MediaMetadataCompat
import android.util.Log
import com.daton.media.SongMetadata
import com.daton.media.ext.*
import java.io.BufferedReader
import java.io.File
import java.io.FileReader
import java.lang.IllegalStateException


/**
 * Class which manages and holds access to the entire media library. Once loaded, all songs, loops
 * and playlists available on the device as device files will be accessible through this class.
 */
class MediaSource(context: Context) : Iterable<MediaMetadataCompat> {

    companion object {
        const val TAG = "MediaSource"

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

        // TODO: Move somewhere else as MediaStore is no longer loading loops and playlists
        const val LoopFileExtension = "loop"
        const val PlaylistFileExtension = "playlist"

        // TODO: Does ExoPlayer support more audio formats? Does it support all of the listed ones? Do all of them work?
        val SupportedAudioExtensions: List<String> = listOf(
            "3gp",
            "mp4",
            "m4a",
            "aac",
            "ts",
            "flac",
            "imy",
            "mp3",
            "mkv",
            "ogg",
            "wav"
        )

        fun loadSong(file: File): MediaMetadataCompat {
            return MediaMetadataCompat.Builder().apply {
                mediaId = "Song_" + file.absolutePath
                this.path = file

                SongMetadata(file).let { songMetadata ->
                    title = songMetadata.title
                    artist = songMetadata.artist
                    albumArt = songMetadata.albumArt
                    duration = songMetadata.duration
                }


            }.build()
        }
    }

    /**
     * Path to the external storage music directory
     */
    lateinit var musicDirectory: File
        private set


    var state: Int = STATE_CREATED
        private set(value) {
            Log.d(TAG, "Setting state to $value")
            if (value == STATE_INITIALIZED || value == STATE_ERROR) {
                synchronized(onReadyListeners) {
                    field = value
                    onReadyListeners.forEach { listener ->
                        listener(state == STATE_INITIALIZED)
                    }
                }
            } else {
                field = value
            }
        }

    /**
     * List of all available media items
     * TODO Should have separate classes for Song, Playlist, ...
     */
    val catalog = mutableListOf<MediaMetadataCompat>()

    private var onReadyListeners = mutableListOf<(Boolean) -> Unit>()
    private var onChangedListeners = mutableListOf<() -> Unit>()

    operator fun plusAssign(items: List<MediaMetadataCompat>) {
        catalog += items
        invokeOnChanged()
    }


    /**
     * Should be called after storage permission is granted to load music that is stored in the phone's
     * external storage
     */
    fun loadDeviceFiles() {
        /**
         * Music directory may not be available, if so we'll set the state to STATE_ERROR
         */
        // TODO: What if music is stored somewhere else?
        val musicDir =
            File(Environment.getExternalStorageDirectory().absolutePath + "/Music")
        if (musicDir.exists()) {
            musicDirectory = musicDir
            Log.d(TAG, "Settings music directory to ${musicDirectory.absolutePath}")
        } else {
            Log.e(TAG, "Music directory not available")
            state = STATE_ERROR
        }


        loadSongs(musicDirectory)
        state = STATE_INITIALIZED
        invokeOnChanged()
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

    fun onChanged(performAction: () -> Unit) {
        synchronized(onChangedListeners) {
            onChangedListeners += performAction
        }
    }

    private fun invokeOnChanged() {
        synchronized(onChangedListeners) {
            onChangedListeners.forEach { listener -> listener() }
        }
    }

    override fun iterator() = catalog.iterator()


    private fun loadSongs(path: File?) {
        if (path == null || !path.exists()) return
        val files = path.listFiles() ?: return
        for (file in files) {
            if (file.isDirectory) loadSongs(file) else {
                if (file.isSongFile) {
                    catalog += loadSong(file)
                }
            }
        }
    }
}