package com.tachyonmusic.media.core

enum class SortOrder {
    Ascending, Descending;

    companion object {
        fun fromInt(ordinal: Int) = values().first { it.ordinal == ordinal }
    }
}

enum class SortType {
    AlphabeticalTitle,
    AlphabeticalArtist,
    LastPlayedDate;

    companion object {
        fun fromInt(ordinal: Int) = values().first { it.ordinal == ordinal }
    }
}