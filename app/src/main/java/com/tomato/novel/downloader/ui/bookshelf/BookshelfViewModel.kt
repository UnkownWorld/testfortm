package com.tomato.novel.downloader.ui.bookshelf

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tomato.novel.downloader.data.local.BookshelfEntity
import com.tomato.novel.downloader.data.repository.BookshelfRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * 书架页面ViewModel
 * 
 * 管理书架状态：
 * - 书籍列表
 * - 搜索功能
 * - 添加/移除书籍
 */
@HiltViewModel
class BookshelfViewModel @Inject constructor(
    private val repository: BookshelfRepository
) : ViewModel() {

    // 书架列表
    private val _books = MutableLiveData<List<BookshelfEntity>>()
    val books: LiveData<List<BookshelfEntity>> = _books

    // 搜索结果
    private val _searchResults = MutableLiveData<List<BookshelfEntity>>()
    val searchResults: LiveData<List<BookshelfEntity>> = _searchResults

    // 是否在搜索
    private val _isSearching = MutableLiveData<Boolean>()
    val isSearching: LiveData<Boolean> = _isSearching

    // 消息
    private val _message = MutableLiveData<String?>()
    val message: LiveData<String?> = _message

    init {
        loadBooks()
    }

    /**
     * 加载书架
     */
    private fun loadBooks() {
        viewModelScope.launch {
            repository.getAllBooks().collectLatest { bookList ->
                _books.value = bookList
            }
        }
    }

    /**
     * 搜索书架
     */
    fun search(keyword: String) {
        if (keyword.isBlank()) {
            _isSearching.value = false
            return
        }

        viewModelScope.launch {
            _isSearching.value = true
            repository.searchBooks(keyword).collectLatest { results ->
                _searchResults.value = results
            }
        }
    }

    /**
     * 清除搜索
     */
    fun clearSearch() {
        _isSearching.value = false
        _searchResults.value = emptyList()
    }

    /**
     * 移除书籍
     */
    fun removeBook(book: BookshelfEntity) {
        viewModelScope.launch {
            repository.removeBook(book)
            _message.value = "已从书架移除"
        }
    }

    /**
     * 更新阅读进度
     */
    fun updateReadProgress(bookId: String, chapter: Int) {
        viewModelScope.launch {
            repository.updateReadProgress(bookId, chapter)
        }
    }

    /**
     * 清除消息
     */
    fun clearMessage() {
        _message.value = null
    }
}
