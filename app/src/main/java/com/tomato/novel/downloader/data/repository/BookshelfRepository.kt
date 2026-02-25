package com.tomato.novel.downloader.data.repository

import com.tomato.novel.downloader.data.local.BookshelfDao
import com.tomato.novel.downloader.data.local.BookshelfEntity
import com.tomato.novel.downloader.data.local.SearchHistoryDao
import com.tomato.novel.downloader.data.local.SearchHistoryEntity
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

/**
 * 书架仓库
 * 
 * 管理书架和搜索历史数据
 */
@Singleton
class BookshelfRepository @Inject constructor(
    private val bookshelfDao: BookshelfDao,
    private val searchHistoryDao: SearchHistoryDao
) {
    /**
     * 获取所有书籍
     */
    fun getAllBooks(): Flow<List<BookshelfEntity>> {
        return bookshelfDao.getAllBooks()
    }

    /**
     * 添加书籍到书架
     */
    suspend fun addBook(book: BookshelfEntity): Long {
        return bookshelfDao.addBook(book)
    }

    /**
     * 从书架移除书籍
     */
    suspend fun removeBook(book: BookshelfEntity) {
        bookshelfDao.removeBook(book)
    }

    /**
     * 检查书籍是否在书架中
     */
    suspend fun isInBookshelf(bookId: String): Boolean {
        return bookshelfDao.isInBookshelf(bookId)
    }

    /**
     * 更新阅读进度
     */
    suspend fun updateReadProgress(bookId: String, chapter: Int) {
        bookshelfDao.updateReadProgress(bookId, chapter)
    }

    /**
     * 搜索书架
     */
    fun searchBooks(keyword: String): Flow<List<BookshelfEntity>> {
        return bookshelfDao.searchBooks("%$keyword%")
    }

    // ==================== 搜索历史 ====================

    /**
     * 添加搜索历史
     */
    suspend fun addSearchHistory(
        keyword: String,
        bookId: String? = null,
        bookName: String? = null
    ): Long {
        return searchHistoryDao.insert(
            SearchHistoryEntity(
                keyword = keyword,
                bookId = bookId,
                bookName = bookName
            )
        )
    }

    /**
     * 获取搜索历史
     */
    fun getSearchHistory(limit: Int = 10): Flow<List<SearchHistoryEntity>> {
        return searchHistoryDao.getRecentHistory(limit)
    }

    /**
     * 清空搜索历史
     */
    suspend fun clearSearchHistory() {
        searchHistoryDao.clearAll()
    }

    /**
     * 删除单条搜索历史
     */
    suspend fun deleteSearchHistory(history: SearchHistoryEntity) {
        searchHistoryDao.delete(history)
    }
}
