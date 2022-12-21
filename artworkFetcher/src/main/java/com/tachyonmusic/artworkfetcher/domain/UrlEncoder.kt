package com.tachyonmusic.artworkfetcher.domain

import com.tachyonmusic.util.Resource

interface UrlEncoder {
    fun encode(baseUrl: String, params: Map<String, String>): Resource<String>
}