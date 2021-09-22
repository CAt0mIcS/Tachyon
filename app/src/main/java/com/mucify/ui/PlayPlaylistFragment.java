package com.mucify.ui;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.GridView;
import android.widget.ListView;
import android.widget.SeekBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.mucify.R;
import com.mucify.Utils;
import com.mucify.objects.Playlist;
import com.mucify.objects.Song;

public class PlayPlaylistFragment extends Fragment {
    private View mView;
    private Playlist mPlaylist;
    private boolean mProgressSeekbarUpdate = true;

    public PlayPlaylistFragment(Playlist playlist) {
        mPlaylist = playlist;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        if(container != null)
            container.removeAllViews();
        return inflater.inflate(R.layout.play_playlist_layout, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        mView = view;

        mPlaylist.setOnNewSongListener(song -> {
            ((SeekBar)mView.findViewById(R.id.pp_sbProgress)).setMax(song.getDuration());
            ((TextView)mView.findViewById(R.id.pp_lblSongName)).setText(song.getName());
        });

        mPlaylist.start();

        // Update progress seekbar with media player position as well as update the song
        mView.findViewById(R.id.pp_sbProgress).post(new Runnable() {
            @Override
            public void run() {
                mPlaylist.update();

                SeekBar progress = mView.findViewById(R.id.pp_sbProgress);
                if(progress == null)
                    return;

                if(mPlaylist.getCurrentSong().isPlaying()) {
                    if(mProgressSeekbarUpdate) {
                        progress.setProgress(mPlaylist.getCurrentSong().getCurrentPosition());
                    }
                    progress.post(this);
                }
                else
                    progress.postDelayed(this, 100);
            }
        });

        ((SeekBar)mView.findViewById(R.id.pp_sbProgress)).setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                TextView lblProgress = mView.findViewById(R.id.pp_lblProgress);
                lblProgress.setText(Utils.millisecondsToReadableString(progress));
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) { mProgressSeekbarUpdate = false; }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                mProgressSeekbarUpdate = true;
                mPlaylist.getCurrentSong().seekTo(seekBar.getProgress());
            }
        });

        ListView lstSongs = mView.findViewById(R.id.pp_lstSongs);
        lstSongs.setAdapter(new ArrayAdapter<>(getContext(), android.R.layout.simple_list_item_1, mPlaylist.getSongs()));
        lstSongs.setOnItemClickListener((parent, view1, position, id) -> mPlaylist.play(position));

        ListView lstLoops = mView.findViewById(R.id.pp_lstLoops);
        lstLoops.setAdapter(new ArrayAdapter<>(getContext(), android.R.layout.simple_list_item_1, mPlaylist.getLoops()));
        lstLoops.setOnItemClickListener((parent, view1, position, id) -> mPlaylist.play(mPlaylist.getSongs().size() + position));
    }

    public void unload() {
        for(Song song : mPlaylist.getSongsAndLoops()) {
            if(song.isPlaying()) {
                song.pause();
                song.seekTo(0);
            }
        }
    }
}
