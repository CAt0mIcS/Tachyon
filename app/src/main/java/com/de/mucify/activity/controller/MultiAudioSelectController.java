package com.de.mucify.activity.controller;

import android.content.Intent;
import android.text.Editable;
import android.text.TextWatcher;
import android.widget.EditText;

import androidx.appcompat.widget.PopupMenu;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.de.mucify.R;
import com.de.mucify.activity.MultiAudioActivity;
import com.de.mucify.activity.MultiAudioEditActivity;
import com.de.mucify.activity.MultiAudioPlayActivity;
import com.de.mucify.activity.PlaylistCreateActivity;
import com.de.mucify.adapter.PlaylistListItemAdapter;
import com.de.mucify.playable.Playlist;
import com.de.mucify.playable.Song;
import com.de.mucify.util.MediaLibrary;

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
        adapter.setOnViewClickedListener(R.id.rvItemLayout, this::onFileClicked);
        adapter.setOnViewClickedListener(R.id.btnFileOptions, this::onFileOptionsClicked);
        mRvFiles.setAdapter(adapter);

        mRvFiles.getAdapter().notifyDataSetChanged();
    }

    private void onFileClicked(RecyclerView.ViewHolder holder) {
        Intent i = new Intent(mActivity, MultiAudioPlayActivity.class);
        i.putExtra("AudioFilePath", getPlaylistFromViewHolder(holder).getPlaylistFilePath().getAbsolutePath());
        mActivity.startActivity(i);
        mActivity.finish();
    }

    private void onFileOptionsClicked(PlaylistListItemAdapter.PlaylistViewHolder holder) {
        PopupMenu popup = new PopupMenu(mActivity, holder.BtnFileOptions);
        popup.inflate(R.menu.loop_playlist_options_menu);

        popup.setOnMenuItemClickListener(item -> {
            switch (item.getItemId()) {
                case R.id.edit:
                    Intent i = new Intent(mActivity, MultiAudioEditActivity.class);
                    i.putExtra("AudioID", holder.getAdapterPosition());
                    mActivity.startActivity(i);
                    mActivity.finish();
                    return true;
                case R.id.delete:
                    getPlaylistFromViewHolder(holder).delete();
                    MediaLibrary.loadAvailablePlaylists();
                    mRvFiles.getAdapter().notifyDataSetChanged();
                    return true;
            }
            return false;
        });
        //displaying the popup
        popup.show();
    }

    private Playlist getPlaylistFromViewHolder(RecyclerView.ViewHolder holder) { return mListItems.get(holder.getAdapterPosition()); }
}
