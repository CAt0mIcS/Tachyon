package com.tachyonmusic.media.ext

import com.tachyonmusic.media.device.MediaSource
import java.io.File


inline val File?.isSongFile: Boolean
    get() = this != null && isFile && MediaSource.SupportedAudioExtensions.contains(extension)