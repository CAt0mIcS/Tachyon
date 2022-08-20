package com.tachyonmusic.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import androidx.annotation.Nullable
import androidx.fragment.app.Fragment
import com.tachyonmusic.media.playback.Playlist
import com.tachyonmusic.media.playback.SinglePlayback
import com.tachyonmusic.app.databinding.FragmentAddToPlaylistBinding


class FragmentAddToPlaylist(
    val playbackToAdd: SinglePlayback,
    private val playlists: List<Playlist>
) :
    Fragment() {

    private var _binding: FragmentAddToPlaylistBinding? = null
    private val binding get() = _binding!!

    var onCreateNewPlaylist: ((String /*playlistName*/) -> Unit)? = null
    var onChanged: ((SinglePlayback /*playbackToAdd*/, Playlist /*playlistToAddTo*/) -> Unit)? =
        null


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentAddToPlaylistBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    override fun onViewCreated(view: View, @Nullable savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        if (playlists.isEmpty()) {
            binding.rvPlaylists.visibility = View.GONE
            binding.bottomDivider.visibility = View.GONE
        } else {
            binding.rvPlaylists.adapter =
                ArrayAdapter(requireContext(), android.R.layout.simple_list_item_1, playlists)

            binding.rvPlaylists.setOnItemClickListener { adapterView, _, i, _ ->
                val playlist = adapterView.getItemAtPosition(i) as Playlist
                onChanged?.invoke(playbackToAdd, playlist)

                (binding.rvPlaylists.adapter as ArrayAdapter<*>).notifyDataSetChanged()
            }
        }

        binding.btnCloseAddToPlaylistDialog.setOnClickListener { parentFragmentManager.popBackStack() }
        binding.btnCreatePlaylist.setOnClickListener {
            onCreateNewPlaylist?.invoke(binding.txtNewPlaylist.text.toString())
            parentFragmentManager.popBackStack()
        }
    }
}