package com.tachyonmusic.util

@JvmName("copyList")
fun <T> List<T>.copy() = List(size) {
    this[it]
}

@JvmName("copyMutableList")
fun <T> MutableList<T>.copy() = List(size) {
    this[it]
}.toMutableList()