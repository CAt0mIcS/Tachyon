package com.tachyonmusic.media.domain.use_case

data class ServiceUseCases(
    val loadPlaylistForPlayback: LoadPlaylistForPlayback,
    val confirmAddedMediaItems: ConfirmAddedMediaItems,
    val preparePlayer: PreparePlayer,
    val getSupportedCommands: GetSupportedCommands,
    val updateTimingDataOfCurrentPlayback: UpdateTimingDataOfCurrentPlayback,
    val addNewPlaybackToHistory: AddNewPlaybackToHistory
)