package com.aibrowser.app.ui.ai

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.lifecycle.lifecycleScope
import com.aibrowser.app.data.AiRepository
import com.aibrowser.app.data.AppDatabase
import com.aibrowser.app.databinding.LayoutAiTranslateBinding
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/**
 * Bottom Sheet modal overlay for translating webpage contents into common target languages.
 */
class AiTranslateBottomSheet : BottomSheetDialogFragment() {

    companion object {
        private const val ARG_TEXT = "arg_text"

        fun newInstance(text: String): AiTranslateBottomSheet {
            val fragment = AiTranslateBottomSheet()
            val args = Bundle().apply {
                putString(ARG_TEXT, text)
            }
            fragment.arguments = args
            return fragment
        }
    }

    private var _binding: LayoutAiTranslateBinding? = null
    private val binding get() = _binding!!
    private lateinit var aiRepository: AiRepository

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = LayoutAiTranslateBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val context = requireContext()
        val db = AppDatabase.getDatabase(context)
        aiRepository = AiRepository(context, db.aiCacheDao())

        setupListeners()
    }

    private fun setupListeners() {
        val articleText = arguments?.getString(ARG_TEXT) ?: ""

        binding.btnLangUrdu.setOnClickListener { translatePage(articleText, "Urdu") }
        binding.btnLangSpanish.setOnClickListener { translatePage(articleText, "Spanish") }
        binding.btnLangFrench.setOnClickListener { translatePage(articleText, "French") }
        binding.btnLangJapanese.setOnClickListener { translatePage(articleText, "Japanese") }
        binding.btnLangHindi.setOnClickListener { translatePage(articleText, "Hindi") }

        binding.btnCopyTranslation.setOnClickListener {
            val text = binding.aiTranslateTextView.text.toString()
            if (text.isNotEmpty() && !text.startsWith("Select a language")) {
                val clipboard = requireContext().getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                val clip = ClipData.newPlainText("AI Translation", text)
                clipboard.setPrimaryClip(clip)
                Toast.makeText(context, "Translation copied to clipboard", Toast.LENGTH_SHORT).show()
            }
        }

        binding.btnCloseTranslate.setOnClickListener {
            dismiss()
        }
    }

    private fun translatePage(text: String, language: String) {
        if (text.isEmpty()) {
            binding.aiTranslateTextView.text = "No clean webpage content to translate."
            return
        }

        binding.loadingProgress.visibility = View.VISIBLE
        binding.aiTranslateTextView.text = "Translating content to $language..."

        lifecycleScope.launch {
            try {
                val result = withContext(Dispatchers.IO) {
                    aiRepository.getPageTranslation(text, language)
                }
                binding.aiTranslateTextView.text = result
            } catch (e: Exception) {
                binding.aiTranslateTextView.text = e.message ?: "AI Translation temporarily unavailable."
            } finally {
                binding.loadingProgress.visibility = View.GONE
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
