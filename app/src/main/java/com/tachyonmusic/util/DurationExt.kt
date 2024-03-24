package com.tachyonmusic.util

import java.util.*

fun Duration.toReadableString(includeMilliseconds: Boolean): String {
    val valueMs = inWholeMilliseconds
    val millis = valueMs % 1000
    val second = valueMs / 1000 % 60
    val minute = valueMs / (1000 * 60) % 60
    val hour = valueMs / (1000 * 60 * 60) % 24

    val formatStr = StringBuilder("%d:%02d")
    val args = mutableListOf<Long>()
    if (hour != 0L) {
        formatStr.insert(0, "%d:")
        args += hour
    }

    args.addAll(listOf(minute, second))

    if (includeMilliseconds) {
        formatStr.append(".%03d")
        args += millis
    }

    return String.format(
        Locale.getDefault(),
        formatStr.toString(),
        *args.toTypedArray()
    )
}


fun Duration.normalize(max: Duration): Float =
    inWholeMilliseconds.normalize(max.inWholeMilliseconds)