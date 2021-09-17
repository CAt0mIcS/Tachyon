package com.example.mucify.ui.main;

import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.mucify.MainActivity;
import com.example.mucify.R;
import com.example.mucify.Util;
import com.example.mucify.program_objects.Playlist;
import com.example.mucify.program_objects.Song;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Optional;

public class PlaylistFragment extends Fragment {

    private MainActivity mActivity;
    private View mView;

    public static String PLAYLIST_IDENTIFIER = "PLAYLIST_";
    public static String PLAYLIST_EXTENSION = ".playlist";

    public Playlist CurrentPlaylist;

    private final ArrayList<File> mAvailablePlaylists = new ArrayList<>();

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.playlist_fragment_layout, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        mView = view;
    }

    public void create(MainActivity activity) {
        if(mActivity != null)
            return;

        mActivity = activity;

        ((Button)mView.findViewById(R.id.pf_btnCreate)).setOnClickListener(this::onCreateNewPlaylistClicked);
        ((Button)mView.findViewById(R.id.pf_btnEdit)).setOnClickListener(this::onEditPlaylistClicked);
        ((Button)mView.findViewById(R.id.pf_btnDelete)).setOnClickListener(this::onDeletePlaylistClicked);

        loadAvailablePlaylists(mActivity.DataDirectory);

        ArrayList<String> playlists = new ArrayList<>();
        for(File file : mAvailablePlaylists)
            playlists.add(file.getName());

        ((ListView)mView.findViewById(R.id.pf_lstboxPlaylists)).setAdapter(new ArrayAdapter<String>(mActivity, android.R.layout.simple_list_item_1, playlists) {
            @Override
            public View getView(int position, View convertView, ViewGroup parent) {
                View view =super.getView(position, convertView, parent);
                TextView textView = view.findViewById(android.R.id.text1);
                textView.setTextColor(Color.WHITE);
                return view;
            }
        });
    }

    public void onCreateNewPlaylistClicked(View view) {
        ArrayList<Song> songs = new ArrayList<>();
        songs.add(new Song(mActivity, "A Himitsu - Cosmic Storm", "/storage/emulated/0/Music/A Himitsu - Cosmic Storm.mp3"));
        songs.add(new Song(mActivity, "ABT X Topic X A7S - Your Love", "/storage/emulated/0/Music/ABT X Topic X A7S - Your Love.mp3"));
        songs.add(new Song(mActivity, "Cymatics - Nigel Stanford", "/storage/emulated/0/Music/Cymatics - Nigel Stanford.mp3"));
        songs.add(new Song(mActivity, "Last Time - Nerxa", "/storage/emulated/0/Music/Last time - Nerxa.mp3"));
        songs.add(new Song(mActivity, "Legends Never Die", "/storage/emulated/0/Music/Legends Never Die.mp3"));
        songs.add(new Song(mActivity, "Sun Mother", "/storage/emulated/0/Music/Sun Mother.wav"));
        songs.add(new Song(mActivity, "United Through Fire", "/storage/emulated/0/Music/United Through Fire.wav"));

        if(CurrentPlaylist != null)
            CurrentPlaylist.reset();
        CurrentPlaylist = new Playlist(mActivity, "TestPlaylist", songs);
        try {
            CurrentPlaylist.save();
        } catch(IOException e) {
            e.printStackTrace();
        }
    }

    public void onEditPlaylistClicked(View view) {
        Object item = ((ListView)view.findViewById(R.id.pf_lstboxPlaylists)).getSelectedItem();
        if(item == null)
            return;

        File file =  new File(PLAYLIST_IDENTIFIER + (String)item + PLAYLIST_EXTENSION);
    }

    public void onDeletePlaylistClicked(View view) {
        Object item = ((ListView)view.findViewById(R.id.pf_lstboxPlaylists)).getSelectedItem();
        if(item == null)
            return;

        File file =  new File(PLAYLIST_IDENTIFIER + (String)item + PLAYLIST_EXTENSION);
        if(file.exists()) {
            if(file.delete())
                mAvailablePlaylists.remove(file);
            else
                Util.messageBox(mActivity, "Error", "Failed to delete file '" + file.getAbsolutePath() + "'");
        }
    }


    private void loadAvailablePlaylists(File dir) {
        if (dir.exists()) {
            File[] files = dir.listFiles();
            if (files != null) {

                for (File file : files) {
                    if (file.isDirectory()) {
                        loadAvailablePlaylists(file);
                    } else {
                        Optional<String> extension = Util.getFileExtension(file.getName());
                        if(extension.isPresent() && extension.get().equals(PLAYLIST_EXTENSION) && file.getName().indexOf(PLAYLIST_IDENTIFIER) == 0)
                            mAvailablePlaylists.add(file);
                    }
                }
            }
        }
    }
}