package com.pulse.music.data.di

import android.content.Context
import androidx.media3.database.DatabaseProvider
import androidx.media3.database.StandaloneDatabaseProvider
import androidx.media3.datasource.cache.Cache
import androidx.media3.datasource.cache.NoOpCacheEvictor
import androidx.media3.datasource.cache.SimpleCache
import androidx.media3.exoplayer.offline.DownloadManager
import com.pulse.music.domain.repository.UserPreferencesRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import java.io.File
import java.util.concurrent.Executors
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DownloadModule {

    @Provides
    @Singleton
    @androidx.annotation.OptIn(androidx.media3.common.util.UnstableApi::class)
    fun provideDatabaseProvider(@ApplicationContext context: Context): DatabaseProvider {
        return StandaloneDatabaseProvider(context)
    }

    @Provides
    @Singleton
    @androidx.annotation.OptIn(androidx.media3.common.util.UnstableApi::class)
    fun provideDownloadCache(
        @ApplicationContext context: Context,
        databaseProvider: DatabaseProvider
    ): Cache {
        val downloadContentDirectory = File(context.getExternalFilesDir(null), "downloads")
        // Use NoOpCacheEvictor for downloads (they shouldn't be evicted automatically)
        return SimpleCache(downloadContentDirectory, NoOpCacheEvictor(), databaseProvider)
    }

    @Provides
    @Singleton
    @androidx.annotation.OptIn(androidx.media3.common.util.UnstableApi::class)
    fun provideDownloadManager(
        @ApplicationContext context: Context,
        databaseProvider: DatabaseProvider,
        cache: Cache
    ): DownloadManager {
        // We need a DataSource.Factory for downloads. 
        // Typically we use DefaultHttpDataSource.Factory for online content.
        val dataSourceFactory = androidx.media3.datasource.DefaultHttpDataSource.Factory()
        
        val downloadExecutor = Executors.newFixedThreadPool(6)
        
        return DownloadManager(
            context,
            databaseProvider,
            cache,
            dataSourceFactory,
            downloadExecutor
        ).apply {
            maxParallelDownloads = 3
        }
    }
}
