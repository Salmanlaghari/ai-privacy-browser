package com.aibrowser.app.ui

import android.content.Context
import android.os.Bundle
import android.webkit.CookieManager
import android.webkit.WebView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.aibrowser.app.data.AppDatabase
import com.aibrowser.app.data.HistoryRepository
import com.aibrowser.app.data.TabManager
import com.aibrowser.app.databinding.ActivitySettingsBinding
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/**
 * Settings configuration screen.
 * Allows users to choose homepage preference, pick default search engine, and purge data.
 */
class SettingsActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySettingsBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySettingsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        loadSettings()
        setupListeners()
    }

    private fun loadSettings() {
        val prefs = getSharedPreferences("browser_settings", MODE_PRIVATE)
        val homepage = prefs.getString("homepage_url", "https://www.google.com")
        val useDuckDuckGo = prefs.getBoolean("use_duckduckgo", false)

        binding.homepageEditText.setText(homepage)
        if (useDuckDuckGo) {
            binding.radioDuckDuckGo.isChecked = true
        } else {
            binding.radioGoogle.isChecked = true
        }
    }

    private fun setupListeners() {
        binding.btnBack.setOnClickListener {
            saveSettingsAndFinish()
        }

        binding.btnClearBrowsingData.setOnClickListener {
            showClearBrowsingDataDialog()
        }
    }

    private fun saveSettingsAndFinish() {
        val prefs = getSharedPreferences("browser_settings", MODE_PRIVATE)
        val newHomepage = binding.homepageEditText.text.toString().trim()
        val useDuckDuckGo = binding.radioDuckDuckGo.isChecked

        prefs.edit().apply {
            putString("homepage_url", if (newHomepage.isNotEmpty()) newHomepage else "https://www.google.com")
            putBoolean("use_duckduckgo", useDuckDuckGo)
            apply()
        }

        Toast.makeText(this, "Settings Saved", Toast.LENGTH_SHORT).show()
        finish()
    }

    private fun showClearBrowsingDataDialog() {
        AlertDialog.Builder(this)
            .setTitle("Clear Browsing Data?")
            .setMessage("This will permanently clear history, cookies, cache, and all open tabs.")
            .setPositiveButton("Clear") { _, _ ->
                clearBrowsingData()
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun clearBrowsingData() {
        lifecycleScope.launch {
            // Clear History Database
            val database = AppDatabase.getDatabase(this@SettingsActivity)
            val historyRepo = HistoryRepository(database.historyDao())
            withContext(Dispatchers.IO) {
                historyRepo.clearAll()
            }

            // Clear Cookies
            CookieManager.getInstance().removeAllCookies(null)
            CookieManager.getInstance().flush()

            // Clear Cache
            val webView = WebView(this@SettingsActivity)
            webView.clearCache(true)

            // Clear TabManager active tabs
            TabManager.clearAllTabs()
            TabManager.initialize(this@SettingsActivity)
            TabManager.persist(this@SettingsActivity)

            Toast.makeText(this@SettingsActivity, "Browsing Data Cleared Successfully", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onBackPressed() {
        saveSettingsAndFinish()
        super.onBackPressed()
    }
}
