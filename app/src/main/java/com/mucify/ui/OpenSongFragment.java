package com.mucify.ui;

import android.content.Context;
import android.os.Bundle;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
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
import com.mucify.objects.Song;

import java.io.File;
import java.io.IOException;
import java.util.List;

public class OpenSongFragment extends Fragment {
    private View mView;

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
        // Only needed for create playlist fragment where we're using the same layout
        mView.findViewById(R.id.os_btnConfirm).setVisibility(View.INVISIBLE);

        ListView lstSongs = mView.findViewById(R.id.os_lstSongs);
        lstSongs.setAdapter(new ArrayAdapter<>(getContext(), android.R.layout.simple_list_item_1, Globals.AvailableSongNames));
        lstSongs.setOnItemClickListener(this::onSongClicked);

        ListView lstLoops = mView.findViewById(R.id.os_lstLoops);
        lstLoops.setAdapter(new ArrayAdapter<>(getContext(), android.R.layout.simple_list_item_1, Globals.AvailableLoopNames));
        lstLoops.setOnItemClickListener(this::onSongClicked);
    }

    private void onSongClicked(AdapterView<?> adapterView, View view, int position, long id) {
        PlaySongFragment newFragment;
        try {
            newFragment = new PlaySongFragment(new Song(getContext(),
                    adapterView == ((ListView)mView.findViewById(R.id.os_lstSongs)) ? Globals.AvailableSongs.get(position) : Globals.AvailableLoops.get(position)));
        } catch(IOException e) {
            Utils.messageBox(getContext(), "Failed to read file", e.getMessage());
            return;
        }

        getParentFragmentManager().beginTransaction()
                .replace(R.id.open_song_fragment, newFragment)
                .addToBackStack(null)
                .commit();
    }
}
