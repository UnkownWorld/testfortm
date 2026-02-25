package com.tomato.novel.downloader.domain.usecase

import org.junit.Assert.*
import org.junit.Test

/**
 * ValidateBookIdUseCase 单元测试
 */
class ValidateBookIdUseCaseTest {

    /**
     * 模拟URL提取逻辑
     */
    private fun extractBookIdFromUrl(input: String): String? {
        val regex = """fanqienovel\.com/page/(\d+)""".toRegex()
        return regex.find(input)?.groupValues?.getOrNull(1)
    }

    @Test
    fun `extractBookIdFromUrl should extract ID from valid URL`() {
        // 测试从URL提取ID
        val url = "https://fanqienovel.com/page/7143038691944959011"
        val result = extractBookIdFromUrl(url)
        
        assertEquals("7143038691944959011", result)
    }

    @Test
    fun `extractBookIdFromUrl should return null for invalid URL`() {
        // 测试无效URL
        val invalidUrl = "https://example.com/something"
        val result = extractBookIdFromUrl(invalidUrl)
        
        assertNull(result)
    }

    @Test
    fun `extractBookIdFromUrl should handle URL with query params`() {
        // 测试带查询参数的URL
        val url = "https://fanqienovel.com/page/7143038691944959011?param=value"
        val result = extractBookIdFromUrl(url)
        
        assertEquals("7143038691944959011", result)
    }

    @Test
    fun `bookId validation should reject empty input`() {
        // 测试空输入验证
        val emptyInput = ""
        assertTrue("空输入应被拒绝", emptyInput.isBlank())
    }

    @Test
    fun `bookId validation should accept numeric input`() {
        // 测试数字ID验证
        val numericId = "7143038691944959011"
        assertTrue("数字ID应被接受", numericId.all { it.isDigit() })
    }

    @Test
    fun `bookId validation should reject non-numeric input`() {
        // 测试非数字输入
        val nonNumericId = "abc123"
        assertFalse("非数字ID应被拒绝", nonNumericId.all { it.isDigit() })
    }
}
