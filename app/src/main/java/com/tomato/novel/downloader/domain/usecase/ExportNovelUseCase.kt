package com.tomato.novel.downloader.domain.usecase

import android.content.Context
import com.tomato.novel.downloader.data.model.ChapterContent
import com.tomato.novel.downloader.data.repository.NovelRepository
import com.tomato.novel.downloader.utils.EpubUtils
import com.tomato.novel.downloader.utils.FileUtils
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import javax.inject.Inject

/**
 * 导出小说用例
 * 
 * 支持导出为TXT和EPUB格式
 */
class ExportNovelUseCase @Inject constructor(
    @ApplicationContext private val context: Context,
    private val repository: NovelRepository
) {
    /**
     * 导出结果
     */
    sealed class ExportResult {
        data class Success(val filePath: String) : ExportResult()
        data class Error(val message: String) : ExportResult()
    }

    /**
     * 导出为TXT格式
     * 
     * @param bookId 小说ID
     * @param bookName 书名
     * @param author 作者
     * @param description 简介
     * @param chapterIds 章节ID列表
     */
    suspend fun exportToTxt(
        bookId: String,
        bookName: String,
        author: String,
        description: String,
        chapterIds: List<String>
    ): ExportResult = withContext(Dispatchers.IO) {
        try {
            // 获取章节内容
            val chaptersResult = repository.getChapterContents(chapterIds)
            
            chaptersResult.fold(
                onSuccess = { contentMap ->
                    // 创建输出文件
                    val fileName = FileUtils.sanitizeFileName(bookName)
                    val outputDir = FileUtils.getDownloadDir(context)
                    val outputFile = File(outputDir, "$fileName.txt")
                    
                    // 写入文件
                    outputFile.bufferedWriter().use { writer ->
                        // 写入头部信息
                        writer.write("书名: $bookName\n")
                        writer.write("作者: $author\n")
                        writer.write("简介: $description\n")
                        writer.write("\n${"=".repeat(50)}\n\n")
                        
                        // 写入章节内容
                        chapterIds.forEach { chapterId ->
                            contentMap[chapterId]?.let { chapter ->
                                writer.write("${chapter.title}\n\n")
                                writer.write("${chapter.content}\n\n")
                            }
                        }
                    }
                    
                    ExportResult.Success(outputFile.absolutePath)
                },
                onFailure = { error ->
                    ExportResult.Error(error.message ?: "导出失败")
                }
            )
        } catch (e: Exception) {
            ExportResult.Error(e.message ?: "导出失败")
        }
    }

    /**
     * 导出为EPUB格式
     * 
     * @param bookId 小说ID
     * @param bookName 书名
     * @param author 作者
     * @param description 简介
     * @param chapterIds 章节ID列表
     * @param coverPath 封面路径（可选）
     */
    suspend fun exportToEpub(
        bookId: String,
        bookName: String,
        author: String,
        description: String,
        chapterIds: List<String>,
        coverPath: String? = null
    ): ExportResult = withContext(Dispatchers.IO) {
        try {
            // 获取章节内容
            val chaptersResult = repository.getChapterContents(chapterIds)
            
            chaptersResult.fold(
                onSuccess = { contentMap ->
                    // 按顺序获取章节
                    val chapters = chapterIds.mapNotNull { contentMap[it] }
                    
                    // 创建输出文件
                    val fileName = FileUtils.sanitizeFileName(bookName)
                    val outputDir = FileUtils.getDownloadDir(context)
                    val outputFile = File(outputDir, "$fileName.epub")
                    
                    // 生成EPUB
                    val filePath = EpubUtils.generateEpub(
                        context = context,
                        bookName = bookName,
                        author = author,
                        description = description,
                        coverPath = coverPath,
                        chapters = chapters,
                        outputPath = outputFile.absolutePath
                    )
                    
                    ExportResult.Success(filePath)
                },
                onFailure = { error ->
                    ExportResult.Error(error.message ?: "导出失败")
                }
            )
        } catch (e: Exception) {
            ExportResult.Error(e.message ?: "导出失败")
        }
    }
}
