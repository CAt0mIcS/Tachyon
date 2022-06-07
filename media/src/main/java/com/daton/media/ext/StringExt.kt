package com.daton.media.ext

import com.daton.media.data.MediaId

fun String.toMediaId(): MediaId = MediaId.deserialize(this)