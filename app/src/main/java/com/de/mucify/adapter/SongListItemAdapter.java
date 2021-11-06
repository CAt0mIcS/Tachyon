package com.de.mucify.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.AsyncDifferConfig;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;

import com.de.mucify.R;
import com.de.mucify.playable.Song;

import java.util.ArrayList;

public class SongListItemAdapter extends RecyclerView.Adapter<SongListItemAdapter.SongViewHolder> {

    private final Context mContext;
    private final ArrayList<Song> mSongs;
    private final SongViewHolder.OnItemClickListener mOnItemClickListener;

    public SongListItemAdapter(Context context, ArrayList<Song> songs, SongViewHolder.OnItemClickListener onItemClickListener) {
        mContext = context;
        mSongs = songs;
        mOnItemClickListener = onItemClickListener;
    }

    @NonNull
    @Override
    public SongViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new SongViewHolder(LayoutInflater.from(mContext)
                .inflate(R.layout.recycler_song_loop_item_layout, parent, false), mOnItemClickListener);
    }

    @Override
    public void onBindViewHolder(@NonNull SongViewHolder holder, int i) {
        Song song = mSongs.get(i);
        holder.mTitle.setText(song.getTitle());
        holder.mArtist.setText(song.getArtist());
    }

    @Override
    public int getItemCount() {
        return mSongs.size();
    }

    public static class SongViewHolder extends RecyclerView.ViewHolder {

        private final LinearLayout mItemLayout;
        private final TextView mTitle;
        private final TextView mArtist;
        private final OnItemClickListener mOnItemClickListener;

        public SongViewHolder(@NonNull View itemView, OnItemClickListener onItemClickListener) {
            super(itemView);

            mOnItemClickListener = onItemClickListener;

            mItemLayout = itemView.findViewById(R.id.rvItemLayout);
            mTitle = itemView.findViewById(R.id.txtTitle);
            mArtist = itemView.findViewById(R.id.txtArtist);

            mItemLayout.setOnClickListener(v -> mOnItemClickListener.onItemClicked(this));
        }

        public interface OnItemClickListener {
            void onItemClicked(SongViewHolder holder);
        }
    }
}
