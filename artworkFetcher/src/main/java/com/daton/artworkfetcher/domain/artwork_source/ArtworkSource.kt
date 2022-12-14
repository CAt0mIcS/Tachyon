package com.daton.artworkfetcher.domain.artwork_source

import com.daton.artworkfetcher.R
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.tachyonmusic.util.Resource
import com.tachyonmusic.util.UiText
import org.jsoup.HttpStatusException
import java.io.FileNotFoundException
import java.net.SocketException
import java.net.SocketTimeoutException
import java.net.UnknownHostException

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

        return try {
            executeSearch(url.data!!, imageSize)
        } catch (e: FileNotFoundException) {
            requestFailed(e, url.data)
        } catch (e: UnknownHostException) {
            requestFailed(e, url.data)
        } catch (e: HttpStatusException) {
            requestFailed(e, url.data)
        } catch (e: SocketException) {
            requestFailed(e, url.data)
        } catch (e: SocketTimeoutException) {
            requestFailed(e, url.data)
        }
    }

    abstract fun getSearchUrl(title: String, artist: String): Resource<String>
    abstract fun executeSearch(url: String, imageSize: Int): Resource<String>

    private fun requestFailed(e: Exception, url: String?) =
        Resource.Error<String>(
            message = UiText.StringResource(R.string.request_to_url_failed, url ?: "Unknown URL"),
            exception = e
        )
}