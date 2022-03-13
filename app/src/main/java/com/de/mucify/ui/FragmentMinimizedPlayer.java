package com.de.mucify.ui;

import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.de.mucify.R;

public class FragmentMinimizedPlayer extends Fragment {

    private String mTitle;
    private String mArtist;

    public FragmentMinimizedPlayer() {
        super();
    }

    public FragmentMinimizedPlayer(String title, String artist) {
        super(R.layout.fragment_minimized_player);
        mTitle = title;
        mArtist = artist;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        ((TextView)view.findViewById(R.id.txtTitle)).setText(mTitle);
        ((TextView)view.findViewById(R.id.txtArtist)).setText(mArtist);
    }
}
