package com.tomato.novel.downloader.utils

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import com.tomato.novel.downloader.data.model.ChapterContent
import java.io.*
import java.util.zip.ZipEntry
import java.util.zip.ZipOutputStream

/**
 * EPUB生成工具类
 * 
 * 用于将小说内容导出为EPUB格式
 * EPUB本质上是一个包含特定结构的ZIP文件
 */
object EpubUtils {

    /**
     * 生成EPUB文件
     * 
     * @param context 上下文
     * @param bookName 书名
     * @param author 作者
     * @param description 简介
     * @param coverPath 封面路径（可选）
     * @param chapters 章节列表
     * @param outputPath 输出路径
     * @return 生成的文件路径
     */
    fun generateEpub(
        context: Context,
        bookName: String,
        author: String,
        description: String,
        coverPath: String? = null,
        chapters: List<ChapterContent>,
        outputPath: String
    ): String {
        val file = File(outputPath)
        ZipOutputStream(BufferedOutputStream(FileOutputStream(file))).use { zipOut ->
            // 1. 添加mimetype文件（必须第一个且不压缩）
            zipOut.putNextEntry(ZipEntry("mimetype"))
            zipOut.write("application/epub+zip".toByteArray())
            zipOut.closeEntry()

            // 2. 添加container.xml
            addContainerXml(zipOut)

            // 3. 添加content.opf
            addContentOpf(zipOut, bookName, author, description, chapters)

            // 4. 添加toc.ncx
            addTocNcx(zipOut, bookName, chapters)

            // 5. 添加封面（如果有）
            coverPath?.let { path ->
                addCoverImage(zipOut, path)
            }

            // 6. 添加CSS样式
            addStylesheet(zipOut)

            // 7. 添加章节内容
            chapters.forEachIndexed { index, chapter ->
                addChapter(zipOut, index + 1, chapter.title, chapter.content)
            }
        }
        return file.absolutePath
    }

    /**
     * 添加container.xml
     */
    private fun addContainerXml(zipOut: ZipOutputStream) {
        zipOut.putNextEntry(ZipEntry("META-INF/container.xml"))
        val content = """<?xml version="1.0" encoding="UTF-8"?>
<container version="1.0" xmlns="urn:oasis:names:tc:opendocument:xmlns:container">
    <rootfiles>
        <rootfile full-path="OEBPS/content.opf" media-type="application/oebps-package+xml"/>
    </rootfiles>
</container>"""
        zipOut.write(content.toByteArray())
        zipOut.closeEntry()
    }

    /**
     * 添加content.opf
     */
    private fun addContentOpf(
        zipOut: ZipOutputStream,
        bookName: String,
        author: String,
        description: String,
        chapters: List<ChapterContent>
    ) {
        zipOut.putNextEntry(ZipEntry("OEBPS/content.opf"))
        
        val manifest = StringBuilder()
        val spine = StringBuilder()
        
        // 添加CSS
        manifest.append("    <item id=\"css\" href=\"stylesheet.css\" media-type=\"text/css\"/>\n")
        
        // 添加章节
        chapters.forEachIndexed { index, _ ->
            val chapterId = "chapter${index + 1}"
            manifest.append("    <item id=\"$chapterId\" href=\"chapter${index + 1}.xhtml\" media-type=\"application/xhtml+xml\"/>\n")
            spine.append("    <itemref idref=\"$chapterId\"/>\n")
        }

        val content = """<?xml version="1.0" encoding="UTF-8"?>
<package xmlns="http://www.idpf.org/2007/opf" version="3.0" unique-identifier="uid">
    <metadata xmlns:dc="http://purl.org/dc/elements/1.1/">
        <dc:identifier id="uid">urn:uuid:${java.util.UUID.randomUUID()}</dc:identifier>
        <dc:title>$bookName</dc:title>
        <dc:creator>$author</dc:creator>
        <dc:description>$description</dc:description>
        <dc:language>zh</dc:language>
        <meta property="dcterms:modified">${java.time.Instant.now().toString().substring(0, 19)}Z</meta>
    </metadata>
    <manifest>
$manifest    </manifest>
    <spine>
$spine    </spine>
</package>"""
        zipOut.write(content.toByteArray())
        zipOut.closeEntry()
    }

    /**
     * 添加toc.ncx
     */
    private fun addTocNcx(
        zipOut: ZipOutputStream,
        bookName: String,
        chapters: List<ChapterContent>
    ) {
        zipOut.putNextEntry(ZipEntry("OEBPS/toc.ncx"))
        
        val navPoints = StringBuilder()
        chapters.forEachIndexed { index, chapter ->
            navPoints.append("""
    <navPoint id="navpoint-${index + 1}" playOrder="${index + 1}">
        <navLabel><text>${chapter.title}</text></navLabel>
        <content src="chapter${index + 1}.xhtml"/>
    </navPoint>""")
        }

        val content = """<?xml version="1.0" encoding="UTF-8"?>
<ncx xmlns="http://www.daisy.org/z3986/2005/ncx/" version="2005-1">
    <head>
        <meta name="dtb:uid" content="urn:uuid:${java.util.UUID.randomUUID()}"/>
        <meta name="dtb:depth" content="1"/>
        <meta name="dtb:totalPageCount" content="0"/>
        <meta name="dtb:maxPageNumber" content="0"/>
    </head>
    <docTitle><text>$bookName</text></docTitle>
    <navMap>$navPoints
    </navMap>
</ncx>"""
        zipOut.write(content.toByteArray())
        zipOut.closeEntry()
    }

    /**
     * 添加封面图片
     */
    private fun addCoverImage(zipOut: ZipOutputStream, coverPath: String) {
        try {
            val coverFile = File(coverPath)
            if (coverFile.exists()) {
                zipOut.putNextEntry(ZipEntry("OEBPS/cover.jpg"))
                FileInputStream(coverFile).use { input ->
                    input.copyTo(zipOut)
                }
                zipOut.closeEntry()
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    /**
     * 添加CSS样式
     */
    private fun addStylesheet(zipOut: ZipOutputStream) {
        zipOut.putNextEntry(ZipEntry("OEBPS/stylesheet.css"))
        val css = """
body {
    font-family: "Microsoft YaHei", "SimSun", sans-serif;
    line-height: 1.8;
    margin: 1em;
    text-align: justify;
}
h1 {
    text-align: center;
    font-size: 1.5em;
    margin: 1em 0;
}
p {
    text-indent: 2em;
    margin: 0.5em 0;
}
"""
        zipOut.write(css.toByteArray())
        zipOut.closeEntry()
    }

    /**
     * 添加章节内容
     */
    private fun addChapter(
        zipOut: ZipOutputStream,
        chapterNum: Int,
        title: String,
        content: String
    ) {
        zipOut.putNextEntry(ZipEntry("OEBPS/chapter$chapterNum.xhtml"))
        
        // 处理内容，将换行转换为段落
        val paragraphs = content.split("\n")
            .filter { it.isNotBlank() }
            .joinToString("\n") { "<p>${escapeHtml(it.trim())}</p>" }

        val xhtml = """<?xml version="1.0" encoding="UTF-8"?>
<!DOCTYPE html>
<html xmlns="http://www.w3.org/1999/xhtml">
<head>
    <title>$title</title>
    <link rel="stylesheet" type="text/css" href="stylesheet.css"/>
</head>
<body>
    <h1>$title</h1>
$paragraphs
</body>
</html>"""
        zipOut.write(xhtml.toByteArray(Charsets.UTF_8))
        zipOut.closeEntry()
    }

    /**
     * HTML转义
     */
    private fun escapeHtml(text: String): String {
        return text
            .replace("&", "&amp;")
            .replace("<", "&lt;")
            .replace(">", "&gt;")
            .replace("\"", "&quot;")
            .replace("'", "&#39;")
    }
}
