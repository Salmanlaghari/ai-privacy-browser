package com.aibrowser.app.ui

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.aibrowser.app.R
import com.aibrowser.app.data.TabManager
import com.aibrowser.app.databinding.ActivityBrowserBinding
import timber.log.Timber

/**
 * Hosts the browser window fragment container.
 * Manages tab persistence and restoration lifecycle events.
 */
class BrowserActivity : AppCompatActivity() {

    companion object {
        const val EXTRA_URL = "extra_url"
    }

    private lateinit var binding: ActivityBrowserBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityBrowserBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Initialize TabManager and restore saved tabs
        TabManager.initialize(this)

        val urlToLoad = intent.getStringExtra(EXTRA_URL)
        if (!urlToLoad.isNullOrEmpty()) {
            // Create a new tab for the target URL
            TabManager.createTab("Loading...", urlToLoad)
        }

        // Host the core BrowserFragment
        if (savedInstanceState == null) {
            supportFragmentManager.beginTransaction()
                .replace(R.id.fragmentContainer, BrowserFragment())
                .commit()
        }
    }

    override fun onPause() {
        super.onPause()
        // Save tabs state on pause
        TabManager.persist(this)
    }

    override fun onStop() {
        super.onStop()
        TabManager.persist(this)
    }

    override fun onDestroy() {
        super.onDestroy()
        // Between browser sessions: show Interstitial Ad on session termination
        com.aibrowser.app.data.ads.AdMobManager.showInterstitialAd(this)
    }
}
