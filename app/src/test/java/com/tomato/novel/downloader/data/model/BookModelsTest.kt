package com.tomato.novel.downloader.data.model

import org.junit.Assert.*
import org.junit.Test

/**
 * BookInfo 数据模型单元测试
 */
class BookInfoTest {

    @Test
    fun `getStatusText should return correct text for different status`() {
        // 测试连载状态文本
        val ongoingBook = BookInfo(
            id = "1",
            name = "测试小说",
            author = "测试作者",
            description = "测试简介",
            status = 1
        )
        assertEquals("连载中", ongoingBook.getStatusText())

        val completedBook = BookInfo(
            id = "2",
            name = "测试小说",
            author = "测试作者",
            description = "测试简介",
            status = 2
        )
        assertEquals("已完结", completedBook.getStatusText())

        val unknownBook = BookInfo(
            id = "3",
            name = "测试小说",
            author = "测试作者",
            description = "测试简介",
            status = 0
        )
        assertEquals("未知", unknownBook.getStatusText())
    }

    @Test
    fun `getFormattedWordCount should format correctly`() {
        // 测试字数格式化
        val smallBook = BookInfo(
            id = "1",
            name = "测试",
            author = "作者",
            description = "简介",
            wordCount = 5000
        )
        assertEquals("5000字", smallBook.getFormattedWordCount())

        val largeBook = BookInfo(
            id = "2",
            name = "测试",
            author = "作者",
            description = "简介",
            wordCount = 150000
        )
        assertTrue("应包含万字", largeBook.getFormattedWordCount().contains("万"))
    }
}
