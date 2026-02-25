package com.tomato.novel.downloader.data.local

import androidx.room.Database
import androidx.room.RoomDatabase

/**
 * 应用数据库
 * 
 * 使用Room持久化库管理本地数据存储
 * 包含下载任务和已下载章节两张表
 */
@Database(
    entities = [
        DownloadTaskEntity::class,
        DownloadedChapterEntity::class
    ],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    
    /**
     * 获取下载任务DAO
     */
    abstract fun downloadTaskDao(): DownloadTaskDao
    
    /**
     * 获取已下载章节DAO
     */
    abstract fun downloadedChapterDao(): DownloadedChapterDao
}
