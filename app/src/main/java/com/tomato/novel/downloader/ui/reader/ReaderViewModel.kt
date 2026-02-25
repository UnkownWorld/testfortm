package com.tomato.novel.downloader.ui.reader

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tomato.novel.downloader.data.model.ChapterContent
import com.tomato.novel.downloader.data.repository.NovelRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * 阅读器ViewModel
 * 
 * 管理阅读器状态：
 * - 章节内容加载
 * - 阅读进度保存
 * - 上下章切换
 */
@HiltViewModel
class ReaderViewModel @Inject constructor(
    private val repository: NovelRepository
) : ViewModel() {

    // 章节标题
    private val _chapterTitle = MutableLiveData<String>()
    val chapterTitle: LiveData<String> = _chapterTitle

    // 章节内容（分段显示）
    private val _chapterContent = MutableLiveData<List<String>>()
    val chapterContent: LiveData<List<String>> = _chapterContent

    // 加载状态
    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading

    // 是否有上一章
    private val _hasPrevious = MutableLiveData<Boolean>()
    val hasPrevious: LiveData<Boolean> = _hasPrevious

    // 是否有下一章
    private val _hasNext = MutableLiveData<Boolean>()
    val hasNext: LiveData<Boolean> = _hasNext

    // 当前章节索引
    private var currentIndex = 0
    private var chapterIds: List<String> = emptyList()

    /**
     * 加载章节
     */
    fun loadChapter(chapterId: String) {
        viewModelScope.launch {
            _isLoading.value = true
            
            val result = repository.getChapterContents(listOf(chapterId))
            
            result.fold(
                onSuccess = { contentMap ->
                    contentMap[chapterId]?.let { chapter ->
                        _chapterTitle.value = chapter.title
                        _chapterContent.value = parseContent(chapter.content)
                    }
                },
                onFailure = {
                    _chapterTitle.value = "加载失败"
                    _chapterContent.value = listOf("无法加载章节内容")
                }
            )
            
            _isLoading.value = false
        }
    }

    /**
     * 设置章节列表
     */
    fun setChapterIds(ids: List<String>, currentIndex: Int) {
        this.chapterIds = ids
        this.currentIndex = currentIndex
        updateNavigationState()
    }

    /**
     * 上一章
     */
    fun previousChapter() {
        if (currentIndex > 0) {
            currentIndex--
            loadChapter(chapterIds[currentIndex])
            updateNavigationState()
        }
    }

    /**
     * 下一章
     */
    fun nextChapter() {
        if (currentIndex < chapterIds.size - 1) {
            currentIndex++
            loadChapter(chapterIds[currentIndex])
            updateNavigationState()
        }
    }

    /**
     * 更新导航状态
     */
    private fun updateNavigationState() {
        _hasPrevious.value = currentIndex > 0
        _hasNext.value = currentIndex < chapterIds.size - 1
    }

    /**
     * 解析内容为段落列表
     */
    private fun parseContent(content: String): List<String> {
        return content.split("\n")
            .map { it.trim() }
            .filter { it.isNotEmpty() }
    }
}
