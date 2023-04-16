package com.tachyonmusic.domain.repository

interface AudioEffectController : MediaBrowserController.EventListener {
    var bass: Int
    var virtualizerStrength: Int

    var bassEnabled: Boolean
    var virtualizerEnabled: Boolean
    var equalizerEnabled: Boolean
    var reverbEnabled: Boolean

    var speed: Float
    var pitch: Float

    val numBands: Int
    val minBandLevel: Int
    val maxBandLevel: Int

    var roomLevel: Int
    var roomHFLevel: Int
    var decayTime: Int
    var decayHFRatio: Int
    var reflectionsLevel: Int
    var reflectionsDelay: Int
    var reverbLevel: Int
    var reverbDelay: Int
    var diffusion: Int
    var density: Int


    fun setBandLevel(band: Int, level: Int)
    fun getBandLevel(band: Int): Int
}