package com.tomato.novel.downloader.data.model

import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import kotlinx.parcelize.Parcelize

/**
 * 小说信息数据模型
 * 
 * 用于表示从番茄小说API获取的小说基本信息
 * 
 * @property id 小说唯一标识符
 * @property name 小说名称
 * @property author 作者名称
 * @property description 小说简介
 * @property coverUrl 封面图片URL
 * @property chapterCount 章节总数
 * @property status 连载状态（连载中/已完结）
 * @property wordCount 总字数
 */
@Parcelize
data class BookInfo(
    @SerializedName("book_id")
    val id: String,
    
    @SerializedName("book_name")
    val name: String,
    
    @SerializedName("author_name")
    val author: String,
    
    @SerializedName("abstract")
    val description: String,
    
    @SerializedName("thumb_url")
    val coverUrl: String? = null,
    
    @SerializedName("chapter_count")
    val chapterCount: Int = 0,
    
    @SerializedName("creation_status")
    val status: Int = 0,
    
    @SerializedName("word_count")
    val wordCount: Long = 0
) : Parcelable {

    /**
     * 获取连载状态描述
     */
    fun getStatusText(): String {
        return when (status) {
            1 -> "连载中"
            2 -> "已完结"
            else -> "未知"
        }
    }

    /**
     * 获取格式化的字数显示
     */
    fun getFormattedWordCount(): String {
        return when {
            wordCount >= 10000 -> String.format("%.1f万字", wordCount / 10000.0)
            else -> "${wordCount}字"
        }
    }
}

/**
 * 章节信息数据模型
 * 
 * 用于表示小说中的单个章节信息
 * 
 * @property id 章节唯一标识符
 * @property title 章节标题
 * @property index 章节序号（从0开始）
 * @property isVip 是否为VIP章节
 * @property isFree 是否为免费章节
 */
@Parcelize
data class ChapterInfo(
    @SerializedName("item_id")
    val id: String,
    
    @SerializedName("title")
    val title: String,
    
    @SerializedName("index")
    val index: Int,
    
    @SerializedName("is_vip")
    val isVip: Boolean = false,
    
    @SerializedName("is_free")
    val isFree: Boolean = true
) : Parcelable

/**
 * 章节内容数据模型
 * 
 * 用于表示章节的完整内容
 * 
 * @property id 章节ID
 * @property title 章节标题
 * @property content 章节内容文本
 */
@Parcelize
data class ChapterContent(
    @SerializedName("item_id")
    val id: String,
    
    @SerializedName("title")
    val title: String,
    
    @SerializedName("content")
    val content: String
) : Parcelable

/**
 * 章节目录响应数据模型
 * 
 * 用于解析番茄小说章节目录API的响应
 */
data class ChapterDirectoryResponse(
    @SerializedName("data")
    val data: ChapterDirectoryData?
)

/**
 * 章节目录数据
 */
data class ChapterDirectoryData(
    @SerializedName("allItemIds")
    val allItemIds: List<String>
)

/**
 * 批量章节内容响应数据模型
 * 
 * 用于解析批量获取章节内容API的响应
 * 键为章节ID，值为章节内容对象
 */
typealias BatchChapterContentResponse = Map<String, ChapterContent>
