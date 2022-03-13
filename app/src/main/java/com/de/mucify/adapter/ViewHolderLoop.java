package com.de.mucify.adapter;

import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.de.mucify.R;

public class ViewHolderLoop extends RecyclerView.ViewHolder {
    public final LinearLayout ParentLayout;
    public final TextView TxtName;
    public final TextView TxtTitle;
    public final TextView TxtArtist;

    public ViewHolderLoop(@NonNull View itemView) {
        super(itemView);

        ParentLayout = itemView.findViewById(R.id.parent_layout);
        TxtName = itemView.findViewById(R.id.txtName);
        TxtTitle = itemView.findViewById(R.id.txtTitle);
        TxtArtist = itemView.findViewById(R.id.txtArtist);
    }

    public void setListener(AdapterEventListener callback) {
        ParentLayout.setOnClickListener(v -> callback.onClick(ViewHolderLoop.this));
    }
}
