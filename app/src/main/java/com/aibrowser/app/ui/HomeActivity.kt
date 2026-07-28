package com.aibrowser.app.ui

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.inputmethod.EditorInfo
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.aibrowser.app.data.AppDatabase
import com.aibrowser.app.data.HistoryRepository
import com.aibrowser.app.databinding.ActivityHomeBinding
import com.aibrowser.app.ui.ai.AiSearchBottomSheet
import com.aibrowser.app.ui.home.CategoryAdapter
import com.aibrowser.app.ui.home.CuratedSite
import com.aibrowser.app.ui.home.ShortcutAdapter
import com.aibrowser.app.ui.theme.ThemeManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.Calendar

/**
 * Redesigned, rich Material 3 Home landing screen.
 * Integrates rotating local South Asian news headlines, greeting titles, search queries,
 * quick AI bottom sheets assistant shortcuts, curated South Asian links, and privacy blockers badge trackers.
 */
class HomeActivity : AppCompatActivity() {

    private lateinit var binding: ActivityHomeBinding
    private lateinit var categoryAdapter: CategoryAdapter
    private lateinit var shortcutAdapter: ShortcutAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Apply Premium Accent Theme dynamically
        ThemeManager.applyAccentTheme(this)

        binding = ActivityHomeBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Setup custom layouts
        setupGreetings()
        setupNewsTicker()
        setupSearchBar()
        setupAiShortcuts()
        setupPrivacyBadge()
        setupCuratedCategories()
        setupPersonalizedShortcuts()
        setupUtilityButtons()
        setupHomeAds()
    }

    private fun setupHomeAds() {
        // Pre-load Interstitial and Rewarded Ads on HomeActivity startup
        com.aibrowser.app.data.ads.AdMobManager.loadInterstitialAd(this)
        com.aibrowser.app.data.ads.AdMobManager.loadRewardedAd(this)

        // Bottom of Home page (small, load banner ad after 5 seconds delay)
        binding.root.postDelayed({
            if (!isDestroyed && !isFinishing) {
                com.aibrowser.app.data.ads.AdMobManager.loadBannerAd(this, binding.adBannerContainer)
            }
        }, 5000L)
    }

    private fun setupGreetings() {
        val hour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)
        val greeting = when {
            hour < 12 -> "Good morning, User"
            hour < 18 -> "Good afternoon, User"
            else -> "Good evening, User"
        }
        binding.greetingText.text = greeting
    }

    private fun setupNewsTicker() {
        binding.newsTicker.startTicker()
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

        binding.btnHomeAiSearch.setOnClickListener {
            openAiSearchOverlay()
        }
    }

    private fun setupAiShortcuts() {
        binding.btnHomeAskAi.setOnClickListener {
            openAiSearchOverlay()
        }

        val educationalToast = { feature: String ->
            Toast.makeText(
                this,
                "To $feature, enter a site from Curated Categories and pick '$feature' option in the browser menu!",
                Toast.LENGTH_LONG
            ).show()
        }

        binding.btnHomeSummarize.setOnClickListener {
            educationalToast("Summarize")
        }

        binding.btnHomeTranslate.setOnClickListener {
            educationalToast("Translate")
        }
    }

    private fun setupPrivacyBadge() {
        // Read tracking count dynamically
        val prefs = getSharedPreferences("browser_settings", MODE_PRIVATE)
        val trackerBlockerCount = prefs.getInt("blocked_trackers_count", 24)
        binding.privacyBadgeText.text = "Privacy Shield: $trackerBlockerCount ad-trackers blocked today"
    }

    private fun setupCuratedCategories() {
        binding.categoryRecyclerView.layoutManager = LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
        categoryAdapter = CategoryAdapter { url ->
            openUrl(url)
        }
        binding.categoryRecyclerView.adapter = categoryAdapter

        // Curated South Asian lists
        val newsSites = listOf(
            CuratedSite("Geo News", "https://www.geo.tv"),
            CuratedSite("Dawn News", "https://www.dawn.com"),
            CuratedSite("ARY News", "https://arynews.tv"),
            CuratedSite("BBC Urdu", "https://www.bbc.com/urdu"),
            CuratedSite("Al Jazeera", "https://www.aljazeera.com")
        )

        val sportsSites = listOf(
            CuratedSite("Cricbuzz", "https://www.cricbuzz.com"),
            CuratedSite("ESPN Cricinfo", "https://www.espncricinfo.com"),
            CuratedSite("Geo Super", "https://www.geosuper.tv")
        )

        val techSites = listOf(
            CuratedSite("TechJuice", "https://www.techjuice.pk"),
            CuratedSite("ProPakistani", "https://propakistani.pk"),
            CuratedSite("PhoneWorld", "https://www.phoneworld.com.pk"),
            CuratedSite("GSMArena", "https://www.gsmarena.com")
        )

        val religiousSites = listOf(
            CuratedSite("Quran.com", "https://quran.com"),
            CuratedSite("Islam.pk", "https://islam.pk"),
            CuratedSite("Daily Hadith", "https://dailyhadith.co")
        )

        val shoppingSites = listOf(
            CuratedSite("Daraz PK", "https://www.daraz.pk"),
            CuratedSite("PriceOye", "https://priceoye.pk"),
            CuratedSite("iShopping", "https://www.ishopping.pk")
        )

        val entertainmentSites = listOf(
            CuratedSite("YouTube", "https://www.youtube.com"),
            CuratedSite("TikTok", "https://www.tiktok.com"),
            CuratedSite("Instagram", "https://www.instagram.com"),
            CuratedSite("Spotify", "https://www.spotify.com")
        )

        // Initialize with default Checked Category (News)
        categoryAdapter.setSites(newsSites)

        binding.categoryChipGroup.setOnCheckedStateChangeListener { _, checkedIds ->
            val checkedId = checkedIds.firstOrNull() ?: binding.chipNews.id
            when (checkedId) {
                binding.chipNews.id -> categoryAdapter.setSites(newsSites)
                binding.chipSports.id -> categoryAdapter.setSites(sportsSites)
                binding.chipTech.id -> categoryAdapter.setSites(techSites)
                binding.chipReligious.id -> categoryAdapter.setSites(religiousSites)
                binding.chipShopping.id -> categoryAdapter.setSites(shoppingSites)
                binding.chipEntertainment.id -> categoryAdapter.setSites(entertainmentSites)
                else -> categoryAdapter.setSites(newsSites)
            }
        }
    }

    private fun setupPersonalizedShortcuts() {
        binding.personalizedRecyclerView.layoutManager = LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
        shortcutAdapter = ShortcutAdapter(
            onShortcutClick = { url -> openUrl(url) },
            onShortcutLongClick = { entry ->
                Toast.makeText(this, "Removing shortcut ${entry.title}", Toast.LENGTH_SHORT).show()
                lifecycleScope.launch(Dispatchers.IO) {
                    val db = AppDatabase.getDatabase(this@HomeActivity)
                    db.historyDao().delete(entry)
                    loadShortcutsFromDb()
                }
            }
        )
        binding.personalizedRecyclerView.adapter = shortcutAdapter

        loadShortcutsFromDb()
    }

    private fun loadShortcutsFromDb() {
        lifecycleScope.launch {
            val db = AppDatabase.getDatabase(this@HomeActivity)
            val historyRepo = HistoryRepository(db.historyDao())
            historyRepo.getRecent(10).observe(this@HomeActivity) { historyList ->
                if (historyList.isNullOrEmpty()) {
                    binding.personalizedTitleText.visibility = View.GONE
                    binding.personalizedRecyclerView.visibility = View.GONE
                } else {
                    binding.personalizedTitleText.visibility = View.VISIBLE
                    binding.personalizedRecyclerView.visibility = View.VISIBLE
                    shortcutAdapter.setShortcuts(historyList)
                }
            }
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

    private fun openAiSearchOverlay() {
        val bottomSheet = AiSearchBottomSheet()
        bottomSheet.show(supportFragmentManager, "AiSearch")
    }

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
            val prefs = getSharedPreferences("browser_settings", MODE_PRIVATE)
            val useGoogle = prefs.getBoolean("use_google", false)
            val searchBase = if (useGoogle) "https://www.google.com/search?q=" else "https://duckduckgo.com/?q="
            searchBase + java.net.URLEncoder.encode(trimmed, "UTF-8")
        }

        openUrl(targetUrl)
    }

    private fun openUrl(url: String) {
        val intent = Intent(this, BrowserActivity::class.java).apply {
            putExtra(BrowserActivity.EXTRA_URL, url)
        }
        startActivity(intent)
    }

    override fun onResume() {
        super.onResume()
        binding.newsTicker.startTicker()
        loadShortcutsFromDb()
        setupGreetings()
    }

    override fun onPause() {
        super.onPause()
        binding.newsTicker.stopTicker()
    }
}
