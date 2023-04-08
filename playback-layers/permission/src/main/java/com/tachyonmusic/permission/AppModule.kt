package com.tachyonmusic.permission

import android.content.Context
import com.tachyonmusic.database.domain.repository.HistoryRepository
import com.tachyonmusic.database.domain.repository.LoopRepository
import com.tachyonmusic.database.domain.repository.PlaylistRepository
import com.tachyonmusic.database.domain.repository.SongRepository
import com.tachyonmusic.permission.data.PermissionMapperRepositoryImpl
import com.tachyonmusic.permission.data.UriPermissionRepositoryImpl
import com.tachyonmusic.permission.domain.PermissionMapperRepository
import com.tachyonmusic.permission.domain.UriPermissionRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
class AppModule {
    @Provides
    @Singleton
    fun providePermissionMapperRepository(
        uriPermissionRepository: UriPermissionRepository,
        songRepository: SongRepository,
        loopRepository: LoopRepository,
        playlistRepository: PlaylistRepository,
        historyRepository: HistoryRepository,
        @ApplicationContext context: Context
    ): PermissionMapperRepository = PermissionMapperRepositoryImpl(
        uriPermissionRepository,
        songRepository,
        loopRepository,
        playlistRepository,
        historyRepository,
        context
    )

    @Provides
    @Singleton
    fun provideUriPermissionRepository(@ApplicationContext context: Context): UriPermissionRepository =
        UriPermissionRepositoryImpl(context)
}