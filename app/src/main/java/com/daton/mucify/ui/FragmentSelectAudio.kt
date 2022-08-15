package com.daton.mucify.ui

import android.os.Bundle
import android.support.v4.media.MediaBrowserCompat
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import androidx.annotation.Nullable
import androidx.fragment.app.Fragment
import com.daton.media.device.Loop
import com.daton.media.device.Playback
import com.daton.media.device.Playlist
import com.daton.media.device.Song
import com.daton.media.ext.*
import com.daton.mucify.databinding.FragmentSelectAudioBinding


class FragmentSelectAudio(private val playbacks: List<Playback>) : Fragment() {

    private lateinit var playbackStrings: List<String>

    private var _binding: FragmentSelectAudioBinding? = null
    private val binding get() = _binding!!

    var onItemClicked: ((Playback) -> Unit)? = null


    constructor() : this(listOf())

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSelectAudioBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    override fun onViewCreated(view: View, @Nullable savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        playbackStrings = playbacks.map {
            if (it is Song)
                it.title + " - " + it.artist
            else if (it is Loop)
                it.name + " - " + it.title + " - " + it.artist
            else if (it is Playlist)
                "*playlist*" + it.name
            else
                TODO("Invalid playback type")
        }

        binding.rvFiles.adapter = ArrayAdapter(
            requireContext(),
            android.R.layout.simple_list_item_1,
            playbackStrings
        )

        binding.rvFiles.setOnItemClickListener { adapterView, _, i, _ ->
            onItemClicked?.invoke(playbacks[i])
        }
    }

}