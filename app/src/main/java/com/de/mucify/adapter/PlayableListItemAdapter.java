package com.de.mucify.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.de.mucify.R;
import com.de.mucify.player.Playback;
import com.de.mucify.player.Playlist;
import com.de.mucify.player.Song;

import java.util.ArrayList;

public class PlayableListItemAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    public final int ITEM_TYPE_LOOP = 1;
    public final int ITEM_TYPE_SONG = 2;
    public final int ITEM_TYPE_PLAYLIST = 3;

    private final Context mContext;
    private final ArrayList<Playback> mItems;


    public PlayableListItemAdapter(Context context, ArrayList<Playback> items) {
        mContext = context;
        mItems = items;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if (viewType == ITEM_TYPE_SONG)
            return new ViewHolderSong(LayoutInflater.from(mContext).inflate(R.layout.recycler_song_item_layout, parent, false));
        else if (viewType == ITEM_TYPE_LOOP)
            return new ViewHolderLoop(LayoutInflater.from(mContext).inflate(R.layout.recycler_loop_item_layout, parent, false));
        else
            return new ViewHolderPlaylist(LayoutInflater.from(mContext).inflate(R.layout.recycler_playlist_item_layout, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder baseHolder, int i) {
        if(baseHolder instanceof ViewHolderSong) {
            Song song = (Song)mItems.get(i);

            ViewHolderSong viewHolder = (ViewHolderSong) baseHolder;
            viewHolder.TxtTitle.setText(song.getTitle());
            viewHolder.TxtArtist.setText(song.getArtist());
        }
        else if(baseHolder instanceof ViewHolderLoop) {
            Song song = (Song)mItems.get(i);

            ViewHolderLoop viewHolder = (ViewHolderLoop) baseHolder;
            viewHolder.TxtName.setText(song.getLoopName());
            viewHolder.TxtTitle.setText(song.getTitle());
            viewHolder.TxtArtist.setText(song.getArtist());
        }
        else {
            Playlist playlist = (Playlist)mItems.get(i);

            ViewHolderPlaylist viewHolder = (ViewHolderPlaylist) baseHolder;
            viewHolder.TxtName.setText(playlist.getName());
            viewHolder.TxtNumSongs.setText(String.valueOf(playlist.getSongs().size()));
        }
    }

    @Override
    public int getItemCount() {
        return mItems.size();
    }

    @Override
    public int getItemViewType(int position) {
        if (mItems.get(position) instanceof Song) {
            if(((Song)mItems.get(position)).isLoop())
                return ITEM_TYPE_LOOP;
            return ITEM_TYPE_SONG;
        }
        return ITEM_TYPE_PLAYLIST;
    }
}
