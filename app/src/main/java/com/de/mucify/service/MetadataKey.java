package com.de.mucify.service;

import android.support.v4.media.MediaMetadataCompat;

public class MetadataKey {
    public static final String Duration = MediaMetadataCompat.METADATA_KEY_DURATION;
    public static final String Title = MediaMetadataCompat.METADATA_KEY_TITLE;
    public static final String Artist = MediaMetadataCompat.METADATA_KEY_ARTIST;
    public static final String AlbumArt = MediaMetadataCompat.METADATA_KEY_ALBUM_ART;
    public static final String StartPos = "com.de.mucify.START_POS";
    public static final String EndPos = "com.de.mucify.END_POS";
    public static final String MediaId = "com.de.mucify.MEDIA_ID";

    public static final String SongInPlaylistMediaId = "com.de.mucify.SONG_IN_PLAYLIST_MEDIA_ID";
    public static final String PlaylistName = "com.de.mucify.PLAYLIST_NAME";
    public static final String SongCountInPlaylist = "com.de.mucify.SONG_COUNT_IN_PLAYLIST";
    public static final String TotalPlaylistLength = "com.de.mucify.TOTAL_PLAYLIST_LENGTH";
}
