package com.de.mucify.activity.controller;

import android.app.AlertDialog;
import android.content.Intent;
import android.widget.EditText;
import android.widget.Toast;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.de.mucify.R;
import com.de.mucify.activity.SingleAudioActivity;
import com.de.mucify.activity.SingleAudioPlayActivity;
import com.de.mucify.adapter.LoopListItemAdapter;
import com.de.mucify.adapter.SongListItemAdapter;
import com.de.mucify.playable.AudioController;
import com.de.mucify.playable.Song;
import com.de.mucify.util.FileManager;
import com.de.mucify.util.MediaLibrary;
import com.de.mucify.util.Utils;
import com.google.android.material.snackbar.Snackbar;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

public class SingleAudioSelectController {
    private final SingleAudioActivity mActivity;
    private ArrayList<Song> mListItems;

    public SingleAudioSelectController(SingleAudioActivity activity) {
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

        SongListItemAdapter adapter = new SongListItemAdapter(mActivity, mListItems);
        adapter.setOnItemClicked(this::onFileClicked);
        rv.setAdapter(adapter);

        rv.getAdapter().notifyDataSetChanged();
    }

    private void loadLoops() {
        RecyclerView rv = mActivity.findViewById(R.id.rvFiles);
        rv.setLayoutManager(new LinearLayoutManager(mActivity));
        mListItems = MediaLibrary.AvailableLoops;

        LoopListItemAdapter adapter = new LoopListItemAdapter(mActivity, mListItems);
        adapter.setOnItemClicked(this::onFileClicked);
        adapter.setOnItemLongClicked(this::onLoopLongClicked);
        rv.setAdapter(adapter);

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


    private void onLoopLongClicked(RecyclerView.ViewHolder v) {

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
