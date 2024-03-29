package com.tachyonmusic.artworkfetcher.data.artwork_source

import com.google.gson.JsonObject
import com.tachyonmusic.artworkfetcher.R
import com.tachyonmusic.artworkfetcher.data.UrlEncoderImpl
import com.tachyonmusic.artworkfetcher.domain.UrlEncoder
import com.tachyonmusic.artworkfetcher.domain.artwork_source.ArtworkSource
import com.tachyonmusic.util.Resource
import com.tachyonmusic.util.UiText
import java.net.URL

class ITunesArtworkSource(
    private val urlEncoder: UrlEncoder = UrlEncoderImpl()
) : ArtworkSource() {
    companion object {
        const val SEARCH_URL = "https://itunes.apple.com/search"
        const val DEFAULT_RESOLUTION = 60
        const val DEFAULT_RESOLUTION_KEY = "artworkUrl$DEFAULT_RESOLUTION"
        const val DEFAULT_RESOLUTION_URL = "${DEFAULT_RESOLUTION}x$DEFAULT_RESOLUTION"
    }

    override fun getSearchUrl(query: String): Resource<String> {
        val urlParams = mapOf(
            "media" to "music",
            "entity" to "album",
            "term" to query
        )
        return urlEncoder.encode(SEARCH_URL, urlParams)
    }

    override fun executeSearch(url: String, imageSize: Int, pageSize: Int): Resource<String> {
        // TODO: Maybe replace [URL.readText] with something better?
        val response = URL(url).readText()
        val obj = GSON.fromJson(response, JsonObject::class.java)

        if (!obj.has("results") ||
            !obj.has("resultCount") ||
            obj["resultCount"].asInt == 0 ||
            !obj["results"].isJsonArray
        )
            return Resource.Error(
                UiText.StringResource(
                    R.string.artwork_api_invalid_json,
                    response
                )
            )

        val results = obj["results"].asJsonArray
        if (!results[0].isJsonObject || !results[0].asJsonObject.has(DEFAULT_RESOLUTION_KEY))
            return Resource.Error(
                UiText.StringResource(
                    R.string.artwork_api_invalid_json,
                    response
                )
            )

        var imageUrl = results[0].asJsonObject[DEFAULT_RESOLUTION_KEY].asString
        if (DEFAULT_RESOLUTION != imageSize && imageUrl.lastIndexOf(DEFAULT_RESOLUTION_URL) != -1) {
            imageUrl = imageUrl.replace(DEFAULT_RESOLUTION_URL, "${imageSize}x$imageSize")
        }

        return Resource.Success(imageUrl)
    }
}