package com.tachyonmusic.presentation.player.data

sealed class TutorialStep(val name: String) {
    override fun toString() = name
    abstract val next: TutorialStep?
    abstract val previous: TutorialStep?

    object AddToPlaylistButton : TutorialStep("AddToPlaylistButton") {
        override val next = RemixButton
        override val previous = null
    }

    object RemixButton : TutorialStep("RemixButton") {
        override val next = RemixInterface
        override val previous = AddToPlaylistButton
    }

    object RemixInterface : TutorialStep("RemixInterface") {
        override val next = SoundEffectButton
        override val previous = RemixButton
    }

    object SoundEffectButton : TutorialStep("SoundEffectButton") {
        override val next = SoundEffectCheckboxes
        override val previous = RemixInterface
    }

    object SoundEffectCheckboxes : TutorialStep("SoundEffectCheckboxes") {
        override val next = Finished
        override val previous = SoundEffectButton
    }

    object Finished : TutorialStep("Finished") {
        override val next = null
        override val previous = SoundEffectCheckboxes
    }

    companion object {
        fun fromString(str: String) = when (str) {
            AddToPlaylistButton.name -> AddToPlaylistButton
            RemixButton.name -> RemixButton
            RemixInterface.name -> RemixInterface
            SoundEffectButton.name -> SoundEffectButton
            SoundEffectCheckboxes.name -> SoundEffectCheckboxes
            Finished.name -> Finished
            else -> throw IllegalArgumentException("Invalid TutorialStep name '$str'")
        }

        val first = AddToPlaylistButton
    }
}