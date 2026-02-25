package com.tomato.novel.downloader.utils

import org.junit.Assert.*
import org.junit.Test

/**
 * FileUtils 单元测试
 */
class FileUtilsTest {

    @Test
    fun `sanitizeFileName should remove invalid characters`() {
        // 测试清理非法字符
        val input = "小说:名称/测试\\文件*名?"
        val result = FileUtils.sanitizeFileName(input)
        
        assertFalse("不应包含冒号", result.contains(":"))
        assertFalse("不应包含斜杠", result.contains("/"))
        assertFalse("不应包含反斜杠", result.contains("\\"))
        assertFalse("不应包含星号", result.contains("*"))
        assertFalse("不应包含问号", result.contains("?"))
    }

    @Test
    fun `sanitizeFileName should keep valid characters`() {
        // 测试保留有效字符
        val input = "小说名称_测试-123"
        val result = FileUtils.sanitizeFileName(input)
        
        assertEquals("有效字符应保持不变", input, result)
    }

    @Test
    fun `formatFileSize should format bytes correctly`() {
        // 测试字节格式化
        assertEquals("0 B", FileUtils.formatFileSize(0))
        assertEquals("100 B", FileUtils.formatFileSize(100))
        assertEquals("1.0 KB", FileUtils.formatFileSize(1024))
        assertEquals("1.0 MB", FileUtils.formatFileSize(1024 * 1024))
        assertEquals("1.0 GB", FileUtils.formatFileSize(1024L * 1024 * 1024))
    }

    @Test
    fun `formatFileSize should handle large values`() {
        // 测试大数值
        val bytes = 1536L // 1.5 KB
        val result = FileUtils.formatFileSize(bytes)
        assertTrue("应包含KB单位", result.contains("KB"))
    }
}
