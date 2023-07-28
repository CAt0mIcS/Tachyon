package com.tachyonmusic.playback_layers.domain

import android.content.Context
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import android.net.NetworkRequest
import android.os.Build
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Job

class GetIsInternetConnectionMetered(
    @ApplicationContext private val appContext: Context
) {
    private var requestJob = Job()
    private var isMetered: Boolean? = null

    suspend operator fun invoke(): Boolean {
        if (isMetered != null)
            return isMetered!!

        val networkRequest = NetworkRequest.Builder()
            .addCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
            .addCapability(NetworkCapabilities.NET_CAPABILITY_NOT_METERED)
            .build()

        val connectivityManager =
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M)
                appContext.getSystemService(ConnectivityManager::class.java) as ConnectivityManager
            else {
                isMetered = true
                return isMetered!!
            }

        connectivityManager.requestNetwork(networkRequest, networkCallback)

        requestJob.join()
        return isMetered!!
    }

    private val networkCallback = object : ConnectivityManager.NetworkCallback() {
        override fun onCapabilitiesChanged(
            network: Network,
            networkCapabilities: NetworkCapabilities
        ) {
            super.onCapabilitiesChanged(network, networkCapabilities)
            isMetered =
                !networkCapabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_NOT_METERED)
            requestJob.complete()
        }
    }
}