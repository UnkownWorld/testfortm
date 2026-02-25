package com.tomato.novel.downloader.ui.settings

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.google.android.material.switchmaterial.SwitchMaterial
import com.tomato.novel.downloader.R
import com.tomato.novel.downloader.databinding.FragmentSettingsBinding
import dagger.hilt.android.AndroidEntryPoint

/**
 * 设置页面Fragment
 * 
 * 提供应用设置功能：
 * - 深色模式切换
 * - 默认下载路径设置
 * - 下载格式选择
 * - 缓存清理
 * - 关于信息
 */
@AndroidEntryPoint
class SettingsFragment : Fragment() {

    private var _binding: FragmentSettingsBinding? = null
    private val binding get() = _binding!!
    
    private val viewModel: SettingsViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSettingsBinding.inflate(inflater, container, false)
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
        // 深色模式开关
        binding.switchDarkMode.setOnCheckedChangeListener { _, isChecked ->
            viewModel.setDarkMode(isChecked)
        }

        // 自动下载封面
        binding.switchAutoCover.setOnCheckedChangeListener { _, isChecked ->
            viewModel.setAutoDownloadCover(isChecked)
        }

        // 下载路径选择
        binding.layoutDownloadPath.setOnClickListener {
            showDownloadPathDialog()
        }

        // 默认格式选择
        binding.layoutDefaultFormat.setOnClickListener {
            showFormatSelectionDialog()
        }

        // 清理缓存
        binding.layoutClearCache.setOnClickListener {
            viewModel.clearCache()
        }

        // 关于
        binding.layoutAbout.setOnClickListener {
            showAboutDialog()
        }
    }

    /**
     * 观察数据变化
     */
    private fun observeData() {
        viewModel.darkMode.observe(viewLifecycleOwner) { isDarkMode ->
            binding.switchDarkMode.isChecked = isDarkMode
        }

        viewModel.autoDownloadCover.observe(viewLifecycleOwner) { autoCover ->
            binding.switchAutoCover.isChecked = autoCover
        }

        viewModel.downloadPath.observe(viewLifecycleOwner) { path ->
            binding.tvDownloadPath.text = path
        }

        viewModel.defaultFormat.observe(viewLifecycleOwner) { format ->
            binding.tvDefaultFormat.text = format.uppercase()
        }

        viewModel.cacheSize.observe(viewLifecycleOwner) { size ->
            binding.tvCacheSize.text = size
        }
    }

    /**
     * 显示下载路径选择对话框
     */
    private fun showDownloadPathDialog() {
        // 实现路径选择对话框
    }

    /**
     * 显示格式选择对话框
     */
    private fun showFormatSelectionDialog() {
        val formats = arrayOf("TXT", "EPUB")
        val currentIndex = if (viewModel.defaultFormat.value == "epub") 1 else 0
        
        android.app.AlertDialog.Builder(requireContext())
            .setTitle(R.string.select_format)
            .setSingleChoiceItems(formats, currentIndex) { dialog, which ->
                viewModel.setDefaultFormat(if (which == 0) "txt" else "epub")
                dialog.dismiss()
            }
            .show()
    }

    /**
     * 显示关于对话框
     */
    private fun showAboutDialog() {
        android.app.AlertDialog.Builder(requireContext())
            .setTitle(R.string.about)
            .setMessage(R.string.about_message)
            .setPositiveButton(R.string.confirm, null)
            .show()
    }
}
