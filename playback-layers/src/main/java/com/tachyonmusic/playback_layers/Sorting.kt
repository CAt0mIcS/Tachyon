package com.tachyonmusic.playback_layers

import com.tachyonmusic.core.domain.playback.Playback
import com.tachyonmusic.core.domain.playback.Playlist
import com.tachyonmusic.core.domain.playback.Remix
import com.tachyonmusic.core.domain.playback.Song

enum class SortOrder {
    Ascending, Descending;

    companion object {
        fun fromInt(ordinal: Int) = entries.first { it.ordinal == ordinal }
    }
}

enum class SortType {
    TitleAlphabetically,
    ArtistAlphabetically,
    NameAlphabetically,
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
fun <T : Playback> Collection<T>.sortedBy(sortType: SortType, sortOrder: SortOrder): List<T> =
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
fun <T : Playback> Collection<T>.sortedBy(sortParams: SortingPreferences) =
    sortedBy(sortParams.type, sortParams.order)


private inline fun <T, R : Comparable<R>> Collection<T>.sortWithOrder(
    sortOrder: SortOrder,
    crossinline selector: (T) -> R?
) = when (sortOrder) {
    SortOrder.Ascending -> sortedBy(selector)
    SortOrder.Descending -> sortedByDescending(selector)
}

private fun Playback.getComparedString(type: SortType) = when (this) {
    is Song -> {
        when (type) {
            SortType.TitleAlphabetically, SortType.NameAlphabetically -> title + artist
            SortType.ArtistAlphabetically -> artist + title
            SortType.DateCreatedOrEdited -> timestampCreatedAddedEdited.toString()
        }
    }

    is Remix -> {
        when (type) {
            SortType.TitleAlphabetically -> title + artist + name
            SortType.ArtistAlphabetically -> artist + name + title
            SortType.NameAlphabetically -> name + title + artist
            SortType.DateCreatedOrEdited -> timestampCreatedAddedEdited.toString()
        }
    }

    is Playlist -> {
        when (type) {
            SortType.TitleAlphabetically -> title + name + artist
            SortType.ArtistAlphabetically -> artist + name + title
            SortType.NameAlphabetically -> name + title + artist
            SortType.DateCreatedOrEdited -> timestampCreatedAddedEdited.toString()
        }
    }

    else -> TODO("Invalid playback type ${this::javaClass.name}")
}
