package com.tachyonmusic.user.data

import android.util.Log
import com.google.gson.Gson
import com.google.gson.internal.LinkedTreeMap
import com.tachyonmusic.core.data.playback.LocalSongImpl
import com.tachyonmusic.core.data.playback.RemoteLoopImpl
import com.tachyonmusic.core.data.playback.RemotePlaylistImpl
import com.tachyonmusic.core.domain.MediaId
import com.tachyonmusic.core.domain.playback.Loop
import com.tachyonmusic.core.domain.playback.Playback
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
    private val _history: MutableStateFlow<List<Playback>> = MutableStateFlow(listOf())

    val loops: StateFlow<List<Loop>>
        get() = _loops
    val playlists: StateFlow<List<Playlist>>
        get() = _playlists
    val history: StateFlow<List<Playback>>
        get() = _history


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

        // TODO: Don't use [Playback]Impl here

        val loadedLoops =
            ((data["loops"] as List<Map<String, Any?>?>?)?.map {
                RemoteLoopImpl.build(it!!)
            } as ArrayList<Loop>?)
                ?: arrayListOf()
        _loops.value = loadedLoops

        val loadedPlaylists = ((data["playlists"] as List<Map<String, Any?>?>?)?.map {
            RemotePlaylistImpl.build(it!!)
        } as ArrayList<Playlist>?)
            ?: arrayListOf()
        _playlists.value = loadedPlaylists


        (data["history"] as List<String?>?)?.forEach { mediaIdStr ->
            val mediaId = MediaId.deserializeIfValid(mediaIdStr)
            if (mediaId != null) {
                if (mediaId.isLocalSong)
                    _history.value += LocalSongImpl.build(mediaId)
                else if (mediaId.isRemoteLoop)
                    loadedLoops.find { it.mediaId == mediaId }?.let { _history.value += it }
                else
                    loadedPlaylists.find { it.mediaId == mediaId }?.let { _history.value += it }
            }
        }
        shrinkHistory()

        Log.d(TAG, "Finished loading metadata")
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
        "history" to history.value.map { it.mediaId.toString() }
    )

    override fun toString(): String = gson.toJson(toHashMap())

    fun addHistory(playback: Playback) {
        if (history.value.contains(playback)) {
            _history.value = history.value.toMutableList().apply {
                remove(playback)
                add(0, playback)
            }
        } else {
            if (history.value.size + 1 <= maxPlaybacksInHistory)
                _history.value = history.value.toMutableList().apply {
                    add(0, playback)
                }
            shrinkHistory()
        }
    }

    fun clearHistory() {
        if (history.value.isNotEmpty())
            _history.value = listOf()
    }

    /**
     * Shrinks length of [history] to [maxPlaybacksInHistory]
     */
    private fun shrinkHistory() {
        if (history.value.size > maxPlaybacksInHistory) {
            _history.value = history.value.subList(0, maxPlaybacksInHistory)
        }
    }

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
        if (_history != other._history) return false

        return true
    }
}