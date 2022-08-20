package com.daton.mucify.ui

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import androidx.annotation.Nullable
import androidx.fragment.app.Fragment
import com.daton.media.MediaController
import com.daton.media.device.BrowserTree
import com.daton.media.playback.Loop
import com.daton.media.playback.Playback
import com.daton.media.playback.Playlist
import com.daton.media.playback.Song
import com.daton.mucify.databinding.FragmentSelectAudioBinding


class FragmentSelectAudio(private val controller: MediaController?) : Fragment() {

    companion object {
        const val TAG = "FragmentSelectAudio"
    }

    private val playbackStrings: MutableList<String> = mutableListOf()
    private val playbacks: MutableList<Playback> = mutableListOf()

    private var _binding: FragmentSelectAudioBinding? = null
    private val binding get() = _binding!!

    var onItemClicked: ((Playback) -> Unit)? = null


    constructor() : this(null)

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

        controller?.subscribe(BrowserTree.SONG_ROOT) {
            Log.d(TAG, "Songs updated")
            for (song in it) {
                val str = (song as Song).title + " - " + song.artist
                if (!playbackStrings.contains(str)) {
                    playbackStrings += str
                    playbacks += song
                }
            }
            (binding.rvFiles.adapter as ArrayAdapter<*>).notifyDataSetChanged()
        }

        controller?.subscribe(BrowserTree.LOOP_ROOT) {
            Log.d(TAG, "Loops updated")
            for (loop in it) {
                val str = (loop as Loop).name + " - " + loop.title + " - " + loop.artist
                if (!playbackStrings.contains(str)) {
                    playbackStrings += str
                    playbacks += loop
                }
            }
            (binding.rvFiles.adapter as ArrayAdapter<*>).notifyDataSetChanged()
        }

        controller?.subscribe(BrowserTree.PLAYLIST_ROOT) {
            Log.d(TAG, "Playlists updated")
            for (playlist in it) {
                if (!playbackStrings.contains((playlist as Playlist).name)) {
                    playbackStrings += playlist.name
                    playbacks += playlist
                }
            }
            (binding.rvFiles.adapter as ArrayAdapter<*>).notifyDataSetChanged()
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