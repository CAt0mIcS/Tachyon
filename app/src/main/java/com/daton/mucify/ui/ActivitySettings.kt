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

        binding.switchAudioFocus.isChecked = !com.daton.user.User.metadata.ignoreAudioFocus
        binding.editAudioIncDecInterval.setText(com.daton.user.User.metadata.songIncDecInterval.toString())
        binding.editAudioUpdateInterval.setText(com.daton.user.User.metadata.audioUpdateInterval.toString())
        binding.editMaxPlaybacksInHistory.setText(com.daton.user.User.metadata.maxPlaybacksInHistory.toString())

        binding.switchAudioFocus.setOnCheckedChangeListener { _, b ->
            com.daton.user.User.metadata.ignoreAudioFocus = !b
            com.daton.user.User.metadata.saveToLocal()
            changed = true
        }

        binding.editAudioIncDecInterval.doAfterTextChanged { text ->
            if (text?.isEmpty() == true)
                return@doAfterTextChanged

            com.daton.user.User.metadata.songIncDecInterval = text?.toString()?.toInt() ?: 100
            com.daton.user.User.metadata.saveToLocal()
            changed = true
        }

        binding.editAudioUpdateInterval.doAfterTextChanged { text ->
            if (text?.isEmpty() == true)
                return@doAfterTextChanged

            com.daton.user.User.metadata.audioUpdateInterval = text?.toString()?.toInt() ?: 100
            com.daton.user.User.metadata.saveToLocal()
            changed = true
        }

        binding.editMaxPlaybacksInHistory.doAfterTextChanged { text ->
            if (text?.isEmpty() == true)
                return@doAfterTextChanged

            com.daton.user.User.metadata.maxPlaybacksInHistory = text?.toString()?.toInt() ?: 25
            com.daton.user.User.metadata.saveToLocal()
            changed = true
        }
    }

    override fun onPause() {
        super.onPause()
        if (changed) {
            com.daton.user.User.uploadMetadata()
            changed = false
        }
    }

}