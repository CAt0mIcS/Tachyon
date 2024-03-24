package com.tachyonmusic.presentation.player.data

import com.tachyonmusic.database.domain.model.SettingsEntity
import com.tachyonmusic.util.Duration

data class SeekIncrements(
    var forward: Duration = SettingsEntity().seekForwardIncrement,
    var back: Duration = SettingsEntity().seekBackIncrement
)