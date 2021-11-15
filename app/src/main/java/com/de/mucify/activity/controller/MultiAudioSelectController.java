package com.de.mucify.activity.controller;

import android.app.AlertDialog;
import android.content.Intent;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.de.mucify.R;
import com.de.mucify.activity.MultiAudioActivity;
import com.de.mucify.activity.MultiAudioPlayActivity;
import com.de.mucify.adapter.PlaylistListItemAdapter;
import com.de.mucify.playable.AudioController;
import com.de.mucify.playable.Playlist;
import com.de.mucify.util.MediaLibrary;

import java.io.IOException;

public class MultiAudioSelectController {
    private final MultiAudioActivity mActivity;

    public MultiAudioSelectController(MultiAudioActivity activity) {
        mActivity = activity;

        MediaLibrary.loadAvailablePlaylists();

        RecyclerView rv = mActivity.findViewById(R.id.rvFiles);
        rv.setLayoutManager(new LinearLayoutManager(mActivity));

        PlaylistListItemAdapter adapter = new PlaylistListItemAdapter(mActivity, MediaLibrary.AvailablePlaylists);
        adapter.setOnItemClicked(this::onFileClicked);
        rv.setAdapter(adapter);

        adapter.notifyDataSetChanged();

        mActivity.findViewById(R.id.btnAddPlaylist).setOnClickListener(this::onAddPlaylistClicked);

    }

    private void onFileClicked(RecyclerView.ViewHolder holder) {
        Intent i = new Intent(mActivity, MultiAudioPlayActivity.class);
        Playlist playlist = MediaLibrary.AvailablePlaylists.get(holder.getAdapterPosition());
        playlist.create(mActivity);

        if(playlist.getSongs().size() == 0) {
            Toast.makeText(mActivity, "Playlist \"" + playlist.getName() + "\" is empty", Toast.LENGTH_SHORT).show();
            return;
        }

        i.putExtra("PlaylistFilePath", playlist.getPlaylistFilePath().getAbsolutePath());
        mActivity.startActivity(i);
        mActivity.finish();
    }

    private void onAddPlaylistClicked(View v) {
        new AlertDialog.Builder(mActivity)
                .setMessage(R.string.dialog_new_playlist)
                .setView(mActivity.getLayoutInflater().inflate(R.layout.save_loop_new_playlist_alert_dialog_layout, null))
                .setPositiveButton(R.string.save, (dialog, id) -> {

                    String playlistName = ((EditText)((AlertDialog)dialog).findViewById(R.id.dialog_txtName)).getText().toString();
                    if(playlistName.isEmpty() || playlistName.contains("_")) {
                        Toast.makeText(mActivity, "Failed to save playlist: Name mustn't contain '_' or be empty", Toast.LENGTH_LONG).show();
                        return;
                    }

                    try {
                        Playlist.createNew(mActivity, playlistName);
                    } catch (IOException e) {
                        Toast.makeText(mActivity, "Failed to save loop: " + e.getMessage(), Toast.LENGTH_LONG).show();
                        return;
                    }

                    MediaLibrary.loadAvailablePlaylists();
                })
                .setNegativeButton(R.string.cancel, (dialog, id) -> dialog.dismiss())
                .create().show();
    }
}
