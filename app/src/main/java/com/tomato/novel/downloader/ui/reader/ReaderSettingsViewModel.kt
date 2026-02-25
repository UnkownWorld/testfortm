package com.tomato.novel.downloader.ui.reader

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
import javax.inject.Inject

/**
 * 阅读设置ViewModel
 * 
 * 管理阅读器设置状态
 */
@HiltViewModel
class ReaderSettingsViewModel @Inject constructor(
    @ApplicationContext private val context: Context
) : ViewModel() {

    private val Context.dataStore by preferencesDataStore(name = "reader_settings")

    // 设置键
    private object PreferencesKeys {
        val FONT_SIZE = intPreferencesKey("font_size")
        val LINE_SPACING = floatPreferencesKey("line_spacing")
        val BRIGHTNESS = intPreferencesKey("brightness")
        val BACKGROUND_COLOR = stringPreferencesKey("background_color")
    }

    // 默认值
    companion object {
        const val DEFAULT_FONT_SIZE = 18
        const val DEFAULT_LINE_SPACING = 1.8f
        const val DEFAULT_BRIGHTNESS = 50
        val DEFAULT_BG_COLOR = ReaderBgColor.WHITE
    }

    // 字体大小
    private val _fontSize = MutableLiveData<Int>()
    val fontSize: LiveData<Int> = _fontSize

    // 行间距
    private val _lineSpacing = MutableLiveData<Float>()
    val lineSpacing: LiveData<Float> = _lineSpacing

    // 亮度
    private val _brightness = MutableLiveData<Int>()
    val brightness: LiveData<Int> = _brightness

    // 背景颜色
    private val _backgroundColor = MutableLiveData<ReaderBgColor>()
    val backgroundColor: LiveData<ReaderBgColor> = _backgroundColor

    init {
        loadSettings()
    }

    /**
     * 加载设置
     */
    private fun loadSettings() {
        viewModelScope.launch {
            context.dataStore.data.map { preferences ->
                ReaderSettings(
                    fontSize = preferences[PreferencesKeys.FONT_SIZE] ?: DEFAULT_FONT_SIZE,
                    lineSpacing = preferences[PreferencesKeys.LINE_SPACING] ?: DEFAULT_LINE_SPACING,
                    brightness = preferences[PreferencesKeys.BRIGHTNESS] ?: DEFAULT_BRIGHTNESS,
                    backgroundColor = try {
                        ReaderBgColor.valueOf(preferences[PreferencesKeys.BACKGROUND_COLOR] ?: DEFAULT_BG_COLOR.name)
                    } catch (e: Exception) {
                        DEFAULT_BG_COLOR
                    }
                )
            }.first().let { settings ->
                _fontSize.value = settings.fontSize
                _lineSpacing.value = settings.lineSpacing
                _brightness.value = settings.brightness
                _backgroundColor.value = settings.backgroundColor
            }
        }
    }

    /**
     * 设置字体大小
     */
    fun setFontSize(size: Int) {
        viewModelScope.launch {
            context.dataStore.edit { preferences ->
                preferences[PreferencesKeys.FONT_SIZE] = size.coerceIn(12, 32)
            }
            _fontSize.value = size.coerceIn(12, 32)
        }
    }

    /**
     * 设置行间距
     */
    fun setLineSpacing(spacing: Float) {
        viewModelScope.launch {
            context.dataStore.edit { preferences ->
                preferences[PreferencesKeys.LINE_SPACING] = spacing.coerceIn(1.0f, 3.0f)
            }
            _lineSpacing.value = spacing.coerceIn(1.0f, 3.0f)
        }
    }

    /**
     * 设置亮度
     */
    fun setBrightness(brightness: Int) {
        viewModelScope.launch {
            context.dataStore.edit { preferences ->
                preferences[PreferencesKeys.BRIGHTNESS] = brightness.coerceIn(0, 100)
            }
            _brightness.value = brightness.coerceIn(0, 100)
        }
    }

    /**
     * 设置背景颜色
     */
    fun setBackgroundColor(color: ReaderBgColor) {
        viewModelScope.launch {
            context.dataStore.edit { preferences ->
                preferences[PreferencesKeys.BACKGROUND_COLOR] = color.name
            }
            _backgroundColor.value = color
        }
    }

    /**
     * 重置设置
     */
    fun resetSettings() {
        viewModelScope.launch {
            context.dataStore.edit { preferences ->
                preferences[PreferencesKeys.FONT_SIZE] = DEFAULT_FONT_SIZE
                preferences[PreferencesKeys.LINE_SPACING] = DEFAULT_LINE_SPACING
                preferences[PreferencesKeys.BRIGHTNESS] = DEFAULT_BRIGHTNESS
                preferences[PreferencesKeys.BACKGROUND_COLOR] = DEFAULT_BG_COLOR.name
            }
            _fontSize.value = DEFAULT_FONT_SIZE
            _lineSpacing.value = DEFAULT_LINE_SPACING
            _brightness.value = DEFAULT_BRIGHTNESS
            _backgroundColor.value = DEFAULT_BG_COLOR
        }
    }

    /**
     * 设置数据类
     */
    data class ReaderSettings(
        val fontSize: Int,
        val lineSpacing: Float,
        val brightness: Int,
        val backgroundColor: ReaderBgColor
    )
}
