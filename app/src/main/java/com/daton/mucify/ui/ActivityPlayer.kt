package com.daton.mucify.ui

import android.os.Bundle
import android.service.autofill.UserData
import androidx.appcompat.app.AppCompatActivity
import com.daton.media.MediaController
import com.daton.mucify.databinding.ActivityPlayerBinding
import com.daton.mucify.user.User


class ActivityPlayer : AppCompatActivity() {
    private val controller = MediaController()
    private lateinit var binding: ActivityPlayerBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPlayerBinding.inflate(layoutInflater)
        setContentView(binding.root)

        controller.create(this)

        controller.onMediaIdChanged = {
            binding.txtTitle.text = controller.title
            binding.txtArtist.text = controller.artist

            val duration = (controller.duration / User.metadata.audioUpdateInterval).toInt()
            binding.sbPos.max = duration
            binding.sbStartPos.max = duration
            binding.sbEndPos.max = duration

            binding.sbStartPos.progress =
                (controller.startTime / User.metadata.audioUpdateInterval).toInt()
            binding.sbEndPos.progress =
                (controller.endTime / User.metadata.audioUpdateInterval).toInt()

        }

        controller.onConnected = {
            // Media id is set by [ActivityMain] before transitioning to [ActivityPlayer]
            controller.onMediaIdChanged?.invoke()
        }
    }

    override fun onStart() {
        super.onStart()
        controller.connect(this)
    }

    override fun onStop() {
        super.onStop()
        controller.disconnect()
    }
}