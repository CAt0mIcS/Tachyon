package com.tachyonmusic.user.data

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.google.gson.Gson
import com.google.gson.internal.LinkedTreeMap
import com.tachyonmusic.core.data.playback.RemoteLoop
import com.tachyonmusic.core.data.playback.RemotePlaylist
import com.tachyonmusic.core.domain.playback.Loop
import com.tachyonmusic.core.domain.playback.Playlist
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class Metadata(private val gson: Gson) {
    companion object {
        const val TAG = "UserMetadata"
    }

    var ignoreAudioFocus = false
    var combineDifferentPlaybackTypes = false
    var songIncDecInterval = 100
    var audioUpdateInterval = 100
    var maxPlaybacksInHistory = 25

    private val _loops: MutableStateFlow<List<Loop>> = MutableStateFlow(listOf())
    private val _playlists: MutableStateFlow<List<Playlist>> = MutableStateFlow(listOf())

    val loops: StateFlow<List<Loop>>
        get() = _loops
    val playlists: StateFlow<List<Playlist>>
        get() = _playlists

//    val history: MutableList<Playback> = mutableListOf()

    constructor(
        gson: Gson,
        data: Map<String, Any?>,
//        onHistoryChanged: ((MutableList<Playback>) -> Unit)?
    ) : this(gson) {
        ignoreAudioFocus = data["ignoreAudioFocus"] as Boolean? ?: false
        combineDifferentPlaybackTypes = data["combineDifferentPlaybackTypes"] as Boolean? ?: false
        songIncDecInterval = (data["songIncDecInterval"] as Long? ?: 100L).toInt()
        audioUpdateInterval = (data["audioUpdateInterval"] as Long? ?: 100L).toInt()
        maxPlaybacksInHistory = (data["maxPlaybacksInHistory"] as Long? ?: 25L).toInt()

        val loadedLoops =
            ((data["loops"] as List<Map<String, Any?>?>?)?.map {
                RemoteLoop.build(it!!)
            } as ArrayList<Loop>?)
                ?: arrayListOf()
        _loops.value = loadedLoops

        val loadedPlaylists = ((data["playlists"] as List<Map<String, Any?>?>?)?.map {
            RemotePlaylist.build(it!!)
        } as ArrayList<Playlist>?)
            ?: arrayListOf()
        _playlists.value = loadedPlaylists

        // TODO: Unable to find loop the first time history loads on release builds only
//        (data["history"] as List<String?>?)?.forEach { mediaIdStr ->
//            val mediaId = MediaId.deserialize(mediaIdStr!!)
//            if (mediaId.isSong)
//                history.add(Song(mediaId))
//            else if (mediaId.isLoop)
//                loadedLoops.find { it.mediaId == mediaId }?.let { history.add(it) }
//            else
//                loadedPlaylists.find { it.mediaId == mediaId }?.let { history.add(it) }
//        }

        Log.d(TAG, "Finished loading metadata")

//        this.onHistoryChanged = onHistoryChanged
//        if (history.isNotEmpty())
//            launch(Dispatchers.Main) { this@Metadata.onHistoryChanged?.invoke(history) }
    }

    constructor(gson: Gson, map: String) : this(
        gson, gson.fromJson<LinkedTreeMap<String, Any>>(map, LinkedTreeMap::class.java)
    )

    fun toHashMap() = hashMapOf(
        "ignoreAudioFocus" to ignoreAudioFocus,
        "combineDifferentPlaybackTypes" to combineDifferentPlaybackTypes,
        "songIncDecInterval" to songIncDecInterval,
        "audioUpdateInterval" to audioUpdateInterval,
        "maxPlaybacksInHistory" to maxPlaybacksInHistory,
        "loops" to loops.value.map { it.toHashMap() },
        "playlists" to playlists.value.map { it.toHashMap() },
//        "history" to history.map { it.mediaId.toString() }
    )

    override fun toString(): String = gson.toJson(toHashMap())

//    fun addHistory(playback: Playback) {
//        if (history.contains(playback)) {
//            history.remove(playback)
//            history.add(0, playback)
//        } else {
//            history.add(0, playback)
//            if (history.size > maxPlaybacksInHistory)
//                shrinkHistory()
//        }
//        onHistoryChanged?.invoke(history)
//    }
//
//    fun clearHistory() {
//        if (history.isNotEmpty()) {
//            history.clear()
//            onHistoryChanged?.invoke(history)
//        }
//    }

    /**
     * Shrinks length of [history] to [maxPlaybacksInHistory]
     */
//    private fun shrinkHistory() {
//        history.subList(0, history.size - maxPlaybacksInHistory).clear()
//    }

    operator fun plusAssign(loop: Loop) {
        val newList = _loops.value + loop
        newList.sortedBy { it.name + it.title + it.artist }
        _loops.value = newList
    }

    operator fun plusAssign(playlist: Playlist) {
        val newList = _playlists.value + playlist
        newList.sortedBy { it.name }
        _playlists.value = newList
    }

    operator fun minusAssign(loop: Loop) {
        _loops.value -= loop
    }

    operator fun minusAssign(playlist: Playlist) {
        _playlists.value -= playlist
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is Metadata) return false

        if (ignoreAudioFocus != other.ignoreAudioFocus) return false
        if (combineDifferentPlaybackTypes != other.combineDifferentPlaybackTypes) return false
        if (songIncDecInterval != other.songIncDecInterval) return false
        if (audioUpdateInterval != other.audioUpdateInterval) return false
        if (maxPlaybacksInHistory != other.maxPlaybacksInHistory) return false
        if (_loops != other._loops) return false
        if (_playlists != other._playlists) return false

        return true
    }
}