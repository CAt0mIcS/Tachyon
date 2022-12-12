package com.tachyonmusic.testutils

import dagger.hilt.android.testing.HiltAndroidRule

fun HiltAndroidRule.tryInject() = try {
    inject()
} catch (_: IllegalStateException) {
}