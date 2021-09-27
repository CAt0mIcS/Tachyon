package com.mucify.ui;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.CheckedTextView;
import android.widget.EditText;
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

import org.w3c.dom.Text;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

public class EditPlaylistFragment extends Fragment {
    private View mView;
    private final String mPlaylistName;
    private final ArrayList<Song> mPlaylistSongs;

    public EditPlaylistFragment(String playlistName, ArrayList<Song> songsInPlaylist) {
        mPlaylistSongs = songsInPlaylist;
        mPlaylistName = playlistName;
    }

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

        ((EditText)mView.findViewById(R.id.os_txtPlaylistName)).setText(mPlaylistName);

        ListView lstSongs = mView.findViewById(R.id.os_lstSongs);
        ListView lstLoops = mView.findViewById(R.id.os_lstLoops);

        lstSongs.setAdapter(new ArrayAdapter<>(getContext(), android.R.layout.simple_list_item_multiple_choice, Globals.AvailableSongNames));
        lstLoops.setAdapter(new ArrayAdapter<>(getContext(), android.R.layout.simple_list_item_multiple_choice, Globals.AvailableLoopNames));
        lstSongs.setOnItemClickListener(this::addSongOrLoop);
        lstLoops.setOnItemClickListener(this::addSongOrLoop);

        for(Song s : mPlaylistSongs) {
            if(s instanceof Loop)
                lstLoops.setItemChecked(Globals.AvailableLoops.indexOf(s.getLoopPath()), true);
            else
                lstSongs.setItemChecked(Globals.AvailableSongs.indexOf(s.getPath()), true);
        }

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
                    mPlaylistSongs.add(new Loop(getContext(), path));
                else
                    mPlaylistSongs.add(new Song(getContext(), path));
            }
            else {
                if(isLoop)
                    mPlaylistSongs.remove(new Loop(getContext(), path));
                else
                    mPlaylistSongs.remove(new Song(getContext(), path));
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
            Playlist.toFile(mPlaylistName).delete();
            new Playlist(name, mPlaylistSongs).save();
            Globals.loadAvailablePlaylists();
        } catch (IOException e) {
            Utils.messageBox(getContext(), "Failed to save playlist", e.getMessage());
        }

        getParentFragmentManager().beginTransaction()
                .replace(R.id.open_song_fragment, new OpenPlaylistFragment())
                .addToBackStack(null)
                .commit();
    }
}
