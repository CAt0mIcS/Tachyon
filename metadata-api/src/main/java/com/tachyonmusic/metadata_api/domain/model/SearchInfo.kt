package com.tachyonmusic.metadata_api.domain.model

data class SearchInfo(
    val title: String? = null,
    val artist: String? = null,
    val album: String? = null
)

internal fun SearchInfo?.isNullOrBlank() =
    this == null || (title == null && artist == null && album == null)