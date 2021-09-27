package com.mucify.ui;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.CheckedTextView;
import android.widget.ListView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.mucify.Globals;
import com.mucify.R;
import com.mucify.Utils;
import com.mucify.objects.Loop;
import com.mucify.objects.Playlist;
import com.mucify.objects.Song;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

public class CreatePlaylistFragment extends Fragment {
    private View mView;
    private final ArrayList<Song> mSongsToAddToPlaylist = new ArrayList<>();

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        if(container != null)
            container.removeAllViews();
        return inflater.inflate(R.layout.open_song_layout, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        mView = view;

        ListView lstSongs = mView.findViewById(R.id.os_lstSongs);
        ListView lstLoops = mView.findViewById(R.id.os_lstLoops);

        lstSongs.setAdapter(new ArrayAdapter<>(getContext(), android.R.layout.simple_list_item_multiple_choice, Globals.AvailableSongNames));
        lstLoops.setAdapter(new ArrayAdapter<>(getContext(), android.R.layout.simple_list_item_multiple_choice, Globals.AvailableLoopNames));
        lstSongs.setOnItemClickListener(this::addSongOrLoop);
        lstLoops.setOnItemClickListener(this::addSongOrLoop);

        mView.findViewById(R.id.os_btnConfirm).setOnClickListener(this::onPlaylistSaveClicked);
    }

    private void addSongOrLoop(AdapterView<?> parent, View view, int position, long id) {
        CheckedTextView v = (CheckedTextView)view;
        File path;
        boolean isLoop = false;
        if(parent.getId() == R.id.os_lstSongs)
            path = Globals.AvailableSongs.get(position);
        else {
            path = Globals.AvailableLoops.get(position);
            isLoop = true;
        }

        try {
            if(v.isChecked()) {
                if(isLoop)
                    mSongsToAddToPlaylist.add(new Loop(getContext(), path));
                else
                    mSongsToAddToPlaylist.add(new Song(getContext(), path));
            }
            else {
                if(isLoop)
                    mSongsToAddToPlaylist.remove(new Loop(getContext(), path));
                else
                    mSongsToAddToPlaylist.remove(new Song(getContext(), path));
            }
        } catch(IOException e) {
            Utils.messageBox(getContext(), "Failed to load song/loop", e.getMessage());
        }
    }

    private void onPlaylistSaveClicked(View view) {
        String name = ((TextView)mView.findViewById(R.id.os_txtPlaylistName)).getText().toString();
        if(name.contains("_") || name.isEmpty()) {
            Utils.messageBox(getContext(), "Error", "Invalid playlist name");
            return;
        }

        // Write playlist and add to global playlist index
        try {
            new Playlist(name, mSongsToAddToPlaylist).save();
            Globals.loadAvailablePlaylists();
        } catch (IOException e) {
            Utils.messageBox(getContext(), "Failed to save playlist", e.getMessage());
        }

        getParentFragmentManager().beginTransaction()
                .replace(R.id.open_song_fragment, new OpenPlaylistFragment())
                .addToBackStack(null)
                .commit();
    };
}
