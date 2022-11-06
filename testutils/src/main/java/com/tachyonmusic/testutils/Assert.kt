package com.tachyonmusic.testutils

import androidx.test.platform.app.InstrumentationRegistry
import com.tachyonmusic.util.Resource

fun <T> assertResource(res: Resource<T>) {
    assert(res is Resource.Success) {
        res.message?.asString(InstrumentationRegistry.getInstrumentation().targetContext)
            ?: "Message: Resource did not include message"
    }
}

fun <T> assertEquals(val1: T, val2: T) {
    assert(val1 == val2) {
        "$val1 != $val2"
    }
}

fun <T> assertNotEquals(val1: T, val2: T) {
    assert(val1 != val2) {
        "$val1 == $val2"
    }
}

fun <T> assertEquals(val1: List<T>, val2: List<T>) {
    assert(val1 == val2) {
        "${val1.joinToString(",") { "[$it]" }} != ${val2.joinToString(",") { "[$it]" }}"
    }
}