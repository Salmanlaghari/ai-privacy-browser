package com.aibrowser.app.ui

import android.content.Intent
import android.os.Bundle
import android.view.inputmethod.EditorInfo
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.aibrowser.app.databinding.ActivityHomeBinding
import timber.log.Timber

/**
 * The default landing screen of the AI Privacy Browser.
 * Features a speed-dial grid of popular sites, a URL/search bar,
 * and direct links to Settings, Bookmarks, and History.
 */
class HomeActivity : AppCompatActivity() {

    private lateinit var binding: ActivityHomeBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityHomeBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupSpeedDial()
        setupSearchBar()
        setupUtilityButtons()
    }

    private fun setupSpeedDial() {
        binding.cardGoogle.setOnClickListener { openUrl("https://www.google.com") }
        binding.cardWikipedia.setOnClickListener { openUrl("https://www.wikipedia.org") }
        binding.cardGithub.setOnClickListener { openUrl("https://github.com") }
        binding.cardReddit.setOnClickListener { openUrl("https://www.reddit.com") }
    }

    private fun setupSearchBar() {
        binding.btnGo.setOnClickListener {
            val input = binding.urlEditText.text.toString().trim()
            if (input.isNotEmpty()) {
                handleSearchInput(input)
            }
        }

        binding.urlEditText.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_GO) {
                val input = binding.urlEditText.text.toString().trim()
                if (input.isNotEmpty()) {
                    handleSearchInput(input)
                    true
                } else false
            } else false
        }

        binding.btnMic.setOnClickListener {
            Toast.makeText(this, "Voice search is a placeholder (Phase 2)", Toast.LENGTH_SHORT).show()
        }
    }

    private fun setupUtilityButtons() {
        binding.btnHomeBookmarks.setOnClickListener {
            startActivity(Intent(this, BookmarksActivity::class.java))
        }
        binding.btnHomeHistory.setOnClickListener {
            startActivity(Intent(this, HistoryActivity::class.java))
        }
        binding.btnHomeSettings.setOnClickListener {
            startActivity(Intent(this, SettingsActivity::class.java))
        }
    }

    /**
     * Resolves user input into either a direct URL loading or a search query.
     */
    private fun handleSearchInput(input: String) {
        val trimmed = input.trim()
        val isUrl = trimmed.contains(".") && (trimmed.contains("://") ||
                trimmed.matches(Regex("^(?i)(https?://)?([a-z0-9]+(-[a-z0-9]+)*\\.)+[a-z]{2,}(/.*)?$")))

        val targetUrl = if (isUrl) {
            if (!trimmed.startsWith("http://") && !trimmed.startsWith("https://")) {
                "https://$trimmed"
            } else {
                trimmed
            }
        } else {
            // Get search engine preference (default to DuckDuckGo privacy-first)
            val prefs = getSharedPreferences("browser_settings", MODE_PRIVATE)
            val useGoogle = prefs.getBoolean("use_google", false)
            val searchBase = if (useGoogle) {
                "https://www.google.com/search?q="
            } else {
                "https://duckduckgo.com/?q="
            }
            searchBase + java.net.URLEncoder.encode(trimmed, "UTF-8")
        }

        openUrl(targetUrl)
    }

    /**
     * Launches the [BrowserActivity] with the requested URL.
     */
    private fun openUrl(url: String) {
        Timber.d("Opening URL: %s", url)
        val intent = Intent(this, BrowserActivity::class.java).apply {
            putExtra(BrowserActivity.EXTRA_URL, url)
        }
        startActivity(intent)
    }
}
