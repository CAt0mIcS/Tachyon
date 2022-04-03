package com.de.mucify.ui.trivial;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.de.common.FileManager;
import com.de.common.MediaLibrary;
import com.de.mucify.R;
import com.de.common.Util;
import com.de.common.player.Playlist;
import com.de.common.player.Song;
import com.de.mucify.ui.adapter.CreatePlaylistDialogListItemAdapter;
import com.de.mucify.ui.adapter.ViewHolderPlaylist;

import java.io.IOException;
import java.util.Arrays;

public class DialogAddToPlaylist extends Dialog {
    private RecyclerView mRvPlaylists;
    private EditText mTxtPlaylistName;
    private Button mBtnCreatePlaylist;

    private Song mSongToAdd;

    public DialogAddToPlaylist(Song song, Context context) {
        super(context);
        mSongToAdd = song;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.dialog_save_create_playlist);
        mRvPlaylists = findViewById(R.id.rvPlaylists);
        mBtnCreatePlaylist = findViewById(R.id.btnCreateNewPlaylist);
        mTxtPlaylistName = findViewById(R.id.editPlaylistName);

        // Hide some UI elements if no playlists exist
        if (MediaLibrary.AvailablePlaylists.size() == 0) {
            mRvPlaylists.setVisibility(View.GONE);
            findViewById(R.id.bottom_divider).setVisibility(View.GONE);
        } else {
            mRvPlaylists.setLayoutManager(new LinearLayoutManager(getContext()));

            CreatePlaylistDialogListItemAdapter adapter = new CreatePlaylistDialogListItemAdapter(getContext(), MediaLibrary.AvailablePlaylists);
            adapter.setListener(new AdapterEventListener());
            mRvPlaylists.setAdapter(adapter);
        }

        findViewById(R.id.btnCloseAddToPlaylistDialog).setOnClickListener(v -> dismiss());
        findViewById(R.id.clCreatePlaylist).setOnClickListener(v -> {
            findViewById(R.id.addToPlaylistParent).setVisibility(View.GONE);
            mTxtPlaylistName.setVisibility(View.VISIBLE);
            mBtnCreatePlaylist.setVisibility(View.VISIBLE);
        });

        mBtnCreatePlaylist.setOnClickListener(v -> {
            String name = mTxtPlaylistName.getText().toString();
            try {
                Playlist.save(FileManager.playlistNameToFile(name));
            } catch (IOException e) {
                // MY_TODO: Error handling
                e.printStackTrace();
                Util.logGlobal(Arrays.toString(e.getStackTrace()));
            }
            MediaLibrary.loadLoopsAndPlaylists(getContext(), () -> {
            });
            dismiss();
        });
    }


    private class AdapterEventListener extends com.de.mucify.ui.adapter.AdapterEventListener {
        @Override
        public void onCheckedChanged(ViewHolderPlaylist holder, boolean checked) {
            Playlist playlist = MediaLibrary.AvailablePlaylists.get(holder.getAdapterPosition());
            if (checked)
                playlist.addSong(mSongToAdd);
            else
                playlist.removeSong(mSongToAdd);
            try {
                playlist.save();
            } catch (IOException e) {
                e.printStackTrace();
                // MY_TODO: Error handling
                Util.logGlobal(Arrays.toString(e.getStackTrace()));
            }
        }
    }
}
