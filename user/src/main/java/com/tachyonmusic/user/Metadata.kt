package com.tachyonmusic.user

import android.util.Log
import com.tachyonmusic.media.data.MediaId
import com.tachyonmusic.media.playback.Loop
import com.tachyonmusic.media.playback.Playback
import com.tachyonmusic.media.playback.Playlist
import com.tachyonmusic.media.playback.Song
import com.tachyonmusic.util.launch
import kotlinx.coroutines.Dispatchers

class Metadata() {
    companion object {
        const val TAG = "UserMetadata"
    }

    var ignoreAudioFocus = false
    var combineDifferentPlaybackTypes = false
    var songIncDecInterval = 100
    var audioUpdateInterval = 100
    var maxPlaybacksInHistory = 25

    var loops: ArrayList<Loop> = arrayListOf()
    var playlists: ArrayList<Playlist> = arrayListOf()

    var history: MutableList<Playback> = mutableListOf()

    var onHistoryChanged: ((MutableList<Playback> /*history*/) -> Unit)? = null

    constructor(
        data: Map<String, Any?>,
        onHistoryChanged: ((MutableList<Playback>) -> Unit)?
    ) : this() {
        ignoreAudioFocus = data["ignoreAudioFocus"] as Boolean? ?: false
        combineDifferentPlaybackTypes = data["combineDifferentPlaybackTypes"] as Boolean? ?: false
        songIncDecInterval = (data["songIncDecInterval"] as Long? ?: 100L).toInt()
        audioUpdateInterval = (data["audioUpdateInterval"] as Long? ?: 100L).toInt()
        maxPlaybacksInHistory = (data["maxPlaybacksInHistory"] as Long? ?: 25L).toInt()

        loops =
            ((data["loops"] as List<HashMap<String, Any?>?>?)?.map {
                Loop.createFromHashMap(it!!)
            } as ArrayList<Loop>?)
                ?: arrayListOf()

        playlists = ((data["playlists"] as List<HashMap<String, Any?>?>?)?.map {
            Playlist.createFromHashMap(it!!)
        } as ArrayList<Playlist>?)
            ?: arrayListOf()

        history = ((data["history"] as List<String?>?)?.map { mediaIdStr ->
            val mediaId = MediaId.deserialize(mediaIdStr!!)
            if (mediaId.isSong)
                Song(mediaId)
            else if (mediaId.isLoop)
                loops.find { it.mediaId == mediaId } ?: TODO("Loop not found")
            else
                playlists.find { it.mediaId == mediaId } ?: TODO("Playlist not found")

        } as MutableList<Playback>?) ?: mutableListOf()

        Log.d(TAG, "Finished loading metadata")

        this.onHistoryChanged = onHistoryChanged
        if (history.isNotEmpty())
            launch(Dispatchers.Main) { this@Metadata.onHistoryChanged?.invoke(history) }
    }

    fun toHashMap() = hashMapOf(
        "ignoreAudioFocus" to ignoreAudioFocus,
        "combineDifferentPlaybackTypes" to combineDifferentPlaybackTypes,
        "songIncDecInterval" to songIncDecInterval,
        "audioUpdateInterval" to audioUpdateInterval,
        "maxPlaybacksInHistory" to maxPlaybacksInHistory,

        "loops" to loops.map { it.toHashMap() },
        "playlists" to playlists.map { it.toHashMap() },
        "history" to history.map { it.mediaId.toString() }
    )

    fun addHistory(playback: Playback) {
        if (history.contains(playback)) {
            history.remove(playback)
            history.add(0, playback)
        } else {
            history.add(0, playback)
            if (history.size > maxPlaybacksInHistory)
                shrinkHistory()
        }
        onHistoryChanged?.invoke(history)
    }

    fun clearHistory() {
        if (history.isNotEmpty()) {
            history.clear()
            onHistoryChanged?.invoke(history)
        }
    }

    /**
     * Shrinks length of [history] to [maxPlaybacksInHistory]
     */
    private fun shrinkHistory() {
        history.subList(0, history.size - maxPlaybacksInHistory).clear()
    }

    operator fun plusAssign(loop: Loop) {
        loops += loop
    }

    operator fun plusAssign(playlist: Playlist) {
        playlists += playlist
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is Metadata) return false

        if (ignoreAudioFocus != other.ignoreAudioFocus) return false
        if (combineDifferentPlaybackTypes != other.combineDifferentPlaybackTypes) return false
        if (songIncDecInterval != other.songIncDecInterval) return false
        if (audioUpdateInterval != other.audioUpdateInterval) return false
        if (maxPlaybacksInHistory != other.maxPlaybacksInHistory) return false
        if (loops != other.loops) return false
        if (playlists != other.playlists) return false

        return true
    }
}