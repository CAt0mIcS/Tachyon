package com.daton.mucify.ui

import android.app.Dialog
import android.content.Context
import android.os.Bundle
import android.view.Menu
import android.view.View
import android.view.Window
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.ListView
import com.daton.media.data.MediaId
import com.daton.media.device.Playback
import com.daton.media.device.Playlist
import com.daton.mucify.R


class DialogAddToPlaylist(
    val playbackToAdd: MediaId,
    private val playlists: List<Playlist>,
    context: Context
) :
    Dialog(context) {

    private lateinit var lstPlaylists: ListView
    private lateinit var btnCreatePlaylist: Button
    private lateinit var txtPlaylistName: EditText

    /**
     * List of playlists to add [playbackToAdd] to
     */
    val playlistsToAddTo = mutableSetOf<Playlist>()

    var onCreateNewPlaylist: ((String /*playlistName*/) -> Unit)? = null
    var onDone: ((MediaId /*playbackToAdd*/, Set<Playlist> /*playlistsToAddTo*/) -> Unit)? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        requestWindowFeature(Window.FEATURE_NO_TITLE)
        setContentView(R.layout.dialog_add_to_playlist)

        lstPlaylists = findViewById(R.id.rvPlaylists)!!
        btnCreatePlaylist = findViewById(R.id.btnCreatePlaylist)!!
        txtPlaylistName = findViewById(R.id.txtNewPlaylist)!!

        // Hide some UI elements if no playlists exist
        if (playlists.isEmpty()) {
            lstPlaylists.visibility = View.GONE
            findViewById<View>(R.id.bottom_divider).visibility = View.GONE
        } else {
            lstPlaylists.adapter =
                ArrayAdapter(context, android.R.layout.simple_list_item_checked, playlists)

            lstPlaylists.setOnItemClickListener { adapterView, _, i, _ ->
                val playlist = adapterView.getItemAtPosition(i) as Playlist
                if (playlistsToAddTo.contains(playlist))
                    playlistsToAddTo -= playlist
                else
                    playlistsToAddTo += playlist
            }
        }
        findViewById<View>(R.id.btnCloseAddToPlaylistDialog).setOnClickListener { dismiss() }
        findViewById<View>(R.id.clCreatePlaylist).setOnClickListener {
            findViewById<View>(R.id.addToPlaylistParent).visibility = View.GONE
            txtPlaylistName.visibility = View.VISIBLE
            btnCreatePlaylist.visibility = View.VISIBLE
        }
        btnCreatePlaylist.setOnClickListener {
            onCreateNewPlaylist?.invoke(txtPlaylistName.text.toString())
            dismiss()
        }
    }

    override fun onStop() {
        super.onStop()
        onDone?.invoke(playbackToAdd, playlistsToAddTo)
    }
}