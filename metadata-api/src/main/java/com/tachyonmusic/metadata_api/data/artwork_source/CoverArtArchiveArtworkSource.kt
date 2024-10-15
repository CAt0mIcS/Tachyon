package com.tachyonmusic.metadata_api.data.artwork_source

import com.ealva.brainzsvc.service.MusicBrainzService
import com.ealva.brainzsvc.service.getErrorString
import com.ealva.ealvabrainz.brainz.data.ReleaseGroupMbid
import com.ealva.ealvabrainz.brainz.data.ReleaseMbid
import com.github.michaelbull.result.get
import com.tachyonmusic.metadata_api.BuildConfig
import com.tachyonmusic.metadata_api.R
import com.tachyonmusic.metadata_api.domain.artwork_source.ArtworkSource
import com.tachyonmusic.metadata_api.domain.model.SearchInfo
import com.tachyonmusic.util.Resource
import com.tachyonmusic.util.UiText

internal val brainzModule = MusicBrainzService(
    appName = BuildConfig.LIBRARY_PACKAGE_NAME,
    appVersion = BuildConfig.VERSION_NAME,
    contactEmail = "c.simon.geier@gmail.com",
    addLoggingInterceptor = BuildConfig.DEBUG
)

class CoverArtArchiveArtworkSource : ArtworkSource() {
    companion object {
        const val SEARCH_URL = "www.coverartarchive.org/release(-group)"
    }


    override fun getSearchUrl(info: SearchInfo): Resource<String> {
        if (info.albumMbid == null)
            return Resource.Error(UiText.StringResource(R.string.invalid_mbid, "null"))

        return Resource.Success("$SEARCH_URL/${info.albumMbid}")
    }

    override suspend fun executeSearch(
        info: SearchInfo,
        imageSize: Int,
        pageSize: Int
    ): Resource<String> {
        var result = brainzModule.coverArtService.getReleaseArt(ReleaseMbid(info.albumMbid!!))
        var artworks = result.get()
        var front = artworks?.images?.find { it.front }
        if (front != null)
            return Resource.Success(front.image)

        result = brainzModule.coverArtService.getReleaseGroupArt(ReleaseGroupMbid(info.albumMbid))
        artworks = result.get()
        front = artworks?.images?.find { it.front }
        if (front != null)
            return Resource.Success(front.image)
        return Resource.Error(UiText.DynamicString(result.getErrorString()))
    }
}