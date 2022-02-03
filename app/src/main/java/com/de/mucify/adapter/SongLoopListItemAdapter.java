package com.de.mucify.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.de.mucify.R;
import com.de.mucify.playable.Song;

import java.util.ArrayList;

public class SongLoopListItemAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    public final int ITEM_TYPE_LOOP = 1;
    public final int ITEM_TYPE_SONG = 2;

    private final Context mContext;
    private final ArrayList<Song> mItems;
    private ArrayList<Boolean> mItemCheckedStatus = new ArrayList<>();

    private boolean mCheckbox = true;

    private SongListItemAdapter.SongViewHolder.OnCheckedChangedListener mOnSongCheckedChangedListener;
    private LoopListItemAdapter.LoopViewHolder.OnCheckedChangedListener mOnLoopCheckedChangedListener;

    private SongListItemAdapter.SongViewHolder.OnItemClickListener mOnSongClickListener;
    private LoopListItemAdapter.LoopViewHolder.OnItemClickListener mOnLoopClickListener;

    public SongLoopListItemAdapter(Context context, ArrayList<Song> items, ArrayList<Song> playlistSongs) {
        mContext = context;
        mItems = items;

        mItemCheckedStatus = new ArrayList<>(mItems.size());
        updateCheckedMap(playlistSongs);
    }

    public SongLoopListItemAdapter(Context context, ArrayList<Song> items) {
        mContext = context;
        mItems = items;

        mCheckbox = false;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if (viewType == ITEM_TYPE_SONG)
            return new SongListItemAdapter.SongViewHolder(LayoutInflater.from(mContext).inflate(R.layout.recycler_song_playlist_item_layout, parent, false));
        else
            return new LoopListItemAdapter.LoopViewHolder(LayoutInflater.from(mContext).inflate(R.layout.recycler_loop_item_layout, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder baseHolder, int i) {
        Song song = mItems.get(i);

        if(baseHolder instanceof SongListItemAdapter.SongViewHolder) {
            SongListItemAdapter.SongViewHolder holder = (SongListItemAdapter.SongViewHolder)baseHolder;
            holder.TxtTitle.setText(song.getTitle());
            holder.TxtArtist.setText(song.getArtist());
            holder.ChkItem.setVisibility(mCheckbox ? View.VISIBLE : View.INVISIBLE);
            holder.OnItemClickListener = mOnSongClickListener;
            if(mCheckbox){
                holder.OnCheckedChangedListener = mOnSongCheckedChangedListener;
                holder.ChkItem.setChecked(mItemCheckedStatus.get(i));
            }
        }
        else {
            LoopListItemAdapter.LoopViewHolder holder = (LoopListItemAdapter.LoopViewHolder)baseHolder;
            holder.TxtName.setText(song.getLoopName());
            holder.TxtTitle.setText(song.getTitle());
            holder.TxtArtist.setText(song.getArtist());
            holder.ChkItem.setVisibility(mCheckbox ? View.VISIBLE : View.INVISIBLE);
            holder.OnItemClickListener = mOnLoopClickListener;
            if(mCheckbox) {
                holder.OnCheckedChangedListener = mOnLoopCheckedChangedListener;
                holder.ChkItem.setChecked(mItemCheckedStatus.get(i));
            }

        }
    }

    @Override
    public int getItemCount() {
        return mItems.size();
    }

    @Override
    public int getItemViewType(int position) {
        if (mItems.get(position).isLoop())
            return ITEM_TYPE_LOOP;
        else
            return ITEM_TYPE_SONG;
    }

    public void setOnCheckedChangedListener(OnCheckedChangedListener listener) {
        mOnSongCheckedChangedListener = (holder, isChecked) -> {
            mItemCheckedStatus.set(holder.getAdapterPosition(), isChecked);
            listener.onCheckedChanged(holder, isChecked);
        };
        mOnLoopCheckedChangedListener = (holder, isChecked) -> {
            mItemCheckedStatus.set(holder.getAdapterPosition(), isChecked);
            listener.onCheckedChanged(holder, isChecked);
        };
    }

    public  void setOnClickListener(OnClickListener listener) {
        mOnSongClickListener = listener::onClick;
        mOnLoopClickListener = listener::onClick;
    }

    public void updateCheckedMap(ArrayList<Song> playlistSongs) {
        mItemCheckedStatus.clear();
        for(Song item : mItems) {
            boolean contains = false;
            for(Song playlistSong : playlistSongs) {
                if(item.equals(playlistSong)) {
                    contains = true;
                    break;
                }
            }
            mItemCheckedStatus.add(contains);
        }
    }

    public interface OnCheckedChangedListener {
        void onCheckedChanged(RecyclerView.ViewHolder holder, boolean isChecked);
    }

    public interface OnClickListener {
        void onClick(RecyclerView.ViewHolder holder);
    }
}
