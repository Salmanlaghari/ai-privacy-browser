package com.aibrowser.app.ui.settings

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.webkit.CookieManager
import android.webkit.WebView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.aibrowser.app.data.AppDatabase
import com.aibrowser.app.data.HistoryRepository
import com.aibrowser.app.data.TabManager
import com.aibrowser.app.databinding.FragmentSettingsPrivacyBinding
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/**
 * Fragment containing Privacy & Security configurations.
 * Handles HTTPS-only, Ad-blocking toggle, Do Not Track headers, and Purge actions.
 */
class PrivacySettingsFragment : Fragment() {

    private var _binding: FragmentSettingsPrivacyBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSettingsPrivacyBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        loadPrivacySettings()
        setupListeners()
    }

    private fun loadPrivacySettings() {
        val prefs = requireContext().getSharedPreferences("browser_settings", Context.MODE_PRIVATE)
        binding.switchAdBlocker.isChecked = prefs.getBoolean("ad_blocker_enabled", true)
        binding.switchHttpsOnly.isChecked = prefs.getBoolean("https_only_enabled", false)
        binding.switchBlockTrackers.isChecked = prefs.getBoolean("block_trackers_enabled", true)
        binding.switchBlockThirdPartyCookies.isChecked = prefs.getBoolean("block_third_party_cookies", false)
        binding.switchDoNotTrack.isChecked = prefs.getBoolean("do_not_track_enabled", false)
    }

    private fun setupListeners() {
        binding.btnPurgeData.setOnClickListener {
            showClearBrowsingDataDialog()
        }
    }

    private fun showClearBrowsingDataDialog() {
        AlertDialog.Builder(requireContext())
            .setTitle("Clear Browsing Data?")
            .setMessage("This will permanently clear history, cookies, cache, and all open tabs.")
            .setPositiveButton("Clear") { _, _ ->
                clearBrowsingData()
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun clearBrowsingData() {
        val context = requireContext()
        lifecycleScope.launch {
            val database = AppDatabase.getDatabase(context)
            val historyRepo = HistoryRepository(database.historyDao())
            withContext(Dispatchers.IO) {
                historyRepo.clearAll()
            }

            CookieManager.getInstance().removeAllCookies(null)
            CookieManager.getInstance().flush()

            val webView = WebView(context)
            webView.clearCache(true)

            TabManager.clearAllTabs()
            TabManager.initialize(context)
            TabManager.persist(context)

            Toast.makeText(context, "Browsing Data Cleared Successfully", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onPause() {
        super.onPause()
        savePrivacySettings()
    }

    private fun savePrivacySettings() {
        val prefs = requireContext().getSharedPreferences("browser_settings", Context.MODE_PRIVATE)
        prefs.edit().apply {
            putBoolean("ad_blocker_enabled", binding.switchAdBlocker.isChecked)
            putBoolean("https_only_enabled", binding.switchHttpsOnly.isChecked)
            putBoolean("block_trackers_enabled", binding.switchBlockTrackers.isChecked)
            putBoolean("block_third_party_cookies", binding.switchBlockThirdPartyCookies.isChecked)
            putBoolean("do_not_track_enabled", binding.switchDoNotTrack.isChecked)
            apply()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
