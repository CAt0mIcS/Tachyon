package com.de.mucify.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
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
        holder.TxtTitle.setText(song.getTitle());
        holder.TxtArtist.setText(song.getArtist());
        holder.OnItemClickListener = mOnItemClickListener;
    }

    @Override
    public int getItemCount() {
        return mSongs.size();
    }

    public void setOnItemClicked(SongViewHolder.OnItemClickListener listener) {
        mOnItemClickListener = listener;
    }

    public static class SongViewHolder extends RecyclerView.ViewHolder {
        
        public final LinearLayout ItemLayout;
        public final TextView TxtTitle;
        public final TextView TxtArtist;
        public final CheckBox ChkItem;
        public OnItemClickListener OnItemClickListener;
        public OnCheckedChangedListener OnCheckedChangedListener;

        public SongViewHolder(@NonNull View itemView) {
            super(itemView);

            ItemLayout = itemView.findViewById(R.id.rvItemLayout);
            TxtTitle = itemView.findViewById(R.id.txtTitle);
            TxtArtist = itemView.findViewById(R.id.txtArtist);
            ChkItem = itemView.findViewById(R.id.chkItem);

            ItemLayout.setOnClickListener(v -> {
                if(OnItemClickListener != null)
                    OnItemClickListener.onItemClicked(this);
            });
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
