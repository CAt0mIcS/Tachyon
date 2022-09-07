package com.tachyonmusic.user.data

import android.util.Log
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.google.gson.ToNumberPolicy
import com.google.gson.internal.LinkedTreeMap
import com.google.gson.reflect.TypeToken
import com.tachyonmusic.core.data.playback.RemoteLoop
import com.tachyonmusic.core.data.playback.RemotePlaylist
import com.tachyonmusic.core.domain.playback.Loop
import com.tachyonmusic.core.domain.playback.Playlist
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.runBlocking

class Metadata(autoComplete: Boolean = true) {
    companion object {
        const val TAG = "UserMetadata"
    }

    var ignoreAudioFocus = false
    var combineDifferentPlaybackTypes = false
    var songIncDecInterval = 100
    var audioUpdateInterval = 100
    var maxPlaybacksInHistory = 25

    var loops: CompletableDeferred<ArrayList<Loop>> = CompletableDeferred()
    var playlists: CompletableDeferred<ArrayList<Playlist>> = CompletableDeferred()

//    val history: MutableList<Playback> = mutableListOf()

    init {
        if(autoComplete) {
            loops.complete(arrayListOf())
            playlists.complete(arrayListOf())
        }
    }

    constructor(
        data: Map<String, Any?>,
//        onHistoryChanged: ((MutableList<Playback>) -> Unit)?
    ) : this(false) {
        ignoreAudioFocus = data["ignoreAudioFocus"] as Boolean? ?: false
        combineDifferentPlaybackTypes = data["combineDifferentPlaybackTypes"] as Boolean? ?: false
        songIncDecInterval = (data["songIncDecInterval"] as Long? ?: 100L).toInt()
        audioUpdateInterval = (data["audioUpdateInterval"] as Long? ?: 100L).toInt()
        maxPlaybacksInHistory = (data["maxPlaybacksInHistory"] as Long? ?: 25L).toInt()

        val loadedLoops =
            ((data["loops"] as List<LinkedTreeMap<String, Any?>?>?)?.map {
                RemoteLoop.build(it!!)
            } as ArrayList<Loop>?)
                ?: arrayListOf()
        loops.complete(loadedLoops)

        val loadedPlaylists =((data["playlists"] as List<LinkedTreeMap<String, Any?>?>?)?.map {
            RemotePlaylist.build(it!!)
        } as ArrayList<Playlist>?)
            ?: arrayListOf()
        playlists.complete(loadedPlaylists)

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

    // TODO TODO TODO: Store Gson instance in Hilt

    constructor(map: String) : this(
        GsonBuilder().setObjectToNumberStrategy(ToNumberPolicy.LONG_OR_DOUBLE).create()
            .fromJson<LinkedTreeMap<String, Any>>(map, LinkedTreeMap::class.java)
    )

    fun toHashMap() = hashMapOf(
        "ignoreAudioFocus" to ignoreAudioFocus,
        "combineDifferentPlaybackTypes" to combineDifferentPlaybackTypes,
        "songIncDecInterval" to songIncDecInterval,
        "audioUpdateInterval" to audioUpdateInterval,
        "maxPlaybacksInHistory" to maxPlaybacksInHistory,
        runBlocking {
            "loops" to loops.await().map { it.toHashMap() }
        },
        runBlocking {
            "playlists" to playlists.await().map { it.toHashMap() }
        },
//        "history" to history.map { it.mediaId.toString() }
    )

    override fun toString(): String =
        GsonBuilder().setObjectToNumberStrategy(ToNumberPolicy.LONG_OR_DOUBLE).create()
            .toJson(toHashMap())

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