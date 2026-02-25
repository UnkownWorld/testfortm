package com.tomato.novel.downloader

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.os.Build
import dagger.hilt.android.HiltAndroidApp

/**
 * 番茄小说下载器应用程序入口类
 * 
 * 职责：
 * - 初始化Hilt依赖注入框架
 * - 创建通知渠道
 * - 配置全局异常处理
 */
@HiltAndroidApp
class TomatoNovelApp : Application() {

    companion object {
        const val CHANNEL_ID_DOWNLOAD = "download_channel"
        const val CHANNEL_NAME_DOWNLOAD = "下载通知"
        const val CHANNEL_DESC_DOWNLOAD = "小说下载进度通知"
    }

    override fun onCreate() {
        super.onCreate()
        initNotificationChannels()
        setupExceptionHandler()
    }

    /**
     * 初始化通知渠道
     * Android 8.0+ 需要创建通知渠道才能显示通知
     */
    private fun initNotificationChannels() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val notificationManager = getSystemService(NotificationManager::class.java)
            
            val downloadChannel = NotificationChannel(
                CHANNEL_ID_DOWNLOAD,
                CHANNEL_NAME_DOWNLOAD,
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = CHANNEL_DESC_DOWNLOAD
                setShowBadge(false)
            }
            
            notificationManager.createNotificationChannel(downloadChannel)
        }
    }

    /**
     * 配置全局异常处理
     * 捕获未处理的异常，防止应用崩溃
     */
    private fun setupExceptionHandler() {
        Thread.setDefaultUncaughtExceptionHandler { thread, throwable ->
            // 记录异常日志，可扩展为上报到崩溃监控平台
            throwable.printStackTrace()
        }
    }
}
