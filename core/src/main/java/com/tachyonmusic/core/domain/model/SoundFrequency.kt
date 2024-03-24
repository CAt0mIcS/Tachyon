package com.tachyonmusic.core.domain.model

class SoundFrequency(private val valueMilliHz: Long) : Comparable<SoundFrequency> {
    override fun compareTo(other: SoundFrequency) = valueMilliHz.compareTo(other.valueMilliHz)

    override fun equals(other: Any?) = other is SoundFrequency && valueMilliHz == other.valueMilliHz

    val inmHz: Long
        get() = valueMilliHz

    val inWholeHz: Long
        get() = valueMilliHz / 1000L

    val inHz: Float
        get() = valueMilliHz.toFloat() / 1000f

    override fun toString() = "$inWholeHz Hz"
}


val Int.mHz: SoundFrequency
    get() = SoundFrequency(toLong())

val Int.Hz: SoundFrequency
    get() = SoundFrequency(toLong() * 1000L)


val Long.mHz: SoundFrequency
    get() = SoundFrequency(this)

val Long.Hz: SoundFrequency
    get() = SoundFrequency(this * 1000L)


val Short.mHz: SoundFrequency
    get() = SoundFrequency(toLong())

val Short.Hz: SoundFrequency
    get() = SoundFrequency(toLong() * 1000L)