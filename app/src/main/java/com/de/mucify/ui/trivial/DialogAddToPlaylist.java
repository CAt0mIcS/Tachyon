package com.de.mucify.ui.trivial;

import android.app.Dialog;
import android.content.Context;
import android.opengl.Visibility;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.de.mucify.MediaLibrary;
import com.de.mucify.R;
import com.de.mucify.UserData;
import com.de.mucify.player.Playback;
import com.de.mucify.player.Playlist;
import com.de.mucify.player.Song;
import com.de.mucify.ui.adapter.AdapterEventListener;
import com.de.mucify.ui.adapter.CreatePlaylistDialogListItemAdapter;
import com.de.mucify.ui.adapter.PlaybackListItemAdapter;
import com.de.mucify.ui.adapter.ViewHolderLoop;
import com.de.mucify.ui.adapter.ViewHolderPlaylist;
import com.de.mucify.ui.adapter.ViewHolderSong;

import java.io.IOException;

public class DialogAddToPlaylist extends Dialog implements AdapterEventListener {
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
        if(MediaLibrary.AvailablePlaylists.size() == 0) {
            mRvPlaylists.setVisibility(View.GONE);
            findViewById(R.id.bottom_divider).setVisibility(View.GONE);
        }
        else {
            mRvPlaylists.setLayoutManager(new LinearLayoutManager(getContext()));

            CreatePlaylistDialogListItemAdapter adapter = new CreatePlaylistDialogListItemAdapter(getContext(), MediaLibrary.AvailablePlaylists);
            adapter.setListener(this);
            mRvPlaylists.setAdapter(adapter);
        }

        findViewById(R.id.btnCloseAddToPlaylistDialog).setOnClickListener(v -> dismiss());
        findViewById(R.id.clCreatePlaylist).setOnClickListener(v -> {
            findViewById(R.id.addToPlaylistParent).setVisibility(View.GONE);
            mTxtPlaylistName.setVisibility(View.VISIBLE);
            mBtnCreatePlaylist.setVisibility(View.VISIBLE);
        });

        mBtnCreatePlaylist.setOnClickListener(v -> {
            
        });
    }

    @Override
    public void onClick(ViewHolderSong holder) {}
    @Override
    public void onClick(ViewHolderLoop holder) {}

    @Override
    public void onClick(ViewHolderPlaylist holder) {
        Playlist playlist = MediaLibrary.AvailablePlaylists.get(holder.getAdapterPosition());
        playlist.addSong(mSongToAdd);
        try {
            playlist.save();
        } catch (IOException e) {
            e.printStackTrace();
            // MY_TODO: Error handling
        }
    }
}