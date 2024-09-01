package com.tachyonmusic.domain.model

import androidx.activity.ComponentActivity

interface RewardAd {
    fun load()
    fun unload()
    fun show(activity: ComponentActivity, onRewardGranted: (Type, Int) -> Unit)

    sealed interface Type {
        data object NewRemixes10 : Type
        data object Invalid: Type

        companion object {
            fun fromString(str: String) = when(str) {
                "0" -> NewRemixes10
                else -> Invalid
            }
        }
    }
}