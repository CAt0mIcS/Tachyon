package com.de.mucify.activity.controller;

import android.app.AlertDialog;
import android.content.Intent;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.de.mucify.R;
import com.de.mucify.activity.MultiAudioActivity;
import com.de.mucify.activity.MultiAudioPlayActivity;
import com.de.mucify.activity.PlaylistCreateActivity;
import com.de.mucify.activity.SingleAudioActivity;
import com.de.mucify.activity.SingleAudioPlayActivity;
import com.de.mucify.adapter.LoopListItemAdapter;
import com.de.mucify.adapter.PlaylistListItemAdapter;
import com.de.mucify.adapter.SongListItemAdapter;
import com.de.mucify.playable.AudioController;
import com.de.mucify.playable.Playlist;
import com.de.mucify.playable.Song;
import com.de.mucify.util.FileManager;
import com.de.mucify.util.MediaLibrary;
import com.de.mucify.util.Utils;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Locale;

public class MultiAudioSelectController {
    private final MultiAudioActivity mActivity;
    private final ArrayList<Playlist> mListItems = new ArrayList<>();

    private final RecyclerView mRvFiles;

    public MultiAudioSelectController(MultiAudioActivity activity) {
        mActivity = activity;

        MediaLibrary.loadAvailablePlaylists();
        mRvFiles = mActivity.findViewById(R.id.rvFiles);
        loadPlaylists();

        ((EditText)mActivity.findViewById(R.id.editSearchFiles)).addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override
            public void onTextChanged(CharSequence s1, int start, int before, int count) {
                mListItems.clear();
                mListItems.addAll(MediaLibrary.AvailablePlaylists);

                String s = s1.toString().toLowerCase(Locale.ROOT);
                if(s.equals("")) {
                    mRvFiles.getAdapter().notifyDataSetChanged();
                    return;
                }

                for(int i = 0; i < mListItems.size(); ++i) {
                    Playlist playlist = mListItems.get(i);

                    // Search through songs in playlist
                    boolean containsSong = false;
                    for(Song song : playlist.getSongs()) {
                        if(song.getTitle().toLowerCase(Locale.ROOT).contains(s) || song.getArtist().toLowerCase(Locale.ROOT).contains(s) || (song.isLoop() && song.getLoopName().toLowerCase(Locale.ROOT).contains(s))) {
                            containsSong = true;
                            break;
                        }
                    }
                    if(!containsSong && !playlist.getName().toLowerCase(Locale.ROOT).contains(s)) {
                        mListItems.remove(i);
                        --i;
                    }
                }
                mRvFiles.getAdapter().notifyDataSetChanged();
            }
            @Override
            public void afterTextChanged(Editable s) {}
        });
        mActivity.findViewById(R.id.btnAddPlaylist).setOnClickListener(v -> {
            Intent i = new Intent(mActivity, PlaylistCreateActivity.class);
            mActivity.startActivity(i);
            mActivity.finish();
        });
    }

    private void loadPlaylists() {
        mListItems.clear();
        mRvFiles.setLayoutManager(new LinearLayoutManager(mActivity));
        mListItems.addAll(MediaLibrary.AvailablePlaylists);

        PlaylistListItemAdapter adapter = new PlaylistListItemAdapter(mActivity, mListItems);
        adapter.setOnItemClicked(this::onFileClicked);
        adapter.setOnItemLongClicked(this::onPlaylistLongClicked);
        mRvFiles.setAdapter(adapter);

        mRvFiles.getAdapter().notifyDataSetChanged();
    }

    private void onFileClicked(RecyclerView.ViewHolder holder) {
        Intent i = new Intent(mActivity, MultiAudioPlayActivity.class);
        Playlist playlist = mListItems.get(holder.getAdapterPosition());
        i.putExtra("AudioFilePath", playlist.getPlaylistFilePath().getAbsolutePath());
        mActivity.startActivity(i);
        mActivity.finish();
    }

    private void onPlaylistLongClicked(RecyclerView.ViewHolder v) {

        LoopListItemAdapter.LoopViewHolder holder = (LoopListItemAdapter.LoopViewHolder)v;

        // Use the Builder class for convenient dialog construction
        AlertDialog.Builder builder = new AlertDialog.Builder(mActivity);
        builder.setMessage(mActivity.getString(R.string.delete_loop) + " \"" + holder.getName() + "\"?")
                .setPositiveButton(mActivity.getString(R.string.yes), (dialog, id) -> {
                    File loopFile = FileManager.loopNameToFile(holder.getName());
                    if(!loopFile.delete())
                        Toast.makeText(mActivity,
                                "Failed to delete loop: " + holder.getName(), Toast.LENGTH_LONG).show();
                })
                .setNegativeButton(R.string.cancel, (dialog, id) -> dialog.dismiss());
        builder.create().show();
    }
}
