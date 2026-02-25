package com.tomato.novel.downloader.data.local

import androidx.room.*
import kotlinx.coroutines.flow.Flow

/**
 * 下载任务实体类
 * 
 * 用于持久化存储下载任务信息
 * 
 * @property id 任务唯一标识符（自动生成）
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
@Entity(tableName = "download_tasks")
data class DownloadTaskEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    
    @ColumnInfo(name = "book_id")
    val bookId: String,
    
    @ColumnInfo(name = "book_name")
    val bookName: String,
    
    @ColumnInfo(name = "author")
    val author: String,
    
    @ColumnInfo(name = "total_chapters")
    val totalChapters: Int,
    
    @ColumnInfo(name = "downloaded_chapters")
    val downloadedChapters: Int = 0,
    
    @ColumnInfo(name = "status")
    val status: Int = DownloadStatus.PENDING.ordinal,
    
    @ColumnInfo(name = "create_time")
    val createTime: Long = System.currentTimeMillis(),
    
    @ColumnInfo(name = "update_time")
    val updateTime: Long = System.currentTimeMillis(),
    
    @ColumnInfo(name = "save_path")
    val savePath: String = "",
    
    @ColumnInfo(name = "format")
    val format: String = "txt"
)

/**
 * 下载状态枚举
 */
enum class DownloadStatus {
    PENDING,      // 等待中
    DOWNLOADING,  // 下载中
    PAUSED,       // 已暂停
    COMPLETED,    // 已完成
    FAILED,       // 失败
    CANCELLED     // 已取消
}

/**
 * 已下载章节记录实体类
 * 
 * 用于记录已下载的章节，支持断点续传
 * 
 * @property id 记录唯一标识符
 * @property bookId 小说ID
 * @property chapterId 章节ID
 * @property downloadTime 下载时间
 */
@Entity(
    tableName = "downloaded_chapters",
    indices = [
        Index(value = ["book_id", "chapter_id"], unique = true)
    ]
)
data class DownloadedChapterEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    
    @ColumnInfo(name = "book_id")
    val bookId: String,
    
    @ColumnInfo(name = "chapter_id")
    val chapterId: String,
    
    @ColumnInfo(name = "download_time")
    val downloadTime: Long = System.currentTimeMillis()
)

/**
 * 下载任务数据访问对象
 */
@Dao
interface DownloadTaskDao {
    
    /**
     * 插入下载任务
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTask(task: DownloadTaskEntity): Long
    
    /**
     * 更新下载任务
     */
    @Update
    suspend fun updateTask(task: DownloadTaskEntity)
    
    /**
     * 删除下载任务
     */
    @Delete
    suspend fun deleteTask(task: DownloadTaskEntity)
    
    /**
     * 根据ID获取下载任务
     */
    @Query("SELECT * FROM download_tasks WHERE id = :taskId")
    suspend fun getTaskById(taskId: Long): DownloadTaskEntity?
    
    /**
     * 根据小说ID获取下载任务
     */
    @Query("SELECT * FROM download_tasks WHERE book_id = :bookId")
    suspend fun getTaskByBookId(bookId: String): DownloadTaskEntity?
    
    /**
     * 获取所有下载任务
     */
    @Query("SELECT * FROM download_tasks ORDER BY create_time DESC")
    fun getAllTasks(): Flow<List<DownloadTaskEntity>>
    
    /**
     * 获取指定状态的下载任务
     */
    @Query("SELECT * FROM download_tasks WHERE status = :status ORDER BY create_time DESC")
    fun getTasksByStatus(status: Int): Flow<List<DownloadTaskEntity>>
    
    /**
     * 更新任务进度
     */
    @Query("UPDATE download_tasks SET downloaded_chapters = :downloaded, update_time = :time WHERE id = :taskId")
    suspend fun updateProgress(taskId: Long, downloaded: Int, time: Long = System.currentTimeMillis())
    
    /**
     * 更新任务状态
     */
    @Query("UPDATE download_tasks SET status = :status, update_time = :time WHERE id = :taskId")
    suspend fun updateStatus(taskId: Long, status: Int, time: Long = System.currentTimeMillis())
}

/**
 * 已下载章节数据访问对象
 */
@Dao
interface DownloadedChapterDao {
    
    /**
     * 插入已下载章节记录
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertDownloadedChapter(chapter: DownloadedChapterEntity)
    
    /**
     * 批量插入已下载章节记录
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertDownloadedChapters(chapters: List<DownloadedChapterEntity>)
    
    /**
     * 获取指定小说已下载的章节ID列表
     */
    @Query("SELECT chapter_id FROM downloaded_chapters WHERE book_id = :bookId")
    suspend fun getDownloadedChapterIds(bookId: String): List<String>
    
    /**
     * 删除指定小说的所有下载记录
     */
    @Query("DELETE FROM downloaded_chapters WHERE book_id = :bookId")
    suspend fun deleteByBookId(bookId: String)
    
    /**
     * 检查章节是否已下载
     */
    @Query("SELECT EXISTS(SELECT 1 FROM downloaded_chapters WHERE book_id = :bookId AND chapter_id = :chapterId)")
    suspend fun isChapterDownloaded(bookId: String, chapterId: String): Boolean
}
