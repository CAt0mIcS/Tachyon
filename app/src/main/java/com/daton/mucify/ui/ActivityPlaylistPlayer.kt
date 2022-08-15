package com.daton.mucify.ui

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.widget.ArrayAdapter
import android.widget.SeekBar
import android.widget.SeekBar.OnSeekBarChangeListener
import androidx.appcompat.app.AppCompatActivity
import com.daton.media.MediaController
import com.daton.media.device.Loop
import com.daton.media.device.Playlist
import com.daton.media.device.SinglePlayback
import com.daton.media.device.Song
import com.daton.media.ext.*
import com.daton.mucify.R
import com.daton.mucify.Util
import com.daton.mucify.databinding.ActivityPlaylistPlayerBinding
import com.daton.user.User


class ActivityPlaylistPlayer : AppCompatActivity() {

    private lateinit var binding: ActivityPlaylistPlayerBinding
    private val controller = MediaController()

    private var playbackStrings = mutableListOf<String>()

    private var isSeeking = false
    private val handler = Handler(Looper.getMainLooper())

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPlaylistPlayerBinding.inflate(layoutInflater)
        setContentView(binding.root)

        controller.create(this)

        binding.rvPlaylistItems.adapter = ArrayAdapter(
            this,
            android.R.layout.simple_list_item_1,
            playbackStrings
        )

        binding.rvPlaylistItems.setOnItemClickListener { adapterView, _, i, _ ->
            (controller.playback as Playlist).currentPlaylistIndex = i
            controller.play()
        }

        // TODO: A lot of unnecessary calls
        controller.onPlaybackChanged = {
            val playlist = controller.playback as Playlist?
            if (playlist?.current != null) {
                binding.txtPlaylistTitle.text = playlist.name
                binding.txtTitle.text = playlist.current!!.title

                binding.sbPos.max =
                    (playlist.current!!.duration / User.metadata.audioUpdateInterval).toInt()
            }
        }

        controller.onPlaybackStateChanged = { isPlaying ->
            binding.btnPlayPause.setImageResource(if (isPlaying) R.drawable.pause else R.drawable.play)
        }

        controller.onConnected = {
            playbackStrings.addAll((controller.playback!! as Playlist).playbacks.map {
                when (it) {
                    is Song -> "*song*" + it.title + " - " + it.artist
                    is Loop -> "*loop*" + it.name + " - " + it.title + " - " + it.artist
                    else -> TODO("Invalid playback type in playlist")
                }
            })

            runOnUiThread(object : Runnable {
                override fun run() {
                    if (controller.isCreated && controller.isPlaying && !isSeeking) {
                        val currentPos: Int =
                            (controller.currentPosition / User.metadata.audioUpdateInterval).toInt()
                        binding.sbPos.progress = currentPos
                    }
                    if (!isDestroyed)
                        handler.postDelayed(
                            this,
                            User.metadata.audioUpdateInterval.toLong()
                        )
                }
            })

            Handler(Looper.getMainLooper()).post {
                (binding.rvPlaylistItems.adapter as ArrayAdapter<*>).notifyDataSetChanged()
                controller.onPlaybackStateChanged!!(controller.isPlaying)
                controller.onPlaybackChanged!!()
            }
        }


        binding.sbPos.setOnSeekBarChangeListener(object : OnSeekBarChangeListener {
            override fun onStopTrackingTouch(seekBar: SeekBar) {
                isSeeking = false
                controller.seekTo((seekBar.progress * User.metadata.audioUpdateInterval).toLong())
            }

            override fun onStartTrackingTouch(seekBar: SeekBar) {
                isSeeking = true
            }

            override fun onProgressChanged(
                seekBar: SeekBar,
                progress: Int,
                fromUser: Boolean
            ) {
                binding.txtPos.text =
                    Util.millisecondsToReadableString(progress * User.metadata.audioUpdateInterval)
            }
        })

        binding.btnPlayPause.setOnClickListener { if (controller.isPaused) controller.play() else controller.pause() }

        binding.btnNext.setOnClickListener { controller.next() }
        binding.btnPrevious.setOnClickListener { controller.previous() }
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