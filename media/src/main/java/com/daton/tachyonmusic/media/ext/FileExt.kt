package com.daton.tachyonmusic.media.ext

import com.daton.tachyonmusic.media.device.MediaSource
import java.io.File


inline val File?.isSongFile: Boolean
    get() = this != null && isFile && MediaSource.SupportedAudioExtensions.contains(extension)