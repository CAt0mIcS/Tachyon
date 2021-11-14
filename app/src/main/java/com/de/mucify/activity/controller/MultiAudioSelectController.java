package com.de.mucify.activity.controller;

import android.content.Intent;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.de.mucify.R;
import com.de.mucify.activity.MultiAudioActivity;
import com.de.mucify.activity.MultiAudioPlayActivity;
import com.de.mucify.activity.SingleAudioPlayActivity;
import com.de.mucify.adapter.PlaylistListItemAdapter;
import com.de.mucify.playable.Playlist;
import com.de.mucify.util.MediaLibrary;

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
    }

    private void onFileClicked(RecyclerView.ViewHolder holder) {
        Intent i = new Intent(mActivity, MultiAudioPlayActivity.class);
        Playlist playlist = MediaLibrary.AvailablePlaylists.get(holder.getAdapterPosition());
        i.putExtra("PlaylistFilePath", playlist.getPlaylistFilePath().getAbsolutePath());
        mActivity.startActivity(i);
        mActivity.finish();
    }
}
