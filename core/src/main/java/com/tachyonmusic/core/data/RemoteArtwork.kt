package com.tachyonmusic.core.data

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
        GlideImage(
            model = uri.toURL().toString(),
            contentDescription = contentDescription,
            modifier = modifier
        )
    }

    // TODO: GlideLazyListPreloader
}