package com.daton.tachyonmusic

import java.util.*

object Util {
    fun millisecondsToReadableString(progress: Int): String? {
        val millis = (progress % 1000).toLong()
        val second = (progress / 1000 % 60).toLong()
        val minute = (progress / (1000 * 60) % 60).toLong()
        val hour = (progress / (1000 * 60 * 60) % 24).toLong()
        return java.lang.String.format(
            Locale.getDefault(),
            "%02d:%02d:%02d.%d",
            hour,
            minute,
            second,
            millis
        )
    }
}