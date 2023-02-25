package com.tachyonmusic.presentation.util

import android.annotation.SuppressLint
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import com.tachyonmusic.app.R
import com.tachyonmusic.media.core.SortOrder
import com.tachyonmusic.media.core.SortType


@SuppressLint("ComposableNaming")
@Composable
fun SortType.asString(sortOrder: SortOrder? = null) = when (this) {
    SortType.AlphabeticalTitle -> stringResource(
        R.string.sorted_by_alphabetical_title,
        sortOrder?.asString() ?: ""
    )

    SortType.AlphabeticalArtist -> stringResource(
        R.string.sorted_by_alphabetical_artist,
        sortOrder?.asString() ?: ""
    )

    SortType.LastPlayedDate -> stringResource(
        R.string.sorted_by_last_played_date,
        sortOrder?.asString() ?: ""
    )
}


@SuppressLint("ComposableNaming")
@Composable
fun SortOrder.asString() = when (this) {
    SortOrder.Ascending -> stringResource(R.string.ascending)
    SortOrder.Descending -> stringResource(R.string.descending)
}
