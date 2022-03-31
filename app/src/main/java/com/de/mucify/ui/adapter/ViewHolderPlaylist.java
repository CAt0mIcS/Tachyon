package com.de.mucify.ui.adapter;

import android.view.View;
import android.widget.CheckBox;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.de.mucify.R;
import com.de.mucify.player.Playback;

public class ViewHolderPlaylist extends RecyclerView.ViewHolder {
    public final LinearLayout ParentLayout;
    public final TextView TxtName;
    public final TextView TxtNumSongs;
    public CheckBox ChkAdded;

    public ViewHolderPlaylist(@NonNull View itemView) {
        super(itemView);

        ParentLayout = itemView.findViewById(R.id.parent_layout);
        TxtName = itemView.findViewById(R.id.txtName);
        TxtNumSongs = itemView.findViewById(R.id.txtNumSongs);
        ChkAdded = itemView.findViewById(R.id.chkAddedToPlaylist);
    }

    public void setListener(AdapterEventListener callback) {
        ParentLayout.setOnClickListener(v -> callback.onClick(ViewHolderPlaylist.this, PlaybackListItemAdapter.ITEM_TYPE_PLAYLIST));
        ParentLayout.setOnLongClickListener(v -> {
            callback.onLongClick(ViewHolderPlaylist.this, PlaybackListItemAdapter.ITEM_TYPE_PLAYLIST);
            return true;
        });
    }
}
