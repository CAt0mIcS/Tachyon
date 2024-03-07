package com.tachyonmusic.core.data

import android.graphics.Bitmap
import android.net.Uri
import android.os.Parcel
import android.os.Parcelable
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import com.tachyonmusic.core.data.constants.PlaceholderArtwork
import com.tachyonmusic.core.domain.Artwork
import com.tachyonmusic.core.domain.SongMetadataExtractor


/**
 * Artwork that is embedded in the audio file
 */
class EmbeddedArtwork(
    val bitmap: Bitmap?,
    val uri: Uri
) : Artwork {

    override val isLoaded: Boolean
        get() = bitmap != null

    @Composable
    override fun Image(contentDescription: String?, modifier: Modifier, contentScale: ContentScale) {
        if (bitmap != null)
            androidx.compose.foundation.Image(
                bitmap = bitmap.asImageBitmap(),
                contentDescription = contentDescription,
                modifier = modifier,
                contentScale = contentScale
            )
        else
            PlaceholderArtwork(contentDescription, modifier, contentScale)
    }

    override fun equals(other: Any?) =
        other is EmbeddedArtwork && other.uri == uri && isLoaded == other.isLoaded

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeParcelable(bitmap, flags)
        parcel.writeString(uri.toString())
    }

    companion object {
        fun load(uri: Uri, metadataExtractor: SongMetadataExtractor, quality: Int = 100) =
            metadataExtractor.loadBitmap(uri, quality)

        @JvmField
        val CREATOR = object : Parcelable.Creator<EmbeddedArtwork> {
            override fun createFromParcel(parcel: Parcel) = EmbeddedArtwork(
                parcel.readParcelable(Bitmap::class.java.classLoader),
                Uri.parse(parcel.readString()!!)
            )

            override fun newArray(size: Int) = arrayOfNulls<EmbeddedArtwork?>(size)
        }
    }
}