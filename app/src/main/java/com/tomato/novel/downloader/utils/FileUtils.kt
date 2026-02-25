package com.tomato.novel.downloader.utils

import android.content.Context
import android.os.Environment
import java.io.File

/**
 * 文件工具类
 * 
 * 提供文件操作相关的工具方法
 */
object FileUtils {

    /**
     * 清理文件名中的非法字符
     */
    fun sanitizeFileName(name: String): String {
        val invalidChars = arrayOf("\\", "/", ":", "*", "?", "\"", "<", ">", "|")
        var result = name
        invalidChars.forEach { char ->
            result = result.replace(char, "_")
        }
        return result.trim()
    }

    /**
     * 格式化文件大小
     */
    fun formatFileSize(bytes: Long): String {
        return when {
            bytes < 1024 -> "$bytes B"
            bytes < 1024 * 1024 -> String.format("%.1f KB", bytes / 1024.0)
            bytes < 1024 * 1024 * 1024 -> String.format("%.1f MB", bytes / (1024.0 * 1024))
            else -> String.format("%.1f GB", bytes / (1024.0 * 1024 * 1024))
        }
    }

    /**
     * 获取下载目录
     */
    fun getDownloadDir(context: Context): File {
        val dir = File(
            Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS),
            "TomatoNovelDownloader"
        )
        if (!dir.exists()) {
            dir.mkdirs()
        }
        return dir
    }

    /**
     * 获取缓存目录
     */
    fun getCacheDir(context: Context): File {
        return context.cacheDir
    }

    /**
     * 计算文件夹大小
     */
    fun getFolderSize(folder: File): Long {
        var size: Long = 0
        if (folder.exists() && folder.isDirectory) {
            folder.listFiles()?.forEach { file ->
                size += if (file.isDirectory) {
                    getFolderSize(file)
                } else {
                    file.length()
                }
            }
        }
        return size
    }

    /**
     * 清空文件夹
     */
    fun clearFolder(folder: File): Boolean {
        return try {
            if (folder.exists() && folder.isDirectory) {
                folder.listFiles()?.forEach { file ->
                    if (file.isDirectory) {
                        clearFolder(file)
                    }
                    file.delete()
                }
            }
            true
        } catch (e: Exception) {
            false
        }
    }

    /**
     * 检查是否有足够的存储空间
     */
    fun hasEnoughSpace(path: File, requiredBytes: Long): Boolean {
        return try {
            val stat = android.os.StatFs(path.absolutePath)
            val availableBytes = stat.availableBlocksLong * stat.blockSizeLong
            availableBytes > requiredBytes
        } catch (e: Exception) {
            false
        }
    }

    /**
     * 获取文件扩展名
     */
    fun getExtension(fileName: String): String {
        val lastDot = fileName.lastIndexOf('.')
        return if (lastDot > 0) {
            fileName.substring(lastDot + 1).lowercase()
        } else {
            ""
        }
    }

    /**
     * 检查是否为文本文件
     */
    fun isTextFile(fileName: String): Boolean {
        val ext = getExtension(fileName)
        return ext in listOf("txt", "md", "json", "xml", "html", "css")
    }

    /**
     * 检查是否为EPUB文件
     */
    fun isEpubFile(fileName: String): Boolean {
        return getExtension(fileName) == "epub"
    }
}
