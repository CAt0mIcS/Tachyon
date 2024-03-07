package com.tachyonmusic.core.domain

import android.os.Parcelable
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale

interface Artwork : Parcelable {
    val isLoaded: Boolean

    @Composable
    operator fun invoke(
        contentDescription: String?,
        modifier: Modifier
    ) {
        invoke(contentDescription, modifier, ContentScale.Fit)
    }

    @Composable
    operator fun invoke(
        contentDescription: String?,
        modifier: Modifier,
        contentScale: ContentScale
    ) {
        Image(contentDescription, modifier, contentScale)
    }

    @Composable
    fun Image(
        contentDescription: String?,
        modifier: Modifier
    ) {
        Image(contentDescription, modifier, ContentScale.Fit)
    }

    @Composable
    fun Image(
        contentDescription: String?,
        modifier: Modifier,
        contentScale: ContentScale
    )

    override fun describeContents() = 0

    override fun equals(other: Any?): Boolean
}