package com.aibrowser.app.ui

import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.aibrowser.app.R
import com.aibrowser.app.databinding.ActivitySettingsBinding
import com.aibrowser.app.ui.settings.GeneralSettingsFragment
import com.aibrowser.app.ui.settings.AppearanceSettingsFragment
import com.aibrowser.app.ui.settings.PrivacySettingsFragment
import com.aibrowser.app.ui.settings.AiSettingsFragment
import com.aibrowser.app.ui.settings.SyncSettingsFragment

/**
 * Settings configuration screen.
 * Swaps sub-fragments dynamically representing sections: General, Appearance, Privacy, AI, and Sync.
 */
class SettingsActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySettingsBinding
    private var currentFragment: Fragment? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySettingsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupListeners()
    }

    private fun setupListeners() {
        binding.btnBack.setOnClickListener {
            handleBackAction()
        }

        binding.cardGeneral.setOnClickListener {
            loadSectionFragment(GeneralSettingsFragment(), "General Settings")
        }

        binding.cardAppearance.setOnClickListener {
            loadSectionFragment(AppearanceSettingsFragment(), "Appearance Customization")
        }

        binding.cardPrivacy.setOnClickListener {
            loadSectionFragment(PrivacySettingsFragment(), "Privacy & Security")
        }

        binding.cardAi.setOnClickListener {
            loadSectionFragment(AiSettingsFragment(), "AI Assistant Options")
        }

        binding.cardSync.setOnClickListener {
            loadSectionFragment(SyncSettingsFragment(), "Cloud Synchronization")
        }
    }

    private fun loadSectionFragment(fragment: Fragment, title: String) {
        currentFragment = fragment
        binding.settingsCategoryScrollView.visibility = View.GONE
        binding.titleTextView.text = title

        supportFragmentManager.beginTransaction()
            .replace(R.id.settingsFragmentContainer, fragment)
            .commit()
    }

    private fun handleBackAction() {
        if (currentFragment != null) {
            // Remove fragment and show main categories
            supportFragmentManager.beginTransaction()
                .remove(currentFragment!!)
                .commit()
            currentFragment = null
            binding.settingsCategoryScrollView.visibility = View.VISIBLE
            binding.titleTextView.text = getString(R.string.title_settings)
        } else {
            finish()
        }
    }

    override fun onBackPressed() {
        if (currentFragment != null) {
            handleBackAction()
        } else {
            super.onBackPressed()
        }
    }
}
