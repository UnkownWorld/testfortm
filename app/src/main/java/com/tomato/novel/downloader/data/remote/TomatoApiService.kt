package com.tomato.novel.downloader.data.remote

import com.tomato.novel.downloader.data.model.*
import retrofit2.Response
import retrofit2.http.*

/**
 * 番茄小说API服务接口
 * 
 * 定义与番茄小说服务器交互的API端点
 * 使用Retrofit进行网络请求
 */
interface TomatoApiService {
    
    /**
     * 获取小说章节目录
     * 
     * @param bookId 小说ID
     * @return 章节目录响应
     */
    @GET("api/reader/directory/detail")
    suspend fun getChapterDirectory(
        @Query("bookId") bookId: String
    ): Response<ChapterDirectoryResponse>
    
    /**
     * 获取小说详情页HTML
     * 用于解析小说名称、作者、简介等信息
     * 
     * @param bookId 小说ID
     * @return HTML页面内容
     */
    @GET("page/{bookId}")
    suspend fun getBookPage(
        @Path("bookId") bookId: String
    ): Response<String>
}

/**
 * 本地API服务接口
 * 
 * 用于与本地运行的官方API服务通信
 * 提供批量获取章节内容的能力
 */
interface LocalApiService {
    
    /**
     * 批量获取章节内容
     * 
     * @param itemIds 章节ID列表（逗号分隔）
     * @return 章节内容映射表
     */
    @GET("content")
    suspend fun getBatchChapterContent(
        @Query("item_ids") itemIds: String
    ): Response<BatchChapterContentResponse>
}

/**
 * API配置常量
 */
object ApiConfig {
    const val TOMATO_BASE_URL = "https://fanqienovel.com/"
    const val LOCAL_API_BASE_URL = "http://127.0.0.1:8080/"
    
    // 请求配置
    const val REQUEST_TIMEOUT = 15L
    const val BATCH_SIZE = 30
    const val MAX_RETRIES = 3
    const val RETRY_DELAY = 1000L
}
