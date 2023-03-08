package com.tachyonmusic.permission.domain

import com.tachyonmusic.permission.domain.model.LoopPermissionEntity
import com.tachyonmusic.permission.domain.model.PlaylistPermissionEntity
import com.tachyonmusic.permission.domain.model.SinglePlaybackPermissionEntity
import com.tachyonmusic.permission.domain.model.SongPermissionEntity
import kotlinx.coroutines.flow.Flow

interface PermissionMapperRepository {

    val songs: Flow<List<SongPermissionEntity>>
    val loops: Flow<List<LoopPermissionEntity>>
    val playlists: Flow<List<PlaylistPermissionEntity>>

    val history: Flow<List<SinglePlaybackPermissionEntity>>
}