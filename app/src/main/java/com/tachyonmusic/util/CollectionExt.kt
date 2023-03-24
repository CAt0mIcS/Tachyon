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

/**
 * Wraps the index around until it gets the element.
 * So for [idx] = 3 and [List.size] = 2 it will get the element at index 1 (3 - 2)
 */
fun <E> List<E>.cycle(idx: Int): E? {
    var index = idx
    while (index >= size) {
        index -= size
    }

    if (index < 0)
        return null
    return this[index]
}