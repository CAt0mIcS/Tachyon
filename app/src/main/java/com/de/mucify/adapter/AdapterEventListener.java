package com.de.mucify.adapter;

public interface AdapterEventListener {
    void onClick(ViewHolderSong holder);
    void onClick(ViewHolderLoop holder);
    void onClick(ViewHolderPlaylist holder);
}