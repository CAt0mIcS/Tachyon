package com.mucify.ui.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.mucify.Globals;
import com.mucify.R;
import com.mucify.objects.Song;
import com.mucify.ui.internal.SongAdapter;

import java.io.IOException;

public class SongSelectFragment extends Fragment {
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        if(container != null)
            container.removeAllViews();
        return inflater.inflate(R.layout.view_files_layout, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        RecyclerView rv = view.findViewById(R.id.rvFiles);
        rv.setLayoutManager(new LinearLayoutManager(getContext()));
        rv.setAdapter(new SongAdapter(getContext(), Globals.AvailableSongs, holder -> {
            try {
                Song.create(getContext(), Globals.AvailableSongs.get(holder.getAdapterPosition()));
            } catch (IOException e) {
                Toast.makeText(getContext(), "Failed to start playing song", Toast.LENGTH_SHORT).show();
            }

            getParentFragmentManager().beginTransaction()
                    .replace(R.id.view_files_fragment, new PlaySongFragment())
                    .commit();
        }));
        rv.getAdapter().notifyDataSetChanged();
    }
}
