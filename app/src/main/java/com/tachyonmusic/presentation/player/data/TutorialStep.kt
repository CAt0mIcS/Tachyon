package com.tachyonmusic.presentation.player.data

import com.tachyonmusic.app.R

sealed class TutorialStep(val name: String) {
    override fun toString() = name
    abstract val next: TutorialStep?
    abstract val previous: TutorialStep?
    abstract val title: Int
    abstract val description: Int

    object PlaybackControls : TutorialStep("PlaybackControls") {
        override val next = RemixButton
        override val previous = null

        override val title = R.string.tutorial_playback_controls
        override val description = R.string.tutorial_playback_controls_desc
    }

    object RemixButton : TutorialStep("RemixButton") {
        override val next = RemixInterface
        override val previous = PlaybackControls

        override val title = R.string.tutorial_remix_button
        override val description = R.string.tutorial_remix_button_desc
    }

    object RemixInterface : TutorialStep("RemixInterface") {
        override val next = RemixButtons
        override val previous = RemixButton

        override val title = R.string.tutorial_remix_interface
        override val description = R.string.tutorial_remix_interface_desc
    }

    object RemixButtons : TutorialStep("RemixButtons") {
        override val next = SaveRemixButton
        override val previous = RemixInterface

        override val title = R.string.tutorial_remix_buttons
        override val description = R.string.tutorial_remix_buttons_desc
    }

    object SaveRemixButton : TutorialStep("SaveRemixButton") {
        override val next = SoundEffectButton
        override val previous = RemixButtons

        override val title = R.string.tutorial_save_remix_button
        override val description = R.string.tutorial_save_remix_button_desc
    }

    object SoundEffectButton : TutorialStep("SoundEffectButton") {
        override val next = SoundEffectCheckboxes
        override val previous = RemixInterface

        override val title = R.string.tutorial_sound_effect_button
        override val description = R.string.tutorial_sound_effect_button_desc
    }

    object SoundEffectCheckboxes : TutorialStep("SoundEffectCheckboxes") {
        override val next = SpeedPitchSliders
        override val previous = SoundEffectButton

        override val title = R.string.tutorial_sound_effect_checkboxes
        override val description = R.string.tutorial_sound_effect_checkboxes_desc
    }

    object SpeedPitchSliders : TutorialStep("SpeedPitchSliders") {
        override val next = Finished
        override val previous = SoundEffectCheckboxes

        override val title = R.string.tutorial_speed_pitch_sliders
        override val description = R.string.tutorial_speed_pitch_sliders_desc
    }

    object Finished : TutorialStep("Finished") {
        override val next = null
        override val previous = SpeedPitchSliders

        override val title = R.string.restart
        override val description = R.string.request_app_restart_for_update
    }

    companion object {
        fun fromString(str: String) = when (str) {
            PlaybackControls.name -> PlaybackControls
            RemixButton.name -> RemixButton
            RemixInterface.name -> RemixInterface
            SoundEffectButton.name -> SoundEffectButton
            SoundEffectCheckboxes.name -> SoundEffectCheckboxes
            Finished.name -> Finished
            else -> throw IllegalArgumentException("Invalid TutorialStep name '$str'")
        }

        val first = PlaybackControls
    }
}