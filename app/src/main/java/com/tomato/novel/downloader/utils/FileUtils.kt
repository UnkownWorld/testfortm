package com.tomato.novel.downloader.utils

import android.content.Context
import android.os.Environment
import java.io.File
import java.text.SimpleDateFormat
import java.util.*

/**
 * 文件工具类
 * 
 * 提供文件操作相关的工具方法
 */
object FileUtils {
    
    private const val APP_DIR = "TomatoNovelDownloader"
    
    /**
     * 获取应用默认下载目录
     */
    fun getDefaultDownloadDir(context: Context): File {
        val dir = File(
            Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS),
            APP_DIR
        )
        if (!dir.exists()) {
            dir.mkdirs()
        }
        return dir
    }
    
    /**
     * 创建小说文件
     * 
     * @param context 上下文
     * @param bookName 小说名称
     * @param format 格式
     * @return 文件对象
     */
    fun createNovelFile(context: Context, bookName: String, format: String = "txt"): File {
        val dir = getDefaultDownloadDir(context)
        val safeName = sanitizeFileName(bookName)
        return File(dir, "$safeName.$format")
    }
    
    /**
     * 清理文件名中的非法字符
     */
    fun sanitizeFileName(name: String): String {
        return name.replace(Regex("[\\\\/:*?\"<>|]"), "_")
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
     * 格式化时间
     */
    fun formatTime(timestamp: Long): String {
        val sdf = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault())
        return sdf.format(Date(timestamp))
    }
    
    /**
     * 写入TXT文件
     * 
     * @param file 目标文件
     * @param bookName 小说名称
     * @param author 作者
     * @param description 简介
     * @param chapters 章节内容列表（标题到内容的映射）
     */
    fun writeTxtFile(
        file: File,
        bookName: String,
        author: String,
        description: String,
        chapters: List<Pair<String, String>>
    ): Boolean {
        return try {
            file.bufferedWriter(charset = Charsets.UTF_8).use { writer ->
                // 写入头部信息
                writer.appendLine("小说名: $bookName")
                writer.appendLine("作者: $author")
                writer.appendLine("内容简介: $description")
                writer.appendLine()
                
                // 写入章节内容
                chapters.forEach { (title, content) ->
                    writer.appendLine(title)
                    writer.appendLine(content)
                    writer.appendLine()
                }
            }
            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }
}

/**
 * 内容处理工具类
 * 
 * 提供章节内容清理和格式化的工具方法
 */
object ContentUtils {
    
    /**
     * 处理章节内容
     * 
     * 清理HTML标签，添加段落缩进
     * 
     * @param content 原始内容
     * @return 处理后的内容
     */
    fun processChapterContent(content: String): String {
        if (content.isBlank()) return ""
        
        var processed = content
        
        // 提取段落内容
        val paragraphs = if ("<p idx=" in processed) {
            Regex("""<p idx="\d+">(.*?)</p>""", RegexOption.DOT_MATCHES_ALL)
                .findAll(processed)
                .map { it.groupValues[1] }
                .toList()
        } else {
            processed.split("\n")
        }
        
        // 清理HTML标签
        processed = paragraphs
            .map { paragraph ->
                paragraph
                    .replace(Regex("<header>.*?</header>", RegexOption.DOT_MATCHES_ALL), "")
                    .replace(Regex("<footer>.*?</footer>", RegexOption.DOT_MATCHES_ALL), "")
                    .replace(Regex("</?article>"), "")
                    .replace(Regex("<[^>]+>"), "")
                    .replace("\\u003c", "")
                    .replace("\\u003e", "")
                    .trim()
            }
            .filter { it.isNotBlank() }
            .joinToString("\n") { "　　$it" }
        
        // 压缩多余空行
        processed = processed.replace(Regex("\n{3,}"), "\n\n").trim()
        
        return processed
    }
}

/**
 * 网络工具类
 */
object NetworkUtils {
    
    /**
     * 生成随机请求头
     */
    fun getRandomHeaders(): Map<String, String> {
        val userAgents = listOf(
            "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Safari/537.36",
            "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/119.0.0.0 Safari/537.36",
            "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_15_7) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Safari/537.36",
            "Mozilla/5.0 (Windows NT 10.0; Win64; x64; rv:121.0) Gecko/20100101 Firefox/121.0"
        )
        
        return mapOf(
            "User-Agent" to userAgents.random(),
            "Accept" to "application/json, text/javascript, */*; q=0.01",
            "Accept-Language" to "zh-CN,zh;q=0.9,en-US;q=0.8,en;q=0.7",
            "Referer" to "https://fanqienovel.com/",
            "X-Requested-With" to "XMLHttpRequest",
            "Content-Type" to "application/json"
        )
    }
}
