package com.tomato.novel.downloader.ui.reader

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.tomato.novel.downloader.databinding.FragmentReaderBinding
import dagger.hilt.android.AndroidEntryPoint

/**
 * 阅读器页面Fragment
 * 
 * 提供小说阅读功能：
 * - 章节内容显示
 * - 上下章切换
 * - 目录导航
 * - 阅读进度保存
 */
@AndroidEntryPoint
class ReaderFragment : Fragment() {

    private var _binding: FragmentReaderBinding? = null
    private val binding get() = _binding!!
    
    private val viewModel: ReaderViewModel by viewModels()
    private lateinit var contentAdapter: ChapterContentAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentReaderBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupViews()
        observeData()
        
        // 获取传入的章节ID
        arguments?.getString("chapter_id")?.let { chapterId ->
            viewModel.loadChapter(chapterId)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    /**
     * 初始化视图
     */
    private fun setupViews() {
        // 设置内容列表
        contentAdapter = ChapterContentAdapter()
        binding.rvContent.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = contentAdapter
        }

        // 上一章按钮
        binding.btnPrevious.setOnClickListener {
            viewModel.previousChapter()
        }

        // 下一章按钮
        binding.btnNext.setOnClickListener {
            viewModel.nextChapter()
        }

        // 目录按钮
        binding.btnCatalog.setOnClickListener {
            showCatalog()
        }

        // 设置按钮
        binding.btnSettings.setOnClickListener {
            showSettings()
        }
    }

    /**
     * 观察数据变化
     */
    private fun observeData() {
        // 章节标题
        viewModel.chapterTitle.observe(viewLifecycleOwner) { title ->
            binding.tvTitle.text = title
        }

        // 章节内容
        viewModel.chapterContent.observe(viewLifecycleOwner) { paragraphs ->
            contentAdapter.submitList(paragraphs)
        }

        // 加载状态
        viewModel.isLoading.observe(viewLifecycleOwner) { isLoading ->
            binding.progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
        }

        // 是否有上一章
        viewModel.hasPrevious.observe(viewLifecycleOwner) { hasPrevious ->
            binding.btnPrevious.isEnabled = hasPrevious
        }

        // 是否有下一章
        viewModel.hasNext.observe(viewLifecycleOwner) { hasNext ->
            binding.btnNext.isEnabled = hasNext
        }
    }

    /**
     * 显示目录
     */
    private fun showCatalog() {
        // 实现目录显示
    }

    /**
     * 显示设置
     */
    private fun showSettings() {
        // 实现阅读设置显示
    }
}
