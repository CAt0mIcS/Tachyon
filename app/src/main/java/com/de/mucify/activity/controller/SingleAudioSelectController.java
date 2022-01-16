package com.de.mucify.activity.controller;

import android.app.AlertDialog;
import android.content.Intent;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.widget.PopupMenu;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.de.mucify.R;
import com.de.mucify.activity.MultiAudioEditActivity;
import com.de.mucify.activity.SingleAudioActivity;
import com.de.mucify.activity.SingleAudioPlayActivity;
import com.de.mucify.adapter.LoopListItemAdapter;
import com.de.mucify.adapter.PlaylistListItemAdapter;
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
import java.util.Locale;

public class SingleAudioSelectController {
    private final SingleAudioActivity mActivity;
    private final ArrayList<Song> mListItems = new ArrayList<>();

    private final RecyclerView mRvFiles;
    private final EditText mSearchSongLoop;

    public SingleAudioSelectController(SingleAudioActivity activity) {
        mActivity = activity;

        MediaLibrary.loadAvailableSongs();
        MediaLibrary.loadAvailableLoops();

        mRvFiles = mActivity.findViewById(R.id.rvFiles);
        mSearchSongLoop = mActivity.findViewById(R.id.editSearchFiles);

        mActivity.menuItemChangedListener = item -> {
            switch(item.getItemId()) {
                case R.id.songs:
                    mSearchSongLoop.setText("");
                    loadSongs();
                    break;
                case R.id.loops:
                    mSearchSongLoop.setText("");
                    loadLoops();
                    break;
            }

        };

        if(mActivity.isInSongTab())
            loadSongs();
        else
            loadLoops();

        mActivity.findViewById(R.id.btnAddPlaylist).setVisibility(View.INVISIBLE);
        mSearchSongLoop.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override
            public void onTextChanged(CharSequence s1, int start, int before, int count) {
                mListItems.clear();
                if(mActivity.isInSongTab())
                    mListItems.addAll(MediaLibrary.AvailableSongs);
                else
                    mListItems.addAll(MediaLibrary.AvailableLoops);

                String s = s1.toString().toLowerCase(Locale.ROOT);
                if(s.equals("")) {
                    mRvFiles.getAdapter().notifyDataSetChanged();
                    return;
                }

                for(int i = 0; i < mListItems.size(); ++i) {
                    Song song = mListItems.get(i);
                    if(song.getArtist().toLowerCase(Locale.ROOT).contains(s) || song.getTitle().toLowerCase(Locale.ROOT).contains(s) || (song.isLoop() && song.getLoopName().toLowerCase(Locale.ROOT).contains(s)))
                        continue;
                    mListItems.remove(i);
                    --i;
                }

                mRvFiles.getAdapter().notifyDataSetChanged();
            }
            @Override
            public void afterTextChanged(Editable s) {}
        });
    }

    private void loadSongs() {
        mListItems.clear();
        mRvFiles.setLayoutManager(new LinearLayoutManager(mActivity));
        mListItems.addAll(MediaLibrary.AvailableSongs);

        SongListItemAdapter adapter = new SongListItemAdapter(mActivity, mListItems);
        adapter.setOnItemClicked(this::onFileClicked);
        mRvFiles.setAdapter(adapter);

        mRvFiles.getAdapter().notifyDataSetChanged();
    }

    private void loadLoops() {
        mListItems.clear();
        mRvFiles.setLayoutManager(new LinearLayoutManager(mActivity));
        mListItems.addAll(MediaLibrary.AvailableLoops);

        LoopListItemAdapter adapter = new LoopListItemAdapter(mActivity, mListItems);
//        adapter.setOnItemClicked(this::onFileClicked);
        adapter.setOnViewClickedListener(R.id.rvCoordinatorLayout, this::onFileClicked);
        adapter.setOnViewClickedListener(R.id.rvLinearLayout, this::onFileClicked);
        adapter.setOnViewClickedListener(R.id.btnFileOptions, this::onFileOptionsClicked);
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

    private void onFileOptionsClicked(LoopListItemAdapter.LoopViewHolder holder) {
        PopupMenu popup = new PopupMenu(mActivity, holder.BtnFileOptions);
        popup.inflate(R.menu.loop_playlist_options_menu);

        popup.setOnMenuItemClickListener(item -> {
            switch (item.getItemId()) {
                case R.id.edit:
                    Intent i = new Intent(mActivity, SingleAudioPlayActivity.class);
                    i.putExtra("EditLoop", true);
                    i.putExtra("AudioFilePath", MediaLibrary.AvailableLoops.get(holder.getAdapterPosition()).getLoopPath().getAbsolutePath());
                    mActivity.startActivity(i);
                    mActivity.finish();
                    return true;
                case R.id.delete:
                    File loopFile = FileManager.loopNameToFile(holder.getName(), holder.getTitle(), holder.getAuthor());
                    if(!loopFile.delete())
                        Toast.makeText(mActivity,
                                "Failed to delete loop: " + holder.getName(), Toast.LENGTH_LONG).show();

                    mListItems.clear();
                    mListItems.addAll(MediaLibrary.loadAvailableLoops());
                    mRvFiles.getAdapter().notifyDataSetChanged();
                    return true;
            }
            return false;
        });
        //displaying the popup
        popup.show();
    }
}
