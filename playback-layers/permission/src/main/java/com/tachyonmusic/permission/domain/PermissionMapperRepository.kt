package com.tachyonmusic.permission.domain

import com.tachyonmusic.permission.domain.model.LoopPermissionEntity
import com.tachyonmusic.permission.domain.model.PlaylistPermissionEntity
import com.tachyonmusic.permission.domain.model.SinglePlaybackPermissionEntity
import com.tachyonmusic.permission.domain.model.SongPermissionEntity
import kotlinx.coroutines.flow.Flow

interface PermissionMapperRepository {

    val songFlow: Flow<List<SongPermissionEntity>>
    val loopFlow: Flow<List<LoopPermissionEntity>>
    val playlistFlow: Flow<List<PlaylistPermissionEntity>>

    val historyFlow: Flow<List<SinglePlaybackPermissionEntity>>


    suspend fun getSongs(): List<SongPermissionEntity>
    suspend fun getLoops(): List<LoopPermissionEntity>
    suspend fun getPlaylists(): List<PlaylistPermissionEntity>

    suspend fun getHistory(): List<SinglePlaybackPermissionEntity>
}