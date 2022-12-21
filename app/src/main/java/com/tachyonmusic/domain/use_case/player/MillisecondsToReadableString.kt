package com.tachyonmusic.domain.use_case.player

import java.util.Locale


class MillisecondsToReadableString {
    operator fun invoke(progress: Long?): String {
        if (progress == null)
            return ""

        val millis = progress % 1000
        val second = progress / 1000 % 60
        val minute = progress / (1000 * 60) % 60
        val hour = progress / (1000 * 60 * 60) % 24

        return if (hour != 0L)
            String.format(
                Locale.getDefault(),
                "%d:%d:%02d.%03d",
                hour,
                minute,
                second,
                millis
            )
        else
            String.format(
                Locale.getDefault(),
                "%d:%02d.%03d",
                minute,
                second,
                millis
            )
    }
}