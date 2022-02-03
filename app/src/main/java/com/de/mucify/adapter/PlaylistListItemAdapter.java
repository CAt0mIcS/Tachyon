package com.de.mucify.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.IdRes;
import androidx.annotation.NonNull;
import androidx.coordinatorlayout.widget.CoordinatorLayout;
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
    private final HashMap<Integer, PlaylistViewHolder.OnItemClickListener> mOnViewLongClickedListeners = new HashMap<>();


    public PlaylistListItemAdapter(Context context, ArrayList<Playlist> playlists) {
        mContext = context;
        mPlaylists = playlists;
    }

    @NonNull
    @Override
    public PlaylistViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new PlaylistViewHolder(LayoutInflater.from(mContext)
                .inflate(R.layout.recycler_song_playlist_item_layout, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull PlaylistViewHolder holder, int i) {
        Playlist playlist = mPlaylists.get(i);
        for (Map.Entry<Integer, PlaylistViewHolder.OnItemClickListener> entry : mOnViewClickedListeners.entrySet()) {
            int key = entry.getKey();
            PlaylistViewHolder.OnItemClickListener listener = entry.getValue();

            getViewById(holder, key).setOnClickListener(v -> listener.onItemClicked(holder));
        }

        for (Map.Entry<Integer, PlaylistViewHolder.OnItemClickListener> entry : mOnViewLongClickedListeners.entrySet()) {
            int key = entry.getKey();
            PlaylistViewHolder.OnItemClickListener listener = entry.getValue();

            getViewById(holder, key).setOnLongClickListener(view -> { listener.onItemClicked(holder); return true; });
        }


        holder.TxtTitle.setText(playlist.getName());
        if(playlist.getSongs().size() == 1)
            holder.TxtArtist.setText("1 Song");
        else
            holder.TxtArtist.setText(playlist.getSongs().size() + " Songs");
    }

    private View getViewById(PlaylistViewHolder holder, @IdRes int key) {
        if (key == holder.LinearLayout.getId())
            return holder.LinearLayout;
        else if (key == holder.CoordinatorLayout.getId())
            return holder.CoordinatorLayout;
        else if(key == holder.TxtTitle.getId())
            return holder.TxtTitle;
        else if(key == holder.TxtArtist.getId())
            return holder.TxtArtist;
        else if(key == holder.ChkItem.getId())
            return holder.ChkItem;
        else
            throw new IllegalArgumentException("No view exists with id " + key);
    }

    @Override
    public int getItemCount() { return mPlaylists.size(); }

    public void setOnViewClickedListener(int btnFileOptions, PlaylistViewHolder.OnItemClickListener onFileOptionsClicked) {
        mOnViewClickedListeners.put(btnFileOptions, onFileOptionsClicked);
    }

    public void setOnViewLongClickedListener(int btnFileOptions, PlaylistViewHolder.OnItemClickListener onFileOptionsClicked) {
        mOnViewLongClickedListeners.put(btnFileOptions, onFileOptionsClicked);
    }

    public static class PlaylistViewHolder extends RecyclerView.ViewHolder {

        public final CoordinatorLayout CoordinatorLayout;
        public final LinearLayout LinearLayout;
        public final TextView TxtTitle;
        public final TextView TxtArtist;
        public final CheckBox ChkItem;

        public PlaylistViewHolder(@NonNull View itemView) {
            super(itemView);

            CoordinatorLayout = itemView.findViewById(R.id.rvCoordinatorLayout);
            LinearLayout = itemView.findViewById(R.id.rvLinearLayout);
            TxtTitle = itemView.findViewById(R.id.txtTitle);
            TxtArtist = itemView.findViewById(R.id.txtAdditionalInfo);
            ChkItem = itemView.findViewById(R.id.chkItem);
        }

        public interface OnItemClickListener {
            void onItemClicked(PlaylistViewHolder holder);
        }
    }
}
