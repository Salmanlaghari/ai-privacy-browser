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
import com.aibrowser.app.databinding.LayoutAiSearchBinding
import com.aibrowser.app.util.MarkdownRenderer
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/**
 * Bottom Sheet modal overlay for conducting smart AI Search queries powered by Google Gemini.
 */
class AiSearchBottomSheet : BottomSheetDialogFragment() {

    private var _binding: LayoutAiSearchBinding? = null
    private val binding get() = _binding!!
    private lateinit var aiRepository: AiRepository

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = LayoutAiSearchBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Initialize repository
        val context = requireContext()
        val db = AppDatabase.getDatabase(context)
        aiRepository = AiRepository(context, db.aiCacheDao())

        setupListeners()
    }

    private fun setupListeners() {
        binding.btnSubmitAiSearch.setOnClickListener {
            val query = binding.aiSearchEditText.text.toString().trim()
            if (query.isNotEmpty()) {
                performSearch(query)
            }
        }

        // Quick chip clicks
        binding.chipWeather.setOnClickListener {
            binding.aiSearchEditText.setText("What's the weather like in New York?")
            performSearch("What's the weather like in New York?")
        }

        binding.chipExplain.setOnClickListener {
            binding.aiSearchEditText.setText("Explain quantum computing like I'm 5 years old")
            performSearch("Explain quantum computing like I'm 5 years old")
        }

        binding.chipFact.setOnClickListener {
            binding.aiSearchEditText.setText("Tell me a mind-blowing fun fact of the day")
            performSearch("Tell me a mind-blowing fun fact of the day")
        }

        binding.btnCopyResponse.setOnClickListener {
            val text = binding.aiResponseTextView.text.toString()
            if (text.isNotEmpty() && text != "Response will be displayed here...") {
                val clipboard = requireContext().getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                val clip = ClipData.newPlainText("AI Response", text)
                clipboard.setPrimaryClip(clip)
                Toast.makeText(context, "Copied response to clipboard", Toast.LENGTH_SHORT).show()
            }
        }

        binding.btnShareResponse.setOnClickListener {
            val text = binding.aiResponseTextView.text.toString()
            if (text.isNotEmpty() && text != "Response will be displayed here...") {
                val sendIntent = android.content.Intent().apply {
                    action = android.content.Intent.ACTION_SEND
                    putExtra(android.content.Intent.EXTRA_TEXT, text)
                    type = "text/plain"
                }
                startActivity(android.content.Intent.createChooser(sendIntent, "Share AI Response"))
            }
        }
    }

    private fun performSearch(query: String) {
        binding.loadingProgress.visibility = View.toBeDisplayedVisibility() ?: View.VISIBLE
        binding.aiResponseTextView.text = "Consulting Gemini..."

        lifecycleScope.launch {
            try {
                val response = withContext(Dispatchers.IO) {
                    aiRepository.getAiSearch(query)
                }
                binding.aiResponseTextView.text = MarkdownRenderer.renderMarkdown(response)
            } catch (e: Exception) {
                val msg = e.message ?: "AI temporarily unavailable. Please try again."
                binding.aiResponseTextView.text = msg

                if (msg.contains("limit reached", ignoreCase = true)) {
                    // Offer AdMob Rewarded trigger to grant more calls!
                    showRewardedAdOfferDialog()
                }
            } finally {
                binding.loadingProgress.visibility = View.GONE
            }
        }
    }

    private fun showRewardedAdOfferDialog() {
        androidx.appcompat.app.AlertDialog.Builder(requireContext())
            .setTitle("Daily Limit Reached")
            .setMessage("Watch a short ad to earn +10 extra free AI calls instantly!")
            .setPositiveButton("Watch Ad") { dialog, _ ->
                com.aibrowser.app.data.ads.AdMobManager.showRewardedAd(requireActivity()) { amount ->
                    aiRepository.addBonusUsageCalls(10)
                    Toast.makeText(context, "Congratulations! You earned +10 more AI calls!", Toast.LENGTH_SHORT).show()
                }
                dialog.dismiss()
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun View.toBeDisplayedVisibility(): Int? {
        return View.VISIBLE
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
