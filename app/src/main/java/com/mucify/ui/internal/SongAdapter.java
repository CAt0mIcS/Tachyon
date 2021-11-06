package com.mucify.ui.internal;

import android.content.Context;
import android.media.MediaMetadataRetriever;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.mucify.R;
import com.mucify.objects.Song;

import java.io.File;
import java.util.ArrayList;

public class SongAdapter extends RecyclerView.Adapter<SongAdapter.SongViewHolder> {
    Context mContext;
    ArrayList<File> mSongs;
    SongViewHolder.SongViewHolderClickListener mListener = null;

    public SongAdapter(Context context, ArrayList<File> songs, SongViewHolder.SongViewHolderClickListener listener) {
        mContext = context;
        mListener = listener;
        mSongs = songs;
    }

    @NonNull
    @Override
    public SongViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new SongViewHolder(LayoutInflater.from(mContext)
                .inflate(R.layout.recycler_song_item_layout, parent, false), mListener);
    }

    @Override
    public void onBindViewHolder(@NonNull SongViewHolder holder, int i) {
        File song = mSongs.get(i);
        MediaMetadataRetriever metaRetriever = new MediaMetadataRetriever();

        try {
            metaRetriever.setDataSource(song.getAbsolutePath());
        } catch(IllegalArgumentException e) {
            e.printStackTrace();
        }
        String artist =  metaRetriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_ARTIST);
        String title = metaRetriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_TITLE);

        if(artist == null || artist.isEmpty())
            artist = mContext.getString(R.string.artist_unknown);
        if(title == null || title.isEmpty())
            title = Song.toName(song);

        holder.mName.setText(title);
        holder.mArtist.setText(artist);
    }

    @Override
    public int getItemCount() {
        return mSongs.size();
    }

    public static class SongViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        private LinearLayout mLinearLayout;
        private TextView mName;
        private TextView mArtist;
        public SongViewHolderClickListener mListener;

        public SongViewHolder(@NonNull View itemView, SongViewHolderClickListener listener) {
            super(itemView);

            mListener = listener;
            mLinearLayout = itemView.findViewById(R.id.rvItemLayout);
            mName = itemView.findViewById(R.id.txtSongName);
            mArtist = itemView.findViewById(R.id.txtArtist);

            mLinearLayout.setOnClickListener(this);
        }

        public View getView() {
            return mLinearLayout;
        }

        @Override
        public void onClick(View v) {
            if(mListener != null)
                mListener.onItemClicked(this);
        }

        public interface SongViewHolderClickListener {
            void onItemClicked(SongViewHolder holder);
        }
    }
}
