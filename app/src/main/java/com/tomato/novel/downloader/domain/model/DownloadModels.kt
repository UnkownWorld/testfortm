package com.tomato.novel.downloader.domain.model

import android.os.Parcelable
import com.tomato.novel.downloader.data.local.DownloadStatus
import kotlinx.parcelize.Parcelize

/**
 * 下载任务领域模型
 * 
 * 表示一个完整的下载任务，包含所有必要信息
 * 用于在UI层和业务逻辑层之间传递数据
 * 
 * @property id 任务唯一标识符
 * @property bookId 小说ID
 * @property bookName 小说名称
 * @property author 作者名称
 * @property totalChapters 总章节数
 * @property downloadedChapters 已下载章节数
 * @property status 下载状态
 * @property createTime 创建时间
 * @property updateTime 更新时间
 * @property savePath 保存路径
 * @property format 输出格式
 */
@Parcelize
data class DownloadTask(
    val id: Long,
    val bookId: String,
    val bookName: String,
    val author: String,
    val totalChapters: Int,
    val downloadedChapters: Int,
    val status: DownloadStatus,
    val createTime: Long,
    val updateTime: Long,
    val savePath: String,
    val format: String
) : Parcelable {
    
    /**
     * 计算下载进度百分比
     * 
     * @return 进度百分比（0-100）
     */
    fun getProgress(): Int {
        if (totalChapters == 0) return 0
        return (downloadedChapters * 100 / totalChapters)
    }
    
    /**
     * 判断是否已完成
     */
    fun isCompleted(): Boolean = status == DownloadStatus.COMPLETED
    
    /**
     * 判断是否正在下载
     */
    fun isDownloading(): Boolean = status == DownloadStatus.DOWNLOADING
    
    /**
     * 判断是否失败
     */
    fun isFailed(): Boolean = status == DownloadStatus.FAILED
    
    /**
     * 判断是否暂停
     */
    fun isPaused(): Boolean = status == DownloadStatus.PAUSED
    
    /**
     * 获取状态描述文本
     */
    fun getStatusText(): String {
        return when (status) {
            DownloadStatus.PENDING -> "等待中"
            DownloadStatus.DOWNLOADING -> "下载中"
            DownloadStatus.PAUSED -> "已暂停"
            DownloadStatus.COMPLETED -> "已完成"
            DownloadStatus.FAILED -> "下载失败"
            DownloadStatus.CANCELLED -> "已取消"
        }
    }
}

/**
 * 下载请求参数
 * 
 * 用于封装创建下载任务所需的参数
 * 
 * @property bookId 小说ID
 * @property bookName 小说名称
 * @property author 作者
 * @property totalChapters 总章节数
 * @property startChapter 起始章节索引（可选）
 * @property endChapter 结束章节索引（可选）
 * @property format 输出格式
 * @property savePath 保存路径
 */
data class DownloadRequest(
    val bookId: String,
    val bookName: String,
    val author: String,
    val totalChapters: Int,
    val startChapter: Int? = null,
    val endChapter: Int? = null,
    val format: String = "txt",
    val savePath: String = ""
)

/**
 * 下载进度信息
 * 
 * 用于实时更新下载进度
 * 
 * @property taskId 任务ID
 * @property currentChapter 当前章节索引
 * @property totalChapters 总章节数
 * @property currentChapterTitle 当前章节标题
 * @property speed 下载速度（字节/秒）
 */
data class DownloadProgress(
    val taskId: Long,
    val currentChapter: Int,
    val totalChapters: Int,
    val currentChapterTitle: String = "",
    val speed: Long = 0
) {
    /**
     * 获取进度百分比
     */
    fun getProgressPercent(): Int {
        if (totalChapters == 0) return 0
        return (currentChapter * 100 / totalChapters)
    }
}
