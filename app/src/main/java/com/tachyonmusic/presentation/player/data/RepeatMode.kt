package com.tachyonmusic.presentation.player.data

import androidx.annotation.DrawableRes
import com.tachyonmusic.app.R

sealed class RepeatMode {
    abstract val next: RepeatMode
    abstract val icon: Int

    object One : RepeatMode() {
        override val next = All

        @DrawableRes
        override val icon = R.drawable.ic_repeat
    }

    object All : RepeatMode() {
        override val next = Shuffle

        @DrawableRes
        override val icon = R.drawable.ic_repeat_all
    }

    object Shuffle : RepeatMode() {
        override val next = One

        @DrawableRes
        override val icon = R.drawable.ic_shuffle
    }
}