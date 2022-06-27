package com.daton.media.ext

import com.daton.media.device.MediaSource
import java.io.File


inline val File.isSongFile: Boolean
    get() = isFile && MediaSource.SupportedAudioExtensions.contains(extension)