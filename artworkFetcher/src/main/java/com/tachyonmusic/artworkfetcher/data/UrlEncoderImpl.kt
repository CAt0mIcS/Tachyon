package com.tachyonmusic.artworkfetcher.data

import com.tachyonmusic.artworkfetcher.R
import com.tachyonmusic.artworkfetcher.domain.UrlEncoder
import com.tachyonmusic.util.Resource
import com.tachyonmusic.util.UiText
import java.io.UnsupportedEncodingException
import java.net.URLEncoder


class UrlEncoderImpl : UrlEncoder {
    override fun encode(baseUrl: String, params: Map<String, String>): Resource<String> {
        if (params.isEmpty())
            return Resource.Error(UiText.StringResource(R.string.encoder_error_no_arguments))

        val sb = StringBuilder()
        sb.append("$baseUrl?")

        for (param in params) {
            if (!sb.endsWith('?'))
                sb.append("&")

            val encodedKey = encodeUtf8(param.key)
            if (encodedKey is Resource.Error)
                return encodedKey

            val encodedValue = encodeUtf8(param.value)
            if (encodedValue is Resource.Error)
                return encodedValue

            if (encodedKey.data == null || encodedValue.data == null)
                return Resource.Error(
                    UiText.StringResource(
                        R.string.unknown_encoder_error,
                        "${param.key}|${param.value}"
                    )
                )

            sb.append("${encodedKey.data!!}=${encodedValue.data!!}")
        }
        return Resource.Success(sb.toString())
    }

    private fun encodeUtf8(value: String): Resource<String> {
        return try {
            Resource.Success(URLEncoder.encode(value, "UTF-8"))
        } catch (e: UnsupportedEncodingException) {
            Resource.Error(
                if (e.localizedMessage != null)
                    UiText.DynamicString(e.localizedMessage!!)
                else UiText.StringResource(R.string.unknown_encoder_error, value)
            )
        }
    }
}