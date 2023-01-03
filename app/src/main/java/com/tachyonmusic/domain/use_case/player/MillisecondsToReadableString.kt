package com.tachyonmusic.domain.use_case.player

import java.util.Locale


class MillisecondsToReadableString {
    operator fun invoke(progress: Long?, includeMilliseconds: Boolean): String {
        if (progress == null)
            return ""

        val millis = progress % 1000
        val second = progress / 1000 % 60
        val minute = progress / (1000 * 60) % 60
        val hour = progress / (1000 * 60 * 60) % 24

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
}