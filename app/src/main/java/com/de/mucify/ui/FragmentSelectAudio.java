package com.de.mucify.ui;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.de.mucify.AudioType;
import com.de.mucify.MediaLibrary;
import com.de.mucify.R;
import com.de.mucify.Util;
import com.de.mucify.adapter.AdapterEventListener;
import com.de.mucify.adapter.PlayableListItemAdapter;
import com.de.mucify.adapter.ViewHolderLoop;
import com.de.mucify.adapter.ViewHolderPlaylist;
import com.de.mucify.adapter.ViewHolderSong;
import com.de.mucify.player.Playback;
import com.de.mucify.player.Playlist;
import com.de.mucify.player.Song;
import com.de.mucify.service.MediaPlaybackService;

import java.util.ArrayList;

/**
 * Fragment for displaying a list of all available songs, loops, or playlists.
 */
public class FragmentSelectAudio extends Fragment implements AdapterEventListener {

    ArrayList<Playback> mPlaybacks = new ArrayList<>();
    private final AudioType mAudioType;

    public FragmentSelectAudio(AudioType audioType) {
        super(R.layout.fragment_select_audio_layout);
        mAudioType = audioType;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Thread.setDefaultUncaughtExceptionHandler(Util.UncaughtExceptionLogger);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        RecyclerView rvFiles = view.findViewById(R.id.rvFiles);
        rvFiles.setLayoutManager(new LinearLayoutManager(getContext()));

        switch (mAudioType) {
            case Song:
                mPlaybacks.addAll(MediaLibrary.AvailableSongs);
                break;
            case Loop:
                mPlaybacks.addAll(MediaLibrary.AvailableLoops);
                break;
            case Playlist:
                mPlaybacks.addAll(MediaLibrary.AvailablePlaylists);
                break;
        }

        PlayableListItemAdapter adapter = new PlayableListItemAdapter(getContext(), mPlaybacks);
        adapter.setListener(this);
        rvFiles.setAdapter(adapter);
    }

    @Override
    public void onClick(ViewHolderSong holder) {
        Song s = (Song)mPlaybacks.get(holder.getAdapterPosition());
        startPlayingActivity(s.getMediaId());
    }

    @Override
    public void onClick(ViewHolderLoop holder) {
        Song s = (Song)mPlaybacks.get(holder.getAdapterPosition());
        startPlayingActivity(s.getMediaId());
    }

    @Override
    public void onClick(ViewHolderPlaylist holder) {
        Playlist s = (Playlist)mPlaybacks.get(holder.getAdapterPosition());
        startPlayingPlaylistActivity(s.getMediaId());
    }

    /**
     * Starts the ActivityPlayer with the specified MediaId
     */
    private void startPlayingActivity(String mediaId) {
        Intent i = new Intent(getActivity(), ActivityPlayer.class);
        i.putExtra("MediaId", mediaId);
        startActivity(i);
    }

    /**
     * Starts the ActivityPlaylistPlayer with the specified MediaId
     */
    private void startPlayingPlaylistActivity(String mediaId) {
        Intent i = new Intent(getActivity(), ActivityPlaylistPlayer.class);
        i.putExtra("MediaId", mediaId);
        startActivity(i);
    }
}
