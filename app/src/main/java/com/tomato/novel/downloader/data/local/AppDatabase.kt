package com.tomato.novel.downloader.data.local

import androidx.room.Database
import androidx.room.RoomDatabase

/**
 * 应用数据库
 * 
 * 使用Room持久化库管理本地数据存储
 * 包含以下表：
 * - download_tasks: 下载任务
 * - downloaded_chapters: 已下载章节
 * - search_history: 搜索历史
 * - bookshelf: 书架
 */
@Database(
    entities = [
        DownloadTaskEntity::class,
        DownloadedChapterEntity::class,
        SearchHistoryEntity::class,
        BookshelfEntity::class
    ],
    version = 2,
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
    
    /**
     * 获取搜索历史DAO
     */
    abstract fun searchHistoryDao(): SearchHistoryDao
    
    /**
     * 获取书架DAO
     */
    abstract fun bookshelfDao(): BookshelfDao
}
