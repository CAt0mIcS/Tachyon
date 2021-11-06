package com.de.mucify.activity.dispatcher;

import android.content.Intent;
import android.view.View;
import android.widget.ListView;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.de.mucify.R;
import com.de.mucify.activity.SingleAudioActivity;
import com.de.mucify.adapter.SongListItemAdapter;
import com.de.mucify.playable.Song;
import com.de.mucify.service.SongPlayForegroundService;
import com.de.mucify.util.MediaLibrary;

import java.util.ArrayList;

public class AudioSelectDispatcher {
    private final SingleAudioActivity mActivity;
    private final ArrayList<Song> mListItems;

    public AudioSelectDispatcher(SingleAudioActivity activity) {
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
        Intent songPlayForegroundIntent = new Intent(mActivity, SongPlayForegroundService.class);
        songPlayForegroundIntent.putExtra("SongFilePath", mListItems.get(holder.getAdapterPosition()).getSongPath().getAbsolutePath());
        mActivity.stopService(songPlayForegroundIntent);
        mActivity.startService(songPlayForegroundIntent);
    }
}
