package com.tachyonmusic.data

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

}