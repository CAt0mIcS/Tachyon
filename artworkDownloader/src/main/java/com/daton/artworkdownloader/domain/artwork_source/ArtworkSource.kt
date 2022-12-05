package com.daton.artworkdownloader.domain.artwork_source

import com.daton.artworkdownloader.R
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.tachyonmusic.util.Resource
import com.tachyonmusic.util.UiText
import java.net.URL

abstract class ArtworkSource {
    companion object {
        val GSON: Gson = GsonBuilder().create()
    }

    fun search(title: String, artist: String, imageSize: Int): Resource<String> {
        if (imageSize <= 0)
            return Resource.Error(
                UiText.StringResource(
                    R.string.invalid_image_size,
                    imageSize.toString()
                )
            )
        if (title.isBlank() || artist.isBlank())
            return Resource.Error(
                UiText.StringResource(
                    R.string.invalid_media_metadata,
                    title, artist
                )
            )

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
        return parseSearchResult(URL(url.data!!).readText(), imageSize)
    }

    abstract fun getSearchUrl(title: String, artist: String): Resource<String>
    abstract fun parseSearchResult(result: String, imageSize: Int): Resource<String>
}