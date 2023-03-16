package com.tachyonmusic.media.di

import android.app.Service
import androidx.media3.cast.CastPlayer
import com.google.android.gms.cast.framework.CastContext
import com.tachyonmusic.database.domain.repository.*
import com.tachyonmusic.logger.domain.Logger
import com.tachyonmusic.media.data.BrowserTree
import com.tachyonmusic.media.data.CustomPlayerImpl
import com.tachyonmusic.media.domain.CustomPlayer
import com.tachyonmusic.media.domain.use_case.*
import com.tachyonmusic.permission.domain.PermissionMapperRepository
import com.tachyonmusic.playback_layers.PlaybackRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ServiceComponent
import dagger.hilt.android.scopes.ServiceScoped
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(ServiceComponent::class)
class MediaPlaybackServiceRepositoryModule {

    @Provides
    @ServiceScoped
    fun provideCastContext(service: Service): CastContext =
        CastContext.getSharedInstance(service)

    @Provides
    @ServiceScoped
    fun provideBrowserTree(
        playbackRepository: PlaybackRepository,
        permissionMapperRepository: PermissionMapperRepository
    ): BrowserTree = BrowserTree(playbackRepository, permissionMapperRepository)

    @Provides
    @ServiceScoped
    fun provideCastPlayer(context: CastContext, log: Logger): CustomPlayer =
        CustomPlayerImpl(CastPlayer(context), log)
}


@Module
@InstallIn(SingletonComponent::class)
class MediaPlaybackUseCaseModule {

    @Provides
    @Singleton
    fun provideAddNewPlaybackToHistoryUseCase(
        historyRepository: HistoryRepository,
        settingsRepository: SettingsRepository
    ) = AddNewPlaybackToHistory(historyRepository, settingsRepository)

    @Provides
    @Singleton
    fun provideSaveRecentlyPlayedUseCase(dataRepository: DataRepository) =
        SaveRecentlyPlayed(dataRepository)

    @Provides
    @Singleton
    fun provideGetSettingsUseCase(settingsRepository: SettingsRepository) =
        GetSettings(settingsRepository)
}