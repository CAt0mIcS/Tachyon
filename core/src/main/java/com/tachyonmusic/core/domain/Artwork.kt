package com.tachyonmusic.core.domain

import android.os.Parcelable
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

interface Artwork : Parcelable {
    val isLoaded: Boolean

    @Composable
    operator fun invoke(contentDescription: String?, modifier: Modifier) {
        Image(contentDescription, modifier)
    }

    @Composable
    fun Image(contentDescription: String?, modifier: Modifier)

    override fun describeContents() = 0

    override fun equals(other: Any?): Boolean
}