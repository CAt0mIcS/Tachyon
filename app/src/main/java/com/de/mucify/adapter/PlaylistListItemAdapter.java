package com.de.mucify.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.de.mucify.R;
import com.de.mucify.playable.Playlist;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class PlaylistListItemAdapter extends RecyclerView.Adapter<PlaylistListItemAdapter.PlaylistViewHolder> {

    private final Context mContext;
    private final ArrayList<Playlist> mPlaylists;

    private final HashMap<Integer, PlaylistViewHolder.OnItemClickListener> mOnViewClickedListeners = new HashMap<>();


    public PlaylistListItemAdapter(Context context, ArrayList<Playlist> playlists) {
        mContext = context;
        mPlaylists = playlists;
    }

    @NonNull
    @Override
    public PlaylistViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new PlaylistViewHolder(LayoutInflater.from(mContext)
                .inflate(R.layout.recycler_playlist_item_layout, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull PlaylistViewHolder holder, int i) {
        Playlist playlist = mPlaylists.get(i);
        for (Map.Entry<Integer, PlaylistViewHolder.OnItemClickListener> entry : mOnViewClickedListeners.entrySet()) {
            int key = entry.getKey();
            PlaylistViewHolder.OnItemClickListener listener = entry.getValue();

            if (key == holder.ItemLayout.getId())
                holder.ItemLayout.setOnClickListener(v -> listener.onItemClicked(holder));
            else if(key == holder.TxtName.getId())
                holder.TxtName.setOnClickListener(v -> listener.onItemClicked(holder));
            else if(key == holder.BtnFileOptions.getId())
                holder.BtnFileOptions.setOnClickListener(v -> listener.onItemClicked(holder));
            else
                throw new IllegalArgumentException("No view exists with id " + key);
        }


        holder.TxtName.setText(playlist.getName());
    }

    @Override
    public int getItemCount() { return mPlaylists.size(); }

    public void setOnViewClickedListener(int btnFileOptions, PlaylistViewHolder.OnItemClickListener onFileOptionsClicked) {
        mOnViewClickedListeners.put(btnFileOptions, onFileOptionsClicked);
    }

    public static class PlaylistViewHolder extends RecyclerView.ViewHolder {

        public final LinearLayout ItemLayout;
        public final TextView TxtName;
        public final ImageButton BtnFileOptions;

        public PlaylistViewHolder(@NonNull View itemView) {
            super(itemView);

            ItemLayout = itemView.findViewById(R.id.rvCoordinatorLayout);
            TxtName = itemView.findViewById(R.id.txtName);
            BtnFileOptions = itemView.findViewById(R.id.btnFileOptions);
        }

        public interface OnItemClickListener {
            void onItemClicked(PlaylistViewHolder holder);
        }
    }
}
