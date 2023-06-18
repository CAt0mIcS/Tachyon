package com.tachyonmusic.domain.use_case.search

import androidx.annotation.DrawableRes
import com.tachyonmusic.app.R

sealed class SearchLocation(val id: Int) {
    abstract val next: SearchLocation
    abstract val icon: Int

    object Local : SearchLocation(0) {
        override val next = Spotify

        @DrawableRes
        override val icon = R.drawable.ic_search

        override fun toString() = "SearchLocation.Local"
    }

    object Spotify : SearchLocation(1) {
        override val next = Local

        @DrawableRes
        override val icon = R.drawable.ic_rewind

        override fun toString() = "SearchLocation.Spotify"
    }

    companion object {
        fun fromId(id: Int) =
            when (id) {
                0 -> Local
                1 -> Spotify
                else -> TODO("Invalid search location id $id")
            }
    }
}