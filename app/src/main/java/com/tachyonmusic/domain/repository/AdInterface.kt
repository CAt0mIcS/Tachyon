package com.tachyonmusic.domain.repository

import androidx.activity.ComponentActivity
import com.tachyonmusic.domain.model.RewardAd

interface AdInterface {
    fun initialize(activity: ComponentActivity)
    fun release()
    fun <T> showRewardAd(activity: ComponentActivity, onUserReward: (RewardAd.Type, Int) -> T): T
    suspend fun <T> showRewardAdSuspend(activity: ComponentActivity, onUserReward: suspend (RewardAd.Type, Int) -> T): T
}