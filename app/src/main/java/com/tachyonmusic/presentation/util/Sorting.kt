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
fun SortType.asString(playbackType: PlaybackType, sortOrder: SortOrder? = null) = when (playbackType) {
    is PlaybackType.Song -> when(this) {
        SortType.TitleAlphabetically -> stringResource(
            R.string.sorted_by_alphabetical_title,
            sortOrder?.asString() ?: ""
        )
        SortType.ArtistAlphabetically -> stringResource(
            R.string.sorted_by_alphabetical_artist,
            sortOrder?.asString() ?: ""
        )
        SortType.DateCreatedOrEdited -> stringResource(
            R.string.sorted_by_date_added,
            sortOrder?.asString() ?: ""
        )
        else -> TODO("Invalid sort type $this for Song")
    }
    is PlaybackType.Remix -> when(this) {
        SortType.TitleAlphabetically -> stringResource(
            R.string.sorted_by_alphabetical_name,
            sortOrder?.asString() ?: ""
        )
        SortType.SubtitleAlphabetically -> stringResource(
            R.string.sorted_by_alphabetical_title,
            sortOrder?.asString() ?: ""
        )
        SortType.ArtistAlphabetically -> stringResource(
            R.string.sorted_by_alphabetical_artist,
            sortOrder?.asString() ?: ""
        )
        SortType.DateCreatedOrEdited -> stringResource(
            R.string.sorted_by_date_created,
            sortOrder?.asString() ?: ""
        )

        else -> TODO("Invalid sort type $this for Remix")
    }
    is PlaybackType.Playlist -> {
        when(this) {
            SortType.TitleAlphabetically -> stringResource(
                R.string.sorted_by_alphabetical_name,
                sortOrder?.asString() ?: ""
            )
            SortType.SubtitleAlphabetically -> stringResource(
                R.string.sorted_by_alphabetical_title,
                sortOrder?.asString() ?: ""
            )
            SortType.ArtistAlphabetically -> stringResource(
                R.string.sorted_by_alphabetical_artist,
                sortOrder?.asString() ?: ""
            )
            SortType.DateCreatedOrEdited -> stringResource(
                R.string.sorted_by_date_edited,
                sortOrder?.asString() ?: ""
            )

            else -> TODO("Invalid sort type $this for Playlist")
        }
    }

    is PlaybackType.Ad -> ""
}


@SuppressLint("ComposableNaming")
@Composable
fun SortOrder.asString() = when (this) {
    SortOrder.Ascending -> stringResource(R.string.ascending)
    SortOrder.Descending -> stringResource(R.string.descending)
}
