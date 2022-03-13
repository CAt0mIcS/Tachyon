package com.de.mucify.adapter;

import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.de.mucify.R;

public class ViewHolderPlaylist extends RecyclerView.ViewHolder {
    public final TextView TxtName;
    public final TextView TxtNumSongs;

    public ViewHolderPlaylist(@NonNull View itemView) {
        super(itemView);

        TxtName = itemView.findViewById(R.id.txtTitle);
        TxtNumSongs = itemView.findViewById(R.id.txtNumSongs);
    }
}
