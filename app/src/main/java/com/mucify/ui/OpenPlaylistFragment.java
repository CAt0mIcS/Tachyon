package com.mucify.ui;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.mucify.Globals;
import com.mucify.R;
import com.mucify.Utils;
import com.mucify.objects.Playlist;
import com.mucify.objects.Song;

import java.io.IOException;
import java.util.ArrayList;

public class OpenPlaylistFragment extends Fragment {
    private View mView;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        if(container != null)
            container.removeAllViews();
        return inflater.inflate(R.layout.open_playlist_layout, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        mView = view;

        loadPlaylists();

        mView.findViewById(R.id.op_btnCreate).setOnClickListener(v -> {
            getParentFragmentManager().beginTransaction()
                    .replace(R.id.open_playlist_fragment, new CreatePlaylistFragment())
                    .addToBackStack(null)
                    .commit();
        });

        ((ListView)mView.findViewById(R.id.op_lstPlaylists)).setOnItemClickListener((parent, view1, position, id) -> {
            try {
                getParentFragmentManager().beginTransaction()
                        .replace(R.id.open_playlist_fragment, new PlayPlaylistFragment(new Playlist(getContext(), Globals.AvailablePlaylists.get(position))))
                        .addToBackStack(null)
                        .commit();
            } catch (IOException e) {
                Utils.messageBox(getContext(), "Failed to load playlist", e.getMessage());
            }
        });
    }

    private void loadPlaylists() {
        ListView playlists = mView.findViewById(R.id.op_lstPlaylists);
        playlists.setAdapter(new ArrayAdapter<>(getContext(), android.R.layout.simple_list_item_1, Globals.AvailablePlaylistNames));
        ViewGroup.LayoutParams params = playlists.getLayoutParams();
        params.height = Utils.getItemHeightOfListView(playlists, playlists.getAdapter().getCount());
        playlists.setLayoutParams(params);
    }
}
