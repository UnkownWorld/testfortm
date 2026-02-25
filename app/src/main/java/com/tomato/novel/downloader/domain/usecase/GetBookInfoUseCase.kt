package com.tomato.novel.downloader.domain.usecase

import com.tomato.novel.downloader.data.model.BookInfo
import com.tomato.novel.downloader.data.model.ChapterInfo
import com.tomato.novel.downloader.data.repository.NovelRepository
import javax.inject.Inject

/**
 * 获取小说信息用例
 * 
 * 封装获取小说信息的业务逻辑
 * 
 * @property repository 小说仓库
 */
class GetBookInfoUseCase @Inject constructor(
    private val repository: NovelRepository
) {
    /**
     * 执行用例
     * 
     * @param bookId 小说ID
     * @return 小说信息结果
     */
    suspend operator fun invoke(bookId: String): Result<BookInfo> {
        // 验证小说ID格式
        if (bookId.isBlank()) {
            return Result.failure(IllegalArgumentException("小说ID不能为空"))
        }
        
        // 验证小说ID是否为纯数字
        if (!bookId.all { it.isDigit() }) {
            return Result.failure(IllegalArgumentException("小说ID格式不正确"))
        }
        
        return repository.getBookInfo(bookId)
    }
}

/**
 * 获取章节列表用例
 * 
 * 封装获取章节列表的业务逻辑
 * 
 * @property repository 小说仓库
 */
class GetChapterListUseCase @Inject constructor(
    private val repository: NovelRepository
) {
    /**
     * 执行用例
     * 
     * @param bookId 小说ID
     * @return 章节列表结果
     */
    suspend operator fun invoke(bookId: String): Result<List<ChapterInfo>> {
        if (bookId.isBlank()) {
            return Result.failure(IllegalArgumentException("小说ID不能为空"))
        }
        
        return repository.getChapterList(bookId)
    }
}

/**
 * 检查小说ID是否有效用例
 * 
 * 验证小说ID格式并检查是否可以获取到小说信息
 * 
 * @property getBookInfoUseCase 获取小说信息用例
 */
class ValidateBookIdUseCase @Inject constructor(
    private val getBookInfoUseCase: GetBookInfoUseCase
) {
    /**
     * 执行用例
     * 
     * @param bookId 小说ID
     * @return 验证结果，成功返回小说信息
     */
    suspend operator fun invoke(bookId: String): Result<BookInfo> {
        // 清理输入
        val cleanId = bookId.trim()
        
        // 尝试从URL中提取ID
        val extractedId = extractBookIdFromUrl(cleanId) ?: cleanId
        
        return getBookInfoUseCase(extractedId)
    }
    
    /**
     * 从URL中提取小说ID
     * 
     * 支持格式：
     * - https://fanqienovel.com/page/7143038691944959011
     * - 7143038691944959011
     */
    private fun extractBookIdFromUrl(input: String): String? {
        // 匹配番茄小说URL格式
        val regex = """fanqienovel\.com/page/(\d+)""".toRegex()
        return regex.find(input)?.groupValues?.getOrNull(1)
    }
}
