package com.daton.artworkdownloader.data

import com.daton.artworkdownloader.domain.UrlEncoder
import com.tachyonmusic.util.Resource
import org.junit.Test

class UrlEncoderTest {
    val encoders = listOf<UrlEncoder>(
        UrlEncoderImpl()
    )

    @Test
    fun `Empty params, returns Resource Error`() = each {
        assert(it.encode("BaseUrl.net", mapOf()) is Resource.Error)
    }

    @Test
    fun `Valid input, returns correct output`() = each {
        val urlParams = mapOf(
            "media" to "music",
            "entity" to "album",
            "term" to "Hello World"
        )

        val encoded = it.encode("BaseUrl.com", urlParams)
        assert(encoded is Resource.Success)
        assert(encoded.data == "BaseUrl.com?media=music&entity=album&term=Hello+World")
    }

    private fun each(action: (UrlEncoder) -> Unit) {
        for (encoder in encoders)
            action(encoder)
    }
}