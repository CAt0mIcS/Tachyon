package com.de.mucify.ui.adapter;

import androidx.recyclerview.widget.RecyclerView;

public abstract class AdapterEventListener {
    public void onClick(RecyclerView.ViewHolder holder, int viewType) {
    }

    public void onLongClick(RecyclerView.ViewHolder holder, int viewType) {
    }

    public void onCheckedChanged(ViewHolderPlaylist holder, boolean checked) {
    }
}
