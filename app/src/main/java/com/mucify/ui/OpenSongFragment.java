package com.mucify.ui;

import android.content.Context;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
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
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.regex.Pattern;

public class OpenSongFragment extends Fragment {
    private View mView;
    private final ArrayList<String> mSongsToDisplay = new ArrayList<>();
    private final ArrayList<String> mLoopsToDisplay = new ArrayList<>();

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

        mSongsToDisplay.addAll(Globals.AvailableSongNames);
        mLoopsToDisplay.addAll(Globals.AvailableLoopNames);

        // Only needed for create playlist fragment where we're using the same layout
        mView.findViewById(R.id.os_btnConfirm).setVisibility(View.INVISIBLE);

        EditText txtSearch = mView.findViewById(R.id.os_txtPlaylistName);
        txtSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) { }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                mSongsToDisplay.clear();
                mLoopsToDisplay.clear();
                mSongsToDisplay.addAll(Globals.AvailableSongNames);
                mLoopsToDisplay.addAll(Globals.AvailableLoopNames);

                for(int i = 0; i < mSongsToDisplay.size(); ++i) {

                    if(!mSongsToDisplay.get(i).toLowerCase(Locale.ROOT).contains(s.toString().toLowerCase(Locale.ROOT))) {
                        mSongsToDisplay.remove(i);
                        --i;
                    }
                }

                for(int i = 0; i < mLoopsToDisplay.size(); ++i) {
                    if(!mLoopsToDisplay.get(i).toLowerCase(Locale.ROOT).contains(s.toString().toLowerCase(Locale.ROOT))) {
                        mLoopsToDisplay.remove(i);
                        --i;
                    }
                }

                updateLists();
            }

            @Override
            public void afterTextChanged(Editable s) { }
        });

        ListView lstSongs = mView.findViewById(R.id.os_lstSongs);
        lstSongs.setAdapter(new ArrayAdapter<>(getContext(), android.R.layout.simple_list_item_1, mSongsToDisplay));
        lstSongs.setOnItemClickListener(this::onSongClicked);

        ListView lstLoops = mView.findViewById(R.id.os_lstLoops);
        lstLoops.setAdapter(new ArrayAdapter<>(getContext(), android.R.layout.simple_list_item_1, mLoopsToDisplay));
        lstLoops.setOnItemClickListener(this::onSongClicked);
    }

    private void onSongClicked(AdapterView<?> adapterView, View view, int position, long id) {
        PlaySongFragment newFragment;
        try {
            newFragment = new PlaySongFragment(
                    adapterView == ((ListView)mView.findViewById(R.id.os_lstSongs)) ?
                            new Song(getContext(), Globals.AvailableSongs.get(Globals.AvailableSongNames.indexOf(mSongsToDisplay.get(position)))) :
                            new Loop(getContext(), Globals.AvailableLoops.get(Globals.AvailableLoopNames.indexOf(mLoopsToDisplay.get(position)))));
        } catch(IOException e) {
            Utils.messageBox(getContext(), "Failed to read file", e.getMessage());
            return;
        }

        getParentFragmentManager().beginTransaction()
                .replace(R.id.open_song_fragment, newFragment)
                .addToBackStack(null)
                .commit();
    }

    public void updateLists() {
        ((ArrayAdapter<String>)((ListView)mView.findViewById(R.id.os_lstSongs)).getAdapter()).notifyDataSetChanged();
        ((ArrayAdapter<String>)((ListView)mView.findViewById(R.id.os_lstLoops)).getAdapter()).notifyDataSetChanged();
    }
}
