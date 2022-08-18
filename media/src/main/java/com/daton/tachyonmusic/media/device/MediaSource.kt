package com.daton.tachyonmusic.media.device

import android.os.Environment
import android.util.Log
import com.daton.tachyonmusic.media.data.MediaId
import com.daton.tachyonmusic.media.ext.isSongFile
import com.daton.tachyonmusic.media.playback.Loop
import com.daton.tachyonmusic.media.playback.Playback
import com.daton.tachyonmusic.media.playback.Playlist
import com.daton.tachyonmusic.media.playback.Song
import com.daton.tachyonmusic.util.launch
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.joinAll
import kotlinx.coroutines.launch
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

        // TODO: Does ExoPlayer support more audio formats? Does it support all of the listed ones? Do all of them work?
        val SupportedAudioExtensions: List<String> = listOf(
//            "3gp",
//            "mp4",
//            "m4a",
//            "aac",
//            "ts",
//            "flac",
//            "imy",
            "mp3",
//            "mkv",
//            "ogg",
            "wav"
        )
    }

    private var songsLoaded = false

    /**
     * Path to the external storage music directory
     */
    lateinit var musicDirectory: File
        private set


    var state: Int = STATE_CREATED
        private set(value) {
            Log.d(TAG, "Setting state to $value")
            if (songsLoaded && _loops != null && _playlists != null && value == STATE_INITIALIZED || value == STATE_ERROR) {
                synchronized(onReadyListeners) {
                    field = value
                    onReadyListeners.forEach { listener ->
                        listener(state == STATE_INITIALIZED)
                    }
                    onReadyListeners.clear()
                }
            } else if (value != STATE_INITIALIZED) {
                field = value
            }
        }

    val songs = mutableListOf<Song>()

    private var _loops: MutableList<Loop>? = null
        set(value) {
            field = value
            field!!.sortBy { it.name }
            eventListener?.onMediaSourceChanged(BrowserTree.LOOP_ROOT, null)
            state = STATE_INITIALIZED
        }

    var loops: MutableList<Loop>
        set(value) {
            _loops = value
        }
        get() = _loops!!


    private var _playlists: MutableList<Playlist>? = null
        set(value) {
            field = value
            field!!.sortBy { it.name }
            eventListener?.onMediaSourceChanged(BrowserTree.PLAYLIST_ROOT, null)
            state = STATE_INITIALIZED
        }

    var playlists: MutableList<Playlist>
        set(value) {
            _playlists = value
        }
        get() = _playlists!!

    private var onReadyListeners = mutableListOf<(Boolean) -> Unit>()

    private var eventListener: IEventListener? = null
        get() = if (state == STATE_INITIALIZED) field else null

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

    fun registerEventListener(listener: IEventListener?) {
        eventListener = listener
    }

    /**
     * Should be called after storage permission is granted to load music that is stored in the phone's
     * external storage (TODO: Suspend function?)
     */
    fun loadSharedDeviceFiles() {
        state = STATE_INITIALIZING
        songsLoaded = false

        songs.clear()
        // TODO: Search subdirectories of [musicDirectory]
        val files = musicDirectory.listFiles()!!
        launch(Dispatchers.IO) {

            MutableList(files.size) { i ->
                launch {
                    if (files[i].isSongFile) {
                        val song = Song(files[i])
                        synchronized(songs) {
                            songs += song
                        }
                    }
                }
            }.joinAll()


            // Order alphabetically by song title
            songs.sortBy { it.title + it.artist }

            eventListener?.onMediaSourceChanged(BrowserTree.SONG_ROOT, null)
            songsLoaded = true
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

    fun clearSongs() {
        val shouldInvokeOnChanged = songs.size != 0
        songs.clear()
        if (shouldInvokeOnChanged)
            eventListener?.onMediaSourceChanged(BrowserTree.SONG_ROOT, null)
    }

    fun findById(mediaId: MediaId): Playback? {
        for (song in songs)
            if (song.mediaId == mediaId)
                return song
        for (loop in loops)
            if (loop.mediaId == mediaId)
                return loop
        for (playlist in playlists)
            if (playlist.mediaId == mediaId)
                return playlist
        return null
    }

    interface IEventListener {
        fun onMediaSourceChanged(root: String, mediaId: String?)
    }

    class EventListener : IEventListener {
        /**
         * Called whenever an item is added/removed from [loops]/[playlists]
         * * The first argument specifies either [BrowserTree.SONG_ROOT], [BrowserTree.LOOP_ROOT]
         *   or [BrowserTree.PLAYLIST_ROOT]
         * * The second argument is null
         *
         * Called whenever an item changes (item added to playlist, for example) and if the
         * [MediaSource] is already loaded
         * * The first argument specifies either [BrowserTree.LOOP_ROOT] or [BrowserTree.PLAYLIST_ROOT]
         *   depending on what changed
         * * The second argument specifies the serialized media id of the playlist that was changed
         *   which can be deserialized using [MediaId.deserialize]
         */
        override fun onMediaSourceChanged(root: String, mediaId: String?) {}
    }
}