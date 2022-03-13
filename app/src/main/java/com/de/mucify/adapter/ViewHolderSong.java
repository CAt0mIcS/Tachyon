package com.de.mucify.adapter;

import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.de.mucify.R;

public class ViewHolderSong extends RecyclerView.ViewHolder {
    public final TextView TxtTitle;
    public final TextView TxtArtist;

    public ViewHolderSong(@NonNull View itemView) {
        super(itemView);

        TxtTitle = itemView.findViewById(R.id.txtTitle);
        TxtArtist = itemView.findViewById(R.id.txtArtist);
    }
}
