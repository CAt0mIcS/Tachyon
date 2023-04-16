package com.tachyonmusic.domain.repository

interface AudioEffectController : MediaBrowserController.EventListener {
    var bass: Int
    var virtualizerStrength: Int

    var bassEnabled: Boolean
    var virtualizerEnabled: Boolean
    var equalizerEnabled: Boolean

    var speed: Float
    var pitch: Float

    val numBands: Int
    val minBandLevel: Int
    val maxBandLevel: Int

    fun setBandLevel(band: Int, level: Int)
    fun getBandLevel(band: Int): Int
}