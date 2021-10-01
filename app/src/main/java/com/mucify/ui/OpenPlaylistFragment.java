package com.mucify.ui;

import android.content.Context;
import android.os.Bundle;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.PopupWindow;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.mucify.Globals;
import com.mucify.R;
import com.mucify.Utils;
import com.mucify.objects.Playlist;
import com.mucify.objects.Song;

import java.io.File;
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

        updateLists();

        mView.findViewById(R.id.op_btnCreate).setOnClickListener(v -> {
            getParentFragmentManager().beginTransaction()
                    .replace(R.id.open_playlist_fragment, new CreatePlaylistFragment())
                    .addToBackStack(null)
                    .commit();
        });

        ListView lstPlaylists = mView.findViewById(R.id.op_lstPlaylists);
        lstPlaylists.setOnItemClickListener((parent, view1, position, id) -> {
            try {
                getParentFragmentManager().beginTransaction()
                        .replace(R.id.open_playlist_fragment, new PlayPlaylistFragment(new Playlist(getContext(), Globals.AvailablePlaylists.get(position))))
                        .addToBackStack(null)
                        .commit();
            } catch (IOException e) {
                Utils.messageBox(getContext(), "Failed to load playlist", e.getMessage());
            }
        });

        lstPlaylists.setOnItemLongClickListener((parent, view12, position, id) -> {
            openContextMenu(Globals.AvailablePlaylists.get(position));
            return true;
        });
    }

    private void openContextMenu(File playlist) {
        LayoutInflater inflater = (LayoutInflater)
                getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View popupView = inflater.inflate(R.layout.playlist_context_menu_layout, null);
        final PopupWindow popupWindow = new PopupWindow(popupView, LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT, true);
        popupWindow.showAtLocation(popupView, Gravity.CENTER, 0, 0);

        // dismiss the popup window when touched
        popupView.setOnTouchListener((v, event) -> {
            popupWindow.dismiss();
            return true;
        });

        popupView.findViewById(R.id.pcm_btnEdit).setOnClickListener(v -> {
            popupWindow.dismiss();

            try {
                Playlist pl = new Playlist(getContext(), playlist);
                getParentFragmentManager().beginTransaction()
                        .replace(R.id.open_playlist_fragment, new EditPlaylistFragment(pl.getName(), pl.getSongsAndLoops()))
                        .addToBackStack(null)
                        .commit();
            } catch (IOException e) {
                Utils.messageBox(getContext(), "Failed to load playlist", e.getMessage());
            }
        });

        popupView.findViewById(R.id.pcm_btnDelete).setOnClickListener(v -> {
            popupWindow.dismiss();

            playlist.delete();
            Globals.loadAvailablePlaylists();
            updateLists();
        });
    }

    public void updateLists() {
        ListView playlists = mView.findViewById(R.id.op_lstPlaylists);
        playlists.setAdapter(new ArrayAdapter<>(getContext(), android.R.layout.simple_list_item_1, Globals.AvailablePlaylistNames));
//        ViewGroup.LayoutParams params = playlists.getLayoutParams();
//        params.height = Utils.getItemHeightOfListView(playlists, playlists.getAdapter().getCount());
//        playlists.setLayoutParams(params);
    }
}
