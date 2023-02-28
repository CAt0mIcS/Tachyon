package com.tachyonmusic.core.data

import android.content.ContentResolver
import android.graphics.Bitmap
import android.net.Uri
import android.os.Parcel
import android.os.Parcelable
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import com.tachyonmusic.core.domain.Artwork
import com.tachyonmusic.core.domain.SongMetadataExtractor
import com.tachyonmusic.util.File


/**
 * Artwork that is embedded in the audio file
 */
class EmbeddedArtwork(
    val bitmap: Bitmap,
    val uri: Uri
) : Artwork {

    @Composable
    override fun Image(contentDescription: String?, modifier: Modifier) {
        androidx.compose.foundation.Image(
            bitmap = bitmap.asImageBitmap(),
            contentDescription = contentDescription,
            modifier = modifier
        )
    }

    override fun equals(other: Any?) = other is EmbeddedArtwork && other.uri == uri

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeParcelable(bitmap, flags)
        parcel.writeString(uri.toString())
    }

    companion object {
        fun load(
            contentResolver: ContentResolver,
            uri: Uri,
            metadataExtractor: SongMetadataExtractor = FileSongMetadataExtractor()
        ) = metadataExtractor.loadBitmap(contentResolver, uri)

        @JvmField
        val CREATOR = object : Parcelable.Creator<EmbeddedArtwork> {
            override fun createFromParcel(parcel: Parcel) = EmbeddedArtwork(
                parcel.readParcelable(Bitmap::class.java.classLoader)!!,
                Uri.parse(parcel.readString()!!)
            )

            override fun newArray(size: Int) = arrayOfNulls<EmbeddedArtwork?>(size)
        }
    }
}