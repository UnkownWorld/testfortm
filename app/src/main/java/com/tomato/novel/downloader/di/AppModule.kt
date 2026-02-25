package com.tomato.novel.downloader.di

import android.content.Context
import androidx.room.Room
import com.tomato.novel.downloader.data.local.AppDatabase
import com.tomato.novel.downloader.data.local.DownloadTaskDao
import com.tomato.novel.downloader.data.local.DownloadedChapterDao
import com.tomato.novel.downloader.data.remote.ApiConfig
import com.tomato.novel.downloader.data.remote.LocalApiService
import com.tomato.novel.downloader.data.remote.TomatoApiService
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit
import javax.inject.Singleton

/**
 * 数据库模块
 * 
 * 提供Room数据库相关依赖
 */
@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {
    
    @Provides
    @Singleton
    fun provideAppDatabase(
        @ApplicationContext context: Context
    ): AppDatabase {
        return Room.databaseBuilder(
            context,
            AppDatabase::class.java,
            "tomato_novel_db"
        )
            .fallbackToDestructiveMigration()
            .build()
    }
    
    @Provides
    fun provideDownloadTaskDao(database: AppDatabase): DownloadTaskDao {
        return database.downloadTaskDao()
    }
    
    @Provides
    fun provideDownloadedChapterDao(database: AppDatabase): DownloadedChapterDao {
        return database.downloadedChapterDao()
    }
}

/**
 * 网络模块
 * 
 * 提供Retrofit和OkHttp相关依赖
 */
@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {
    
    @Provides
    @Singleton
    fun provideOkHttpClient(): OkHttpClient {
        return OkHttpClient.Builder()
            .connectTimeout(ApiConfig.REQUEST_TIMEOUT, TimeUnit.SECONDS)
            .readTimeout(ApiConfig.REQUEST_TIMEOUT, TimeUnit.SECONDS)
            .writeTimeout(ApiConfig.REQUEST_TIMEOUT, TimeUnit.SECONDS)
            .addInterceptor(
                HttpLoggingInterceptor().apply {
                    level = HttpLoggingInterceptor.Level.BASIC
                }
            )
            .build()
    }
    
    @Provides
    @Singleton
    @TomatoRetrofit
    fun provideTomatoRetrofit(okHttpClient: OkHttpClient): Retrofit {
        return Retrofit.Builder()
            .baseUrl(ApiConfig.TOMATO_BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }
    
    @Provides
    @Singleton
    @LocalRetrofit
    fun provideLocalRetrofit(okHttpClient: OkHttpClient): Retrofit {
        return Retrofit.Builder()
            .baseUrl(ApiConfig.LOCAL_API_BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }
    
    @Provides
    @Singleton
    fun provideTomatoApiService(@TomatoRetrofit retrofit: Retrofit): TomatoApiService {
        return retrofit.create(TomatoApiService::class.java)
    }
    
    @Provides
    @Singleton
    fun provideLocalApiService(@LocalRetrofit retrofit: Retrofit): LocalApiService {
        return retrofit.create(LocalApiService::class.java)
    }
}

/**
 * Retrofit限定注解
 */
@javax.inject.Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class TomatoRetrofit

@javax.inject.Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class LocalRetrofit
