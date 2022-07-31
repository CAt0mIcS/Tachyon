package com.daton.mucify.ui

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.widget.doAfterTextChanged
import com.daton.mucify.databinding.ActivitySettingsBinding
import com.daton.user.User

class ActivitySettings : AppCompatActivity() {

    private lateinit var binding: ActivitySettingsBinding
    private var changed = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySettingsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.switchAudioFocus.isChecked = !User.metadata.ignoreAudioFocus
        binding.switchCombinePlaybackTypes.isChecked = User.metadata.combineDifferentPlaybackTypes
        binding.editAudioIncDecInterval.setText(User.metadata.songIncDecInterval.toString())
        binding.editAudioUpdateInterval.setText(User.metadata.audioUpdateInterval.toString())
        binding.editMaxPlaybacksInHistory.setText(User.metadata.maxPlaybacksInHistory.toString())

        binding.switchAudioFocus.setOnCheckedChangeListener { _, b ->
            User.metadata.ignoreAudioFocus = !b
            User.metadata.saveToLocal()
            changed = true
        }

        binding.switchCombinePlaybackTypes.setOnCheckedChangeListener { _, b ->
            User.metadata.combineDifferentPlaybackTypes = b
            User.metadata.saveToLocal()
            changed = true
        }

        binding.editAudioIncDecInterval.doAfterTextChanged { text ->
            if (text?.isEmpty() == true)
                return@doAfterTextChanged

            User.metadata.songIncDecInterval = text?.toString()?.toInt() ?: 100
            User.metadata.saveToLocal()
            changed = true
        }

        binding.editAudioUpdateInterval.doAfterTextChanged { text ->
            if (text?.isEmpty() == true)
                return@doAfterTextChanged

            User.metadata.audioUpdateInterval = text?.toString()?.toInt() ?: 100
            User.metadata.saveToLocal()
            changed = true
        }

        binding.editMaxPlaybacksInHistory.doAfterTextChanged { text ->
            if (text?.isEmpty() == true)
                return@doAfterTextChanged

            User.metadata.maxPlaybacksInHistory = text?.toString()?.toInt() ?: 25
            User.metadata.saveToLocal()
            changed = true
        }
    }

    override fun onPause() {
        super.onPause()
        if (changed) {
            User.uploadMetadata()
            changed = false
        }
    }

}