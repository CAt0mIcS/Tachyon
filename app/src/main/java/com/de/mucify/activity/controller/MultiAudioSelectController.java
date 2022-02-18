package com.de.mucify.activity.controller;

import android.app.AlertDialog;
import android.content.Intent;
import android.text.Editable;
import android.text.TextWatcher;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.widget.PopupMenu;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.de.mucify.R;
import com.de.mucify.activity.MultiAudioActivity;
import com.de.mucify.activity.MultiAudioEditActivity;
import com.de.mucify.activity.MultiAudioPlayActivity;
import com.de.mucify.adapter.PlaylistListItemAdapter;
import com.de.mucify.playable.Playlist;
import com.de.mucify.playable.Song;
import com.de.mucify.util.FileManager;
import com.de.mucify.util.InterstitialAdvertiser;
import com.de.mucify.util.MediaLibrary;
import com.google.android.gms.ads.FullScreenContentCallback;
import com.google.android.gms.ads.interstitial.InterstitialAd;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Locale;

public class MultiAudioSelectController extends InterstitialAdvertiser {
    private final MultiAudioActivity mActivity;
    private final ArrayList<Playlist> mListItems = new ArrayList<>();

    private final RecyclerView mRvFiles;

    public MultiAudioSelectController(MultiAudioActivity activity) {
        super(activity);
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
        mActivity.findViewById(R.id.btnAddPlaylist).setOnClickListener(v -> onNewPlaylistClicked());
    }

    private void loadPlaylists() {
        mListItems.clear();
        mRvFiles.setLayoutManager(new LinearLayoutManager(mActivity));
        mListItems.addAll(MediaLibrary.AvailablePlaylists);

        PlaylistListItemAdapter adapter = new PlaylistListItemAdapter(mActivity, mListItems);
        adapter.setOnViewClickedListener(R.id.rvCoordinatorLayout, this::onFileClicked);
        adapter.setOnViewLongClickedListener(R.id.rvCoordinatorLayout, this::onFileOptionsClicked);
        adapter.setOnViewClickedListener(R.id.rvLinearLayout, this::onFileClicked);
        adapter.setOnViewLongClickedListener(R.id.rvLinearLayout, this::onFileOptionsClicked);
        mRvFiles.setAdapter(adapter);

        mRvFiles.getAdapter().notifyDataSetChanged();
    }

    private void switchActivity(RecyclerView.ViewHolder holder) {
        Intent i = new Intent(mActivity, MultiAudioPlayActivity.class);
        i.putExtra("AudioFilePath", getPlaylistFromViewHolder(holder).getPlaylistFilePath().getAbsolutePath());
        mActivity.startActivity(i);
        mActivity.finish();
    }

    private void onFileClicked(RecyclerView.ViewHolder holder) {
        if(mInterstitialAd != null && allowedToAdvertise()) {
            showAd(new FullScreenContentCallback() {
                @Override
                public void onAdDismissedFullScreenContent() {
                    loadAd();
                    switchActivity(holder);
                }
            });
        }
        else
            switchActivity(holder);
    }

    private void onFileOptionsClicked(PlaylistListItemAdapter.PlaylistViewHolder holder) {
        PopupMenu popup = new PopupMenu(mActivity, holder.CoordinatorLayout);
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
                    mListItems.clear();
                    mListItems.addAll(MediaLibrary.loadAvailablePlaylists());
                    mRvFiles.getAdapter().notifyDataSetChanged();
                    return true;
            }
            return false;
        });
        //displaying the popup
        popup.show();
    }

    private Playlist getPlaylistFromViewHolder(RecyclerView.ViewHolder holder) { return mListItems.get(holder.getAdapterPosition()); }

    private void onNewPlaylistClicked() {
        new AlertDialog.Builder(mActivity)
                .setMessage(R.string.dialog_new_playlist)
                .setView(mActivity.getLayoutInflater().inflate(R.layout.save_loop_new_playlist_alert_dialog_layout, null))
                .setPositiveButton(R.string.save, (dialog, id) -> {

                    String playlistName = ((EditText)((AlertDialog)dialog).findViewById(R.id.dialog_txtName)).getText().toString();
                    if(playlistName.isEmpty() || playlistName.contains("_")) {
                        Toast.makeText(mActivity, "Failed to save playlist: Name mustn't contain '_' or be empty", Toast.LENGTH_LONG).show();
                        return;
                    }

                    try {
                        new Playlist(FileManager.playlistNameToFile(playlistName), new ArrayList<>()).save();
                        MediaLibrary.loadAvailablePlaylists();

                        for(int i = 0; i < MediaLibrary.AvailablePlaylists.size(); ++i) {
                            if(MediaLibrary.AvailablePlaylists.get(i).getName().equals(playlistName)) {
                                Intent intent = new Intent(mActivity, MultiAudioEditActivity.class);
                                intent.putExtra("AudioID", i);
                                mActivity.startActivity(intent);
                                mActivity.finish();
                                break;
                            }
                        }

                    } catch (IOException e) {
                        Toast.makeText(mActivity, "Failed to save loop: " + e.getMessage(), Toast.LENGTH_LONG).show();
                        return;
                    }
                })
                .setNegativeButton(R.string.cancel, (dialog, id) -> dialog.dismiss())
                .create().show();
    }
}
