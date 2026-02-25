package com.tomato.novel.downloader.domain.model

import com.tomato.novel.downloader.data.local.DownloadStatus
import org.junit.Assert.*
import org.junit.Test

/**
 * DownloadTask 领域模型单元测试
 */
class DownloadTaskTest {

    private fun createTask(
        status: DownloadStatus,
        downloaded: Int = 50,
        total: Int = 100
    ) = DownloadTask(
        id = 1,
        bookId = "test_book_id",
        bookName = "测试小说",
        author = "测试作者",
        totalChapters = total,
        downloadedChapters = downloaded,
        status = status,
        createTime = System.currentTimeMillis(),
        updateTime = System.currentTimeMillis(),
        savePath = "/path/to/file",
        format = "txt"
    )

    @Test
    fun `getProgress should return correct percentage`() {
        // 测试进度计算
        val task = createTask(status = DownloadStatus.DOWNLOADING, downloaded = 50, total = 100)
        assertEquals(50, task.getProgress())
    }

    @Test
    fun `getProgress should return 0 for zero total`() {
        // 测试总章节数为0的情况
        val task = createTask(status = DownloadStatus.DOWNLOADING, downloaded = 0, total = 0)
        assertEquals(0, task.getProgress())
    }

    @Test
    fun `getProgress should handle partial downloads`() {
        // 测试部分下载
        val task = createTask(status = DownloadStatus.DOWNLOADING, downloaded = 33, total = 100)
        assertEquals(33, task.getProgress())
    }

    @Test
    fun `isCompleted should return true for completed status`() {
        // 测试完成状态
        val task = createTask(status = DownloadStatus.COMPLETED)
        assertTrue(task.isCompleted())
    }

    @Test
    fun `isCompleted should return false for other status`() {
        // 测试非完成状态
        val downloadingTask = createTask(status = DownloadStatus.DOWNLOADING)
        val pausedTask = createTask(status = DownloadStatus.PAUSED)
        val failedTask = createTask(status = DownloadStatus.FAILED)
        
        assertFalse(downloadingTask.isCompleted())
        assertFalse(pausedTask.isCompleted())
        assertFalse(failedTask.isCompleted())
    }

    @Test
    fun `isDownloading should return true only for downloading status`() {
        // 测试下载中状态
        val task = createTask(status = DownloadStatus.DOWNLOADING)
        assertTrue(task.isDownloading())
        
        val otherTask = createTask(status = DownloadStatus.PAUSED)
        assertFalse(otherTask.isDownloading())
    }

    @Test
    fun `isFailed should return true only for failed status`() {
        // 测试失败状态
        val task = createTask(status = DownloadStatus.FAILED)
        assertTrue(task.isFailed())
        
        val otherTask = createTask(status = DownloadStatus.DOWNLOADING)
        assertFalse(otherTask.isFailed())
    }

    @Test
    fun `isPaused should return true only for paused status`() {
        // 测试暂停状态
        val task = createTask(status = DownloadStatus.PAUSED)
        assertTrue(task.isPaused())
        
        val otherTask = createTask(status = DownloadStatus.DOWNLOADING)
        assertFalse(otherTask.isPaused())
    }

    @Test
    fun `getStatusText should return correct Chinese text`() {
        // 测试状态文本
        assertEquals("等待中", createTask(status = DownloadStatus.PENDING).getStatusText())
        assertEquals("下载中", createTask(status = DownloadStatus.DOWNLOADING).getStatusText())
        assertEquals("已暂停", createTask(status = DownloadStatus.PAUSED).getStatusText())
        assertEquals("已完成", createTask(status = DownloadStatus.COMPLETED).getStatusText())
        assertEquals("下载失败", createTask(status = DownloadStatus.FAILED).getStatusText())
        assertEquals("已取消", createTask(status = DownloadStatus.CANCELLED).getStatusText())
    }
}
