package com.tomato.novel.downloader.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * 搜索历史实体类
 * 
 * 用于记录用户的搜索历史
 * 
 * @property id 记录唯一标识符
 * @property keyword 搜索关键词
 * @property bookId 小说ID（如果是有效的小说ID）
 * @property bookName 小说名称
 * @property searchTime 搜索时间
 */
@Entity(tableName = "search_history")
data class SearchHistoryEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    
    val keyword: String,
    
    val bookId: String? = null,
    
    val bookName: String? = null,
    
    val searchTime: Long = System.currentTimeMillis()
)

/**
 * 书架实体类
 * 
 * 用于记录用户收藏的小说
 * 
 * @property id 记录唯一标识符
 * @property bookId 小说ID
 * @property bookName 小说名称
 * @property author 作者
 * @property coverUrl 封面URL
 * @property description 简介
 * @property totalChapters 总章节数
 * @property lastReadChapter 最后阅读章节
 * @property lastReadTime 最后阅读时间
 * @property addTime 添加时间
 */
@Entity(
    tableName = "bookshelf",
    indices = [androidx.room.Index(value = ["book_id"], unique = true)]
)
data class BookshelfEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    
    @androidx.room.ColumnInfo(name = "book_id")
    val bookId: String,
    
    @androidx.room.ColumnInfo(name = "book_name")
    val bookName: String,
    
    val author: String,
    
    @androidx.room.ColumnInfo(name = "cover_url")
    val coverUrl: String? = null,
    
    val description: String? = null,
    
    @androidx.room.ColumnInfo(name = "total_chapters")
    val totalChapters: Int = 0,
    
    @androidx.room.ColumnInfo(name = "last_read_chapter")
    val lastReadChapter: Int = 0,
    
    @androidx.room.ColumnInfo(name = "last_read_time")
    val lastReadTime: Long = 0,
    
    @androidx.room.ColumnInfo(name = "add_time")
    val addTime: Long = System.currentTimeMillis()
)
