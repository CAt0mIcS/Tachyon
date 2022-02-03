package com.de.mucify.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.coordinatorlayout.widget.CoordinatorLayout;
import androidx.recyclerview.widget.RecyclerView;

import com.de.mucify.R;
import com.de.mucify.playable.Song;

import java.util.ArrayList;

public class SongListItemAdapter extends RecyclerView.Adapter<SongListItemAdapter.SongViewHolder> {

    private final Context mContext;
    private final ArrayList<Song> mSongs;
    private SongViewHolder.OnItemClickListener mOnItemClickListener;
    private SongViewHolder.OnItemClickListener mOnItemLongClickListener;

    public SongListItemAdapter(Context context, ArrayList<Song> songs) {
        mContext = context;
        mSongs = songs;
    }

    @NonNull
    @Override
    public SongViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new SongViewHolder(LayoutInflater.from(mContext)
                .inflate(R.layout.recycler_song_playlist_item_layout, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull SongViewHolder holder, int i) {
        Song song = mSongs.get(i);
        holder.TxtTitle.setText(song.getTitle());
        holder.TxtArtist.setText(song.getArtist());
        holder.OnItemClickListener = mOnItemClickListener;
        holder.OnItemLongClickListener = mOnItemLongClickListener;
    }

    @Override
    public int getItemCount() {
        return mSongs.size();
    }

    public void setOnItemClicked(SongViewHolder.OnItemClickListener listener) {
        mOnItemClickListener = listener;
    }

    public void setOnItemLongClicked(SongViewHolder.OnItemClickListener listener) {
        mOnItemLongClickListener = listener;
    }

    public static class SongViewHolder extends RecyclerView.ViewHolder {
        
        public final CoordinatorLayout CoordinatorLayout;
        public final LinearLayout LinearLayout;
        public final TextView TxtTitle;
        public final TextView TxtArtist;
        public final CheckBox ChkItem;
        public OnItemClickListener OnItemClickListener;
        public OnItemClickListener OnItemLongClickListener;
        public OnCheckedChangedListener OnCheckedChangedListener;

        public SongViewHolder(@NonNull View itemView) {
            super(itemView);

            CoordinatorLayout = itemView.findViewById(R.id.rvCoordinatorLayout);
            LinearLayout = itemView.findViewById(R.id.rvLinearLayout);
            TxtTitle = itemView.findViewById(R.id.txtTitle);
            TxtArtist = itemView.findViewById(R.id.txtAdditionalInfo);
            ChkItem = itemView.findViewById(R.id.chkItem);

            View.OnClickListener l1 = v -> {
                if(OnItemClickListener != null)
                    OnItemClickListener.onItemClicked(this);
            };
            CoordinatorLayout.setOnClickListener(l1);
            LinearLayout.setOnClickListener(l1);

            View.OnLongClickListener l2 = v -> {
                if(OnItemLongClickListener != null)
                    OnItemLongClickListener.onItemClicked(this);
                return true;
            };
            CoordinatorLayout.setOnLongClickListener(l2);
            LinearLayout.setOnLongClickListener(l2);

            ChkItem.setOnCheckedChangeListener((v, isChecked) -> {
                if(OnCheckedChangedListener != null)
                    OnCheckedChangedListener.onCheckedChanged(this, isChecked);
            });
        }

        public interface OnItemClickListener {
            void onItemClicked(SongViewHolder holder);
        }

        public interface OnCheckedChangedListener {
            void onCheckedChanged(SongViewHolder holder, boolean isChecked);
        }
    }
}
