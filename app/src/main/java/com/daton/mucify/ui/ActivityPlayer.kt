package com.daton.mucify.ui

import com.daton.mucify.R
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.widget.EditText
import android.widget.SeekBar
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.daton.media.MediaController
import com.daton.media.device.Loop
import com.daton.media.device.Playlist
import com.daton.mucify.Util
import com.daton.mucify.databinding.ActivityPlayerBinding
import com.daton.user.User


class ActivityPlayer : AppCompatActivity() {
    private val controller = MediaController()
    private lateinit var binding: ActivityPlayerBinding

    private val handler = Handler(Looper.getMainLooper())

    private var isSeeking = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPlayerBinding.inflate(layoutInflater)
        setContentView(binding.root)

        controller.create(this)

        controller.onMediaIdChanged = {
            binding.txtTitle.text = controller.title
            binding.txtArtist.text = controller.artist

            val duration = (controller.duration / com.daton.user.User.metadata.audioUpdateInterval).toInt()
            binding.sbPos.max = duration
            binding.sbStartPos.max = duration
            binding.sbEndPos.max = duration

            binding.sbStartPos.progress =
                (controller.startTime / com.daton.user.User.metadata.audioUpdateInterval).toInt()
            binding.sbEndPos.progress =
                (controller.endTime / com.daton.user.User.metadata.audioUpdateInterval).toInt()

        }

        controller.onPlaybackStateChanged = { isPlaying ->
            binding.btnPlayPause.setImageResource(if (isPlaying) R.drawable.pause else R.drawable.play)
        }

        controller.onConnected = {
            runOnUiThread(object : Runnable {
                override fun run() {
                    if (controller.isCreated && controller.isPlaying && !isSeeking) {
                        val currentPos: Int =
                            (controller.currentPosition / com.daton.user.User.metadata.audioUpdateInterval).toInt()
                        binding.sbPos.progress = currentPos
                    }
                    if (!isDestroyed)
                        handler.postDelayed(this, com.daton.user.User.metadata.audioUpdateInterval.toLong())
                }
            })

            binding.sbPos.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
                override fun onProgressChanged(p0: SeekBar?, progress: Int, p2: Boolean) {
                    binding.txtPos.text =
                        Util.millisecondsToReadableString(progress * com.daton.user.User.metadata.audioUpdateInterval)
                }

                override fun onStartTrackingTouch(p0: SeekBar?) {
                    isSeeking = true
                }

                override fun onStopTrackingTouch(sb: SeekBar) {
                    isSeeking = false
                    controller.seekTo(sb.progress * com.daton.user.User.metadata.audioUpdateInterval.toLong())
                }
            })

            binding.sbStartPos.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
                override fun onProgressChanged(p0: SeekBar?, progress: Int, p2: Boolean) {
                    binding.txtStartPos.text =
                        Util.millisecondsToReadableString(progress * com.daton.user.User.metadata.audioUpdateInterval)
                }

                override fun onStartTrackingTouch(p0: SeekBar?) {}

                override fun onStopTrackingTouch(sb: SeekBar) {
                    controller.startTime =
                        (sb.progress * com.daton.user.User.metadata.audioUpdateInterval).toLong()
                }

            })

            binding.sbEndPos.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
                override fun onProgressChanged(p0: SeekBar?, progress: Int, p2: Boolean) {
                    binding.txtEndPos.text =
                        Util.millisecondsToReadableString(progress * com.daton.user.User.metadata.audioUpdateInterval)
                }

                override fun onStartTrackingTouch(p0: SeekBar?) {}

                override fun onStopTrackingTouch(sb: SeekBar) {
                    controller.endTime =
                        (sb.progress * com.daton.user.User.metadata.audioUpdateInterval).toLong()
                }

            })

            binding.btnStartPosDec.setOnClickListener {
                var time: Long = controller.startTime - com.daton.user.User.metadata.songIncDecInterval
                if (time < 0) time = 0
                controller.startTime = time
                binding.sbStartPos.progress = (time / com.daton.user.User.metadata.audioUpdateInterval).toInt()
            }
            binding.btnStartPosInc.setOnClickListener {
                var time: Long = controller.startTime + com.daton.user.User.metadata.songIncDecInterval
                if (time > controller.duration) time = controller.duration
                controller.startTime = time
                binding.sbStartPos.progress = (time / com.daton.user.User.metadata.audioUpdateInterval).toInt()
            }
            binding.btnEndPosDec.setOnClickListener {
                var time: Long = controller.endTime - com.daton.user.User.metadata.songIncDecInterval
                if (time < 0) time = 0
                controller.endTime = time
                binding.sbEndPos.progress = (time / com.daton.user.User.metadata.audioUpdateInterval).toInt()
            }
            binding.btnEndPosInc.setOnClickListener {
                var time: Long = controller.endTime + com.daton.user.User.metadata.songIncDecInterval
                if (time > controller.duration) time = controller.duration
                controller.endTime = time
                binding.sbEndPos.progress = (time / com.daton.user.User.metadata.audioUpdateInterval).toInt()
            }

            binding.linearLayoutStartPos.setOnClickListener {
                controller.startTime = controller.currentPosition
                binding.sbStartPos.progress =
                    (controller.startTime / com.daton.user.User.metadata.audioUpdateInterval).toInt()
            }
            binding.linearLayoutEndPos.setOnClickListener {
                controller.endTime = controller.currentPosition
                binding.sbEndPos.progress =
                    (controller.endTime / com.daton.user.User.metadata.audioUpdateInterval).toInt()
            }

            binding.btnPlayPause.setOnClickListener { if (controller.isPaused) controller.play() else controller.pause() }

            binding.btnSaveLoop.setOnClickListener { displaySaveLoopDialog() }
            binding.btnSavePlaylist.setOnClickListener { displaySavePlaylistDialog() }

            controller.onPlaybackStateChanged?.invoke(controller.isPlaying)

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

    private fun displaySaveLoopDialog() {
        val editLoopName = EditText(this)
        AlertDialog.Builder(this)
            .setMessage("Enter loop name")
            .setView(editLoopName)
            .setPositiveButton("Save") { _, _ ->
                val loopName = editLoopName.text.toString()
                if (loopName.isEmpty()) {
                    Toast.makeText(
                        this,
                        "Failed to save loop: Name mustn't be empty",
                        Toast.LENGTH_LONG
                    ).show()
                    return@setPositiveButton
                }

                com.daton.user.User.metadata += Loop(
                    loopName,
                    (binding.sbStartPos.progress * com.daton.user.User.metadata.audioUpdateInterval).toLong(),
                    (binding.sbEndPos.progress * com.daton.user.User.metadata.audioUpdateInterval).toLong(),
                    // Already a loop but modified
                    if (controller.mediaId.underlyingMediaId != null) controller.mediaId.underlyingMediaId!! else controller.mediaId
                )
                controller.sendLoops(com.daton.user.User.metadata.loops)
                com.daton.user.User.metadata.saveToLocal()
                com.daton.user.User.uploadMetadata()

            }
            .setNegativeButton(android.R.string.cancel) { dialog, _ -> dialog.dismiss() }
            .create().show()
    }

    private fun displaySavePlaylistDialog() {
        controller.playlists { playlists ->
            DialogAddToPlaylist(
                controller.mediaId,
                playlists,
                this@ActivityPlayer
            ).apply {
                onCreateNewPlaylist = { name ->
                    com.daton.user.User.metadata += Playlist(name)
                    com.daton.user.User.metadata.saveToLocal()
                    com.daton.user.User.uploadMetadata()
                    controller.sendPlaylists(com.daton.user.User.metadata.playlists)
                }

                onDone = { toAdd, playlistsToAddTo ->
                    for (playlist in playlistsToAddTo) {
                        com.daton.user.User.metadata.playlists.find { it.mediaId == playlist.mediaId }!! += toAdd
                        com.daton.user.User.metadata.saveToLocal()
                        com.daton.user.User.uploadMetadata()
                        controller.sendPlaylists(com.daton.user.User.metadata.playlists)
                    }
                }

                show()
            }
        }
    }
}