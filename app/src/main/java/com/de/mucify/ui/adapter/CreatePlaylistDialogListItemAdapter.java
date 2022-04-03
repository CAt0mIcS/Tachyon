package com.de.mucify.ui.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.de.mucify.R;
import com.de.common.player.Playlist;

import java.util.ArrayList;

public class CreatePlaylistDialogListItemAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private final Context mContext;
    private final ArrayList<Playlist> mItems;

    private AdapterEventListener mCallback;


    public CreatePlaylistDialogListItemAdapter(Context context, ArrayList<Playlist> items) {
        mContext = context;
        mItems = items;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new ViewHolderPlaylist(LayoutInflater.from(mContext).inflate(R.layout.recycler_playlist_create_playlist_item, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder baseHolder, int i) {
        Playlist playlist = mItems.get(i);

        ViewHolderPlaylist viewHolder = (ViewHolderPlaylist) baseHolder;
        viewHolder.TxtName.setText(playlist.getName());
        viewHolder.ChkAdded.setOnCheckedChangeListener((buttonView, isChecked) -> mCallback.onCheckedChanged(viewHolder, isChecked));
        viewHolder.setListener(mCallback);
    }

    @Override
    public int getItemCount() {
        return mItems.size();
    }

    public void setListener(AdapterEventListener callback) {
        mCallback = callback;
    }
}
