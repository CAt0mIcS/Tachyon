package com.tachyonmusic.core.domain

import android.annotation.SuppressLint
import com.tachyonmusic.core.data.constants.Constants
import com.tachyonmusic.testutils.assertEquals
import com.tachyonmusic.testutils.assertNotEquals
import com.tachyonmusic.util.File
import org.junit.Test


@SuppressLint("CheckResult")
internal class MediaIdTest {

    @Test
    fun of_local_song_creates_correct_media_id() {
        val path =
            File("${Constants.EXTERNAL_STORAGE_DIRECTORY}/Music/DANGER - 0.59 - MrSuicideSheep.mp3")
        val mediaId = MediaId.ofLocalSong(path)

        val expectedSource = "*0*/Music/DANGER - 0.59 - MrSuicideSheep.mp3"
        assertEquals(mediaId.source, expectedSource)
        assert(mediaId.isLocalSong)
        assertNotEquals(mediaId.uri, null)
        assertEquals(mediaId.uri!!.absolutePath, path.absolutePath)
    }
}