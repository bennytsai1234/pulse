package com.pulse.music.data.di

import com.pulse.music.data.network.LastFmApi
import com.pulse.music.data.network.LrcLibApi
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit
import javax.inject.Qualifier
import javax.inject.Singleton
import com.pulse.music.domain.repository.UserPreferencesRepository
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking

@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class LrcLibRetrofit

@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class LastFmRetrofit

@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {

    @Provides
    @Singleton
    fun provideOkHttpClient(
        userPreferencesRepository: UserPreferencesRepository
    ): OkHttpClient {
        // Warning: This creates a client with fixed settings at startup.
        // Dynamic updates require creating new clients or custom interceptors.
        // For simplicity in MVP, we read initial values.
        val connectTimeout = runBlocking { 
            try { userPreferencesRepository.connectTimeout.first() } catch (e: Exception) { 10000L }
        }
        val readTimeout = runBlocking { 
            try { userPreferencesRepository.readTimeout.first() } catch (e: Exception) { 10000L }
        }
        val userAgent = runBlocking { 
            try { userPreferencesRepository.userAgent.first() } catch (e: Exception) { "Pulse Music Player" }
        }

        val logging = HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BODY
        }
        
        return OkHttpClient.Builder()
            .addInterceptor(logging)
            .addInterceptor { chain ->
                val request = chain.request().newBuilder()
                    .header("User-Agent", userAgent)
                    .build()
                chain.proceed(request)
            }
            .connectTimeout(connectTimeout, TimeUnit.MILLISECONDS)
            .readTimeout(readTimeout, TimeUnit.MILLISECONDS)
            .build()
    }

    @Provides
    @Singleton
    @LrcLibRetrofit
    fun provideLrcLibRetrofit(okHttpClient: OkHttpClient): Retrofit {
        return Retrofit.Builder()
            .baseUrl("https://lrclib.net/")
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }
    
    @Provides
    @Singleton
    @LastFmRetrofit
    fun provideLastFmRetrofit(okHttpClient: OkHttpClient): Retrofit {
        return Retrofit.Builder()
            .baseUrl("https://ws.audioscrobbler.com/")
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    @Provides
    @Singleton
    fun provideLrcLibApi(@LrcLibRetrofit retrofit: Retrofit): LrcLibApi {
        return retrofit.create(LrcLibApi::class.java)
    }
    
    @Provides
    @Singleton
    fun provideLastFmApi(@LastFmRetrofit retrofit: Retrofit): LastFmApi {
        return retrofit.create(LastFmApi::class.java)
    }

    @Provides
    @Singleton
    fun provideWebDavDataSource(okHttpClient: OkHttpClient): com.pulse.music.data.source.WebDavDataSource {
        return com.pulse.music.data.source.WebDavDataSource(okHttpClient)
    }
}

