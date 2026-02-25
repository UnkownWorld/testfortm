package com.tomato.novel.downloader.ui.bookshelf

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.tomato.novel.downloader.data.local.BookshelfEntity
import com.tomato.novel.downloader.databinding.ItemBookshelfBinding

/**
 * 书架适配器
 * 
 * 用于显示书架中的书籍列表
 */
class BookshelfAdapter(
    private val onItemClick: (BookshelfEntity) -> Unit,
    private val onItemLongClick: (BookshelfEntity) -> Unit
) : ListAdapter<BookshelfEntity, BookshelfAdapter.ViewHolder>(DiffCallback) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemBookshelfBinding.inflate(
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
        private val binding: ItemBookshelfBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        init {
            binding.root.setOnClickListener {
                val position = bindingAdapterPosition
                if (position != RecyclerView.NO_POSITION) {
                    onItemClick(getItem(position))
                }
            }

            binding.root.setOnLongClickListener {
                val position = bindingAdapterPosition
                if (position != RecyclerView.NO_POSITION) {
                    onItemLongClick(getItem(position))
                }
                true
            }
        }

        fun bind(book: BookshelfEntity) {
            binding.apply {
                tvBookName.text = book.bookName
                tvAuthor.text = book.author
                
                // 显示阅读进度
                if (book.totalChapters > 0) {
                    val progress = (book.lastReadChapter * 100) / book.totalChapters
                    tvProgress.text = "阅读进度: $progress%"
                } else {
                    tvProgress.text = "未阅读"
                }

                // 封面图片（如果有）
                // Glide.with(ivCover).load(book.coverUrl).into(ivCover)
            }
        }
    }

    companion object DiffCallback : DiffUtil.ItemCallback<BookshelfEntity>() {
        override fun areItemsTheSame(oldItem: BookshelfEntity, newItem: BookshelfEntity): Boolean {
            return oldItem.bookId == newItem.bookId
        }

        override fun areContentsTheSame(oldItem: BookshelfEntity, newItem: BookshelfEntity): Boolean {
            return oldItem == newItem
        }
    }
}
