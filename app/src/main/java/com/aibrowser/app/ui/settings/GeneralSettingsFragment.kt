package com.aibrowser.app.ui.settings

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.aibrowser.app.databinding.FragmentSettingsGeneralBinding

/**
 * Fragment containing basic browser general settings.
 */
class GeneralSettingsFragment : Fragment() {

    private var _binding: FragmentSettingsGeneralBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSettingsGeneralBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        loadGeneralSettings()
    }

    private fun loadGeneralSettings() {
        val prefs = requireContext().getSharedPreferences("browser_settings", Context.MODE_PRIVATE)
        val homepage = prefs.getString("homepage_url", "https://www.google.com")
        val useGoogle = prefs.getBoolean("use_google", false)

        binding.homepageEditText.setText(homepage)
        if (useGoogle) {
            binding.radioGoogle.isChecked = true
        } else {
            binding.radioDuckDuckGo.isChecked = true
        }
    }

    override fun onPause() {
        super.onPause()
        saveGeneralSettings()
    }

    private fun saveGeneralSettings() {
        val prefs = requireContext().getSharedPreferences("browser_settings", Context.MODE_PRIVATE)
        val home = binding.homepageEditText.text.toString().trim()
        val useGoogle = binding.radioGoogle.isChecked

        prefs.edit().apply {
            putString("homepage_url", if (home.isNotEmpty()) home else "https://www.google.com")
            putBoolean("use_google", useGoogle)
            apply()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
