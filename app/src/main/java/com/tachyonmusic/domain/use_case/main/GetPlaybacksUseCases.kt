package com.tachyonmusic.domain.use_case.main

class GetPlaybacksUseCases(
    val songs: GetSongs,
    val loops: GetLoops,
    val playlists: GetPlaylists,
    val history: GetHistory
)