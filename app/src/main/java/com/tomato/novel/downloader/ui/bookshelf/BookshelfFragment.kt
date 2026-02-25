package com.tomato.novel.downloader.ui.bookshelf

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.GridLayoutManager
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.tomato.novel.downloader.R
import com.tomato.novel.downloader.databinding.FragmentBookshelfBinding
import dagger.hilt.android.AndroidEntryPoint

/**
 * 书架页面Fragment
 * 
 * 提供书架管理功能：
 * - 显示收藏的小说
 * - 搜索书架
 * - 移除书籍
 * - 继续阅读
 */
@AndroidEntryPoint
class BookshelfFragment : Fragment() {

    private var _binding: FragmentBookshelfBinding? = null
    private val binding get() = _binding!!
    
    private val viewModel: BookshelfViewModel by viewModels()
    private lateinit var bookshelfAdapter: BookshelfAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentBookshelfBinding.inflate(inflater, container, false)
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
        // 设置书架列表
        bookshelfAdapter = BookshelfAdapter(
            onItemClick = { book -> onBookClick(book) },
            onItemLongClick = { book -> showBookOptions(book) }
        )
        
        binding.rvBookshelf.apply {
            layoutManager = GridLayoutManager(requireContext(), 3)
            adapter = bookshelfAdapter
        }

        // 搜索功能
        binding.etSearch.setOnQueryTextListener(object : 
            androidx.appcompat.widget.SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                query?.let { viewModel.search(it) }
                return true
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                if (newText.isNullOrBlank()) {
                    viewModel.clearSearch()
                }
                return true
            }
        })
    }

    /**
     * 观察数据变化
     */
    private fun observeData() {
        // 书架列表
        viewModel.books.observe(viewLifecycleOwner) { books ->
            bookshelfAdapter.submitList(books)
            binding.emptyView.visibility = if (books.isEmpty()) View.VISIBLE else View.GONE
        }

        // 搜索结果
        viewModel.searchResults.observe(viewLifecycleOwner) { results ->
            if (viewModel.isSearching.value == true) {
                bookshelfAdapter.submitList(results)
            }
        }

        // 消息
        viewModel.message.observe(viewLifecycleOwner) { message ->
            message?.let {
                android.widget.Toast.makeText(requireContext(), it, android.widget.Toast.LENGTH_SHORT).show()
                viewModel.clearMessage()
            }
        }
    }

    /**
     * 点击书籍
     */
    private fun onBookClick(book: com.tomato.novel.downloader.data.local.BookshelfEntity) {
        // 导航到阅读页面
        val bundle = Bundle().apply {
            putString("book_id", book.bookId)
            putString("book_name", book.bookName)
        }
        // findNavController().navigate(R.id.action_bookshelf_to_reader, bundle)
    }

    /**
     * 显示书籍选项
     */
    private fun showBookOptions(book: com.tomato.novel.downloader.data.local.BookshelfEntity) {
        val options = arrayOf("继续阅读", "查看详情", "从书架移除")
        
        MaterialAlertDialogBuilder(requireContext())
            .setTitle(book.bookName)
            .setItems(options) { _, which ->
                when (which) {
                    0 -> onBookClick(book)
                    1 -> showBookDetail(book)
                    2 -> confirmRemove(book)
                }
            }
            .show()
    }

    /**
     * 显示书籍详情
     */
    private fun showBookDetail(book: com.tomato.novel.downloader.data.local.BookshelfEntity) {
        MaterialAlertDialogBuilder(requireContext())
            .setTitle(book.bookName)
            .setMessage("""
                作者：${book.author}
                总章节：${book.totalChapters}
                阅读进度：${book.lastReadChapter}/${book.totalChapters}
                
                ${book.description ?: "暂无简介"}
            """.trimIndent())
            .setPositiveButton(R.string.confirm, null)
            .show()
    }

    /**
     * 确认移除
     */
    private fun confirmRemove(book: com.tomato.novel.downloader.data.local.BookshelfEntity) {
        MaterialAlertDialogBuilder(requireContext())
            .setTitle(R.string.remove_from_bookshelf)
            .setMessage(R.string.remove_from_bookshelf_message)
            .setPositiveButton(R.string.confirm) { _, _ ->
                viewModel.removeBook(book)
            }
            .setNegativeButton(R.string.cancel, null)
            .show()
    }
}
