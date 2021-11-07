package com.de.mucify.activity.controller;

import android.content.Intent;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.de.mucify.R;
import com.de.mucify.activity.SingleAudioActivity;
import com.de.mucify.activity.SingleAudioPlayActivity;
import com.de.mucify.adapter.SongListItemAdapter;
import com.de.mucify.playable.Song;
import com.de.mucify.util.MediaLibrary;

import java.util.ArrayList;

public class AudioSelectController {
    private final SingleAudioActivity mActivity;
    private final ArrayList<Song> mListItems;

    public AudioSelectController(SingleAudioActivity activity) {
        mActivity = activity;

        if(mActivity.isInSongTab())
            mListItems = MediaLibrary.loadAvailableSongs();
        else
            mListItems = MediaLibrary.loadAvailableLoops();

        RecyclerView rv = mActivity.findViewById(R.id.rvFiles);
        rv.setLayoutManager(new LinearLayoutManager(mActivity));
        rv.setAdapter(new SongListItemAdapter(mActivity, mListItems, this::onFileClicked));
        rv.getAdapter().notifyDataSetChanged();
    }

    private void onFileClicked(SongListItemAdapter.SongViewHolder holder) {
        Intent i = new Intent(mActivity, SingleAudioPlayActivity.class);
        i.putExtra("SongFilePath", mListItems.get(holder.getAdapterPosition()).getSongPath().getAbsolutePath());
        mActivity.startActivity(i);
        mActivity.finish();
    }
}
