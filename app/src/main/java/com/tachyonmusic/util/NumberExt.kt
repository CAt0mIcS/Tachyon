package com.tachyonmusic.util

fun Float.normalize(max: Float): Float {
    if (max == 0f)
        return 0f
    return this / max
}

fun Float.normalize(max: Long) = normalize(max.toFloat())

fun Long.normalize(max: Long) = toFloat().normalize(max)
