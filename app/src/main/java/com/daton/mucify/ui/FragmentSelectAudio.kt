package com.daton.mucify.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import androidx.annotation.Nullable
import androidx.fragment.app.Fragment
import com.daton.media.playback.Loop
import com.daton.media.playback.Playback
import com.daton.media.playback.Playlist
import com.daton.media.playback.Song
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
            when (it) {
                is Song -> it.title + " - " + it.artist
                is Loop -> it.name + " - " + it.title + " - " + it.artist
                is Playlist -> "*playlist*" + it.name
                else -> TODO("Invalid playback type")
            }
        }

        binding.rvFiles.adapter = ArrayAdapter(
            requireContext(),
            android.R.layout.simple_list_item_1,
            playbackStrings
        )

        binding.rvFiles.setOnItemClickListener { _, _, i, _ ->
            onItemClicked?.invoke(playbacks[i])
        }
    }

}