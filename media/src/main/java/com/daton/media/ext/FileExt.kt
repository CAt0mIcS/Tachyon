package com.daton.media.ext

import com.daton.media.device.MediaSource
import java.io.File


inline val File?.isSongFile: Boolean
    get() = this != null && isFile && MediaSource.SupportedAudioExtensions.contains(extension)