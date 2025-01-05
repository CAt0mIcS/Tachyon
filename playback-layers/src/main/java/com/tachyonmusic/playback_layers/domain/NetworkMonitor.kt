package com.tachyonmusic.playback_layers.domain

import kotlinx.coroutines.flow.Flow

interface NetworkMonitor {
    val networkConnectionState: Flow<NetworkInfo>

    data class NetworkInfo(
        val connectionStatus: ConnectionStatus,
        val isMetered: Boolean = false
    )

    enum class ConnectionStatus {
        Disconnected, Disconnecting, Available, Connected
    }
}