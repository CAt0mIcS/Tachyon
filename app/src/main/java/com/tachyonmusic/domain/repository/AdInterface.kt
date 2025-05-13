package com.tachyonmusic.domain.repository

import androidx.activity.ComponentActivity
import androidx.lifecycle.LifecycleOwner
import com.google.android.gms.ads.nativead.NativeAd
import com.tachyonmusic.domain.model.RewardAd
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow

interface AdInterface {
    // TODO: NativeAd should be com.tachyonmusic interface
    val smallNativeAdCache: Flow<List<NativeAd>>
    val rewardAdType: RewardAd.Type?
    val mediumNativeAd: StateFlow<NativeAd?>

    fun initialize(activity: ComponentActivity)
    fun release()
    fun <T> showRewardAd(activity: ComponentActivity, onUserReward: (RewardAd.Type, Int) -> T): T
    suspend fun <T> showRewardAdSuspend(activity: ComponentActivity, onUserReward: suspend (RewardAd.Type, Int) -> T): T

    fun loadRewardAd(lifecycleOwner: LifecycleOwner)
    fun loadNativeInstallAds(lifecycleOwner: LifecycleOwner)
    fun unloadRewardAd()
    fun unloadSmallNativeInstallAds()
    fun unloadMediumNativeInstallAd()
}