package com.tachyonmusic.metadata_api.domain.model

data class SearchInfo(
    val title: String? = null,
    val artist: String? = null,
    val album: String? = null,
    val mbid: String? = null
)