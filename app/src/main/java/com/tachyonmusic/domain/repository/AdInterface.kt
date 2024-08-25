package com.tachyonmusic.domain.repository

import com.tachyonmusic.presentation.entry.ActivityMain

interface AdInterface {
    fun initialize(activity: ActivityMain)
    fun showRewardAd(onUserReward: (Int) -> Unit)
    suspend fun showRewardAdSuspend(onUserReward: suspend (Int) -> Unit)
}