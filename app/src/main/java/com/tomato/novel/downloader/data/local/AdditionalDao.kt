package com.tomato.novel.downloader.data.local

import androidx.room.*
import kotlinx.coroutines.flow.Flow

/**
 * 搜索历史数据访问对象
 */
@Dao
interface SearchHistoryDao {
    
    /**
     * 插入搜索历史
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(history: SearchHistoryEntity): Long
    
    /**
     * 获取所有搜索历史
     */
    @Query("SELECT * FROM search_history ORDER BY searchTime DESC")
    fun getAllHistory(): Flow<List<SearchHistoryEntity>>
    
    /**
     * 获取最近N条搜索历史
     */
    @Query("SELECT * FROM search_history ORDER BY searchTime DESC LIMIT :limit")
    fun getRecentHistory(limit: Int = 10): Flow<List<SearchHistoryEntity>>
    
    /**
     * 搜索关键词
     */
    @Query("SELECT * FROM search_history WHERE keyword LIKE :keyword ORDER BY searchTime DESC")
    fun searchHistory(keyword: String): Flow<List<SearchHistoryEntity>>
    
    /**
     * 删除指定搜索历史
     */
    @Delete
    suspend fun delete(history: SearchHistoryEntity)
    
    /**
     * 清空所有搜索历史
     */
    @Query("DELETE FROM search_history")
    suspend fun clearAll()
    
    /**
     * 删除重复记录，只保留最新的
     */
    @Query("DELETE FROM search_history WHERE id NOT IN (SELECT MAX(id) FROM search_history GROUP BY keyword)")
    suspend fun removeDuplicates()
}

/**
 * 书架数据访问对象
 */
@Dao
interface BookshelfDao {
    
    /**
     * 添加书籍到书架
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun addBook(book: BookshelfEntity): Long
    
    /**
     * 从书架移除书籍
     */
    @Delete
    suspend fun removeBook(book: BookshelfEntity)
    
    /**
     * 根据ID移除书籍
     */
    @Query("DELETE FROM bookshelf WHERE book_id = :bookId")
    suspend fun removeByBookId(bookId: String)
    
    /**
     * 获取书架所有书籍
     */
    @Query("SELECT * FROM bookshelf ORDER BY last_read_time DESC")
    fun getAllBooks(): Flow<List<BookshelfEntity>>
    
    /**
     * 获取指定书籍
     */
    @Query("SELECT * FROM bookshelf WHERE book_id = :bookId")
    suspend fun getBook(bookId: String): BookshelfEntity?
    
    /**
     * 检查书籍是否在书架中
     */
    @Query("SELECT EXISTS(SELECT 1 FROM bookshelf WHERE book_id = :bookId)")
    suspend fun isInBookshelf(bookId: String): Boolean
    
    /**
     * 更新阅读进度
     */
    @Query("UPDATE bookshelf SET last_read_chapter = :chapter, last_read_time = :time WHERE book_id = :bookId")
    suspend fun updateReadProgress(bookId: String, chapter: Int, time: Long = System.currentTimeMillis())
    
    /**
     * 搜索书架
     */
    @Query("SELECT * FROM bookshelf WHERE book_name LIKE :keyword OR author LIKE :keyword ORDER BY last_read_time DESC")
    fun searchBooks(keyword: String): Flow<List<BookshelfEntity>>
}
