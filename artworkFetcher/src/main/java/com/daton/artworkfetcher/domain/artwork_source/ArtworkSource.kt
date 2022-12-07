package com.daton.artworkfetcher.domain.artwork_source

import com.daton.artworkfetcher.R
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.tachyonmusic.util.Resource
import com.tachyonmusic.util.UiText
import java.io.FileNotFoundException

abstract class ArtworkSource {
    companion object {
        val GSON: Gson = GsonBuilder().create()
    }

    fun search(title: String, artist: String, imageSize: Int): Resource<String> {
        val url = getSearchUrl(title, artist)
        if (url is Resource.Error)
            return url
        if (url.data == null)
            return Resource.Error(
                UiText.StringResource(
                    R.string.unknown_encoder_error,
                    "$title, $artist"
                )
            )

        // TODO: Maybe replace [URL.readText] with something better?
        return try {
            executeSearch(url.data!!, imageSize)
        } catch (e: FileNotFoundException) {
            Resource.Error(
                if (e.localizedMessage != null)
                    UiText.DynamicString(e.localizedMessage!!)
                else
                    UiText.StringResource(R.string.request_to_url_failed, url.data!!)
            )
        }
    }

    abstract fun getSearchUrl(title: String, artist: String): Resource<String>
    abstract fun executeSearch(url: String, imageSize: Int): Resource<String>
}