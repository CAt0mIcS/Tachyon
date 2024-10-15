package com.tachyonmusic.metadata_api.domain

import com.tachyonmusic.util.Resource

interface UrlEncoder {
    fun encode(baseUrl: String, params: Map<String, String>): Resource<String>
}