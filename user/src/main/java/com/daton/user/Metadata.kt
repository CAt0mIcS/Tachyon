package com.daton.user

import com.daton.media.playback.Loop
import com.daton.media.playback.Playlist

class Metadata() {
    var ignoreAudioFocus = false
    var combineDifferentPlaybackTypes = false
    var songIncDecInterval = 100
    var audioUpdateInterval = 100
    var maxPlaybacksInHistory = 25

    var loops: ArrayList<Loop> = arrayListOf()
    var playlists: ArrayList<Playlist> = arrayListOf()
//    val history: MutableList<Playback>

    constructor(data: Map<String, Any?>) : this() {
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
    }

    fun toHashMap() = hashMapOf(
        "ignoreAudioFocus" to ignoreAudioFocus,
        "combineDifferentPlaybackTypes" to combineDifferentPlaybackTypes,
        "songIncDecInterval" to songIncDecInterval,
        "audioUpdateInterval" to audioUpdateInterval,
        "maxPlaybacksInHistory" to maxPlaybacksInHistory,

        "loops" to loops.map { it.toHashMap() },
        "playlists" to playlists.map { it.toHashMap() }
    )

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