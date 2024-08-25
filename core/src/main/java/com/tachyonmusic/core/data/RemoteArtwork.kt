package com.tachyonmusic.core.data

import android.os.Parcel
import android.os.Parcelable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import com.skydoves.landscapist.ImageOptions
import com.skydoves.landscapist.glide.GlideImage
import com.tachyonmusic.core.data.constants.ArtworkLoadingIndicator
import com.tachyonmusic.core.data.constants.PlaceholderArtwork
import com.tachyonmusic.core.domain.Artwork
import java.net.URI


/**
 * Artwork which can be downloaded using a link
 */
class RemoteArtwork(
    val uri: URI
) : Artwork {
    override val isLoaded = true

    @Composable
    override fun Image(contentDescription: String?, modifier: Modifier, contentScale: ContentScale) {
        val url = try {
            uri.toURL().toString()
        } catch (e: java.lang.IllegalArgumentException) {
            TODO("Invalid Uri: $uri: ${e.localizedMessage}")
        }

        Box(modifier) {
            GlideImage(
                modifier = Modifier.fillMaxSize(),
                imageModel = { url },
                imageOptions = ImageOptions(
                    contentDescription = contentDescription,
                    contentScale = contentScale
                ),
                failure = {
                    PlaceholderArtwork(contentDescription, modifier, contentScale)
                },
                loading = {
                    ArtworkLoadingIndicator(modifier)
                }
            )
        }
    }

    override fun equals(other: Any?) = other is RemoteArtwork && other.uri == uri

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(uri.toString())
    }

    companion object CREATOR : Parcelable.Creator<RemoteArtwork> {
        override fun createFromParcel(parcel: Parcel) = RemoteArtwork(URI(parcel.readString()))
        override fun newArray(size: Int) = arrayOfNulls<RemoteArtwork?>(size)
    }
}