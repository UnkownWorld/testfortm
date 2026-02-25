package com.tomato.novel.downloader.service

import android.app.Notification
import android.app.PendingIntent
import android.app.Service
import android.content.Intent
import android.os.IBinder
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.tomato.novel.downloader.R
import com.tomato.novel.downloader.TomatoNovelApp
import com.tomato.novel.downloader.data.local.DownloadStatus
import com.tomato.novel.downloader.data.repository.NovelRepository
import com.tomato.novel.downloader.ui.main.MainActivity
import com.tomato.novel.downloader.utils.ContentUtils
import com.tomato.novel.downloader.utils.FileUtils
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.collectLatest
import javax.inject.Inject

/**
 * 下载服务
 * 
 * 后台服务，负责执行小说下载任务
 * 支持断点续传、进度通知、错误重试
 * 
 * 设计原则：
 * - 单一职责：只负责下载执行
 * - 健壮性：完善的错误处理和重试机制
 * - 可观测性：通过通知和广播通知进度
 */
@AndroidEntryPoint
class DownloadService : Service() {
    
    @Inject
    lateinit var repository: NovelRepository
    
    private val serviceScope = CoroutineScope(Dispatchers.IO + SupervisorJob())
    private val activeDownloads = mutableMapOf<Long, Job>()
    
    companion object {
        const val ACTION_START_DOWNLOAD = "com.tomato.novel.downloader.START_DOWNLOAD"
        const val ACTION_PAUSE_DOWNLOAD = "com.tomato.novel.downloader.PAUSE_DOWNLOAD"
        const val ACTION_RESUME_DOWNLOAD = "com.tomato.novel.downloader.RESUME_DOWNLOAD"
        const val ACTION_CANCEL_DOWNLOAD = "com.tomato.novel.downloader.CANCEL_DOWNLOAD"
        
        const val EXTRA_TASK_ID = "task_id"
        const val EXTRA_BOOK_ID = "book_id"
        const val EXTRA_BOOK_NAME = "book_name"
        const val EXTRA_AUTHOR = "author"
        const val EXTRA_TOTAL_CHAPTERS = "total_chapters"
        const val EXTRA_FORMAT = "format"
        
        const val NOTIFICATION_ID_BASE = 1000
    }
    
    override fun onBind(intent: Intent?): IBinder? = null
    
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            ACTION_START_DOWNLOAD -> {
                val taskId = intent.getLongExtra(EXTRA_TASK_ID, -1)
                if (taskId > 0) {
                    startDownload(taskId)
                }
            }
            ACTION_PAUSE_DOWNLOAD -> {
                val taskId = intent.getLongExtra(EXTRA_TASK_ID, -1)
                if (taskId > 0) {
                    pauseDownload(taskId)
                }
            }
            ACTION_RESUME_DOWNLOAD -> {
                val taskId = intent.getLongExtra(EXTRA_TASK_ID, -1)
                if (taskId > 0) {
                    resumeDownload(taskId)
                }
            }
            ACTION_CANCEL_DOWNLOAD -> {
                val taskId = intent.getLongExtra(EXTRA_TASK_ID, -1)
                if (taskId > 0) {
                    cancelDownload(taskId)
                }
            }
        }
        return START_STICKY
    }
    
    override fun onDestroy() {
        super.onDestroy()
        serviceScope.cancel()
    }
    
    /**
     * 开始下载任务
     */
    private fun startDownload(taskId: Long) {
        if (activeDownloads.containsKey(taskId)) {
            return // 已经在下载中
        }
        
        val job = serviceScope.launch {
            try {
                executeDownload(taskId)
            } catch (e: CancellationException) {
                // 任务被取消
            } catch (e: Exception) {
                repository.updateDownloadStatus(taskId, DownloadStatus.FAILED)
            }
        }
        activeDownloads[taskId] = job
    }
    
    /**
     * 执行下载
     */
    private suspend fun executeDownload(taskId: Long) {
        // 获取任务信息
        val tasks = repository.getAllDownloadTasks().first()
        val task = tasks.find { it.id == taskId } ?: return
        
        // 更新状态为下载中
        repository.updateDownloadStatus(taskId, DownloadStatus.DOWNLOADING)
        
        // 获取已下载章节
        val downloadedIds = repository.getDownloadedChapterIds(task.bookId)
        
        // 获取章节列表
        val chapterListResult = repository.getChapterList(task.bookId)
        if (chapterListResult.isFailure) {
            repository.updateDownloadStatus(taskId, DownloadStatus.FAILED)
            return
        }
        
        val allChapters = chapterListResult.getOrThrow()
        val pendingChapters = allChapters.filter { it.id !in downloadedIds }
        
        if (pendingChapters.isEmpty()) {
            repository.updateDownloadStatus(taskId, DownloadStatus.COMPLETED)
            return
        }
        
        // 创建文件
        val file = FileUtils.createNovelFile(applicationContext, task.bookName, task.format)
        
        // 获取小说信息
        val bookInfoResult = repository.getBookInfo(task.bookId)
        val bookInfo = bookInfoResult.getOrNull()
        
        // 下载章节内容
        val downloadedContents = mutableListOf<Pair<String, String>>()
        var downloadedCount = downloadedIds.size
        
        // 批量下载
        pendingChapters.chunked(30).forEach { batch ->
            // 检查是否被取消或暂停
            val currentTask = repository.getAllDownloadTasks().first()
                .find { it.id == taskId }
            
            if (currentTask?.status == DownloadStatus.PAUSED || 
                currentTask?.status == DownloadStatus.CANCELLED) {
                return@forEach
            }
            
            val contentResult = repository.getChapterContents(batch.map { it.id })
            
            if (contentResult.isSuccess) {
                val contents = contentResult.getOrThrow()
                
                batch.forEach { chapter ->
                    val content = contents[chapter.id]
                    if (content != null) {
                        val processedContent = ContentUtils.processChapterContent(content.content)
                        downloadedContents.add(content.title to processedContent)
                        downloadedCount++
                        
                        // 更新进度
                        repository.updateDownloadProgress(taskId, downloadedCount)
                        repository.saveDownloadedChapters(task.bookId, listOf(chapter.id))
                        
                        // 更新通知
                        updateNotification(
                            taskId, 
                            task.bookName, 
                            downloadedCount, 
                            task.totalChapters
                        )
                    }
                }
            }
            
            // 延迟避免请求过快
            delay(500)
        }
        
        // 写入文件
        if (downloadedContents.isNotEmpty()) {
            FileUtils.writeTxtFile(
                file = file,
                bookName = task.bookName,
                author = task.author,
                description = bookInfo?.description ?: "",
                chapters = downloadedContents
            )
        }
        
        // 检查是否完成
        if (downloadedCount >= task.totalChapters) {
            repository.updateDownloadStatus(taskId, DownloadStatus.COMPLETED)
        }
        
        activeDownloads.remove(taskId)
    }
    
    /**
     * 暂停下载
     */
    private fun pauseDownload(taskId: Long) {
        activeDownloads[taskId]?.cancel()
        activeDownloads.remove(taskId)
        serviceScope.launch {
            repository.updateDownloadStatus(taskId, DownloadStatus.PAUSED)
        }
    }
    
    /**
     * 恢复下载
     */
    private fun resumeDownload(taskId: Long) {
        startDownload(taskId)
    }
    
    /**
     * 取消下载
     */
    private fun cancelDownload(taskId: Long) {
        activeDownloads[taskId]?.cancel()
        activeDownloads.remove(taskId)
        serviceScope.launch {
            repository.updateDownloadStatus(taskId, DownloadStatus.CANCELLED)
        }
    }
    
    /**
     * 更新通知
     */
    private fun updateNotification(taskId: Long, bookName: String, current: Int, total: Int) {
        val notification = createNotification(
            taskId = taskId,
            title = bookName,
            progress = current,
            max = total
        )
        
        NotificationManagerCompat.from(this)
            .notify(NOTIFICATION_ID_BASE + taskId.toInt(), notification)
    }
    
    /**
     * 创建通知
     */
    private fun createNotification(
        taskId: Long,
        title: String,
        progress: Int,
        max: Int
    ): Notification {
        val intent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_SINGLE_TOP
        }
        
        val pendingIntent = PendingIntent.getActivity(
            this,
            taskId.toInt(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        
        return NotificationCompat.Builder(this, TomatoNovelApp.CHANNEL_ID_DOWNLOAD)
            .setContentTitle(title)
            .setContentText("下载进度: $progress/$max")
            .setSmallIcon(R.drawable.ic_download)
            .setProgress(max, progress, false)
            .setContentIntent(pendingIntent)
            .setOngoing(true)
            .build()
    }
    
    /**
     * Flow扩展：获取第一个元素
     */
    private suspend fun <T> kotlinx.coroutines.flow.Flow<T>.first(): T {
        var result: T? = null
        collectLatest { 
            result = it
        }
        return result!!
    }
}
