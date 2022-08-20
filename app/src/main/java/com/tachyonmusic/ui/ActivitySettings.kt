package com.tachyonmusic.ui

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.widget.doAfterTextChanged
import com.tachyonmusic.app.databinding.ActivitySettingsBinding
import com.tachyonmusic.user.User


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
            changed = true
        }

        binding.switchCombinePlaybackTypes.setOnCheckedChangeListener { _, b ->
            User.metadata.combineDifferentPlaybackTypes = b
            changed = true
        }

        binding.editAudioIncDecInterval.doAfterTextChanged { text ->
            if (text?.isEmpty() == true)
                return@doAfterTextChanged

            User.metadata.songIncDecInterval = text?.toString()?.toInt() ?: 100
            changed = true
        }

        binding.editAudioUpdateInterval.doAfterTextChanged { text ->
            if (text?.isEmpty() == true)
                return@doAfterTextChanged

            User.metadata.audioUpdateInterval = text?.toString()?.toInt() ?: 100
            changed = true
        }

        binding.editMaxPlaybacksInHistory.doAfterTextChanged { text ->
            if (text?.isEmpty() == true)
                return@doAfterTextChanged

            User.metadata.maxPlaybacksInHistory = text?.toString()?.toInt() ?: 25
            changed = true
        }
    }

    override fun onPause() {
        super.onPause()
        if (changed) {
            User.upload()
            changed = false
        }
    }

}