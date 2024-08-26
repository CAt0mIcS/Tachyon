package com.tachyonmusic.data.repository

import androidx.lifecycle.lifecycleScope
import com.google.android.gms.ads.AdError
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.FullScreenContentCallback
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.MobileAds
import com.google.android.gms.ads.RequestConfiguration
import com.google.android.gms.ads.rewarded.RewardedAd
import com.google.android.gms.ads.rewarded.RewardedAdLoadCallback
import com.tachyonmusic.domain.repository.AdInterface
import com.tachyonmusic.logger.domain.Logger
import com.tachyonmusic.presentation.entry.ActivityMain
import com.tachyonmusic.util.delay
import com.tachyonmusic.util.min
import com.tachyonmusic.util.runOnUiThread
import kotlinx.coroutines.Job
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

class AndroidAdInterface(
    private val log: Logger
) : AdInterface {
    private var rewardedAd: RewardedAd? = null
    private lateinit var activity: ActivityMain // TODO: Get out of here

    private var adLoadHandler: Job? = null
    private var initialized = false

    override fun initialize(activity: ActivityMain) {
        this.activity = activity

        MobileAds.initialize(activity)
        MobileAds.setRequestConfiguration(
            RequestConfiguration.Builder().setTestDeviceIds(listOf("TEST_EMULATOR")).build()
        )

        if (!initialized) {
            restartLoadHandler()
            initialized = true
        }
    }

    override fun showRewardAd(onUserReward: (Int) -> Unit) {
        rewardedAd?.show(activity) { rewardItem ->
            onUserReward(rewardItem.amount)
        }
        restartLoadHandler()
    }

    override suspend fun showRewardAdSuspend(onUserReward: suspend (Int) -> Unit) {
        val pair = runOnUiThread {
            suspendCoroutine { cont ->
                rewardedAd?.show(activity) { rewardItem ->
                    cont.resume(onUserReward to rewardItem.amount)
                }
            }
        }

        pair.first(pair.second)
        restartLoadHandler()
    }


    private fun loadRewardAdInternal() {
        val adRequest = AdRequest.Builder().build()
        RewardedAd.load(
            activity,
            "ca-app-pub-7145716621236451~4657802755",
            adRequest,
            object : RewardedAdLoadCallback() {
                override fun onAdFailedToLoad(adError: LoadAdError) {
                    log.warning("[TAdInt] Error " + adError.message)
                    rewardedAd = null
                }

                override fun onAdLoaded(ad: RewardedAd) {
                    log.debug("[TAdInt] Reward ad loaded successfully")
                    rewardedAd = ad
                }
            })

        rewardedAd?.fullScreenContentCallback = object : FullScreenContentCallback() {
            override fun onAdClicked() {
                // Called when a click is recorded for an ad.
                log.debug("[TAdInt] Ad was clicked.")
            }

            override fun onAdDismissedFullScreenContent() {
                // Called when ad is dismissed.
                // Set the ad reference to null so you don't show the ad a second time.
                log.debug("[TAdInt] Ad dismissed fullscreen content.")
                rewardedAd = null
            }

            override fun onAdFailedToShowFullScreenContent(adError: AdError) {
                // Called when ad fails to show.
                log.debug("[TAdInt] Ad failed to show fullscreen content.")
                rewardedAd = null
            }

            override fun onAdImpression() {
                // Called when an impression is recorded for an ad.
                log.debug("[TAdInt] Ad recorded an impression.")
            }

            override fun onAdShowedFullScreenContent() {
                // Called when ad is shown.
                log.debug("[TAdInt] Ad showed fullscreen content.")
            }
        }
    }

    private fun restartLoadHandler() {
        adLoadHandler?.cancel()
        adLoadHandler = activity.lifecycleScope.launch {
            while (isActive) {
                loadRewardAdInternal()
                delay(60.min) // Ads time out after 60 minutes
            }
        }
    }
}