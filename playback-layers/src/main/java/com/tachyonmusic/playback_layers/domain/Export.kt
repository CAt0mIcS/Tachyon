package com.tachyonmusic.playback_layers.domain

import com.tachyonmusic.artwork.domain.ArtworkMapperRepository

/**
 * [PlaybackRepository] will always be equal to the last repository in the chain. It should only be
 * injected if the flows for playbacks or the getters for playbacks need to be called as things like
 * [ArtworkMapperRepository.triggerPlaybackReload] won't be accessible anymore through [PlaybackRepository]
 * typealias once another repository is added to the chain after the [ArtworkMapperRepository]
 */
typealias PlaybackRepository = ArtworkMapperRepository