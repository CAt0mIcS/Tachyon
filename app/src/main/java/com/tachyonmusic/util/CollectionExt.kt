package com.tachyonmusic.util

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