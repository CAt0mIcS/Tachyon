package com.tachyonmusic.core

import androidx.annotation.DrawableRes

sealed class RepeatMode(val id: Int) {
    abstract val next: RepeatMode
    abstract val icon: Int

    object One : RepeatMode(0) {
        override val next = All

        @DrawableRes
        override val icon = R.drawable.ic_repeat
    }

    object All : RepeatMode(1) {
        override val next = Shuffle

        @DrawableRes
        override val icon = R.drawable.ic_repeat_all
    }

    object Shuffle : RepeatMode(2) {
        override val next = One

        @DrawableRes
        override val icon = R.drawable.ic_shuffle
    }

    companion object {
        fun fromId(id: Int) =
            when (id) {
                0 -> One
                1 -> All
                2 -> Shuffle
                else -> TODO("Invalid repeat mode id $id")
            }
    }
}