package com.tachyonmusic.util

import kotlin.time.Duration.Companion.milliseconds


class Duration(private val valueMs: Long) : Comparable<Duration> {
    val inWholeDays: Long
        get() = inWholeHours / 24

    val inWholeHours: Long
        get() = inWholeMinutes / 60

    val inWholeMinutes: Long
        get() = inWholeSeconds / 60

    val inWholeSeconds: Long
        get() = valueMs / 1000

    val inWholeMilliseconds: Long
        get() = valueMs

    override fun toString() = valueMs.milliseconds.toString()
    fun toIsoString() = valueMs.milliseconds.toIsoString()

    override fun equals(other: Any?): Boolean {
        if (other !is Duration) return false
        return other.valueMs == valueMs
    }

    operator fun plus(other: Duration) = Duration(valueMs + other.valueMs)
    operator fun minus(other: Duration) = Duration(valueMs - other.valueMs)

    operator fun plusAssign(other: Duration) {
        valueMs + other.valueMs
    }

    operator fun minusAssign(other: Duration) {
        valueMs - other.valueMs
    }

    override fun compareTo(other: Duration) = valueMs.compareTo(other.valueMs)
}


inline val Int.ms get() = Duration(toLong())
inline val Long.ms get() = Duration(this)
inline val Float.ms get() = Duration(toLong())
inline val Double.ms get() = Duration(toLong())

inline val Int.sec get() = Duration(toLong() * 1000L)
inline val Long.sec get() = Duration(this * 1000L)
inline val Float.sec get() = Duration((this * 1000).toLong())
inline val Double.sec get() = Duration((this * 1000).toLong())

inline val Int.min get() = Duration(toLong() * 1000L * 60L)
inline val Long.min get() = Duration(this * 1000L * 60L)
inline val Float.min get() = Duration((this * 1000 * 60).toLong())
inline val Double.min get() = Duration((this * 1000 * 60).toLong())

inline val Int.h get() = Duration(toLong() * 1000L * 60L * 60L)
inline val Long.h get() = Duration(this * 1000L * 60L * 60L)
inline val Float.h get() = Duration((this * 1000 * 60 * 60).toLong())
inline val Double.h get() = Duration((this * 1000 * 60 * 60).toLong())


suspend fun delay(duration: Duration) = kotlinx.coroutines.delay(duration.inWholeMilliseconds)