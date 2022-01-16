package com.de.mucify.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.coordinatorlayout.widget.CoordinatorLayout;
import androidx.recyclerview.widget.RecyclerView;

import com.de.mucify.R;
import com.de.mucify.playable.Song;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class LoopListItemAdapter extends RecyclerView.Adapter<LoopListItemAdapter.LoopViewHolder> {

    private final Context mContext;
    private final ArrayList<Song> mSongs;

    private final HashMap<Integer, LoopViewHolder.OnItemClickListener> mOnViewClickedListeners = new HashMap<>();

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

        for (Map.Entry<Integer, LoopViewHolder.OnItemClickListener> entry : mOnViewClickedListeners.entrySet()) {
            int key = entry.getKey();
            LoopViewHolder.OnItemClickListener listener = entry.getValue();

            if (key == holder.LinearLayout.getId())
                holder.LinearLayout.setOnClickListener(v -> listener.onItemClicked(holder));
            else if (key == holder.CoordinatorLayout.getId())
                holder.CoordinatorLayout.setOnClickListener(v -> listener.onItemClicked(holder));
            else if(key == holder.TxtName.getId())
                holder.TxtName.setOnClickListener(v -> listener.onItemClicked(holder));
            else if(key == holder.TxtTitle.getId())
                holder.TxtTitle.setOnClickListener(v -> listener.onItemClicked(holder));
            else if(key == holder.TxtArtist.getId())
                holder.TxtArtist.setOnClickListener(v -> listener.onItemClicked(holder));
            else if(key == holder.BtnFileOptions.getId())
                holder.BtnFileOptions.setOnClickListener(v -> listener.onItemClicked(holder));
            else
                throw new IllegalArgumentException("No view exists with id " + key);
        }

        holder.TxtName.setText(song.getLoopName());
        holder.TxtTitle.setText(song.getTitle());
        holder.TxtArtist.setText(song.getArtist());
    }

    public void setOnViewClickedListener(int btnFileOptions, LoopViewHolder.OnItemClickListener onClicked) {
        mOnViewClickedListeners.put(btnFileOptions, onClicked);
    }

    @Override
    public int getItemCount() {
        return mSongs.size();
    }

    public static class LoopViewHolder extends RecyclerView.ViewHolder {

        public final CoordinatorLayout CoordinatorLayout;
        public final LinearLayout LinearLayout;
        public final TextView TxtName;
        public final TextView TxtTitle;
        public final TextView TxtArtist;
        public final CheckBox ChkItem;
        public final ImageButton BtnFileOptions;
        public OnCheckedChangedListener OnCheckedChangedListener;

        public LoopViewHolder(@NonNull View itemView) {
            super(itemView);

            CoordinatorLayout = itemView.findViewById(R.id.rvCoordinatorLayout);
            LinearLayout = itemView.findViewById(R.id.rvLinearLayout);
            TxtName = itemView.findViewById(R.id.txtName);
            TxtTitle = itemView.findViewById(R.id.txtTitle);
            TxtArtist = itemView.findViewById(R.id.txtArtist);
            ChkItem = itemView.findViewById(R.id.chkItem);
            BtnFileOptions = itemView.findViewById(R.id.btnFileOptions);

            ChkItem.setOnCheckedChangeListener((v, isChecked) -> {
                if(OnCheckedChangedListener != null)
                    OnCheckedChangedListener.onCheckedChanged(this, isChecked);
            });
        }

        public String getName() { return TxtName.getText().toString(); }
        public String getTitle() { return TxtTitle.getText().toString(); }
        public String getAuthor() { return TxtArtist.getText().toString(); }

        public interface OnItemClickListener {
            void onItemClicked(LoopViewHolder holder);
        }

        public interface OnCheckedChangedListener {
            void onCheckedChanged(LoopViewHolder holder, boolean isChecked);
        }
    }
}
