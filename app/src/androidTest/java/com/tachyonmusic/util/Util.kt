package com.tachyonmusic.util

import dagger.hilt.android.testing.HiltAndroidRule
import java.lang.IllegalStateException

fun HiltAndroidRule.tryInject() = try {
    inject()
} catch (e: IllegalStateException) {
}

const val TEST_EMAIL = "test1@test.com"
const val TEST_PASSWORD = "testPassword"