package com.tachyonmusic.util

import dagger.hilt.android.testing.HiltAndroidRule
import java.lang.IllegalStateException

fun HiltAndroidRule.tryInject() = try {
    inject()
} catch (e: IllegalStateException) {
}