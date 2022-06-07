package com.daton.mucify.ui

import android.os.Bundle
import android.support.v4.media.MediaBrowserCompat
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import androidx.annotation.Nullable
import androidx.fragment.app.Fragment
import com.daton.media.data.MediaId
import com.daton.media.ext.toMediaId
import com.daton.mucify.databinding.FragmentSelectAudioBinding


class FragmentSelectAudio(private val playbacks: List<MediaBrowserCompat.MediaItem>) : Fragment() {

    private val playbackStrings = mutableListOf<MediaId>()

    private var _binding: FragmentSelectAudioBinding? = null
    private val binding get() = _binding!!

    var onItemClicked: ((MediaId) -> Unit)? = null


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

        playbackStrings.clear()
        for (metadata in playbacks) {
            playbackStrings += metadata.mediaId!!.toMediaId()
        }

        binding.rvFiles.adapter = ArrayAdapter(
            requireContext(),
            android.R.layout.simple_list_item_1,
            playbackStrings
        )

        binding.rvFiles.setOnItemClickListener { adapterView, _, i, _ ->
            onItemClicked?.invoke(adapterView.getItemAtPosition(i) as MediaId)
        }
    }

}