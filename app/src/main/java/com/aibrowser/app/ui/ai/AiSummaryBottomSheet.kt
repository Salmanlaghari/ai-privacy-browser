package com.aibrowser.app.ui.ai

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.lifecycle.lifecycleScope
import com.aibrowser.app.data.AiRepository
import com.aibrowser.app.data.AppDatabase
import com.aibrowser.app.databinding.LayoutAiSummaryBinding
import com.aibrowser.app.util.MarkdownRenderer
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/**
 * Bottom Sheet modal overlay for summarizing clean web contents in 5 quick bullets.
 */
class AiSummaryBottomSheet : BottomSheetDialogFragment() {

    companion object {
        private const val ARG_TEXT = "arg_text"

        fun newInstance(text: String): AiSummaryBottomSheet {
            val fragment = AiSummaryBottomSheet()
            val args = Bundle().apply {
                putString(ARG_TEXT, text)
            }
            fragment.arguments = args
            return fragment
        }
    }

    private var _binding: LayoutAiSummaryBinding? = null
    private val binding get() = _binding!!
    private lateinit var aiRepository: AiRepository

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = LayoutAiSummaryBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val context = requireContext()
        val db = AppDatabase.getDatabase(context)
        aiRepository = AiRepository(context, db.aiCacheDao())

        val articleText = arguments?.getString(ARG_TEXT) ?: ""
        if (articleText.isNotEmpty()) {
            calculateReadingTimeSaved(articleText)
            generateSummary(articleText)
        } else {
            binding.aiSummaryTextView.text = "No clean webpage content extracted to summarize."
        }

        setupListeners()
    }

    private fun calculateReadingTimeSaved(text: String) {
        val wordCount = text.split(Regex("\\s+")).size
        val minutesOriginal = (wordCount / 200).coerceAtLeast(1)
        val minutesSaved = (minutesOriginal - 1).coerceAtLeast(1)
        binding.readingTimeSavedTextView.text = "Reading time saved: ~$minutesSaved minutes!"
    }

    private fun generateSummary(text: String) {
        binding.loadingProgress.visibility = View.VISIBLE
        binding.aiSummaryTextView.text = "Extracting details and generating bullet points..."

        lifecycleScope.launch {
            try {
                val summary = withContext(Dispatchers.IO) {
                    aiRepository.getPageSummary(text)
                }
                binding.aiSummaryTextView.text = MarkdownRenderer.renderMarkdown(summary)
            } catch (e: Exception) {
                binding.aiSummaryTextView.text = e.message ?: "AI Summary temporarily unavailable."
            } finally {
                binding.loadingProgress.visibility = View.GONE
            }
        }
    }

    private fun setupListeners() {
        binding.btnCloseSummary.setOnClickListener {
            dismiss()
        }

        binding.btnShareSummary.setOnClickListener {
            val summaryText = binding.aiSummaryTextView.text.toString()
            if (summaryText.isNotEmpty() && !summaryText.startsWith("Extracting")) {
                val sendIntent = android.content.Intent().apply {
                    action = android.content.Intent.ACTION_SEND
                    putExtra(android.content.Intent.EXTRA_TEXT, summaryText)
                    type = "text/plain"
                }
                startActivity(android.content.Intent.createChooser(sendIntent, "Share Webpage Summary"))
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
