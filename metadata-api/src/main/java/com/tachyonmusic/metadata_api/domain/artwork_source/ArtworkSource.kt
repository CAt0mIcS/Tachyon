package com.tachyonmusic.metadata_api.domain.artwork_source

import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.tachyonmusic.metadata_api.R
import com.tachyonmusic.metadata_api.domain.model.SearchInfo
import com.tachyonmusic.util.Resource
import com.tachyonmusic.util.UiText
import org.jsoup.HttpStatusException
import java.io.FileNotFoundException
import java.io.IOException
import java.net.SocketException
import java.net.SocketTimeoutException
import java.net.UnknownHostException

// TODO: MusicBrainz API and CoverArtArchive
/**
 * https://musicbrainz.org/doc/MusicBrainz_API
 * https://musicbrainz.org/release/5be9bb1e-586e-4cb2-b4b1-52693339fadb/details
 * https://musicbrainz.org/doc/Cover_Art_Archive/API
 * https://coverartarchive.org/release-group/5be9bb1e-586e-4cb2-b4b1-52693339fadb
 */

abstract class ArtworkSource {
    companion object {
        val GSON: Gson = GsonBuilder().create()
    }

    suspend fun search(info: SearchInfo, imageSize: Int, pageSize: Int): Resource<String> {
        val url = getSearchUrl(info)
        if (url is Resource.Error)
            return url
        if (url.data == null)
            return Resource.Error(UiText.StringResource(R.string.unknown_encoder_error, info.toString()))

        return try {
            executeSearch(info, imageSize, pageSize)
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
        } catch (e: IOException) {
            requestFailed(e, url.data)
        }
    }

    abstract fun getSearchUrl(info: SearchInfo): Resource<String>
    abstract suspend fun executeSearch(info: SearchInfo, imageSize: Int, pageSize: Int): Resource<String>

    private fun requestFailed(e: Exception, url: String?) =
        Resource.Error<String>(
            message = UiText.StringResource(R.string.request_to_url_failed, url ?: "Unknown URL"),
            exception = e
        )
}