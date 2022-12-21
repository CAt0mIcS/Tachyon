package com.tachyonmusic.core.data

import android.os.Parcel
import android.os.Parcelable
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.bumptech.glide.integration.compose.ExperimentalGlideComposeApi
import com.bumptech.glide.integration.compose.GlideImage
import com.tachyonmusic.core.domain.Artwork
import java.net.URI


/**
 * Artwork which can be downloaded using a link
 */
class RemoteArtwork(
    val uri: URI
) : Artwork {
    @OptIn(ExperimentalGlideComposeApi::class)
    @Composable
    override fun Image(contentDescription: String?, modifier: Modifier) {
        val url = try {
            uri.toURL().toString()
        } catch (e: java.lang.IllegalArgumentException) {
            TODO("Invalid URI: $uri: ${e.localizedMessage}")
        }

        GlideImage(
            model = url,
            contentDescription = contentDescription,
            modifier = modifier
        )

    }

    // TODO: GlideLazyListPreloader

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(uri.toString())
    }

    companion object CREATOR : Parcelable.Creator<RemoteArtwork> {
        override fun createFromParcel(parcel: Parcel) = RemoteArtwork(URI(parcel.readString()))
        override fun newArray(size: Int) = arrayOfNulls<RemoteArtwork?>(size)
    }
}