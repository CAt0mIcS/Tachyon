package com.tachyonmusic.domain.use_case.library

import com.tachyonmusic.core.data.constants.PlaybackType
import com.tachyonmusic.core.domain.MediaId
import com.tachyonmusic.database.domain.repository.CustomizedSongRepository
import com.tachyonmusic.database.domain.repository.PlaylistRepository
import com.tachyonmusic.database.domain.repository.SongRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class UpdatePlaybackMetadata(
    private val songRepository: SongRepository,
    private val customizedSongRepository: CustomizedSongRepository,
    private val playlistRepository: PlaylistRepository
) {
    suspend operator fun invoke(mediaId: MediaId, title: String?, artist: String?, name: String?) =
        withContext(Dispatchers.IO) {
            when (mediaId.playbackType) {
                is PlaybackType.Song -> songRepository.updateMetadata(
                    mediaId,
                    title ?: return@withContext,
                    artist ?: return@withContext
                )

                is PlaybackType.CustomizedSong.Local -> {
                    if (name != null)
                        customizedSongRepository.updateMetadata(
                            mediaId,
                            MediaId.ofLocalCustomizedSong(name, mediaId.underlyingMediaId!!)
                        )

                    if (title != null && artist != null) {
                        songRepository.updateMetadata(
                            mediaId.underlyingMediaId!!,
                            title,
                            artist
                        )
                    }
                }

                is PlaybackType.Playlist.Local -> playlistRepository.updateMetadata(
                    mediaId,
                    name ?: return@withContext,
                    MediaId.ofLocalPlaylist(name),
                )
            }
        }
}