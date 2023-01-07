package com.tachyonmusic.data

import android.os.Parcel
import android.os.Parcelable
import androidx.annotation.DrawableRes
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import com.tachyonmusic.core.domain.Artwork

class PlaceholderArtwork(
    @DrawableRes private val id: Int
) : Artwork {
    @Composable
    override fun Image(contentDescription: String?, modifier: Modifier) {
        androidx.compose.foundation.Image(
            painter = painterResource(id),
            contentDescription = contentDescription,
            modifier = modifier
        )
    }

    override fun equals(other: Any?) = other is PlaceholderArtwork

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeInt(id)
    }

    companion object CREATOR : Parcelable.Creator<PlaceholderArtwork> {
        override fun createFromParcel(parcel: Parcel) = PlaceholderArtwork(parcel.readInt())
        override fun newArray(size: Int) = arrayOfNulls<PlaceholderArtwork?>(size)
    }
}