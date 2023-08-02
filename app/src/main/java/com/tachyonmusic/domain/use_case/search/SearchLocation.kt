package com.tachyonmusic.domain.use_case.search

import androidx.annotation.DrawableRes
import com.tachyonmusic.app.R

sealed class SearchLocation(val id: Int) {
    abstract val next: SearchLocation
    abstract val icon: Int

    object Local : SearchLocation(0) {
        override val next = Local

        @DrawableRes
        override val icon = R.drawable.ic_search

        override fun toString() = "SearchLocation.Local"
    }

    companion object {
        fun fromId(id: Int) =
            when (id) {
                0 -> Local
                else -> TODO("Invalid search location id $id")
            }
    }
}