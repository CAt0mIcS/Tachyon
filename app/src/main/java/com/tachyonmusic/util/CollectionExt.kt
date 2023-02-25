package com.tachyonmusic.util

import com.tachyonmusic.core.domain.playback.Playback
import com.tachyonmusic.media.core.SortOrder
import com.tachyonmusic.media.core.SortType
import com.tachyonmusic.presentation.util.displayTitle

fun <T> MutableList<T>.removeFirst(predicate: (T) -> Boolean): Boolean {
    for (i in 0 until size) {
        if (predicate(this[i])) {
            removeAt(i)
            return true
        }
    }
    return false
}


fun <E> Collection<E>.indexOfOrNull(element: E): Int? {
    val i = indexOf(element)
    return if (i == -1) null else i
}


fun <T : Playback> Collection<T>.sortedBy(sortType: SortType, sortOrder: SortOrder): List<T> =
    when (sortType) {
        SortType.AlphabeticalTitle -> {
            sortWithOrder(sortOrder) { it.displayTitle }
        }
        SortType.AlphabeticalArtist -> {
            sortWithOrder(sortOrder) { it.underlyingSinglePlayback?.artist }
        }
        SortType.LastPlayedDate -> {
//            sortWithOrder(sortOrder) { it.lastPlayedDate }
            TODO()
        }
    }

private inline fun <T, R : Comparable<R>> Collection<T>.sortWithOrder(
    sortOrder: SortOrder,
    crossinline selector: (T) -> R?
) = when (sortOrder) {
    SortOrder.Ascending -> sortedBy(selector)
    SortOrder.Descending -> sortedByDescending(selector)
}
