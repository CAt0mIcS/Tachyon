package com.tachyonmusic.domain.use_case

import java.util.*

class MillisecondsToReadableString {
    operator fun invoke(progress: Long?): String {
        if (progress == null)
            return ""

        val millis = progress % 1000
        val second = progress / 1000 % 60
        val minute = progress / (1000 * 60) % 60
        val hour = progress / (1000 * 60 * 60) % 24
        return String.format(
            Locale.getDefault(),
            "%02d:%02d:%02d.%d",
            hour,
            minute,
            second,
            millis
        )
    }
}