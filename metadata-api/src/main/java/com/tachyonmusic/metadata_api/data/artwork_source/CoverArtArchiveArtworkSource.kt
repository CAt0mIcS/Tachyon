package com.tachyonmusic.metadata_api.data.artwork_source

import com.tachyonmusic.metadata_api.R
import com.tachyonmusic.metadata_api.domain.artwork_source.ArtworkSource
import com.tachyonmusic.metadata_api.domain.model.SearchInfo
import com.tachyonmusic.util.Resource
import com.tachyonmusic.util.UiText

class CoverArtArchiveArtworkSource : ArtworkSource() {
    companion object {
        const val SEARCH_URL = "www.coverartarchive.org/release-group"
    }


    override fun getSearchUrl(info: SearchInfo): Resource<String> {
        if(info.mbid == null)
            return Resource.Error(UiText.StringResource(R.string.invalid_mbid, "null"))

        return Resource.Success("$SEARCH_URL/${info.mbid}")
    }

    override fun executeSearch(url: String, imageSize: Int, pageSize: Int): Resource<String> {


    }
}