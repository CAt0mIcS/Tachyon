package com.tachyonmusic.media.data.ext

import android.os.Bundle
import android.os.Parcelable

inline fun <reified T : Parcelable> Bundle.parcelable(key: String?): T? {
    classLoader = T::class.java.classLoader
    return getParcelable(key)
}