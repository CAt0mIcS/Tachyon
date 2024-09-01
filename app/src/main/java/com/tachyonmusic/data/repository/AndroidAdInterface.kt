package com.tachyonmusic.data.repository

import androidx.activity.ComponentActivity
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.lifecycleScope
import com.google.android.gms.ads.MobileAds
import com.google.android.gms.ads.RequestConfiguration
import com.tachyonmusic.domain.model.RewardAd
import com.tachyonmusic.domain.repository.AdInterface
import com.tachyonmusic.logger.domain.Logger
import com.tachyonmusic.util.delay
import com.tachyonmusic.util.min
import com.tachyonmusic.util.runOnUiThread
import kotlinx.coroutines.Job
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

class AndroidAdInterface(
    private val log: Logger,
    private val rewardAd: RewardAd
) : AdInterface {
    private var adLoadHandler: Job? = null

    override fun initialize(activity: ComponentActivity) {
        MobileAds.initialize(activity)
        MobileAds.setRequestConfiguration(
            RequestConfiguration.Builder().setTestDeviceIds(listOf("TEST_EMULATOR")).build()
        )
        restartLoadHandler(activity)
    }

    override fun release() {
        adLoadHandler?.cancel()
        rewardAd.unload()
    }

    override fun <T> showRewardAd(
        activity: ComponentActivity,
        onUserReward: (RewardAd.Type, Int) -> T
    ): T {
        var ret: T? = null
        rewardAd.show(activity) { type, amount ->
            ret = onUserReward(type, amount)
        }
        restartLoadHandler(activity)
        return ret!!
    }

    override suspend fun <T> showRewardAdSuspend(
        activity: ComponentActivity,
        onUserReward: suspend (RewardAd.Type, Int) -> T
    ): T {
        val pair = runOnUiThread {
            suspendCoroutine { cont ->
                rewardAd.show(activity) { type, amount ->
                    cont.resume(onUserReward to (type to amount))
                }
            }
        }

        return pair.first(pair.second.first, pair.second.second).apply {
            restartLoadHandler(activity)
        }
    }


    private fun restartLoadHandler(lifecycleOwner: LifecycleOwner) {
        adLoadHandler?.cancel()
        adLoadHandler = lifecycleOwner.lifecycleScope.launch {
            while (isActive) {
                rewardAd.load()
                delay(60.min) // Ads time out after 60 minutes
            }
        }
    }
}