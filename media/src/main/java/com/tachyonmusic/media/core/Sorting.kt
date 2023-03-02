package com.tachyonmusic.media.core

import com.tachyonmusic.core.domain.playback.*
import com.tachyonmusic.database.domain.model.SinglePlaybackEntity
import com.tachyonmusic.database.util.toPlayback

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


@JvmName("sortedByPlayback")
fun <T : Playback> Collection<T>.sortedBy(sortType: SortType, sortOrder: SortOrder): List<T> =
    sortWithOrder(sortOrder) {
        it.getComparedString(sortType)
    }

@JvmName("sortedByPb")
fun <T : Playback> Collection<T>.sortedBy(sortParams: SortParameters) =
    sortedBy(sortParams.type, sortParams.order)


@JvmName("sortedByEntity")
fun <T : SinglePlaybackEntity> Collection<T>.sortedBy(
    sortType: SortType,
    sortOrder: SortOrder
): List<T> = sortWithOrder(sortOrder) {
    it.toPlayback().getComparedString(sortType)
}

@JvmName("sortedByE")
fun <T : SinglePlaybackEntity> Collection<T>.sortedBy(sortParams: SortParameters) =
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
            SortType.AlphabeticalTitle -> title + artist
            SortType.AlphabeticalArtist -> artist + title
            SortType.LastPlayedDate -> TODO()
        }
    }

    is Loop -> {
        when (type) {
            SortType.AlphabeticalTitle -> name + title + artist
            SortType.AlphabeticalArtist -> artist + name + title
            SortType.LastPlayedDate -> TODO()
        }
    }

    is Playlist -> {
        when (type) {
            SortType.AlphabeticalTitle -> name + title + artist
            SortType.AlphabeticalArtist -> artist + name + title
            SortType.LastPlayedDate -> TODO()
        }
    }

    else -> TODO("Invalid playback type ${this::javaClass.name}")
}
