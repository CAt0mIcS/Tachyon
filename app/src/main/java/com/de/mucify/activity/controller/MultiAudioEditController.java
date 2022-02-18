package com.de.mucify.activity.controller;

import android.content.Intent;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.Toast;

import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.de.mucify.R;
import com.de.mucify.activity.MultiAudioActivity;
import com.de.mucify.activity.MultiAudioEditActivity;
import com.de.mucify.activity.PlaylistCreateActivity;
import com.de.mucify.adapter.LoopListItemAdapter;
import com.de.mucify.adapter.SongListItemAdapter;
import com.de.mucify.adapter.SongLoopListItemAdapter;
import com.de.mucify.playable.Playlist;
import com.de.mucify.playable.Song;
import com.de.mucify.util.MediaLibrary;
import com.de.mucify.util.Utils;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Locale;
import java.util.TreeSet;

public class MultiAudioEditController {
    private final MultiAudioEditActivity mActivity;
    private final Playlist mPlaylist;
    private final ArrayList<Song> mListItems = new ArrayList<>();
    private final TreeSet<File> mNewSongs = new TreeSet<>();

    private final RecyclerView mRvFiles;
    private final EditText mSearchSongLoop;

    public MultiAudioEditController(MultiAudioEditActivity activity, Playlist playlist) {
        mActivity = activity;
        mPlaylist = playlist;

        mRvFiles = mActivity.findViewById(R.id.rvFiles);
        mSearchSongLoop = mActivity.findViewById(R.id.editSearchFiles);

        loadItems();
        mSearchSongLoop.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override
            public void onTextChanged(CharSequence s1, int start, int before, int count) {
                mListItems.clear();
                mListItems.addAll(MediaLibrary.AvailableSongs);
                mListItems.addAll(MediaLibrary.AvailableLoops);

                String s = s1.toString().toLowerCase(Locale.ROOT);
                if(s.equals("")) {
                    mRvFiles.getAdapter().notifyDataSetChanged();
                    ((SongLoopListItemAdapter)mRvFiles.getAdapter()).updateCheckedMap(mPlaylist.getSongs());
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
                ((SongLoopListItemAdapter)mRvFiles.getAdapter()).updateCheckedMap(mPlaylist.getSongs());
            }
            @Override
            public void afterTextChanged(Editable s) {}
        });

        mActivity.findViewById(R.id.btnSavePlaylist).setOnClickListener(v -> {
            try {
                ArrayList<Song> songs = new ArrayList<>();
                for(File f : mNewSongs) {
                    try {
                        songs.add(new Song(f));
                    } catch (Song.LoadingFailedException e) {
                        Utils.startErrorActivity("Failed to load song: " + f + "\n" + Utils.getDetailedError(e));
                    }
                }

                new Playlist(mPlaylist.getPlaylistFilePath(), songs).save();
                Intent i = new Intent(mActivity, MultiAudioActivity.class);
                mActivity.startActivity(i);
                mActivity.finish();
            } catch (IOException e) {
                Utils.startErrorActivity("Failed to save playlist: " + mPlaylist.getPlaylistFilePath() + "\n" + Utils.getDetailedError(e));
            }
        });
    }

    public void loadItems() {
        mListItems.clear();
        mRvFiles.setLayoutManager(new LinearLayoutManager(mActivity));
        mListItems.addAll(MediaLibrary.AvailableSongs);
        mListItems.addAll(MediaLibrary.AvailableLoops);
        for(Song s : mPlaylist.getSongs()) {
            if(s.isLoop())
                mNewSongs.add(s.getLoopPath());
            else
                mNewSongs.add(s.getSongPath());
        }


        SongLoopListItemAdapter adapter = new SongLoopListItemAdapter(mActivity, mListItems, mPlaylist.getSongs());
        adapter.setOnCheckedChangedListener(this::onCheckedChanged);
        mRvFiles.setAdapter(adapter);

        mRvFiles.getAdapter().notifyDataSetChanged();

//        new ItemTouchHelper(new ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT | ItemTouchHelper.RIGHT | ItemTouchHelper.DOWN | ItemTouchHelper.UP) {
//
//            @Override
//            public boolean onMove(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder, RecyclerView.ViewHolder target) {
//                Toast.makeText(mActivity, "on Move", Toast.LENGTH_SHORT).show();
//                return false;
//            }
//
//            @Override
//            public void onSwiped(RecyclerView.ViewHolder viewHolder, int swipeDir) {
//                Toast.makeText(mActivity, "on Swiped ", Toast.LENGTH_SHORT).show();
//                // Remove swiped item from list and notify the RecyclerView
//                int position = viewHolder.getAdapterPosition();
//
//                switch(swipeDir) {
//                    case ItemTouchHelper.LEFT:
//                    case ItemTouchHelper.RIGHT:
//                        mListItems.remove(position);
//                        break;
//                    case ItemTouchHelper.DOWN:
//                        if(position != mListItems.size() - 1) {
//                            Song item = mListItems.remove(position);
//                            mListItems.add(position + 1, item);
//                        }
//                        break;
//                    case ItemTouchHelper.UP:
//                        if(position != 0) {
//                            Song item = mListItems.remove(position);
//                            mListItems.add(position - 1, item);
//                        }
//                        break;
//                }
//
//
//                adapter.notifyItemRemoved(position);
//
//            }
//        }
//        ).attachToRecyclerView(mRvFiles);
    }

    private void onCheckedChanged(RecyclerView.ViewHolder baseHolder, boolean isChecked) {
        Song song = mListItems.get(baseHolder.getAdapterPosition());
        if(isChecked) {
            mNewSongs.add(song.isLoop() ? song.getLoopPath() : song.getSongPath());
        }
        else
            mNewSongs.remove(song.isLoop() ? song.getLoopPath() : song.getSongPath());
    }
}
