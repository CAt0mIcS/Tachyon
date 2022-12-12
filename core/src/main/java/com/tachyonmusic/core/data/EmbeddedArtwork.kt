package com.tachyonmusic.core.data

import android.graphics.Bitmap
import android.os.Parcel
import android.os.Parcelable
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import com.tachyonmusic.core.domain.Artwork
import java.io.File


/**
 * Artwork that is embedded in the audio file
 */
class EmbeddedArtwork(
    val bitmap: Bitmap
) : Artwork {

    @Composable
    override fun Image(contentDescription: String?, modifier: Modifier) {
        androidx.compose.foundation.Image(
            bitmap = bitmap.asImageBitmap(),
            contentDescription = contentDescription,
            modifier = modifier
        )
    }

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeParcelable(bitmap, flags)
    }

    companion object {
        fun load(path: File) = SongMetadata.loadBitmap(path)

        @JvmField
        val CREATOR = object : Parcelable.Creator<EmbeddedArtwork> {
            override fun createFromParcel(parcel: Parcel) = EmbeddedArtwork(
                parcel.readParcelable(
                    Bitmap::class.java.classLoader,
                    Bitmap::class.java
                )!!
            )

            override fun newArray(size: Int) = arrayOfNulls<EmbeddedArtwork?>(size)
        }
    }
}