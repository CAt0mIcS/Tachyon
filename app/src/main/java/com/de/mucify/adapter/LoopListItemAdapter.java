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
        holder.OnItemClickListener = mOnItemClickListener;
        holder.OnItemLongClickListener = mOnItemLongClickListener;
        holder.TxtName.setText(song.getLoopName());
        holder.TxtTitle.setText(song.getTitle());
        holder.TxtArtist.setText(song.getArtist());
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

        public final CoordinatorLayout CoordinatorLayout;
        public final LinearLayout LinearLayout;
        public final TextView TxtName;
        public final TextView TxtTitle;
        public final TextView TxtArtist;
        public final CheckBox ChkItem;
        public OnItemClickListener OnItemClickListener;
        public OnItemClickListener OnItemLongClickListener;
        public OnCheckedChangedListener OnCheckedChangedListener;

        public LoopViewHolder(@NonNull View itemView) {
            super(itemView);

            CoordinatorLayout = itemView.findViewById(R.id.rvCoordinatorLayout);
            LinearLayout = itemView.findViewById(R.id.rvLinearLayout);
            TxtName = itemView.findViewById(R.id.txtName);
            TxtTitle = itemView.findViewById(R.id.txtTitle);
            TxtArtist = itemView.findViewById(R.id.txtArtist);
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

        public String getName() { return TxtName.getText().toString(); }

        public interface OnItemClickListener {
            void onItemClicked(LoopViewHolder holder);
        }

        public interface OnCheckedChangedListener {
            void onCheckedChanged(LoopViewHolder holder, boolean isChecked);
        }
    }
}
