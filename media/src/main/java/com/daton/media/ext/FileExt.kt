package com.daton.media.ext

import com.daton.media.device.MediaSource
import java.io.File


inline val File.isSongFile: Boolean
    get() = isFile && MediaSource.SupportedAudioExtensions.contains(extension)

inline val File.isLoopFile: Boolean
    get() = isFile && extension == MediaSource.LoopFileExtension

inline val File.isPlaylistFile: Boolean
    get() = isFile && extension == MediaSource.PlaylistFileExtension