package com.tachyonmusic.metadata_api.di

import com.ealva.brainzsvc.service.MusicBrainzService
import com.tachyonmusic.metadata_api.BuildConfig

// TODO: Shouldn't be global and needs wrapper
val brainzModule = MusicBrainzService(
    appName = BuildConfig.LIBRARY_PACKAGE_NAME,
    appVersion = BuildConfig.VERSION_NAME,
    contactEmail = "c.simon.geier@gmail.com",
    addLoggingInterceptor = BuildConfig.DEBUG
)