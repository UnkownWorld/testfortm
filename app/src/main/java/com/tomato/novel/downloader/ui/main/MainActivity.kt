package com.tomato.novel.downloader.ui.main

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.tomato.novel.downloader.R
import com.tomato.novel.downloader.data.local.DownloadStatus
import com.tomato.novel.downloader.databinding.ActivityMainBinding
import com.tomato.novel.downloader.domain.model.DownloadTask
import com.tomato.novel.downloader.service.DownloadService
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

/**
 * 主界面Activity
 * 
 * 应用的入口界面，提供以下功能：
 * - 输入小说ID或URL搜索小说
 * - 显示小说信息和章节列表
 * - 创建下载任务
 * - 管理下载任务列表
 * 
 * 设计原则：
 * - MVVM架构：View只负责UI展示，业务逻辑在ViewModel中
 * - 响应式UI：通过LiveData观察数据变化
 * - 单一职责：Activity只处理UI相关逻辑
 */
@AndroidEntryPoint
class MainActivity : AppCompatActivity() {
    
    private lateinit var binding: ActivityMainBinding
    private val viewModel: MainViewModel by viewModels()
    private lateinit var downloadAdapter: DownloadTaskAdapter
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        
        setupViews()
        observeData()
    }
    
    /**
     * 初始化视图
     */
    private fun setupViews() {
        // 设置工具栏
        setSupportActionBar(binding.toolbar)
        
        // 设置搜索按钮
        binding.btnSearch.setOnClickListener {
            val input = binding.etBookId.text.toString().trim()
            if (input.isNotEmpty()) {
                viewModel.searchBook(input)
            } else {
                Toast.makeText(this, R.string.error_empty_book_id, Toast.LENGTH_SHORT).show()
            }
        }
        
        // 设置下载按钮
        binding.btnDownload.setOnClickListener {
            showDownloadOptionsDialog()
        }
        
        // 设置取消按钮
        binding.btnCancel.setOnClickListener {
            viewModel.resetState()
        }
        
        // 设置下载任务列表
        downloadAdapter = DownloadTaskAdapter(
            onPauseClick = { task -> viewModel.pauseDownload(task.id) },
            onResumeClick = { task -> startDownloadService(task.id) },
            onCancelClick = { task -> showCancelDialog(task) },
            onDeleteClick = { task -> showDeleteDialog(task) }
        )
        
        binding.rvDownloadTasks.apply {
            layoutManager = LinearLayoutManager(this@MainActivity)
            adapter = downloadAdapter
        }
    }
    
    /**
     * 观察数据变化
     */
    private fun observeData() {
        // 观察UI状态
        lifecycleScope.launch {
            viewModel.uiState.observe(this@MainActivity) { state ->
                when (state) {
                    is MainUiState.Idle -> showIdleState()
                    is MainUiState.Loading -> showLoadingState()
                    is MainUiState.BookLoaded -> showBookLoadedState()
                    is MainUiState.Error -> showErrorState()
                    is MainUiState.DownloadStarted -> {
                        startDownloadService(state.taskId)
                        showIdleState()
                    }
                }
            }
        }
        
        // 观察小说信息
        lifecycleScope.launch {
            viewModel.bookInfo.observe(this@MainActivity) { book ->
                book?.let {
                    binding.tvBookName.text = it.name
                    binding.tvAuthor.text = getString(R.string.author_format, it.author)
                    binding.tvDescription.text = it.description
                }
            }
        }
        
        // 观察章节列表
        lifecycleScope.launch {
            viewModel.chapterList.observe(this@MainActivity) { chapters ->
                binding.tvChapterCount.text = getString(R.string.chapter_count_format, chapters.size)
            }
        }
        
        // 观察下载任务列表
        lifecycleScope.launch {
            viewModel.downloadTasks.observe(this@MainActivity) { tasks ->
                downloadAdapter.submitList(tasks)
                binding.emptyView.visibility = if (tasks.isEmpty()) View.VISIBLE else View.GONE
            }
        }
        
        // 观察错误信息
        lifecycleScope.launch {
            viewModel.errorMessage.observe(this@MainActivity) { message ->
                message?.let {
                    Toast.makeText(this, it, Toast.LENGTH_LONG).show()
                    viewModel.clearError()
                }
            }
        }
    }
    
    /**
     * 显示空闲状态
     */
    private fun showIdleState() {
        binding.progressBar.visibility = View.GONE
        binding.groupBookInfo.visibility = View.GONE
        binding.groupInput.visibility = View.VISIBLE
        binding.etBookId.text?.clear()
    }
    
    /**
     * 显示加载状态
     */
    private fun showLoadingState() {
        binding.progressBar.visibility = View.VISIBLE
        binding.groupBookInfo.visibility = View.GONE
        binding.groupInput.visibility = View.VISIBLE
    }
    
    /**
     * 显示书籍加载完成状态
     */
    private fun showBookLoadedState() {
        binding.progressBar.visibility = View.GONE
        binding.groupBookInfo.visibility = View.VISIBLE
        binding.groupInput.visibility = View.GONE
    }
    
    /**
     * 显示错误状态
     */
    private fun showErrorState() {
        binding.progressBar.visibility = View.GONE
        binding.groupBookInfo.visibility = View.GONE
        binding.groupInput.visibility = View.VISIBLE
    }
    
    /**
     * 显示下载选项对话框
     */
    private fun showDownloadOptionsDialog() {
        val chapters = viewModel.chapterList.value ?: return
        val options = arrayOf(
            getString(R.string.download_all, chapters.size),
            getString(R.string.download_range),
            getString(R.string.download_format_epub)
        )
        
        MaterialAlertDialogBuilder(this)
            .setTitle(R.string.download_options)
            .setItems(options) { _, which ->
                when (which) {
                    0 -> viewModel.startDownload("txt")
                    1 -> showChapterRangeDialog()
                    2 -> viewModel.startDownload("epub")
                }
            }
            .show()
    }
    
    /**
     * 显示章节范围选择对话框
     */
    private fun showChapterRangeDialog() {
        // 简化实现，实际应用中可以使用自定义对话框
        Toast.makeText(this, R.string.feature_coming_soon, Toast.LENGTH_SHORT).show()
    }
    
    /**
     * 启动下载服务
     */
    private fun startDownloadService(taskId: Long) {
        val intent = Intent(this, DownloadService::class.java).apply {
            action = DownloadService.ACTION_START_DOWNLOAD
            putExtra(DownloadService.EXTRA_TASK_ID, taskId)
        }
        startService(intent)
    }
    
    /**
     * 显示取消确认对话框
     */
    private fun showCancelDialog(task: DownloadTask) {
        MaterialAlertDialogBuilder(this)
            .setTitle(R.string.cancel_download)
            .setMessage(R.string.cancel_download_message)
            .setPositiveButton(R.string.confirm) { _, _ ->
                viewModel.cancelDownload(task.id)
            }
            .setNegativeButton(R.string.cancel, null)
            .show()
    }
    
    /**
     * 显示删除确认对话框
     */
    private fun showDeleteDialog(task: DownloadTask) {
        MaterialAlertDialogBuilder(this)
            .setTitle(R.string.delete_task)
            .setMessage(R.string.delete_task_message)
            .setPositiveButton(R.string.confirm) { _, _ ->
                viewModel.deleteDownloadTask(task.id)
            }
            .setNegativeButton(R.string.cancel, null)
            .show()
    }
}
