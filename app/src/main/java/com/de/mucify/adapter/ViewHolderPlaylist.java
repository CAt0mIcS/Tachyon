package com.de.mucify.adapter;

import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.de.mucify.R;

public class ViewHolderPlaylist extends RecyclerView.ViewHolder {
    public final LinearLayout ParentLayout;
    public final TextView TxtName;
    public final TextView TxtNumSongs;

    public ViewHolderPlaylist(@NonNull View itemView) {
        super(itemView);

        ParentLayout = itemView.findViewById(R.id.parent_layout);
        TxtName = itemView.findViewById(R.id.txtTitle);
        TxtNumSongs = itemView.findViewById(R.id.txtNumSongs);
    }

    public void setListener(AdapterEventListener callback) {
        ParentLayout.setOnClickListener(v -> callback.onClick(ViewHolderPlaylist.this));
    }
}
