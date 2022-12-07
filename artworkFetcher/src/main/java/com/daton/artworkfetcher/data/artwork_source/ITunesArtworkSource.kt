package com.daton.artworkfetcher.data.artwork_source

import com.daton.artworkfetcher.R
import com.daton.artworkfetcher.data.UrlEncoderImpl
import com.daton.artworkfetcher.domain.UrlEncoder
import com.daton.artworkfetcher.domain.artwork_source.ArtworkSource
import com.google.gson.JsonObject
import com.tachyonmusic.util.Resource
import com.tachyonmusic.util.UiText

class ITunesArtworkSource(
    private val urlEncoder: UrlEncoder = UrlEncoderImpl()
) : ArtworkSource() {
    companion object {
        const val SEARCH_URL = "https://itunes.apple.com/search"
        const val DEFAULT_RESOLUTION = 60
        const val DEFAULT_RESOLUTION_KEY = "artworkUrl$DEFAULT_RESOLUTION"
        const val DEFAULT_RESOLUTION_URL = "${DEFAULT_RESOLUTION}x$DEFAULT_RESOLUTION"
    }

    override fun getSearchUrl(title: String, artist: String): Resource<String> {
        val urlParams = mapOf(
            "media" to "music",
            "entity" to "album",
            "term" to "$artist $title"
        )
        return urlEncoder.encode(SEARCH_URL, urlParams)
    }

    override fun parseSearchResult(result: String, imageSize: Int): Resource<String> {
        val obj = GSON.fromJson(result, JsonObject::class.java)

        if (!obj.has("results") ||
            !obj.has("resultCount") ||
            obj["resultCount"].asInt == 0 ||
            !obj["results"].isJsonArray
        )
            return Resource.Error(UiText.StringResource(R.string.artwork_api_invalid_json, result))

        val results = obj["results"].asJsonArray
        if (!results[0].isJsonObject || !results[0].asJsonObject.has(DEFAULT_RESOLUTION_KEY))
            return Resource.Error(UiText.StringResource(R.string.artwork_api_invalid_json, result))

        var url = results[0].asJsonObject[DEFAULT_RESOLUTION_KEY].asString
        if (DEFAULT_RESOLUTION != imageSize && url.lastIndexOf(DEFAULT_RESOLUTION_URL) != -1) {
            url = url.replace(DEFAULT_RESOLUTION_URL, "${imageSize}x$imageSize")
        }

        return Resource.Success(url)
    }
}