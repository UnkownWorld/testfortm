package com.tomato.novel.downloader.utils

import org.junit.Assert.*
import org.junit.Test

/**
 * ContentUtils 单元测试
 */
class ContentUtilsTest {

    @Test
    fun `processChapterContent should return empty for blank input`() {
        // 测试空输入
        val result = ContentUtils.processChapterContent("")
        assertEquals("空输入应返回空字符串", "", result)
    }

    @Test
    fun `processChapterContent should return empty for null-like input`() {
        // 测试null类型输入
        val result = ContentUtils.processChapterContent("   ")
        assertEquals("空白输入应返回空字符串", "", result)
    }

    @Test
    fun `processChapterContent should add indentation to paragraphs`() {
        // 测试段落缩进
        val input = "第一段内容\n第二段内容"
        val result = ContentUtils.processChapterContent(input)
        
        assertTrue("应添加中文缩进", result.contains("　　"))
    }

    @Test
    fun `processChapterContent should remove HTML tags`() {
        // 测试HTML标签移除
        val input = "<p>测试内容</p><div>其他内容</div>"
        val result = ContentUtils.processChapterContent(input)
        
        assertFalse("不应包含HTML标签", result.contains("<p>"))
        assertFalse("不应包含div标签", result.contains("<div>"))
    }

    @Test
    fun `processChapterContent should extract content from p tags`() {
        // 测试从p标签提取内容
        val input = "<p idx=\"1\">段落一</p><p idx=\"2\">段落二</p>"
        val result = ContentUtils.processChapterContent(input)
        
        assertTrue("应包含段落一", result.contains("段落一"))
        assertTrue("应包含段落二", result.contains("段落二"))
    }

    @Test
    fun `processChapterContent should compress multiple newlines`() {
        // 测试压缩多余空行
        val input = "内容\n\n\n\n\n更多内容"
        val result = ContentUtils.processChapterContent(input)
        
        assertFalse("不应包含连续三个以上换行", result.contains("\n\n\n"))
    }
}
