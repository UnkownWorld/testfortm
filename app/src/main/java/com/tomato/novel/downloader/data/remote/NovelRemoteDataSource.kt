package com.tomato.novel.downloader.data.remote

import com.tomato.novel.downloader.data.model.*
import com.tomato.novel.downloader.utils.NetworkUtils
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.jsoup.Jsoup
import javax.inject.Inject
import javax.inject.Singleton

/**
 * 小说数据远程数据源
 * 
 * 负责与番茄小说服务器进行网络通信
 * 处理HTML解析和API响应
 * 
 * @property apiService 番茄小说API服务
 * @property localApiService 本地API服务
 */
@Singleton
class NovelRemoteDataSource @Inject constructor(
    private val apiService: TomatoApiService,
    private val localApiService: LocalApiService
) {
    
    /**
     * 获取小说信息
     * 
     * 通过解析小说详情页HTML获取小说名称、作者、简介等信息
     * 
     * @param bookId 小说ID
     * @return 小说信息，失败时返回null
     */
    suspend fun getBookInfo(bookId: String): Result<BookInfo> = withContext(Dispatchers.IO) {
        try {
            val response = apiService.getBookPage(bookId)
            
            if (!response.isSuccessful) {
                return@withContext Result.failure(Exception("请求失败: ${response.code()}"))
            }
            
            val html = response.body() ?: return@withContext Result.failure(Exception("响应内容为空"))
            
            // 使用Jsoup解析HTML
            val doc = Jsoup.parse(html)
            
            // 提取小说名称
            val name = doc.selectFirst("h1")?.text() ?: "未知书名"
            
            // 提取作者名称
            val author = doc.selectFirst("div.author-name span.author-name-text")?.text() ?: "未知作者"
            
            // 提取简介
            val description = doc.selectFirst("div.page-abstract-content p")?.text() ?: "无简介"
            
            // 提取封面URL
            val coverUrl = doc.selectFirst("img.page-abstract-img")?.attr("src")
            
            val bookInfo = BookInfo(
                id = bookId,
                name = name,
                author = author,
                description = description,
                coverUrl = coverUrl
            )
            
            Result.success(bookInfo)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * 获取章节列表
     * 
     * 从番茄小说API获取小说的所有章节ID
     * 
     * @param bookId 小说ID
     * @return 章节ID列表
     */
    suspend fun getChapterList(bookId: String): Result<List<String>> = withContext(Dispatchers.IO) {
        try {
            val response = apiService.getChapterDirectory(bookId)
            
            if (!response.isSuccessful) {
                return@withContext Result.failure(Exception("请求失败: ${response.code()}"))
            }
            
            val chapterIds = response.body()?.data?.allItemIds ?: emptyList()
            
            Result.success(chapterIds)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * 批量获取章节内容
     * 
     * 通过本地API服务批量获取章节内容
     * 支持自动重试机制
     * 
     * @param chapterIds 章节ID列表
     * @return 章节内容映射表
     */
    suspend fun getBatchChapterContent(chapterIds: List<String>): Result<Map<String, ChapterContent>> = 
        withContext(Dispatchers.IO) {
            val results = mutableMapOf<String, ChapterContent>()
            val failedIds = mutableListOf<String>()
            
            // 分批处理
            chapterIds.chunked(ApiConfig.BATCH_SIZE).forEach { batch ->
                var retryCount = 0
                var success = false
                
                while (retryCount < ApiConfig.MAX_RETRIES && !success) {
                    try {
                        val response = localApiService.getBatchChapterContent(batch.joinToString(","))
                        
                        if (response.isSuccessful) {
                            response.body()?.let { results.putAll(it) }
                            success = true
                        } else {
                            retryCount++
                            if (retryCount < ApiConfig.MAX_RETRIES) {
                                Thread.sleep(ApiConfig.RETRY_DELAY)
                            }
                        }
                    } catch (e: Exception) {
                        retryCount++
                        if (retryCount < ApiConfig.MAX_RETRIES) {
                            Thread.sleep(ApiConfig.RETRY_DELAY)
                        }
                    }
                }
                
                if (!success) {
                    failedIds.addAll(batch)
                }
            }
            
            if (failedIds.isEmpty()) {
                Result.success(results)
            } else {
                Result.success(results) // 返回部分成功的结果
            }
        }
    
    /**
     * 获取章节标题
     * 
     * 批量获取章节的标题信息
     * 
     * @param chapterIds 章节ID列表
     * @return 章节ID到标题的映射
     */
    suspend fun getChapterTitles(chapterIds: List<String>): Result<Map<String, String>> = 
        withContext(Dispatchers.IO) {
            val result = getBatchChapterContent(chapterIds)
            
            result.map { contentMap ->
                contentMap.mapValues { (_, content) -> content.title }
            }
        }
}
