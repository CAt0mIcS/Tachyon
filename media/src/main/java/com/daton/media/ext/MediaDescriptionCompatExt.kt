package com.daton.media.ext

import android.graphics.Bitmap
import android.support.v4.media.MediaDescriptionCompat


inline val MediaDescriptionCompat.artist: String
    get() = subtitle as String


inline var MediaDescriptionCompat.Builder.title: String
    get() = throw IllegalAccessException("Cannot get from MediaDescriptionCompat.Builder")
    set(value) {
        setTitle(value)
    }

inline var MediaDescriptionCompat.Builder.artist: String
    get() = throw IllegalAccessException("Cannot get from MediaDescriptionCompat.Builder")
    set(value) {
        setSubtitle(value)
    }

inline var MediaDescriptionCompat.Builder.iconBitmap: Bitmap?
    get() = throw IllegalAccessException("Cannot get from MediaDescriptionCompat.Builder")
    set(value) {
        setIconBitmap(value)
    }
