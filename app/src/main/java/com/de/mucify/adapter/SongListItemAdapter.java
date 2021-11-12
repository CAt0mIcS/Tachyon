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
    private SongViewHolder.OnItemClickListener mOnItemClickListener;

    public SongListItemAdapter(Context context, ArrayList<Song> songs) {
        mContext = context;
        mSongs = songs;
    }

    @NonNull
    @Override
    public SongViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new SongViewHolder(LayoutInflater.from(mContext)
                .inflate(R.layout.recycler_song_item_layout, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull SongViewHolder holder, int i) {
        Song song = mSongs.get(i);
        holder.mOnItemClickListener = mOnItemClickListener;
        holder.mTitle.setText(song.getTitle());
        holder.mArtist.setText(song.getArtist());
    }

    @Override
    public int getItemCount() {
        return mSongs.size();
    }

    public void setOnItemClicked(SongViewHolder.OnItemClickListener listener) {
        mOnItemClickListener = listener;
    }

    public static class SongViewHolder extends RecyclerView.ViewHolder {

        private final LinearLayout mItemLayout;
        private final TextView mTitle;
        private final TextView mArtist;
        private OnItemClickListener mOnItemClickListener;

        public SongViewHolder(@NonNull View itemView) {
            super(itemView);

            mItemLayout = itemView.findViewById(R.id.rvItemLayout);
            mTitle = itemView.findViewById(R.id.txtTitle);
            mArtist = itemView.findViewById(R.id.txtArtist);

            mItemLayout.setOnClickListener(v -> {
                if(mOnItemClickListener != null)
                    mOnItemClickListener.onItemClicked(this);
            });
        }

        public interface OnItemClickListener {
            void onItemClicked(SongViewHolder holder);
        }
    }
}
