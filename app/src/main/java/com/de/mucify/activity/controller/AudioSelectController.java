package com.de.mucify.activity.controller;

import android.content.Intent;
import android.view.MenuItem;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.de.mucify.R;
import com.de.mucify.activity.SingleAudioActivity;
import com.de.mucify.activity.SingleAudioPlayActivity;
import com.de.mucify.adapter.LoopListItemAdapter;
import com.de.mucify.adapter.SongListItemAdapter;
import com.de.mucify.playable.Song;
import com.de.mucify.util.MediaLibrary;

import java.util.ArrayList;

public class AudioSelectController {
    private final SingleAudioActivity mActivity;
    private ArrayList<Song> mListItems;

    public AudioSelectController(SingleAudioActivity activity) {
        mActivity = activity;

        MediaLibrary.loadAvailableSongs();
        MediaLibrary.loadAvailableLoops();

        mActivity.menuItemChangedListener = item -> {
            switch(item.getItemId()) {
                case R.id.songs:
                    loadSongs();
                    break;
                case R.id.loops:
                    loadLoops();
                    break;
            }

        };

        if(mActivity.isInSongTab()) {
            mListItems = MediaLibrary.AvailableSongs;
            loadSongs();
        }
        else {
            mListItems = MediaLibrary.AvailableLoops;
            loadLoops();
        }
    }

    private void loadSongs() {
        RecyclerView rv = mActivity.findViewById(R.id.rvFiles);
        rv.setLayoutManager(new LinearLayoutManager(mActivity));
        mListItems = MediaLibrary.AvailableSongs;
        rv.setAdapter(new SongListItemAdapter(mActivity, mListItems, this::onFileClicked));
        rv.getAdapter().notifyDataSetChanged();
    }

    private void loadLoops() {
        RecyclerView rv = mActivity.findViewById(R.id.rvFiles);
        rv.setLayoutManager(new LinearLayoutManager(mActivity));
        mListItems = MediaLibrary.AvailableLoops;
        rv.setAdapter(new LoopListItemAdapter(mActivity, mListItems, this::onFileClicked));
        rv.getAdapter().notifyDataSetChanged();
    }

    private void onFileClicked(RecyclerView.ViewHolder holder) {
        Intent i = new Intent(mActivity, SingleAudioPlayActivity.class);
        Song song = mListItems.get(holder.getAdapterPosition());
        if(song.getLoopPath() != null)
            i.putExtra("AudioFilePath", song.getLoopPath().getAbsolutePath());
        else
            i.putExtra("AudioFilePath", song.getSongPath().getAbsolutePath());
        i.putExtra("NavItemID", mActivity.isInSongTab() ? R.id.songs : R.id.loops);
        mActivity.startActivity(i);
        mActivity.finish();
    }
}
