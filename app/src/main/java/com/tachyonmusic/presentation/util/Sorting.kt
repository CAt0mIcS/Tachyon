package com.tachyonmusic.presentation.util

import android.annotation.SuppressLint
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import com.tachyonmusic.app.R
import com.tachyonmusic.core.data.constants.PlaybackType
import com.tachyonmusic.playback_layers.SortOrder
import com.tachyonmusic.playback_layers.SortType


@SuppressLint("ComposableNaming")
@Composable
fun SortType.asString(playbackType: PlaybackType, sortOrder: SortOrder? = null) = when (this) {
    SortType.TitleAlphabetically -> stringResource(
        R.string.sorted_by_alphabetical_title,
        sortOrder?.asString() ?: ""
    )

    SortType.ArtistAlphabetically -> stringResource(
        R.string.sorted_by_alphabetical_artist,
        sortOrder?.asString() ?: ""
    )

    SortType.NameAlphabetically -> stringResource(
        R.string.sorted_by_alphabetical_name,
        sortOrder?.asString() ?: ""
    )

    SortType.DateCreatedOrEdited -> when(playbackType) {
        is PlaybackType.Song -> stringResource(
            R.string.sorted_by_date_added,
            sortOrder?.asString() ?: ""
        )
        is PlaybackType.Remix -> stringResource(
            R.string.sorted_by_date_created,
            sortOrder?.asString() ?: ""
        )
        is PlaybackType.Playlist -> stringResource(
            R.string.sorted_by_date_edited,
            sortOrder?.asString() ?: ""
        )
        is PlaybackType.Ad -> ""
    }
}


@SuppressLint("ComposableNaming")
@Composable
fun SortOrder.asString() = when (this) {
    SortOrder.Ascending -> stringResource(R.string.ascending)
    SortOrder.Descending -> stringResource(R.string.descending)
}
