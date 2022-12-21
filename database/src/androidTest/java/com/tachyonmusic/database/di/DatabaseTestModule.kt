package com.tachyonmusic.database.di

import android.app.Application
import androidx.room.Room
import com.tachyonmusic.database.data.data_source.Database
import com.tachyonmusic.database.data.data_source.room.RoomDatabase
import dagger.Module
import dagger.Provides
import dagger.hilt.components.SingletonComponent
import dagger.hilt.testing.TestInstallIn
import javax.inject.Singleton

@Module
@TestInstallIn(components = [SingletonComponent::class], replaces = [DatabaseModule::class])
class DatabaseTestModule {
    @Provides
    @Singleton
    fun provideDatabase(app: Application): Database = Room.inMemoryDatabaseBuilder(
        app,
        RoomDatabase::class.java,
    ).build()
}