package com.tachyonmusic.domain.use_case.home

import com.ealva.brainzsvc.service.MusicBrainzService
import com.ealva.ealvabrainz.brainz.data.Release
import com.ealva.ealvabrainz.brainz.data.mbid
import com.ealva.ealvabrainz.common.ArtistName
import com.ealva.ealvabrainz.common.Limit
import com.ealva.ealvabrainz.common.RecordingTitle
import com.github.michaelbull.result.get
import com.tachyonmusic.database.domain.model.SongEntity

class LoadUUIDForSongEntity(private val brainzModule: MusicBrainzService) {
    suspend operator fun invoke(entity: SongEntity): SongEntity? {
        val songs = brainzModule.findRecording(limit = Limit(25)) {
            recording(RecordingTitle(entity.title)) and (artist(ArtistName(entity.artist)) or artistName(
                ArtistName(entity.artist)
            ))

            status(Release.Status.Official)
        }.get()?.recordings?.sortedBy { it.firstReleaseDate.take(4).toIntOrNull() ?: Int.MAX_VALUE }

        val song = songs?.firstOrNull() ?: return null

        return entity.apply {
            mbid = song.mbid.value
        }
    }
}