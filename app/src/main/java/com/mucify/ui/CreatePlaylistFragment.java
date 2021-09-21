package com.mucify.ui;

import android.content.Context;
import android.os.Bundle;
import android.provider.Settings;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.CheckedTextView;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.PopupWindow;

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
        CheckedTextView v =(CheckedTextView)view;
        File path;
        if(parent.getId() == R.id.os_lstSongs)
            path = Globals.AvailableSongs.get(position);
        else
            path = Globals.AvailableLoops.get(position);

        try {
            if(v.isChecked())
                mSongsToAddToPlaylist.add(new Song(getContext(), path));
            else
                mSongsToAddToPlaylist.remove(new Song(getContext(), path));
        } catch(IOException e) {
            Utils.messageBox(getContext(), "Failed to load song/loop", e.getMessage());
        }
    }

    private void onPlaylistSaveClicked(View view) {
        LayoutInflater inflater = (LayoutInflater)
                getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View popupView = inflater.inflate(R.layout.save_dialog, null);
        final PopupWindow popupWindow = new PopupWindow(popupView, LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT, true);
        popupWindow.showAtLocation(popupView, Gravity.CENTER, 0, 0);

        // dismiss the popup window when touched
        popupView.setOnTouchListener((v, event) -> {
            popupWindow.dismiss();
            return true;
        });

        popupView.findViewById(R.id.sd_btnSave).setOnClickListener(v -> {
            String name = ((EditText)popupView.findViewById(R.id.sd_txtSave)).getText().toString();
            if(name.isEmpty() || name.contains("_"))
                return;

            popupWindow.dismiss();

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
        });
    }
}
