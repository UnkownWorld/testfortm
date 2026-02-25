package com.tomato.novel.downloader.ui.settings

import android.content.Context
import androidx.datastore.preferences.core.*
import androidx.datastore.preferences.preferencesDataStore
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import java.io.File
import javax.inject.Inject

/**
 * 设置页面ViewModel
 * 
 * 管理应用设置状态：
 * - 深色模式
 * - 下载路径
 * - 默认格式
 * - 缓存大小
 */
@HiltViewModel
class SettingsViewModel @Inject constructor(
    @ApplicationContext private val context: Context
) : ViewModel() {

    private val Context.dataStore by preferencesDataStore(name = "settings")

    // 设置键
    private object PreferencesKeys {
        val DARK_MODE = booleanPreferencesKey("dark_mode")
        val AUTO_DOWNLOAD_COVER = booleanPreferencesKey("auto_download_cover")
        val DOWNLOAD_PATH = stringPreferencesKey("download_path")
        val DEFAULT_FORMAT = stringPreferencesKey("default_format")
    }

    // 深色模式
    private val _darkMode = MutableLiveData<Boolean>()
    val darkMode: LiveData<Boolean> = _darkMode

    // 自动下载封面
    private val _autoDownloadCover = MutableLiveData<Boolean>()
    val autoDownloadCover: LiveData<Boolean> = _autoDownloadCover

    // 下载路径
    private val _downloadPath = MutableLiveData<String>()
    val downloadPath: LiveData<String> = _downloadPath

    // 默认格式
    private val _defaultFormat = MutableLiveData<String>()
    val defaultFormat: LiveData<String> = _defaultFormat

    // 缓存大小
    private val _cacheSize = MutableLiveData<String>()
    val cacheSize: LiveData<String> = _cacheSize

    init {
        loadSettings()
        calculateCacheSize()
    }

    /**
     * 加载设置
     */
    private fun loadSettings() {
        viewModelScope.launch {
            context.dataStore.data.map { preferences ->
                Settings(
                    darkMode = preferences[PreferencesKeys.DARK_MODE] ?: false,
                    autoDownloadCover = preferences[PreferencesKeys.AUTO_DOWNLOAD_COVER] ?: true,
                    downloadPath = preferences[PreferencesKeys.DOWNLOAD_PATH] 
                        ?: getDefaultDownloadPath(),
                    defaultFormat = preferences[PreferencesKeys.DEFAULT_FORMAT] ?: "txt"
                )
            }.first().let { settings ->
                _darkMode.value = settings.darkMode
                _autoDownloadCover.value = settings.autoDownloadCover
                _downloadPath.value = settings.downloadPath
                _defaultFormat.value = settings.defaultFormat
            }
        }
    }

    /**
     * 设置深色模式
     */
    fun setDarkMode(enabled: Boolean) {
        viewModelScope.launch {
            context.dataStore.edit { preferences ->
                preferences[PreferencesKeys.DARK_MODE] = enabled
            }
            _darkMode.value = enabled
        }
    }

    /**
     * 设置自动下载封面
     */
    fun setAutoDownloadCover(enabled: Boolean) {
        viewModelScope.launch {
            context.dataStore.edit { preferences ->
                preferences[PreferencesKeys.AUTO_DOWNLOAD_COVER] = enabled
            }
            _autoDownloadCover.value = enabled
        }
    }

    /**
     * 设置下载路径
     */
    fun setDownloadPath(path: String) {
        viewModelScope.launch {
            context.dataStore.edit { preferences ->
                preferences[PreferencesKeys.DOWNLOAD_PATH] = path
            }
            _downloadPath.value = path
        }
    }

    /**
     * 设置默认格式
     */
    fun setDefaultFormat(format: String) {
        viewModelScope.launch {
            context.dataStore.edit { preferences ->
                preferences[PreferencesKeys.DEFAULT_FORMAT] = format
            }
            _defaultFormat.value = format
        }
    }

    /**
     * 清理缓存
     */
    fun clearCache() {
        viewModelScope.launch {
            try {
                // 清理缓存目录
                context.cacheDir.deleteRecursively()
                context.cacheDir.mkdirs()
                
                // 重新计算缓存大小
                calculateCacheSize()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    /**
     * 计算缓存大小
     */
    private fun calculateCacheSize() {
        viewModelScope.launch {
            try {
                val size = getFolderSize(context.cacheDir)
                _cacheSize.value = formatFileSize(size)
            } catch (e: Exception) {
                _cacheSize.value = "0 B"
            }
        }
    }

    /**
     * 获取默认下载路径
     */
    private fun getDefaultDownloadPath(): String {
        return File(
            android.os.Environment.getExternalStoragePublicDirectory(
                android.os.Environment.DIRECTORY_DOWNLOADS
            ),
            "TomatoNovelDownloader"
        ).absolutePath
    }

    /**
     * 获取文件夹大小
     */
    private fun getFolderSize(folder: File): Long {
        var size: Long = 0
        folder.listFiles()?.forEach { file ->
            size += if (file.isDirectory) {
                getFolderSize(file)
            } else {
                file.length()
            }
        }
        return size
    }

    /**
     * 格式化文件大小
     */
    private fun formatFileSize(bytes: Long): String {
        return when {
            bytes < 1024 -> "$bytes B"
            bytes < 1024 * 1024 -> String.format("%.1f KB", bytes / 1024.0)
            bytes < 1024 * 1024 * 1024 -> String.format("%.1f MB", bytes / (1024.0 * 1024))
            else -> String.format("%.1f GB", bytes / (1024.0 * 1024 * 1024))
        }
    }

    /**
     * 设置数据类
     */
    data class Settings(
        val darkMode: Boolean,
        val autoDownloadCover: Boolean,
        val downloadPath: String,
        val defaultFormat: String
    )
}
