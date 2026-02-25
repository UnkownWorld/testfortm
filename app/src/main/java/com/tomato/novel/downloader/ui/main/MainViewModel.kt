package com.tomato.novel.downloader.ui.main

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tomato.novel.downloader.data.model.BookInfo
import com.tomato.novel.downloader.data.model.ChapterInfo
import com.tomato.novel.downloader.domain.model.DownloadRequest
import com.tomato.novel.downloader.domain.model.DownloadTask
import com.tomato.novel.downloader.domain.usecase.*
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * 主界面ViewModel
 * 
 * 负责管理主界面的数据和业务逻辑
 * 采用单向数据流架构
 * 
 * @property validateBookIdUseCase 验证小说ID用例
 * @property getChapterListUseCase 获取章节列表用例
 * @property createDownloadTaskUseCase 创建下载任务用例
 * @property getDownloadTasksUseCase 获取下载任务列表用例
 * @property updateDownloadStatusUseCase 更新下载状态用例
 * @property deleteDownloadTaskUseCase 删除下载任务用例
 */
@HiltViewModel
class MainViewModel @Inject constructor(
    private val validateBookIdUseCase: ValidateBookIdUseCase,
    private val getChapterListUseCase: GetChapterListUseCase,
    private val createDownloadTaskUseCase: CreateDownloadTaskUseCase,
    private val getDownloadTasksUseCase: GetDownloadTasksUseCase,
    private val updateDownloadStatusUseCase: UpdateDownloadStatusUseCase,
    private val deleteDownloadTaskUseCase: DeleteDownloadTaskUseCase
) : ViewModel() {
    
    // UI状态
    private val _uiState = MutableLiveData<MainUiState>()
    val uiState: LiveData<MainUiState> = _uiState
    
    // 小说信息
    private val _bookInfo = MutableLiveData<BookInfo?>()
    val bookInfo: LiveData<BookInfo?> = _bookInfo
    
    // 章节列表
    private val _chapterList = MutableLiveData<List<ChapterInfo>>()
    val chapterList: LiveData<List<ChapterInfo>> = _chapterList
    
    // 下载任务列表
    private val _downloadTasks = MutableLiveData<List<DownloadTask>>()
    val downloadTasks: LiveData<List<DownloadTask>> = _downloadTasks
    
    // 错误信息
    private val _errorMessage = MutableLiveData<String?>()
    val errorMessage: LiveData<String?> = _errorMessage
    
    init {
        loadDownloadTasks()
    }
    
    /**
     * 搜索小说
     * 
     * @param input 用户输入（小说ID或URL）
     */
    fun searchBook(input: String) {
        viewModelScope.launch {
            _uiState.value = MainUiState.Loading
            
            val result = validateBookIdUseCase(input)
            
            result.fold(
                onSuccess = { book ->
                    _bookInfo.value = book
                    _uiState.value = MainUiState.BookLoaded
                    
                    // 自动获取章节列表
                    loadChapterList(book.id)
                },
                onFailure = { error ->
                    _errorMessage.value = error.message ?: "获取小说信息失败"
                    _uiState.value = MainUiState.Error
                }
            )
        }
    }
    
    /**
     * 加载章节列表
     */
    private fun loadChapterList(bookId: String) {
        viewModelScope.launch {
            val result = getChapterListUseCase(bookId)
            
            result.fold(
                onSuccess = { chapters ->
                    _chapterList.value = chapters
                },
                onFailure = {
                    // 章节列表获取失败不影响显示基本信息
                }
            )
        }
    }
    
    /**
     * 开始下载
     * 
     * @param format 输出格式
     * @param startChapter 起始章节（可选）
     * @param endChapter 结束章节（可选）
     */
    fun startDownload(
        format: String = "txt",
        startChapter: Int? = null,
        endChapter: Int? = null
    ) {
        val book = _bookInfo.value ?: return
        val chapters = _chapterList.value ?: return
        
        viewModelScope.launch {
            val request = DownloadRequest(
                bookId = book.id,
                bookName = book.name,
                author = book.author,
                totalChapters = chapters.size,
                startChapter = startChapter,
                endChapter = endChapter,
                format = format
            )
            
            val result = createDownloadTaskUseCase(request)
            
            result.fold(
                onSuccess = { taskId ->
                    _uiState.value = MainUiState.DownloadStarted(taskId)
                    // 重置状态
                    _bookInfo.value = null
                    _chapterList.value = emptyList()
                },
                onFailure = { error ->
                    _errorMessage.value = error.message ?: "创建下载任务失败"
                }
            )
        }
    }
    
    /**
     * 加载下载任务列表
     */
    private fun loadDownloadTasks() {
        viewModelScope.launch {
            getDownloadTasksUseCase.getAll().collectLatest { tasks ->
                _downloadTasks.value = tasks
            }
        }
    }
    
    /**
     * 暂停下载
     */
    fun pauseDownload(taskId: Long) {
        viewModelScope.launch {
            updateDownloadStatusUseCase.pause(taskId)
        }
    }
    
    /**
     * 恢复下载
     */
    fun resumeDownload(taskId: Long) {
        viewModelScope.launch {
            updateDownloadStatusUseCase.resume(taskId)
        }
    }
    
    /**
     * 取消下载
     */
    fun cancelDownload(taskId: Long) {
        viewModelScope.launch {
            updateDownloadStatusUseCase.cancel(taskId)
        }
    }
    
    /**
     * 删除下载任务
     */
    fun deleteDownloadTask(taskId: Long) {
        viewModelScope.launch {
            deleteDownloadTaskUseCase(taskId)
        }
    }
    
    /**
     * 清除错误信息
     */
    fun clearError() {
        _errorMessage.value = null
    }
    
    /**
     * 重置状态
     */
    fun resetState() {
        _bookInfo.value = null
        _chapterList.value = emptyList()
        _uiState.value = MainUiState.Idle
    }
}

/**
 * 主界面UI状态
 */
sealed class MainUiState {
    object Idle : MainUiState()
    object Loading : MainUiState()
    object BookLoaded : MainUiState()
    object Error : MainUiState()
    data class DownloadStarted(val taskId: Long) : MainUiState()
}
