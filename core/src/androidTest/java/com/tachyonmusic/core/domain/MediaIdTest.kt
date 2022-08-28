package com.tachyonmusic.core.domain

import android.annotation.SuppressLint
import com.google.common.truth.Truth.assertThat
import org.junit.Test
import java.io.File


@SuppressLint("CheckResult")
class MediaIdTest {

    @Test
    fun of_local_song_creates_correct_media_id() {
        val path = File("/users/0/shared_storage/Music/TestSong - ByArtist.mp3")
        val mediaId = MediaId.ofLocalSong(path)

        val expectedSource = "*0*${path.absolutePath}"
        assertThat(mediaId.source == expectedSource)
        assertThat(mediaId.isLocalSong)
    }
}