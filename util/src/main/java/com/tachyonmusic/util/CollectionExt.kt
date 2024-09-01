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

fun <T> MutableList<T>.replaceAt(i: Int, value: T): List<T> {
    removeAt(i)
    add(i, value)
    return this
}

fun <T> MutableList<T>.replaceWith(newValue: T, operator: (T) -> Boolean): List<T> {
    for(i in indices) {
        if(operator(this[i]))
            return replaceAt(i, newValue)
    }
    return this
}

inline fun <T> Iterable<T>.findAndSkip(skip: Int = 0, predicate: (T) -> Boolean): T? {
    var index = 0
    for (element in this) {
        if (predicate(element) && index++ == skip)
            return element
    }

    return null
}

fun <E> Collection<E>.indexOfOrNull(element: E): Int? {
    val i = indexOf(element)
    return if (i == -1) null else i
}

fun <E> List<E>.indexOf(pred: (E) -> Boolean): Int? {
    for (i in indices) {
        if (pred(this[i]))
            return i
    }
    return null
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