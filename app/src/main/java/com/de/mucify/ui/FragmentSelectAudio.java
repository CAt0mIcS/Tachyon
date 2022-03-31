package com.de.mucify.ui;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.de.mucify.AudioType;
import com.de.mucify.MediaLibrary;
import com.de.mucify.R;
import com.de.mucify.Util;
import com.de.mucify.ui.adapter.AdapterEventListener;
import com.de.mucify.ui.adapter.PlaybackListItemAdapter;
import com.de.mucify.ui.adapter.ViewHolderLoop;
import com.de.mucify.ui.adapter.ViewHolderPlaylist;
import com.de.mucify.ui.adapter.ViewHolderSong;
import com.de.mucify.player.Playback;
import com.de.mucify.player.Playlist;
import com.de.mucify.player.Song;

import java.util.ArrayList;
import java.util.Objects;

/**
 * Fragment for displaying a list of all available songs, loops, or playlists.
 */
public class FragmentSelectAudio extends Fragment {

    ArrayList<Playback> mPlaybacks = new ArrayList<>();
    private AudioType mAudioType;
    private RecyclerView mRvFiles;

    public FragmentSelectAudio() {
    }

    public FragmentSelectAudio(AudioType audioType) {
        super(R.layout.fragment_select_audio);
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

        mRvFiles = view.findViewById(R.id.rvFiles);
        mRvFiles.setLayoutManager(new LinearLayoutManager(getContext()));

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
            default:
                throw new UnsupportedOperationException("Unknown audio type " + mAudioType);
        }

        PlaybackListItemAdapter adapter = new PlaybackListItemAdapter(getContext(), mPlaybacks);
        adapter.setListener(new AdapterEventListener());
        mRvFiles.setAdapter(adapter);
    }


    private class AdapterEventListener extends com.de.mucify.ui.adapter.AdapterEventListener {
        @Override
        public void onClick(RecyclerView.ViewHolder holder, int viewType) {
            switch (viewType) {
                case PlaybackListItemAdapter.ITEM_TYPE_SONG:
                case PlaybackListItemAdapter.ITEM_TYPE_LOOP:
                    startPlayingActivity(mPlaybacks.get(holder.getAdapterPosition()).getMediaId());
                    break;
                case PlaybackListItemAdapter.ITEM_TYPE_PLAYLIST:
                    Playlist s = (Playlist) mPlaybacks.get(holder.getAdapterPosition());
                    if (s.getSongs().size() > 0)
                        startPlayingPlaylistActivity(s.getMediaId());
                    else
                        // MY_TODO: Better error message
                        Toast.makeText(getActivity(), "Playlist doesn't contain any songs", Toast.LENGTH_LONG).show();
            }
        }

        @Override
        public void onLongClick(RecyclerView.ViewHolder holder, int viewType) {
            if (viewType == PlaybackListItemAdapter.ITEM_TYPE_LOOP)
                ((Song) mPlaybacks.get(holder.getAdapterPosition())).deleteLoop();
            else if (viewType == PlaybackListItemAdapter.ITEM_TYPE_PLAYLIST)
                ((Playlist) mPlaybacks.get(holder.getAdapterPosition())).delete();
        }
    }


    /**
     * Starts the ActivityPlayer with the specified MediaId
     */
    private void startPlayingActivity(String mediaId) {
        Thread.setDefaultUncaughtExceptionHandler(Util.UncaughtExceptionLogger);
        ((MediaControllerActivity) requireActivity()).setMediaId(mediaId);

        Intent i = new Intent(getActivity(), ActivityPlayer.class);
        // Automatically start playing audio
        i.putExtra("StartPlaying", true);
        startActivity(i);
    }

    /**
     * Starts the ActivityPlaylistPlayer with the specified MediaId
     */
    private void startPlayingPlaylistActivity(String mediaId) {
        Thread.setDefaultUncaughtExceptionHandler(Util.UncaughtExceptionLogger);
        ((MediaControllerActivity) requireActivity()).setMediaId(mediaId);

        Intent i = new Intent(getActivity(), ActivityPlaylistPlayer.class);
        // Automatically start playing audio
        i.putExtra("StartPlaying", true);
        startActivity(i);
    }
}
