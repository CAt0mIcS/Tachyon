package com.tachyonmusic.database.util

import com.tachyonmusic.core.domain.MediaId
import com.tachyonmusic.database.domain.ArtworkType
import com.tachyonmusic.database.domain.model.SinglePlaybackEntity
import org.junit.Test


internal class GetArtworkForPlaybackTest {
    @Test
    fun `Null entity, returns null artwork`() {
        assert(getArtworkForPlayback(null) == null)
    }

    @Test
    fun `NO_ARTWORK entity, returns null artwork`() {
        assert(
            getArtworkForPlayback(
                SinglePlaybackEntity(
                    MediaId(""),
                    "",
                    "",
                    0,
                    ArtworkType.NO_ARTWORK,
                    "bad url"
                )
            ) == null
        )
    }

    @Test
    fun `Invalid artwork type, returns null`() {
        assert(
            getArtworkForPlayback(
                SinglePlaybackEntity(
                    MediaId(""),
                    "",
                    "",
                    0,
                    "Some bad artwork type here",
                    "bad url"
                )
            ) == null
        )
    }

    @Test
    fun `REMOTE artwork type invalid url, returns null`() {
        assert(
            getArtworkForPlayback(
                SinglePlaybackEntity(
                    MediaId(""),
                    "",
                    "",
                    0,
                    ArtworkType.REMOTE,
                    artworkUrl = "   \n   \t  "
                )
            ) == null
        )

        assert(
            getArtworkForPlayback(
                SinglePlaybackEntity(
                    MediaId(""),
                    "",
                    "",
                    0,
                    ArtworkType.REMOTE,
                    artworkUrl = null
                )
            ) == null
        )
    }
}