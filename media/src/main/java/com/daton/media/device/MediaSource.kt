package com.daton.media.device

import android.os.Environment
import android.support.v4.media.MediaMetadataCompat
import android.util.Log
import com.daton.media.data.MediaId
import com.daton.media.data.SongMetadata
import com.daton.media.ext.*
import java.io.File


/**
 * Class which manages and holds access to the entire media library. Once loaded, all songs, loops
 * and playlists available on the device as device files will be accessible through this class.
 */
class MediaSource {

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
                mediaId = MediaId.fromSongFile(file)
//                this.path = file

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
                    onReadyListeners.clear()
                }
            } else {
                field = value
            }
        }

    val songs = mutableListOf<MediaMetadataCompat>()

    var loops = mutableListOf<Loop>()
        set(value) {
            field = value
            onChangedListener?.invoke(BrowserTree.LOOP_ROOT, null)
        }

    var playlists = mutableListOf<Playlist>()
        set(value) {
            field = value
            onChangedListener?.invoke(BrowserTree.PLAYLIST_ROOT, null)
        }

    private var onReadyListeners = mutableListOf<(Boolean) -> Unit>()

    /**
     * Called whenever an item is added/removed from [loops]/[playlists]
     * * The first argument specifies either [BrowserTree.SONG_ROOT], [BrowserTree.LOOP_ROOT]
     *   or [BrowserTree.PLAYLIST_ROOT]
     * * The second argument is null
     *
     * Called whenever an item changes (item added to playlist, for example)
     * * The first argument specifies either [BrowserTree.LOOP_ROOT] or [BrowserTree.PLAYLIST_ROOT]
     *   depending on what changed
     * * The second argument specifies the serialized media id of the playlist that was changed
     *   which can be deserialized using [MediaId.deserialize]
     */
    var onChangedListener: ((String, String?) -> Unit)? = null

    init {
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
    }

    /**
     * Should be called after storage permission is granted to load music that is stored in the phone's
     * external storage
     */
    fun loadSharedDeviceFiles() {
        songs.clear()
        loadSongs(musicDirectory)
        onChangedListener?.invoke(BrowserTree.SONG_ROOT, null)
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

    fun getSong(mediaId: MediaId) = songs.find { it.mediaId == mediaId }
    fun getLoop(mediaId: MediaId) = loops.find { it.mediaId == mediaId }
    fun getPlaylist(mediaId: MediaId) = playlists.find { it.mediaId == mediaId }

    fun forEachSong(perSong: (MediaMetadataCompat) -> Unit) {
        for (song in songs)
            perSong(song)
    }

    fun forEachLoop(perLoop: (Loop) -> Unit) {
        for (loop in loops)
            perLoop(loop)
    }

    fun forEachPlaylist(perPlaylist: (Playlist) -> Unit) {
        for (playlist in playlists)
            perPlaylist(playlist)
    }

    fun findSong(pred: (MediaMetadataCompat) -> Boolean): MediaMetadataCompat? {
        for (song in songs)
            if (pred(song))
                return song
        return null
    }

    fun findLoop(pred: (Loop) -> Boolean): Loop? {
        for (loop in loops)
            if (pred(loop))
                return loop
        return null
    }

    fun findPlaylist(pred: (Playlist) -> Boolean): Playlist? {
        for (playlist in playlists)
            if (pred(playlist))
                return playlist
        return null
    }


    fun indexOfSong(pred: (MediaMetadataCompat) -> Boolean): Int {
        for (i in 0 until songs.size)
            if (pred(songs[i]))
                return i
        return -1
    }

    fun indexOfLoop(pred: (Loop) -> Boolean): Int {
        for (i in 0 until loops.size)
            if (pred(loops[i]))
                return i
        return -1
    }

    fun indexOfPlaylist(pred: (Playlist) -> Boolean): Int {
        for (i in 0 until playlists.size)
            if (pred(playlists[i]))
                return i
        return -1
    }


    private fun loadSongs(path: File?) {
        if (path == null || !path.exists()) return
        val files = path.listFiles() ?: return
        for (file in files) {
            if (file.isDirectory) loadSongs(file) else {
                if (file.isSongFile) {
                    songs += loadSong(file)
                }
            }
        }
    }

    fun clearSongs() {
        val shouldInvokeOnChanged = songs.size != 0
        songs.clear()
        if (shouldInvokeOnChanged)
            onChangedListener?.invoke(BrowserTree.SONG_ROOT, null)
    }
}