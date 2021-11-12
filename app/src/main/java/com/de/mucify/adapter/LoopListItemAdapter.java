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

public class LoopListItemAdapter extends RecyclerView.Adapter<LoopListItemAdapter.LoopViewHolder> {

    private final Context mContext;
    private final ArrayList<Song> mSongs;
    private LoopViewHolder.OnItemClickListener mOnItemClickListener;
    private LoopViewHolder.OnItemClickListener mOnItemLongClickListener;

    public LoopListItemAdapter(Context context, ArrayList<Song> songs) {
        mContext = context;
        mSongs = songs;
    }

    @NonNull
    @Override
    public LoopViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new LoopViewHolder(LayoutInflater.from(mContext)
                .inflate(R.layout.recycler_loop_item_layout, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull LoopViewHolder holder, int i) {
        Song song = mSongs.get(i);
        holder.mOnItemClickListener = mOnItemClickListener;
        holder.mOnItemLongClickListener = mOnItemLongClickListener;
        holder.mName.setText(song.getLoopName());
        holder.mTitle.setText(song.getTitle());
        holder.mArtist.setText(song.getArtist());
    }

    public void setOnItemClicked(LoopViewHolder.OnItemClickListener listener) {
        mOnItemClickListener = listener;
    }

    public void setOnItemLongClicked(LoopViewHolder.OnItemClickListener listener) {
        mOnItemLongClickListener = listener;
    }

    @Override
    public int getItemCount() {
        return mSongs.size();
    }

    public static class LoopViewHolder extends RecyclerView.ViewHolder {

        private final LinearLayout mItemLayout;
        private final TextView mName;
        private final TextView mTitle;
        private final TextView mArtist;
        private OnItemClickListener mOnItemClickListener;
        private OnItemClickListener mOnItemLongClickListener;

        public LoopViewHolder(@NonNull View itemView) {
            super(itemView);

            mItemLayout = itemView.findViewById(R.id.rvItemLayout);
            mName = itemView.findViewById(R.id.txtName);
            mTitle = itemView.findViewById(R.id.txtTitle);
            mArtist = itemView.findViewById(R.id.txtArtist);

            mItemLayout.setOnClickListener(v -> {
                if(mOnItemClickListener != null)
                    mOnItemClickListener.onItemClicked(this);
            });
            mItemLayout.setOnLongClickListener(v -> {
                if(mOnItemLongClickListener != null)
                    mOnItemLongClickListener.onItemClicked(this);
                return true;
            });
        }

        public String getName() { return mName.getText().toString(); }

        public interface OnItemClickListener {
            void onItemClicked(LoopViewHolder holder);
        }
    }
}
