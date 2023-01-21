package com.tachyonmusic.core.data.constants

import androidx.compose.material.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.tachyonmusic.core.R
import com.tachyonmusic.core.data.ResourceArtwork

val PlaceholderArtwork = ResourceArtwork(R.drawable.ic_placeholder_artwork)

@Composable
fun ArtworkLoadingIndicator(modifier: Modifier = Modifier) {
    CircularProgressIndicator(modifier)
}
