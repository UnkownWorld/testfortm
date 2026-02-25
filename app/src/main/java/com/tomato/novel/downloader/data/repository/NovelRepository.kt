package com.tomato.novel.downloader.data.repository

import com.tomato.novel.downloader.data.local.*
import com.tomato.novel.downloader.data.model.*
import com.tomato.novel.downloader.data.remote.NovelRemoteDataSource
import com.tomato.novel.downloader.domain.model.DownloadTask
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

/**
 * 小说下载仓库
 * 
 * 协调远程数据源和本地数据源
 * 提供统一的数据访问接口
 * 实现单一数据源原则
 * 
 * @property remoteDataSource 远程数据源
 * @property downloadTaskDao 下载任务DAO
 * @property downloadedChapterDao 已下载章节DAO
 */
@Singleton
class NovelRepository @Inject constructor(
    private val remoteDataSource: NovelRemoteDataSource,
    private val downloadTaskDao: DownloadTaskDao,
    private val downloadedChapterDao: DownloadedChapterDao
) {
    
    /**
     * 获取小说信息
     * 
     * @param bookId 小说ID
     * @return 小说信息结果
     */
    suspend fun getBookInfo(bookId: String): Result<BookInfo> {
        return remoteDataSource.getBookInfo(bookId)
    }
    
    /**
     * 获取章节列表
     * 
     * @param bookId 小说ID
     * @return 章节信息列表
     */
    suspend fun getChapterList(bookId: String): Result<List<ChapterInfo>> {
        val result = remoteDataSource.getChapterList(bookId)
        
        return result.map { chapterIds ->
            chapterIds.mapIndexed { index, id ->
                ChapterInfo(
                    id = id,
                    title = "第${index + 1}章",
                    index = index
                )
            }
        }
    }
    
    /**
     * 获取章节内容
     * 
     * @param chapterIds 章节ID列表
     * @return 章节内容映射
     */
    suspend fun getChapterContents(chapterIds: List<String>): Result<Map<String, ChapterContent>> {
        return remoteDataSource.getBatchChapterContent(chapterIds)
    }
    
    /**
     * 创建下载任务
     * 
     * @param bookId 小说ID
     * @param bookName 小说名称
     * @param author 作者
     * @param totalChapters 总章节数
     * @param format 输出格式
     * @param savePath 保存路径
     * @return 任务ID
     */
    suspend fun createDownloadTask(
        bookId: String,
        bookName: String,
        author: String,
        totalChapters: Int,
        format: String = "txt",
        savePath: String = ""
    ): Long {
        val task = DownloadTaskEntity(
            bookId = bookId,
            bookName = bookName,
            author = author,
            totalChapters = totalChapters,
            format = format,
            savePath = savePath
        )
        return downloadTaskDao.insertTask(task)
    }
    
    /**
     * 获取所有下载任务
     * 
     * @return 下载任务列表流
     */
    fun getAllDownloadTasks(): Flow<List<DownloadTask>> {
        return downloadTaskDao.getAllTasks().map { entities ->
            entities.map { it.toDomain() }
        }
    }
    
    /**
     * 获取指定状态的下载任务
     * 
     * @param status 下载状态
     * @return 下载任务列表流
     */
    fun getDownloadTasksByStatus(status: DownloadStatus): Flow<List<DownloadTask>> {
        return downloadTaskDao.getTasksByStatus(status.ordinal).map { entities ->
            entities.map { it.toDomain() }
        }
    }
    
    /**
     * 更新下载进度
     * 
     * @param taskId 任务ID
     * @param downloaded 已下载章节数
     */
    suspend fun updateDownloadProgress(taskId: Long, downloaded: Int) {
        downloadTaskDao.updateProgress(taskId, downloaded)
    }
    
    /**
     * 更新下载状态
     * 
     * @param taskId 任务ID
     * @param status 新状态
     */
    suspend fun updateDownloadStatus(taskId: Long, status: DownloadStatus) {
        downloadTaskDao.updateStatus(taskId, status.ordinal)
    }
    
    /**
     * 获取已下载的章节ID列表
     * 
     * @param bookId 小说ID
     * @return 已下载章节ID集合
     */
    suspend fun getDownloadedChapterIds(bookId: String): Set<String> {
        return downloadedChapterDao.getDownloadedChapterIds(bookId).toSet()
    }
    
    /**
     * 保存已下载章节记录
     * 
     * @param bookId 小说ID
     * @param chapterIds 章节ID列表
     */
    suspend fun saveDownloadedChapters(bookId: String, chapterIds: List<String>) {
        val entities = chapterIds.map { chapterId ->
            DownloadedChapterEntity(bookId = bookId, chapterId = chapterId)
        }
        downloadedChapterDao.insertDownloadedChapters(entities)
    }
    
    /**
     * 删除下载任务
     * 
     * @param taskId 任务ID
     */
    suspend fun deleteDownloadTask(taskId: Long) {
        downloadTaskDao.getTaskById(taskId)?.let { task ->
            downloadTaskDao.deleteTask(task)
            downloadedChapterDao.deleteByBookId(task.bookId)
        }
    }
    
    /**
     * 检查小说是否已存在下载任务
     * 
     * @param bookId 小说ID
     * @return 是否存在
     */
    suspend fun hasExistingTask(bookId: String): Boolean {
        return downloadTaskDao.getTaskByBookId(bookId) != null
    }
}

/**
 * 实体转领域模型扩展函数
 */
private fun DownloadTaskEntity.toDomain(): DownloadTask {
    return DownloadTask(
        id = id,
        bookId = bookId,
        bookName = bookName,
        author = author,
        totalChapters = totalChapters,
        downloadedChapters = downloadedChapters,
        status = DownloadStatus.values()[status],
        createTime = createTime,
        updateTime = updateTime,
        savePath = savePath,
        format = format
    )
}
