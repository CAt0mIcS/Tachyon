package com.tachyonmusic.core.data

import android.graphics.Bitmap
import android.os.Parcel
import android.os.Parcelable
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.painter.BitmapPainter
import androidx.compose.ui.graphics.painter.Painter
import com.tachyonmusic.core.data.playback.LocalSongImpl
import com.tachyonmusic.core.domain.Artwork
import com.tachyonmusic.core.domain.playback.Loop
import com.tachyonmusic.core.domain.playback.SinglePlayback
import com.tachyonmusic.core.domain.playback.Song
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