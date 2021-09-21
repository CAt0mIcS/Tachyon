package com.mucify.ui;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.mucify.R;
import com.mucify.objects.Playlist;
import com.mucify.objects.Song;

public class PlayPlaylistFragment extends Fragment {
    private View mView;
    private Playlist mPlaylist;

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
    }

    public void unload() {
        for(Song song : mPlaylist.getSongs()) {
            song.pause();
            song.seekTo(0);
        }
    }
}
