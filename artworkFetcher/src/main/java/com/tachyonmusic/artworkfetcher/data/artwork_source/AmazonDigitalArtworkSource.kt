package com.tachyonmusic.artworkfetcher.data.artwork_source

import com.tachyonmusic.artworkfetcher.R
import com.tachyonmusic.artworkfetcher.data.UrlEncoderImpl
import com.tachyonmusic.artworkfetcher.domain.UrlEncoder
import com.tachyonmusic.artworkfetcher.domain.artwork_source.ArtworkSource
import com.tachyonmusic.util.Resource
import com.tachyonmusic.util.UiText
import org.jsoup.Jsoup
import org.jsoup.nodes.Element
import android.net.Uri

class AmazonDigitalArtworkSource(
    private val urlEncoder: UrlEncoder = UrlEncoderImpl()
) : ArtworkSource() {
    companion object {
        const val BASE_URL = "https://www.amazon.com/s"
        const val USER_AGENT =
            "Mozilla/5.0 (Windows NT 10.0; Win64; x64; rv:107.0) Gecko/20100101 Firefox/107.0"
    }

    override fun getSearchUrl(query: String): Resource<String> {
        val urlParams = mapOf(
            "k" to query,
            "i" to "digital-music",
            "s" to "relevancerank"
        )
        return urlEncoder.encode(BASE_URL, urlParams)
    }

    override fun executeSearch(url: String, imageSize: Int, pageSize: Int): Resource<String> {
        val doc = Jsoup.connect(url)
            .userAgent(USER_AGENT)
            .headers(
                mapOf(
                    // Mimic Firefox header to avoid being detected as bot
                    "User-Agent" to USER_AGENT,
                    "Accept" to "text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8",
                    "Accept-Language" to "en-US,en;q=0.9",
                    "DNT" to "1",
                    "Connection" to "Keep-Alive",
                    "Upgrade-Insecure-Requests" to "1",
                    "Cache-Control" to "max-age=0",
                    "TE" to "Trailers",
                )
            )
            .get()
        val searchResult =
            doc.getElementsByAttributeValue("cel_widget_id", "MAIN-SEARCH_RESULTS-1").firstOrNull()
                ?: return Resource.Error(
                    UiText.StringResource(R.string.no_artwork, url)
                )

        var imageElement: Element? = null
        for (child in searchResult.children()) {
            imageElement = getChildWithClass(child, "s-image")
            if (imageElement != null)
                break
        }

        if (imageElement == null)
            return Resource.Error(UiText.StringResource(R.string.no_artwork, url))

        val srcSet = imageElement.attributes()["srcset"]
        val srcSetWithoutLastSpace = srcSet.substring(0, srcSet.indexOfLast { it == ' ' })
        val lastSpaceIndex = srcSetWithoutLastSpace.indexOfLast { it == ' ' }
        if (lastSpaceIndex < 0)
            return Resource.Error(UiText.StringResource(R.string.no_artwork, url))

        val imageUrl = srcSetWithoutLastSpace.substring(lastSpaceIndex + 1)

        if (!Uri.parse(imageUrl).isAbsolute)
            return Resource.Error(
                UiText.StringResource(
                    R.string.src_set_parsing_failed,
                    srcSet,
                    imageUrl
                )
            )

        return Resource.Success(imageUrl)
    }

    private fun getChildWithClass(element: Element, className: String): Element? {
        if (element.className().lowercase() == className.lowercase())
            return element

        for (child in element.children()) {
            val possibleClassChild = getChildWithClass(child, className)
            if (possibleClassChild != null)
                return possibleClassChild
        }

        return null
    }
}