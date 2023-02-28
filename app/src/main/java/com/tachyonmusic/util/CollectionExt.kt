package com.tachyonmusic.util

import android.content.Context
import com.tachyonmusic.core.domain.playback.SinglePlayback
import com.tachyonmusic.media.util.isPlayable
import kotlinx.coroutines.flow.update

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

fun List<SinglePlayback>.setPlayableState(context: Context) =
    onEach { pb -> pb.isPlayable.update { pb.mediaId.uri.isPlayable(context) } }