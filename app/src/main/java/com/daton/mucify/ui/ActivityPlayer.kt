package com.daton.mucify.ui

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.widget.EditText
import android.widget.SeekBar
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.daton.media.MediaController
import com.daton.media.playback.Loop
import com.daton.media.playback.Playlist
import com.daton.media.playback.SinglePlayback
import com.daton.media.playback.Song
import com.daton.mucify.R
import com.daton.mucify.Util
import com.daton.mucify.databinding.ActivityPlayerBinding
import com.daton.user.User


class ActivityPlayer : AppCompatActivity(),
    MediaController.IEventListener by MediaController.EventListener() {
    private val controller = MediaController()
    private lateinit var binding: ActivityPlayerBinding

    private val audioUpdateInterval = 100
    private val songIncDecInterval = 100

    private val handler = Handler(Looper.getMainLooper())

    private var isSeeking = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPlayerBinding.inflate(layoutInflater)
        setContentView(binding.root)

        controller.create(this)
        controller.registerEventListener(this)
    }

    override fun onStart() {
        super.onStart()
        controller.connect(this)
    }

    override fun onStop() {
        super.onStop()
        controller.disconnect()
    }

    override fun onConnected() {
        runOnUiThread(object : Runnable {
            override fun run() {
                if (controller.isCreated && controller.isPlaying && !isSeeking) {
                    val currentPos: Int =
                        (controller.currentPosition / audioUpdateInterval).toInt()
                    binding.sbPos.progress = currentPos
                }
                if (!isDestroyed)
                    handler.postDelayed(
                        this,
                        audioUpdateInterval.toLong()
                    )
            }
        })

        binding.sbPos.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(p0: SeekBar?, progress: Int, p2: Boolean) {
                binding.txtPos.text =
                    Util.millisecondsToReadableString(progress * audioUpdateInterval)
            }

            override fun onStartTrackingTouch(p0: SeekBar?) {
                isSeeking = true
            }

            override fun onStopTrackingTouch(sb: SeekBar) {
                isSeeking = false
                controller.seekTo(sb.progress * audioUpdateInterval.toLong())
            }
        })

        binding.sbStartPos.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(p0: SeekBar?, progress: Int, p2: Boolean) {
                binding.txtStartPos.text =
                    Util.millisecondsToReadableString(progress * audioUpdateInterval)
            }

            override fun onStartTrackingTouch(p0: SeekBar?) {}

            override fun onStopTrackingTouch(sb: SeekBar) {
                controller.playback?.startTime =
                    (sb.progress * audioUpdateInterval).toLong()
            }

        })

        binding.sbEndPos.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(p0: SeekBar?, progress: Int, p2: Boolean) {
                binding.txtEndPos.text =
                    Util.millisecondsToReadableString(progress * audioUpdateInterval)
            }

            override fun onStartTrackingTouch(p0: SeekBar?) {}

            override fun onStopTrackingTouch(sb: SeekBar) {
                controller.playback?.endTime =
                    (sb.progress * audioUpdateInterval).toLong()
            }

        })

        binding.btnStartPosDec.setOnClickListener {
            val playback = controller.playback as SinglePlayback
            var time: Long =
                playback.startTime - songIncDecInterval
            if (time < 0) time = 0
            playback.startTime = time
            binding.sbStartPos.progress =
                (time / audioUpdateInterval).toInt()
        }
        binding.btnStartPosInc.setOnClickListener {
            val playback = controller.playback as SinglePlayback
            var time: Long =
                playback.startTime + songIncDecInterval
            if (time > playback.duration) time = playback.duration
            playback.startTime = time
            binding.sbStartPos.progress =
                (time / audioUpdateInterval).toInt()
        }
        binding.btnEndPosDec.setOnClickListener {
            val playback = controller.playback as SinglePlayback
            var time: Long =
                playback.endTime - songIncDecInterval
            if (time < 0) time = 0
            playback.endTime = time
            binding.sbEndPos.progress =
                (time / audioUpdateInterval).toInt()
        }
        binding.btnEndPosInc.setOnClickListener {
            val playback = controller.playback as SinglePlayback
            var time: Long =
                playback.endTime + songIncDecInterval
            if (time > playback.duration) time = playback.duration
            playback.endTime = time
            binding.sbEndPos.progress =
                (time / audioUpdateInterval).toInt()
        }

        binding.linearLayoutStartPos.setOnClickListener {
            val playback = controller.playback as SinglePlayback
            playback.startTime = controller.currentPosition
            binding.sbStartPos.progress =
                (playback.startTime / audioUpdateInterval).toInt()
        }
        binding.linearLayoutEndPos.setOnClickListener {
            val playback = controller.playback as SinglePlayback
            playback.endTime = controller.currentPosition
            binding.sbEndPos.progress =
                (playback.endTime / audioUpdateInterval).toInt()
        }

        binding.btnPlayPause.setOnClickListener { if (controller.isPaused) controller.play() else controller.pause() }

        binding.btnSaveLoop.setOnClickListener { displaySaveLoopDialog() }
        binding.btnSavePlaylist.setOnClickListener { displaySavePlaylistDialog() }

        onPlaybackStateChanged(controller.isPlaying)
        // Media id is set by [ActivityMain] before transitioning to [ActivityPlayer]
        onSetPlayback()
    }

    override fun onSetPlayback() {
        val playback = controller.playback as SinglePlayback?
        if (playback != null) {
            binding.txtTitle.text = playback.title
            binding.txtArtist.text = playback.artist

            val duration =
                (playback.duration / audioUpdateInterval).toInt()
            binding.sbPos.max = duration
            binding.sbStartPos.max = duration
            binding.sbEndPos.max = duration

            binding.sbStartPos.progress =
                (playback.startTime / audioUpdateInterval).toInt()
            binding.sbEndPos.progress =
                (playback.endTime / audioUpdateInterval).toInt()
        }
    }

    override fun onPlaybackStateChanged(isPlaying: Boolean) {
        binding.btnPlayPause.setImageResource(if (isPlaying) R.drawable.pause else R.drawable.play)
    }

    private fun displaySaveLoopDialog() {
//        val editLoopName = EditText(this)
//        AlertDialog.Builder(this)
//            .setMessage("Enter loop name")
//            .setView(editLoopName)
//            .setPositiveButton("Save") { _, _ ->
//                val loopName = editLoopName.text.toString()
//                if (loopName.isEmpty()) {
//                    Toast.makeText(
//                        this,
//                        "Failed to save loop: Name mustn't be empty",
//                        Toast.LENGTH_LONG
//                    ).show()
//                    return@setPositiveButton
//                }
//
//                User.metadata += Loop(
//                    loopName,
//                    (binding.sbStartPos.progress * audioUpdateInterval).toLong(),
//                    (binding.sbEndPos.progress * audioUpdateInterval).toLong(),
//                    // Already a loop but modified
//                    if (controller.playback is Loop) (controller.playback as Loop).song else controller.playback as Song
//                )
//                controller.sendLoops(loops)
//                saveToLocal()
//                User.uploadMetadata()
//
//            }
//            .setNegativeButton(android.R.string.cancel) { dialog, _ -> dialog.dismiss() }
//            .create().show()
    }

    private fun displaySavePlaylistDialog() {
//        FragmentAddToPlaylist(
//            controller.playback!! as SinglePlayback,
//            playlists
//        ).apply {
//            onCreateNewPlaylist = { name ->
//                User.metadata += Playlist(name)
//                saveToLocal()
//                User.uploadMetadata()
//                controller.sendPlaylists(playlists)
//            }
//
//            onChanged = { toAdd, playlistToAddTo ->
//
//                if (playlistToAddTo.playbacks.contains(toAdd))
//                    playlistToAddTo.playbacks.remove(toAdd)
//                else
//                    playlistToAddTo.playbacks.add(toAdd)
//
//                saveToLocal()
//                User.uploadMetadata()
//                controller.sendPlaylists(playlists)
//            }
//
//            supportFragmentManager.beginTransaction()
//                .addToBackStack(null)
//                .add(R.id.fragment_container_view, this)
//                .commit()
//        }
    }
}