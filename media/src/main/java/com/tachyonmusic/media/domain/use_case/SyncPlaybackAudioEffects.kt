package com.tachyonmusic.media.domain.use_case

import androidx.media3.common.PlaybackParameters
import androidx.media3.common.Player
import com.tachyonmusic.core.ReverbConfig
import com.tachyonmusic.core.domain.playback.Playback
import com.tachyonmusic.media.domain.AudioEffectController

class SyncPlaybackAudioEffects(
    private val audioEffectController: AudioEffectController
) {
    operator fun invoke(playback: Playback, currentPlayer: Player): Playback? {
        if (audioEffectController.setBassEnabled(playback.bassBoostEnabled))
            audioEffectController.setBass(playback.bassBoost)
        if (audioEffectController.setVirtualizerEnabled(playback.virtualizerEnabled))
            audioEffectController.setVirtualizerStrength(playback.virtualizerStrength)
        if (audioEffectController.setReverbEnabled(playback.reverbEnabled))
            audioEffectController.setReverb(playback.reverb ?: ReverbConfig())

        playback.playbackParameters.let { params ->
            currentPlayer.playbackParameters = PlaybackParameters(params.speed, params.pitch)
            currentPlayer.volume = params.volume // TODO: Volume boosting (higher than 1)
        }

        if (audioEffectController.setEqualizerEnabled(playback.equalizerEnabled)) {

            if (playback.equalizerPreset != null && audioEffectController.currentPreset != playback.equalizerPreset) {
                audioEffectController.setEqualizerPreset(playback.equalizerPreset!!)
                return playback.copy(equalizerBands = audioEffectController.bands.value)
            }

            playback.equalizerBands?.forEach { equalizerBand ->
                // TODO: Do we need all this information to differentiate different bands?
                audioEffectController.getEqualizerBandIndex(
                    equalizerBand.lowerBandFrequency,
                    equalizerBand.upperBandFrequency,
                    equalizerBand.centerFrequency
                )?.let { band ->
                    audioEffectController.setEqualizerBandLevel(band, equalizerBand.level)
                }
            }
        }

        return null
    }
}