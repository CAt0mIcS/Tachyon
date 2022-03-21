package com.de.mucify.ui.adapter;

import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.de.mucify.R;

public class ViewHolderSong extends RecyclerView.ViewHolder {
    public final LinearLayout ParentLayout;
    public final TextView TxtTitle;
    public final TextView TxtArtist;

    public ViewHolderSong(@NonNull View itemView) {
        super(itemView);

        ParentLayout = itemView.findViewById(R.id.parent_layout);
        TxtTitle = itemView.findViewById(R.id.txtTitle);
        TxtArtist = itemView.findViewById(R.id.txtArtist);
    }

    public void setListener(AdapterEventListener callback) {
        ParentLayout.setOnClickListener(v -> callback.onClick(ViewHolderSong.this));
    }
}
