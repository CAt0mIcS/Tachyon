
package com.daton.media.ext

/**
 * Does the top-level media id point to a song
 */
inline val String.isSongMediaId: Boolean
    get() = contains("Song_")

/**
 * Does the top-level media id point to a loop
 */
inline val String.isLoopMediaId: Boolean
    get() = contains("Loop_")

/**
 * Does the top-level media id point to a playlist
 */
inline val String.isPlaylistMediaId: Boolean
    get() = contains("Playlist_")


/**
 * If we want to pass the starting media item of e.g. a playlist, we'll add it to the end of the playlist
 * media item in parenthesis.
 * For Example:
 *      Playlist_Test1.playlist*This is my beautiful song.mp3**
 *  Where playlist 'Test1' would be loaded and 'This is my beautiful song.mp3' would be played
 *
 * If the media id is a song/loop media id, its underlying playback will be the song/loop itself
 */
inline val String.hasUnderlyingPlayback: Boolean
    get() = isSongMediaId || isLoopMediaId || (contains('*') && contains("**"))


/**
 * Gets the underlying playback as described in [hasUnderlyingPlayback]. Throws if it doesn't have
 * an underlying playback
 */
inline val String.underlyingPlayback: String
    get() {
        if (!hasUnderlyingPlayback)
            throw UnsupportedOperationException("No loop/playlist media id or underlying playback found")

        return if (isSongMediaId || isLoopMediaId) this else substring(
            indexOf('*') + 1,
            indexOf("**")
        )
    }

/**
 * Gets the base playback. Meaning the media id without any underlying playback attached. See [hasUnderlyingPlayback]
 * For song/loops/playlists, the path to the song/loop/playlist will be returned
 */
inline val String.basePlayback: String
    get() {
        return if (isSongMediaId || isLoopMediaId)
            this
        else {
            if (hasUnderlyingPlayback)
                substring(0, indexOf('*'))
            else
                this
        }
    }


object MediaId {
    /**
     * Builds a string describing the song to play in the playlist
     * @param rawPlaylistMediaId The media id of the playlist without any underlying playback attached
     * @param playbackToPlayPath The path to the song/loop to play in the playlist defined by the [rawPlaylistMediaId]
     */
    fun buildPlaylistMediaId(rawPlaylistMediaId: String, playbackToPlayPath: String): String {
        return "$rawPlaylistMediaId*$playbackToPlayPath**"
    }
}




