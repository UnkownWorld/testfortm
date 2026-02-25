package com.tomato.novel.downloader.domain.usecase

import com.tomato.novel.downloader.data.local.DownloadStatus
import com.tomato.novel.downloader.data.repository.NovelRepository
import com.tomato.novel.downloader.domain.model.DownloadRequest
import com.tomato.novel.downloader.domain.model.DownloadTask
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

/**
 * 创建下载任务用例
 * 
 * 封装创建下载任务的业务逻辑
 * 包括验证、去重检查等
 * 
 * @property repository 小说仓库
 */
class CreateDownloadTaskUseCase @Inject constructor(
    private val repository: NovelRepository
) {
    /**
     * 执行用例
     * 
     * @param request 下载请求参数
     * @return 任务ID，失败时返回异常
     */
    suspend operator fun invoke(request: DownloadRequest): Result<Long> {
        // 验证参数
        if (request.bookId.isBlank()) {
            return Result.failure(IllegalArgumentException("小说ID不能为空"))
        }
        
        if (request.totalChapters <= 0) {
            return Result.failure(IllegalArgumentException("章节数必须大于0"))
        }
        
        // 检查是否已存在任务
        if (repository.hasExistingTask(request.bookId)) {
            return Result.failure(IllegalStateException("该小说已存在下载任务"))
        }
        
        // 创建任务
        val taskId = repository.createDownloadTask(
            bookId = request.bookId,
            bookName = request.bookName,
            author = request.author,
            totalChapters = request.totalChapters,
            format = request.format,
            savePath = request.savePath
        )
        
        return Result.success(taskId)
    }
}

/**
 * 获取下载任务列表用例
 * 
 * 获取所有或特定状态的下载任务
 * 
 * @property repository 小说仓库
 */
class GetDownloadTasksUseCase @Inject constructor(
    private val repository: NovelRepository
) {
    /**
     * 获取所有下载任务
     */
    fun getAll(): Flow<List<DownloadTask>> {
        return repository.getAllDownloadTasks()
    }
    
    /**
     * 获取指定状态的下载任务
     */
    fun getByStatus(status: DownloadStatus): Flow<List<DownloadTask>> {
        return repository.getDownloadTasksByStatus(status)
    }
}

/**
 * 更新下载状态用例
 * 
 * 用于暂停、恢复、取消下载任务
 * 
 * @property repository 小说仓库
 */
class UpdateDownloadStatusUseCase @Inject constructor(
    private val repository: NovelRepository
) {
    /**
     * 暂停下载
     */
    suspend fun pause(taskId: Long): Result<Unit> {
        return try {
            repository.updateDownloadStatus(taskId, DownloadStatus.PAUSED)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * 恢复下载
     */
    suspend fun resume(taskId: Long): Result<Unit> {
        return try {
            repository.updateDownloadStatus(taskId, DownloadStatus.PENDING)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * 取消下载
     */
    suspend fun cancel(taskId: Long): Result<Unit> {
        return try {
            repository.updateDownloadStatus(taskId, DownloadStatus.CANCELLED)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * 标记为失败
     */
    suspend fun markFailed(taskId: Long): Result<Unit> {
        return try {
            repository.updateDownloadStatus(taskId, DownloadStatus.FAILED)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * 标记为完成
     */
    suspend fun markCompleted(taskId: Long): Result<Unit> {
        return try {
            repository.updateDownloadStatus(taskId, DownloadStatus.COMPLETED)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}

/**
 * 删除下载任务用例
 * 
 * 删除任务及其相关数据
 * 
 * @property repository 小说仓库
 */
class DeleteDownloadTaskUseCase @Inject constructor(
    private val repository: NovelRepository
) {
    /**
     * 执行用例
     * 
     * @param taskId 任务ID
     * @return 操作结果
     */
    suspend operator fun invoke(taskId: Long): Result<Unit> {
        return try {
            repository.deleteDownloadTask(taskId)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
