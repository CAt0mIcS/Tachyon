package com.tachyonmusic.sort

import com.tachyonmusic.permission.domain.PermissionMapperRepository
import com.tachyonmusic.sort.data.SortedPlaybackRepositoryImpl
import com.tachyonmusic.sort.domain.SortedPlaybackRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
class AppModule {

    @Provides
    @Singleton
    fun provideSortedPlaybackRepository(permissionMapperRepository: PermissionMapperRepository): SortedPlaybackRepository =
        SortedPlaybackRepositoryImpl(permissionMapperRepository)
}