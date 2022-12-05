package com.daton.artworkdownloader.domain

import com.tachyonmusic.util.Resource

interface UrlEncoder {
    fun encode(baseUrl: String, params: Map<String, String>): Resource<String>
}