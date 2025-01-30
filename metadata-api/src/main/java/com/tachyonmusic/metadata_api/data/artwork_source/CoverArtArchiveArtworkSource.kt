package com.tachyonmusic.metadata_api.data.artwork_source

import com.ealva.brainzsvc.service.getErrorString
import com.ealva.ealvabrainz.brainz.data.Release
import com.ealva.ealvabrainz.brainz.data.ReleaseGroupMbid
import com.ealva.ealvabrainz.brainz.data.ReleaseMbid
import com.ealva.ealvabrainz.common.AlbumTitle
import com.ealva.ealvabrainz.common.ArtistName
import com.github.michaelbull.result.get
import com.github.michaelbull.result.getOrElse
import com.tachyonmusic.metadata_api.R
import com.tachyonmusic.metadata_api.di.brainzModule
import com.tachyonmusic.metadata_api.domain.artwork_source.ArtworkSource
import com.tachyonmusic.metadata_api.domain.model.SearchInfo
import com.tachyonmusic.util.Resource
import com.tachyonmusic.util.UiText

class CoverArtArchiveArtworkSource : ArtworkSource() {
    companion object {
        const val SEARCH_URL = "www.coverartarchive.org/release(-group)"
    }


    override fun getSearchUrl(info: SearchInfo): Resource<String> {
        if (info.album == null || info.artist == null)
            return Resource.Error(UiText.StringResource(R.string.invalid_mbid, "null"))

        return Resource.Success("$SEARCH_URL/${info.album}")
    }

    override suspend fun executeSearch(
        info: SearchInfo,
        imageSize: Int,
        pageSize: Int
    ): Resource<String> {
        val albumMbid = brainzModule.findRelease {
            release(AlbumTitle(info.album!!)) and
                    (artist(ArtistName(info.artist!!)) or artistName(ArtistName(info.artist)))
            status(Release.Status.Official)
        }.get()?.releases?.sortedBy { it.firstReleaseDate }?.firstOrNull()?.title
            ?: return Resource.Error(UiText.StringResource(R.string.album_not_found, info.album!!))


        var result = brainzModule.coverArtService.getReleaseArt(ReleaseMbid(albumMbid))
        var artworks = result.get()
        var front = artworks?.images?.find { it.front }
        if (front != null)
            return Resource.Success(front.image)

        result = brainzModule.coverArtService.getReleaseGroupArt(ReleaseGroupMbid(albumMbid))
        artworks = result.get()
        front = artworks?.images?.find { it.front }
        if (front != null)
            return Resource.Success(front.image)
        return Resource.Error(UiText.DynamicString(result.getErrorString()))
    }
}