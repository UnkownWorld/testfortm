package com.tomato.novel.downloader.ui.reader

import android.app.Dialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.SeekBar
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.activityViewModels
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.tomato.novel.downloader.R
import com.tomato.novel.downloader.databinding.DialogReaderSettingsBinding

/**
 * 阅读设置对话框
 * 
 * 提供阅读器设置选项：
 * - 字体大小调节
 * - 行间距调节
 * - 背景颜色选择
 * - 亮度调节
 */
class ReaderSettingsDialog : BottomSheetDialogFragment() {

    private var _binding: DialogReaderSettingsBinding? = null
    private val binding get() = _binding!!
    
    private val viewModel: ReaderSettingsViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = DialogReaderSettingsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupViews()
        observeData()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    /**
     * 初始化视图
     */
    private fun setupViews() {
        // 字体大小调节
        binding.seekFontSize.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                if (fromUser) {
                    val fontSize = progress + 12 // 12-32范围
                    binding.tvFontSizeValue.text = fontSize.toString()
                    viewModel.setFontSize(fontSize)
                }
            }
            override fun onStartTrackingTouch(seekBar: SeekBar?) {}
            override fun onStopTrackingTouch(seekBar: SeekBar?) {}
        })

        // 行间距调节
        binding.seekLineSpacing.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                if (fromUser) {
                    val spacing = (progress + 10) / 10f // 1.0-3.0范围
                    binding.tvLineSpacingValue.text = String.format("%.1f", spacing)
                    viewModel.setLineSpacing(spacing)
                }
            }
            override fun onStartTrackingTouch(seekBar: SeekBar?) {}
            override fun onStopTrackingTouch(seekBar: SeekBar?) {}
        })

        // 亮度调节
        binding.seekBrightness.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                if (fromUser) {
                    viewModel.setBrightness(progress)
                }
            }
            override fun onStartTrackingTouch(seekBar: SeekBar?) {}
            override fun onStopTrackingTouch(seekBar: SeekBar?) {}
        })

        // 背景颜色选择
        binding.bgWhite.setOnClickListener { viewModel.setBackgroundColor(ReaderBgColor.WHITE) }
        binding.bgSepia.setOnClickListener { viewModel.setBackgroundColor(ReaderBgColor.SEPIA) }
        binding.bgGreen.setOnClickListener { viewModel.setBackgroundColor(ReaderBgColor.GREEN) }
        binding.bgGray.setOnClickListener { viewModel.setBackgroundColor(ReaderBgColor.GRAY) }
        binding.bgBlack.setOnClickListener { viewModel.setBackgroundColor(ReaderBgColor.BLACK) }

        // 重置设置
        binding.btnReset.setOnClickListener {
            viewModel.resetSettings()
        }
    }

    /**
     * 观察数据变化
     */
    private fun observeData() {
        viewModel.fontSize.observe(viewLifecycleOwner) { size ->
            binding.seekFontSize.progress = size - 12
            binding.tvFontSizeValue.text = size.toString()
        }

        viewModel.lineSpacing.observe(viewLifecycleOwner) { spacing ->
            binding.seekLineSpacing.progress = ((spacing * 10) - 10).toInt()
            binding.tvLineSpacingValue.text = String.format("%.1f", spacing)
        }

        viewModel.brightness.observe(viewLifecycleOwner) { brightness ->
            binding.seekBrightness.progress = brightness
        }

        viewModel.backgroundColor.observe(viewLifecycleOwner) { color ->
            updateBackgroundSelection(color)
        }
    }

    /**
     * 更新背景选择状态
     */
    private fun updateBackgroundSelection(color: ReaderBgColor) {
        // 重置所有选中状态
        listOf(
            binding.bgWhite, binding.bgSepia, binding.bgGreen,
            binding.bgGray, binding.bgBlack
        ).forEach { it.isSelected = false }

        // 设置选中状态
        when (color) {
            ReaderBgColor.WHITE -> binding.bgWhite.isSelected = true
            ReaderBgColor.SEPIA -> binding.bgSepia.isSelected = true
            ReaderBgColor.GREEN -> binding.bgGreen.isSelected = true
            ReaderBgColor.GRAY -> binding.bgGray.isSelected = true
            ReaderBgColor.BLACK -> binding.bgBlack.isSelected = true
        }
    }
}

/**
 * 阅读背景颜色枚举
 */
enum class ReaderBgColor(val colorRes: Int, val textColorRes: Int) {
    WHITE(android.R.color.white, android.R.color.black),
    SEPIA(R.color.bg_sepia, android.R.color.black),
    GREEN(R.color.bg_green, android.R.color.black),
    GRAY(R.color.bg_gray, android.R.color.black),
    BLACK(android.R.color.black, android.R.color.white)
}
