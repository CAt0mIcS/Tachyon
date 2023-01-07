package com.tachyonmusic.core.data

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
    val path: File
) : Artwork {

    @Composable
    override fun Image(contentDescription: String?, modifier: Modifier) {
        androidx.compose.foundation.Image(
            bitmap = bitmap.asImageBitmap(),
            contentDescription = contentDescription,
            modifier = modifier
        )
    }

    override fun equals(other: Any?) = other is EmbeddedArtwork && other.path == path

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeParcelable(bitmap, flags)
        parcel.writeString(path.absolutePath)
    }

    companion object {
        fun load(
            path: File,
            metadataExtractor: SongMetadataExtractor = FileSongMetadataExtractor()
        ) = metadataExtractor.loadBitmap(Uri.fromFile(path.raw))

        @JvmField
        val CREATOR = object : Parcelable.Creator<EmbeddedArtwork> {
            override fun createFromParcel(parcel: Parcel) = EmbeddedArtwork(
                parcel.readParcelable(
                    Bitmap::class.java.classLoader,
                    Bitmap::class.java
                )!!, // (TODO)
                File(parcel.readString()!!)
            )

            override fun newArray(size: Int) = arrayOfNulls<EmbeddedArtwork?>(size)
        }
    }
}