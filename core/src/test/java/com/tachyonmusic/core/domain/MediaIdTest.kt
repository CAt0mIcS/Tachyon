package com.tachyonmusic.core.domain

import android.annotation.SuppressLint
import com.google.common.truth.Truth.assertThat
import org.junit.Test

@SuppressLint("CheckResult")
class MediaIdTest {

    @Test
    fun of_remote_loop_creates_correct_media_id() {
        val name = "LoopNameHere"
        val songMediaId = MediaId("*0*/SomeSon|||g.mp3")
        val mediaId = MediaId.ofRemoteLoop(name, songMediaId)
        assertThat(mediaId.source == "*1*$name")
        assertThat(mediaId.underlyingMediaId == songMediaId)
        assertThat(mediaId.isRemoteLoop)
    }

    @Test
    fun of_remote_playlist_creates_correct_media_id() {
        val name = "PlaylistNameHere"
        val mediaId = MediaId.ofRemotePlaylist(name)
        assertThat(mediaId.source == "*2*$name")
        assertThat(mediaId.isRemotePlaylist)
    }

    @Test
    fun deserialize_deserializes_song_correctly() {
        val songSource = "*0*/SomePath/SomeSong - BySomeArtist.mp3"
        assertThat(MediaId.deserialize(songSource).source == songSource)
    }
}