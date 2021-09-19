package com.example.mucify.ui.main;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.mucify.GlobalConfig;
import com.example.mucify.R;
import com.example.mucify.program_objects.Playlist;

import java.io.File;
import java.io.IOException;
import java.util.Objects;

public class EditPlaylistFragment extends Fragment {
    private View mView;
    private Playlist mEditingPlaylist;

    public EditPlaylistFragment(Playlist playlist) {
        mEditingPlaylist = playlist;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.playlist_edit_fragment, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        mView = view;

        // Save playlist and return
        mView.findViewById(R.id.pef_btnOk).setOnClickListener((v) -> {
            try {
                mEditingPlaylist.save();
            } catch (IOException e) {
                e.printStackTrace();
            }

            mView.findViewById(R.id.pef_btnCancel).performClick();
        });

        // Return without saving
        mView.findViewById(R.id.pef_btnCancel).setOnClickListener((v) -> {
            getParentFragmentManager().beginTransaction()
                    .replace(R.id.playlist_edit_fragment, new PlaylistFragment())
                    .addToBackStack(null)
                    .commit();
        });
    }
}
