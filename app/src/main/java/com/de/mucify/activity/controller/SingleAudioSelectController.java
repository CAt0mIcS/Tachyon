package com.de.mucify.activity.controller;

import android.app.AlertDialog;
import android.content.Intent;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
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
    private final ArrayList<Song> mListItems = new ArrayList<>();

    private final RecyclerView mRvFiles;

    public SingleAudioSelectController(SingleAudioActivity activity) {
        mActivity = activity;

        MediaLibrary.loadAvailableSongs();
        MediaLibrary.loadAvailableLoops();

        mRvFiles = mActivity.findViewById(R.id.rvFiles);

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

        if(mActivity.isInSongTab())
            loadSongs();
        else
            loadLoops();

        mActivity.findViewById(R.id.btnAddPlaylist).setVisibility(View.INVISIBLE);
        ((EditText)mActivity.findViewById(R.id.editSearchFiles)).addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                mListItems.clear();
                if(mActivity.isInSongTab())
                    mListItems.addAll(MediaLibrary.AvailableSongs);
                else
                    mListItems.addAll(MediaLibrary.AvailableLoops);

                if(s.toString().equals(""))
                    return;

                for(int i = 0; i < mListItems.size(); ++i) {
                    Song song = mListItems.get(i);
                    if(!song.getArtist().contains(s.toString()) && !song.getTitle().contains(s.toString())) {
                        if(song.isLoop() && song.getLoopName().contains(s.toString()))
                            continue;
                        mListItems.remove(song);
                        --i;
                    }
                }
                mRvFiles.getAdapter().notifyDataSetChanged();
            }
            @Override
            public void afterTextChanged(Editable s) {}
        });
    }

    private void loadSongs() {
        mRvFiles.setLayoutManager(new LinearLayoutManager(mActivity));
        mListItems.addAll(MediaLibrary.AvailableSongs);

        SongListItemAdapter adapter = new SongListItemAdapter(mActivity, mListItems);
        adapter.setOnItemClicked(this::onFileClicked);
        mRvFiles.setAdapter(adapter);

        mRvFiles.getAdapter().notifyDataSetChanged();
    }

    private void loadLoops() {
        mRvFiles.setLayoutManager(new LinearLayoutManager(mActivity));
        mListItems.addAll(MediaLibrary.AvailableLoops);

        LoopListItemAdapter adapter = new LoopListItemAdapter(mActivity, mListItems);
        adapter.setOnItemClicked(this::onFileClicked);
        adapter.setOnItemLongClicked(this::onLoopLongClicked);
        mRvFiles.setAdapter(adapter);

        mRvFiles.getAdapter().notifyDataSetChanged();
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
