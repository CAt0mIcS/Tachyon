package com.tachyonmusic.media.data.ext

import androidx.media3.common.MediaMetadata
import com.tachyonmusic.core.constants.MetadataKeys

val MediaMetadata.name: String?
get() = extras?.getString(MetadataKeys.Name)