package com.aibrowser.app.ui.settings

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.aibrowser.app.databinding.FragmentSettingsAiBinding

/**
 * Fragment containing configurations for AI Helpers.
 * Configures default tasks, sharing triggers, and summaries.
 */
class AiSettingsFragment : Fragment() {

    private var _binding: FragmentSettingsAiBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSettingsAiBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        loadAiSettings()
    }

    private fun loadAiSettings() {
        val prefs = requireContext().getSharedPreferences("browser_settings", Context.MODE_PRIVATE)
        val defaultFeature = prefs.getString("default_ai_feature", "search") ?: "search"

        when (defaultFeature) {
            "search" -> binding.radioAiSearch.isChecked = true
            "summarize" -> binding.radioAiSummarize.isChecked = true
            "translate" -> binding.radioAiTranslate.isChecked = true
        }

        binding.switchAutoSummarize.isChecked = prefs.getBoolean("auto_summarize_on_share", false)
    }

    override fun onPause() {
        super.onPause()
        saveAiSettings()
    }

    private fun saveAiSettings() {
        val prefs = requireContext().getSharedPreferences("browser_settings", Context.MODE_PRIVATE)
        val feature = when (binding.aiFeatureRadioGroup.checkedRadioButtonId) {
            binding.radioAiSearch.id -> "search"
            binding.radioAiSummarize.id -> "summarize"
            binding.radioAiTranslate.id -> "translate"
            else -> "search"
        }

        prefs.edit().apply {
            putString("default_ai_feature", feature)
            putBoolean("auto_summarize_on_share", binding.switchAutoSummarize.isChecked)
            apply()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
