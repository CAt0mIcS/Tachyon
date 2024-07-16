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
    suspend operator fun invoke(
        mediaId: MediaId,
        oldTitle: String,
        newTitle: String?,
        oldArtist: String,
        newArtist: String?,
        oldName: String?,
        newName: String?,
        oldAlbum: String?,
        newAlbum: String?
    ) =
        withContext(Dispatchers.IO) {
            when (mediaId.playbackType) {
                is PlaybackType.Song -> songRepository.updateMetadata(
                    mediaId,
                    newTitle?: oldTitle,
                    newArtist ?:oldArtist,
                    newAlbum ?: oldAlbum
                )

                is PlaybackType.CustomizedSong.Local -> {
                    if (newName != null)
                        customizedSongRepository.updateMetadata(
                            mediaId,
                            MediaId.ofLocalCustomizedSong(newName, mediaId.underlyingMediaId!!)
                        )

                    if (newTitle != null || newArtist != null || newAlbum != null) {
                        songRepository.updateMetadata(
                            mediaId.underlyingMediaId!!,
                            newTitle ?:oldTitle,
                            newArtist ?: oldArtist,
                            newAlbum ?: oldAlbum
                        )
                    }
                }

                is PlaybackType.Playlist.Local -> playlistRepository.updateMetadata(
                    mediaId,
                    newName ?: return@withContext,
                    MediaId.ofLocalPlaylist(newName),
                )

                is PlaybackType.Ad -> {}
            }
        }
}