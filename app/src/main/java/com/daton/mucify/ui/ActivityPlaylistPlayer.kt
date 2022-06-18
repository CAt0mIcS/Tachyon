package com.daton.mucify.ui

import android.os.Bundle
import android.widget.ArrayAdapter
import androidx.appcompat.app.AppCompatActivity
import com.daton.media.MediaController
import com.daton.media.data.MediaId
import com.daton.media.ext.toMediaId
import com.daton.mucify.databinding.ActivityPlaylistPlayerBinding

class ActivityPlaylistPlayer : AppCompatActivity() {

    private lateinit var binding: ActivityPlaylistPlayerBinding
    private val controller = MediaController()

    private var playbackStrings = listOf<MediaId>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPlaylistPlayerBinding.inflate(layoutInflater)
        setContentView(binding.root)

        controller.create(this)

        controller.onMediaIdChanged = {
            binding.txtPlaylistTitle.text = controller.playlistName
            binding.txtTitle.text = controller.title

            binding.rvPlaylistItems.adapter = ArrayAdapter(
                this,
                android.R.layout.simple_list_item_1,
                playbackStrings
            )
        }

        controller.onConnected = {
            // Getting media id requires connection to MediaService
            controller.subscribe(controller.mediaId.baseMediaId.serialize()) { playlistItems ->
                playbackStrings = playlistItems.map { it.mediaId!!.toMediaId() }
                (binding.rvPlaylistItems.adapter as ArrayAdapter<*>).notifyDataSetChanged()
            }
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