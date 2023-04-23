package com.tachyonmusic.core.domain.model

class SoundLevel(private val valueMilliDb: Long) : Comparable<SoundLevel> {
    override fun compareTo(other: SoundLevel) = valueMilliDb.compareTo(other.valueMilliDb)

    override fun equals(other: Any?) = other is SoundLevel && valueMilliDb == other.valueMilliDb


    val inmDb: Long
        get() = valueMilliDb

    val inWholeDb: Long
        get() = valueMilliDb / 1000L

    val inDb: Float
        get() = valueMilliDb.toFloat() / 1000f

    override fun toString() = "$inWholeDb Db"
}


val Int.mDb: SoundLevel
    get() = SoundLevel(toLong())

val Int.Db: SoundLevel
    get() = SoundLevel(toLong() * 1000L)


val Long.mDb: SoundLevel
    get() = SoundLevel(this)

val Long.Db: SoundLevel
    get() = SoundLevel(this * 1000L)


val Short.mDb: SoundLevel
    get() = SoundLevel(toLong())

val Short.Db: SoundLevel
    get() = SoundLevel(toLong() * 1000L)