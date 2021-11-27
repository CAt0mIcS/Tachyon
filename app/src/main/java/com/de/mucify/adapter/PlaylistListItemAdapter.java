package com.de.mucify.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.de.mucify.R;
import com.de.mucify.playable.Playlist;

import java.util.ArrayList;

public class PlaylistListItemAdapter extends RecyclerView.Adapter<PlaylistListItemAdapter.PlaylistViewHolder> {

    private final Context mContext;
    private final ArrayList<Playlist> mPlaylists;
    private PlaylistViewHolder.OnItemClickListener mOnItemClickListener;

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
        holder.mOnItemClickListener = mOnItemClickListener;
//        holder.mName.setText(playlist.getName());
    }

    @Override
    public int getItemCount() { return mPlaylists.size(); }

    public void setOnItemClicked(PlaylistViewHolder.OnItemClickListener listener) {
        mOnItemClickListener = listener;
    }

    public static class PlaylistViewHolder extends RecyclerView.ViewHolder {

        private final LinearLayout mItemLayout;
        private final TextView mName;
        private OnItemClickListener mOnItemClickListener;

        public PlaylistViewHolder(@NonNull View itemView) {
            super(itemView);

            mItemLayout = itemView.findViewById(R.id.rvItemLayout);
            mName = itemView.findViewById(R.id.txtName);

            mItemLayout.setOnClickListener(v -> {
                if(mOnItemClickListener != null)
                    mOnItemClickListener.onItemClicked(this);
            });
        }

        public interface OnItemClickListener {
            void onItemClicked(PlaylistViewHolder holder);
        }
    }
}
