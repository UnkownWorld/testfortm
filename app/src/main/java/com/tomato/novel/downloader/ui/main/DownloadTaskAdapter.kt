package com.tomato.novel.downloader.ui.main

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.tomato.novel.downloader.data.local.DownloadStatus
import com.tomato.novel.downloader.databinding.ItemDownloadTaskBinding
import com.tomato.novel.downloader.domain.model.DownloadTask
import com.tomato.novel.downloader.utils.FileUtils

/**
 * 下载任务列表适配器
 * 
 * 使用DiffUtil优化列表更新
 * 支持不同下载状态的UI展示
 */
class DownloadTaskAdapter(
    private val onPauseClick: (DownloadTask) -> Unit,
    private val onResumeClick: (DownloadTask) -> Unit,
    private val onCancelClick: (DownloadTask) -> Unit,
    private val onDeleteClick: (DownloadTask) -> Unit
) : ListAdapter<DownloadTask, DownloadTaskAdapter.ViewHolder>(DiffCallback) {
    
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemDownloadTaskBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return ViewHolder(binding)
    }
    
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(getItem(position))
    }
    
    inner class ViewHolder(
        private val binding: ItemDownloadTaskBinding
    ) : RecyclerView.ViewHolder(binding.root) {
        
        fun bind(task: DownloadTask) {
            binding.apply {
                // 基本信息
                tvBookName.text = task.bookName
                tvAuthor.text = task.author
                tvStatus.text = task.getStatusText()
                tvProgress.text = "${task.downloadedChapters}/${task.totalChapters}"
                
                // 进度条
                progressBar.max = task.totalChapters
                progressBar.progress = task.downloadedChapters
                
                // 时间
                tvTime.text = FileUtils.formatTime(task.updateTime)
                
                // 根据状态设置按钮
                setupButtons(task)
            }
        }
        
        private fun setupButtons(task: DownloadTask) {
            binding.apply {
                when (task.status) {
                    DownloadStatus.DOWNLOADING -> {
                        btnPause.visibility = android.view.View.VISIBLE
                        btnResume.visibility = android.view.View.GONE
                        btnCancel.visibility = android.view.View.VISIBLE
                        btnDelete.visibility = android.view.View.GONE
                    }
                    DownloadStatus.PAUSED -> {
                        btnPause.visibility = android.view.View.GONE
                        btnResume.visibility = android.view.View.VISIBLE
                        btnCancel.visibility = android.view.View.VISIBLE
                        btnDelete.visibility = android.view.View.GONE
                    }
                    DownloadStatus.PENDING -> {
                        btnPause.visibility = android.view.View.GONE
                        btnResume.visibility = android.view.View.GONE
                        btnCancel.visibility = android.view.View.VISIBLE
                        btnDelete.visibility = android.view.View.GONE
                    }
                    DownloadStatus.COMPLETED, DownloadStatus.FAILED, DownloadStatus.CANCELLED -> {
                        btnPause.visibility = android.view.View.GONE
                        btnResume.visibility = android.view.View.GONE
                        btnCancel.visibility = android.view.View.GONE
                        btnDelete.visibility = android.view.View.VISIBLE
                    }
                }
                
                // 点击事件
                btnPause.setOnClickListener { onPauseClick(task) }
                btnResume.setOnClickListener { onResumeClick(task) }
                btnCancel.setOnClickListener { onCancelClick(task) }
                btnDelete.setOnClickListener { onDeleteClick(task) }
            }
        }
    }
    
    companion object DiffCallback : DiffUtil.ItemCallback<DownloadTask>() {
        override fun areItemsTheSame(oldItem: DownloadTask, newItem: DownloadTask): Boolean {
            return oldItem.id == newItem.id
        }
        
        override fun areContentsTheSame(oldItem: DownloadTask, newItem: DownloadTask): Boolean {
            return oldItem == newItem
        }
    }
}
