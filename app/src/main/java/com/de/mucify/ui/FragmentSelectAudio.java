package com.de.mucify.ui;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.de.mucify.MediaLibrary;
import com.de.mucify.R;
import com.de.mucify.adapter.AdapterEventListener;
import com.de.mucify.adapter.PlayableListItemAdapter;
import com.de.mucify.adapter.ViewHolderLoop;
import com.de.mucify.adapter.ViewHolderPlaylist;
import com.de.mucify.adapter.ViewHolderSong;
import com.de.mucify.player.Playback;
import com.de.mucify.player.Playlist;
import com.de.mucify.player.Song;

import java.io.File;
import java.util.ArrayList;

public class FragmentSelectAudio extends Fragment implements AdapterEventListener {

    ArrayList<Playback> mPlaybacks = new ArrayList<>();

    public FragmentSelectAudio() {
        super(R.layout.fragment_select_audio_layout);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        RecyclerView rvFiles = view.findViewById(R.id.rvFiles);
        rvFiles.setLayoutManager(new LinearLayoutManager(getContext()));

        for(Song s : MediaLibrary.AvailableSongs)
            mPlaybacks.add(s);

        PlayableListItemAdapter adapter = new PlayableListItemAdapter(getContext(), mPlaybacks);
        adapter.setListener(this);
        rvFiles.setAdapter(adapter);
        rvFiles.getAdapter().notifyDataSetChanged();
    }

    @Override
    public void onClick(ViewHolderSong holder) {
        Song s = (Song)mPlaybacks.get(holder.getAdapterPosition());
        startPlayingActivity(s.getSongPath());
    }

    @Override
    public void onClick(ViewHolderLoop holder) {
        Song s = (Song)mPlaybacks.get(holder.getAdapterPosition());
        startPlayingActivity(s.getLoopPath());
    }

    @Override
    public void onClick(ViewHolderPlaylist holder) {
        Playlist s = (Playlist)mPlaybacks.get(holder.getAdapterPosition());
        startPlayingPlaylistActivity(s.getPlaylistFilePath());
    }

    private void startPlayingActivity(File filepath) {
        Intent i = new Intent(getActivity(), ActivityPlayer.class);
        i.putExtra("AudioPath", filepath.toString());
        startActivity(i);
        getActivity().finish();
    }

    private void startPlayingPlaylistActivity(File filepath) {
        Intent i = new Intent(getActivity(), ActivityPlaylistPlayer.class);
        i.putExtra("AudioPath", filepath.toString());
        startActivity(i);
        getActivity().finish();
    }
}
