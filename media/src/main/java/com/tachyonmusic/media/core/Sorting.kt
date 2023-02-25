package com.tachyonmusic.media.core

import com.tachyonmusic.core.domain.playback.Loop
import com.tachyonmusic.core.domain.playback.Playback
import com.tachyonmusic.core.domain.playback.Playlist
import com.tachyonmusic.core.domain.playback.Song

enum class SortOrder {
    Ascending, Descending;

    companion object {
        fun fromInt(ordinal: Int) = values().first { it.ordinal == ordinal }
    }
}

enum class SortType {
    AlphabeticalTitle,
    AlphabeticalArtist,
    LastPlayedDate;

    companion object {
        fun fromInt(ordinal: Int) = values().first { it.ordinal == ordinal }
    }
}

data class SortParameters(
    val type: SortType = SortType.AlphabeticalTitle,
    val order: SortOrder = SortOrder.Ascending
)


fun <T : Playback> Collection<T>.sortedBy(sortType: SortType, sortOrder: SortOrder): List<T> =
    when (sortType) {
        SortType.AlphabeticalTitle -> {
            sortWithOrder(sortOrder) {
                when (it) {
                    is Song -> it.title
                    is Loop -> it.name
                    is Playlist -> it.name
                    else -> TODO("Invalid playback type ${this.javaClass.name}")
                    // TODO: Use it.displayTitle
                }
            }
        }
        SortType.AlphabeticalArtist -> {
            sortWithOrder(sortOrder) { it.underlyingSinglePlayback?.artist }
        }
        SortType.LastPlayedDate -> {
//            sortWithOrder(sortOrder) { it.lastPlayedDate }
            TODO()
        }
    }

fun <T : Playback> Collection<T>.sortedBy(sortParams: SortParameters) =
    sortedBy(sortParams.type, sortParams.order)

private inline fun <T, R : Comparable<R>> Collection<T>.sortWithOrder(
    sortOrder: SortOrder,
    crossinline selector: (T) -> R?
) = when (sortOrder) {
    SortOrder.Ascending -> sortedBy(selector)
    SortOrder.Descending -> sortedByDescending(selector)
}