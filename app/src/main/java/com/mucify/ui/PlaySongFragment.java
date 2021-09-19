package com.mucify.ui;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.mucify.R;
import com.mucify.objects.Loop;
import com.mucify.objects.Song;

public class PlaySongFragment extends Fragment {
    private View mView;
    private final Song mSong;


    public PlaySongFragment(Song song) {
        mSong = song;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        if(container != null)
            container.removeAllViews();
        return inflater.inflate(R.layout.play_song_layout, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        mView = view;

        ((TextView)mView.findViewById(R.id.ps_lblSongName)).setText(mSong.getName());
        mView.findViewById(R.id.ps_btnBack).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getParentFragmentManager().beginTransaction()
                        .replace(R.id.play_song_fragment, new OpenSongFragment())
                        .addToBackStack(null)
                        .commit();
            }
        });
    }
}
