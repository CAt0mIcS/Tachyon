package com.example.mucify.ui.main;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.mucify.R;

import java.util.Objects;

public class EditPlaylistFragment extends Fragment {
    private View mView;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.playlist_edit_fragment, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        mView = view;

        mView.findViewById(R.id.pef_btnOk).setOnClickListener((v) -> {
            getParentFragmentManager().beginTransaction()
                    .replace(R.id.playlist_edit_fragment, new PlaylistFragment())
                    .addToBackStack(null)
                    .commit();
        });
    }
}
