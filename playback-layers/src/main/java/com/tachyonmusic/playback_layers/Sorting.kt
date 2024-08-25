package com.tachyonmusic.playback_layers

import com.tachyonmusic.core.domain.playback.Playback
import com.tachyonmusic.core.domain.playback.Playlist

enum class SortOrder {
    Ascending, Descending;

    companion object {
        fun fromInt(ordinal: Int) = entries.first { it.ordinal == ordinal }
    }
}

enum class SortType {
    TitleAlphabetically,
    ArtistAlphabetically,
    SubtitleAlphabetically,
    DateCreatedOrEdited;

    companion object {
        fun fromInt(ordinal: Int) = entries.first { it.ordinal == ordinal }
    }
}

data class SortingPreferences(
    val type: SortType = SortType.TitleAlphabetically,
    val order: SortOrder = SortOrder.Ascending
)


@JvmName("sortedByPlayback")
fun Collection<Playback>.sortedBy(sortType: SortType, sortOrder: SortOrder): List<Playback> =
    when (sortType) {
        SortType.DateCreatedOrEdited -> {
            sortWithOrder(sortOrder) {
                -it.getComparedString(sortType).toLong()
            }
        }

        else -> {
            sortWithOrder(sortOrder) {
                it.getComparedString(sortType)
            }
        }
    }


@JvmName("sortedByPb")
fun Collection<Playback>.sortedBy(sortParams: SortingPreferences) =
    sortedBy(sortParams.type, sortParams.order)


@JvmName("sortedByPlaylist")
fun Collection<Playlist>.sortedBy(sortType: SortType, sortOrder: SortOrder): List<Playlist> =
    when (sortType) {
        SortType.DateCreatedOrEdited -> {
            sortWithOrder(sortOrder) {
                -it.getComparedString(sortType).toLong()
            }
        }

        else -> {
            sortWithOrder(sortOrder) {
                it.getComparedString(sortType)
            }
        }
    }


@JvmName("sortedByPl")
fun Collection<Playlist>.sortedBy(sortParams: SortingPreferences) =
    sortedBy(sortParams.type, sortParams.order)


private inline fun <T, R : Comparable<R>> Collection<T>.sortWithOrder(
    sortOrder: SortOrder,
    crossinline selector: (T) -> R?
) = when (sortOrder) {
    SortOrder.Ascending -> sortedBy(selector)
    SortOrder.Descending -> sortedByDescending(selector)
}

private fun Playback.getComparedString(type: SortType) =
    if (isSong) {
        when (type) {
            SortType.TitleAlphabetically, SortType.SubtitleAlphabetically -> title + artist
            SortType.ArtistAlphabetically -> artist + title
            SortType.DateCreatedOrEdited -> timestampCreatedAddedEdited.toString()
        }
    } else if (isRemix) {
        when (type) {
            SortType.TitleAlphabetically -> name + title + artist
            SortType.ArtistAlphabetically -> artist + name + title
            SortType.SubtitleAlphabetically -> title + name + artist
            SortType.DateCreatedOrEdited -> timestampCreatedAddedEdited.toString()
        }
    } else
        TODO("Invalid playback type ${this::javaClass.name}")


private fun Playlist.getComparedString(type: SortType) = when (type) {
    SortType.TitleAlphabetically -> name + playbacks[0].title + playbacks[0].artist
    SortType.ArtistAlphabetically -> playbacks[0].artist + name + playbacks[0].title
    SortType.SubtitleAlphabetically -> playbacks[0].title + name + playbacks[0].artist
    SortType.DateCreatedOrEdited -> timestampCreatedAddedEdited.toString()
}
