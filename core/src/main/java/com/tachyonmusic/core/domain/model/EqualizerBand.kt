package com.tachyonmusic.core.domain.model

data class EqualizerBand(
    val level: SoundLevel,
    val lowerBandFrequency: SoundFrequency,
    val upperBandFrequency: SoundFrequency,
    val centerFrequency: SoundFrequency
) {
    override fun toString() =
        "${level.inmDb}|${upperBandFrequency.inmHz}|${lowerBandFrequency.inmHz}|${centerFrequency.inmHz}"

    companion object {
        fun fromString(str: String): EqualizerBand {
            val args = str.split('|')
            return EqualizerBand(
                args[0].toLong().mDb,
                args[2].toLong().mHz,
                args[1].toLong().mHz,
                args[3].toLong().mHz
            )
        }
    }
}