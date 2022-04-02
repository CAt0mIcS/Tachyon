package com.de.mucify.ui.adapter;

import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.RecyclerView;

import com.de.mucify.R;

public class ViewHolderSong extends RecyclerView.ViewHolder {
    public final ConstraintLayout ParentLayout;
    public final TextView TxtTitle;
    public final TextView TxtArtist;
    public final TextView TxtDuration;
    public final ImageView ImgAlbumArt;

    public ViewHolderSong(@NonNull View itemView) {
        super(itemView);

        ParentLayout = itemView.findViewById(R.id.parent_layout);
        TxtTitle = itemView.findViewById(R.id.txtTitle);
        TxtArtist = itemView.findViewById(R.id.txtArtist);
        TxtDuration = itemView.findViewById(R.id.txtDuration);
        ImgAlbumArt = itemView.findViewById(R.id.albumArt);
    }

    public void setListener(AdapterEventListener callback) {
        ParentLayout.setOnClickListener(v -> callback.onClick(ViewHolderSong.this, PlaybackListItemAdapter.ITEM_TYPE_SONG));
        ParentLayout.setOnLongClickListener(v -> {
            callback.onLongClick(ViewHolderSong.this, PlaybackListItemAdapter.ITEM_TYPE_SONG);
            return true;
        });
    }
}
