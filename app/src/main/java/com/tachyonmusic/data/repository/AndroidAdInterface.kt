package com.tachyonmusic.data.repository

import androidx.activity.ComponentActivity
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.lifecycleScope
import com.google.android.gms.ads.AdListener
import com.google.android.gms.ads.AdLoader
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.MobileAds
import com.google.android.gms.ads.RequestConfiguration
import com.google.android.gms.ads.nativead.NativeAd
import com.google.android.gms.ads.nativead.NativeAdOptions
import com.tachyonmusic.domain.model.RewardAd
import com.tachyonmusic.domain.repository.AdInterface
import com.tachyonmusic.logger.domain.Logger
import com.tachyonmusic.util.delay
import com.tachyonmusic.util.min
import com.tachyonmusic.util.runOnUiThread
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine
import kotlin.time.Duration.Companion.milliseconds

class AndroidAdInterface(
    private val log: Logger,
    private val rewardAd: RewardAd
) : AdInterface {

    private var smallNativeAdLoadHandler: Job? = null
    private var _smallNativeAdCache = MutableStateFlow<List<NativeAd>>(emptyList())
    override val smallNativeAdCache: Flow<List<NativeAd>> =
        _smallNativeAdCache.debounce(300.milliseconds)
    private lateinit var smallNativeAdLoader: AdLoader
    val isLoadingSmallNativeAd: Boolean
        get() = smallNativeAdLoader.isLoading

    private var rewardAdLoadHandler: Job? = null
    override val rewardAdType: RewardAd.Type?
        get() = rewardAd.type

    private var mediumNativeAdLoadHandler: Job? = null
    private var _mediumNativeAd = MutableStateFlow<NativeAd?>(null)
    override val mediumNativeAd = _mediumNativeAd.asStateFlow()
    private lateinit var mediumNativeAdLoader: AdLoader
    val isLoadingMediumNativeAd: Boolean
        get() = mediumNativeAdLoader.isLoading

    override fun initialize(activity: ComponentActivity) {
        MobileAds.initialize(activity)
        MobileAds.setRequestConfiguration(
            RequestConfiguration.Builder().setTestDeviceIds(listOf("TEST_EMULATOR")).build()
        )

        mediumNativeAdLoader = AdLoader.Builder(
            activity,
            MEDIUM_NATIVE_AD_ID
        ).forNativeAd { nativeAd ->
            _mediumNativeAd.update { nativeAd }
            log.debug("[AdInterface] Native Medium Ad (HomeScreen) loaded")
        }.withAdListener(object : AdListener() {
            override fun onAdFailedToLoad(adError: LoadAdError) {
                log.error("[AdInterface] Medium Native Ad failed to load: ${adError.message}")
            }
        }).withNativeAdOptions(
            NativeAdOptions.Builder()
                .setAdChoicesPlacement(NativeAdOptions.ADCHOICES_BOTTOM_LEFT)
                .build()
        ).build()

        smallNativeAdLoader = AdLoader.Builder(
            activity,
            SMALL_NATIVE_AD_ID
        ).forNativeAd { nativeAd ->
            // Ensure the ad is of type App Install Ad before displaying

            _smallNativeAdCache.update { it + nativeAd }
            log.debug("[AdInterface] Native Install Ad loaded")

        }.withAdListener(object : AdListener() {
            override fun onAdFailedToLoad(adError: LoadAdError) {
                log.error("[AdInterface] Native Ad failed to load: ${adError.message}")
            }
        }).withNativeAdOptions(
            NativeAdOptions.Builder()
                .setAdChoicesPlacement(NativeAdOptions.ADCHOICES_TOP_RIGHT) // Customize AdChoices icon placement
                .build()
        ).build()
    }

    override fun release() {
        unloadRewardAd()
        unloadSmallNativeInstallAds()
        unloadMediumNativeInstallAd()
    }

    override fun <T> showRewardAd(
        activity: ComponentActivity,
        onUserReward: (RewardAd.Type, Int) -> T
    ): T {
        var ret: T? = null
        rewardAd.show(activity) { type, amount ->
            ret = onUserReward(type, amount)
        }
        restartRewardLoadHandler(activity)
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
            restartRewardLoadHandler(activity)
        }
    }

    override fun loadRewardAd(lifecycleOwner: LifecycleOwner) {
        restartRewardLoadHandler(lifecycleOwner)
    }

    override fun loadNativeInstallAds(lifecycleOwner: LifecycleOwner) {
        restartNativeInstallAdLoadHandler(lifecycleOwner)
        restartMediumNativeAdLoadHandler(lifecycleOwner)
    }

    override fun unloadRewardAd() {
        rewardAdLoadHandler?.cancel()
        rewardAdLoadHandler = null
        rewardAd.unload()
    }

    override fun unloadSmallNativeInstallAds() {
        smallNativeAdLoadHandler?.cancel()
        smallNativeAdLoadHandler = null
        _smallNativeAdCache.value.forEach { it.destroy() }
        _smallNativeAdCache.update { emptyList() }
        log.debug("[AdInterface] Unloaded native app install ads")
    }

    override fun unloadMediumNativeInstallAd() {
        mediumNativeAdLoadHandler?.cancel()
        mediumNativeAdLoadHandler = null
        mediumNativeAd.value?.destroy()
        _mediumNativeAd.update { null }
    }


    private fun restartRewardLoadHandler(lifecycleOwner: LifecycleOwner) {
        rewardAdLoadHandler?.cancel()
        rewardAdLoadHandler = lifecycleOwner.lifecycleScope.launch {
            while (isActive) {
                rewardAd.load()
                delay(60.min) // Ads time out after 60 minutes
            }
        }
    }

    private fun restartNativeInstallAdLoadHandler(lifecycleOwner: LifecycleOwner) {
        smallNativeAdLoadHandler?.cancel()
        smallNativeAdLoadHandler = lifecycleOwner.lifecycleScope.launch(Dispatchers.IO) {
            while (isActive) {
                loadNativeInstallAdsInternal()
                delay(60.min) // Ads time out after 60 minutes
                _smallNativeAdCache.value.forEach { it.destroy() }
                _smallNativeAdCache.update { emptyList() }
            }
        }
    }

    private fun restartMediumNativeAdLoadHandler(lifecycleOwner: LifecycleOwner) {
        mediumNativeAdLoadHandler?.cancel()
        mediumNativeAdLoadHandler = lifecycleOwner.lifecycleScope.launch(Dispatchers.IO) {
            while (isActive) {
                loadMediumNativeAdsInternal()
                delay(60.min) // Ads time out after 60 minutes
                mediumNativeAd.value?.destroy()
                _mediumNativeAd.update { null }
            }
        }
    }

    private fun loadNativeInstallAdsInternal() {
        if (isLoadingSmallNativeAd || _smallNativeAdCache.value.size == 5)
            return

        log.debug("[AdInterface] Loading 5 native install ads...")
        smallNativeAdLoader.loadAds(AdRequest.Builder().build(), 5)
    }

    private fun loadMediumNativeAdsInternal() {
        if (isLoadingMediumNativeAd)
            return

        log.debug("[AdInterface] Loading medium install ad...")
        mediumNativeAdLoader.loadAd(AdRequest.Builder().build())
    }

    companion object {
        const val SMALL_NATIVE_TEST_AD_ID = "ca-app-pub-3940256099942544/2247696110"
        const val SMALL_NATIVE_AD_ID = "ca-app-pub-7145716621236451/6099968554"

        const val MEDIUM_NATIVE_AD_ID = "ca-app-pub-7145716621236451/9365998327"
    }
}