package com.daton.media.device

import android.content.Context
import android.graphics.BitmapFactory
import android.media.MediaMetadataRetriever
import android.os.Environment
import android.support.v4.media.MediaMetadataCompat
import android.util.Log
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

                val metaRetriever = MediaMetadataRetriever()
                try {
                    metaRetriever.setDataSource(file.absolutePath)
                } catch (e: IllegalArgumentException) {
                    e.printStackTrace()
                    TODO("Implement error handling")
                }

                title =
                    metaRetriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_TITLE)
                        ?: file.nameWithoutExtension

                artist =
                    metaRetriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_ARTIST)
                        ?: "Unknown Artist"

                val art: ByteArray? = metaRetriever.embeddedPicture
                if (art != null) {
                    albumArt = BitmapFactory.decodeByteArray(art, 0, art.size)
                }

                duration =
                    metaRetriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION)!!
                        .toLong()

            }.build()
        }

        fun loadLoop(file: File): MediaMetadataCompat {
            return MediaMetadataCompat.Builder().apply {
                mediaId = "Loop_" + file.absolutePath

                // TODO: Temporary
                val reader = BufferedReader(FileReader(file))
                val songPath = File(reader.readLine())
                path = songPath
                startTime = reader.readLine().toLong()
                endTime = reader.readLine().toLong()

                val songMetadata = loadSong(songPath)
                title = songMetadata.title
                artist = songMetadata.artist

                reader.close()

            }.build()
        }

        fun loadPlaylist(path: File): MutableList<MediaMetadataCompat> {
            val list = mutableListOf<MediaMetadataCompat>()
            val reader = BufferedReader(FileReader(path))

            while (reader.ready()) {

                val songOrLoopPath = File(reader.readLine())
                list.add(
                    if (songOrLoopPath.isSongFile)
                        loadSong(songOrLoopPath)
                    else if (songOrLoopPath.isLoopFile)
                        loadLoop(songOrLoopPath)
                    else
                        throw IllegalStateException("Invalid path: $songOrLoopPath")
                )
            }
            reader.close()

            return list
        }
    }

    /**
     * Path to the directory where loops and playlists are stored
     */
    val dataDirectory: File = context.filesDir

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
    private var catalog = mutableListOf<MediaMetadataCompat>()

    private var onReadyListeners = mutableListOf<(Boolean) -> Unit>()


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
        loadLoopsAndPlaylists()
        state = STATE_INITIALIZED
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

    private fun loadLoopsAndPlaylists() {
        if (!dataDirectory.exists()) return
        val files = dataDirectory.listFiles() ?: return
        for (file in files) {
            if (file.isLoopFile) {
                catalog += loadLoop(file)
            } else if (file.isPlaylistFile) {
                catalog += MediaMetadataCompat.Builder().apply {
                    mediaId = "Playlist_" + file.absolutePath
                    path = file
                }.build()
            }
        }
    }

    fun setOrAddLoop(mediaId: String, songMediaId: String, startTime: Long, endTime: Long) {
        val mediaMetadata = MediaMetadataCompat.Builder().apply {
            this.mediaId = mediaId
            path = songMediaId.path
            this.startTime = startTime
            this.endTime = endTime

            // TODO: Optimize
            val songMetadata = loadSong(songMediaId.path)
            title = songMetadata.title
            artist = songMetadata.artist
        }.build()

        // Replace if already contained in catalog
        // TODO: Offline loop might be newer than online loop
        for (item in catalog) {
            if (item.mediaId == mediaId) {
                val index = catalog.indexOf(item)
                catalog.removeAt(index)
                catalog.add(index, mediaMetadata)
                return
            }
        }

        // Not contained in catalog
        catalog.add(mediaMetadata)
    }

    fun setOrAddPlaylist(mediaId: String, mediaIds: Array<String>) {

    }

}