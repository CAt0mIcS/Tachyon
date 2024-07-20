package com.tachyonmusic.core.domain

import android.annotation.SuppressLint
import com.tachyonmusic.testutils.assertEquals
import org.junit.Test

@SuppressLint("CheckResult")
internal class MediaIdTest {

    @Test
    fun of_remote_remix_creates_correct_media_id() {
        val name = "CustomizedSongNameHere"
        val songMediaId = MediaId("*0*/SomeSon|||g.mp3")
        val mediaId = MediaId.ofLocalRemix(name, songMediaId)
        assertEquals(mediaId.source, "*1*$name")
        assertEquals(mediaId.underlyingMediaId, songMediaId)
        assert(mediaId.isLocalRemix)
    }

    @Test
    fun of_remote_playlist_creates_correct_media_id() {
        val name = "PlaylistNameHere"
        val mediaId = MediaId.ofLocalPlaylist(name)
        assertEquals(mediaId.source, "*2*$name")
        assert(mediaId.isLocalPlaylist)
    }

    @Test
    fun deserialize_deserializes_song_correctly() {
        val songSource = "*0*/SomePath/SomeSong - BySomeArtist.mp3"
        assertEquals(MediaId.deserialize(songSource).source, songSource)
    }
}