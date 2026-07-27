package com.aibrowser.app.ui.onboarding

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager2.widget.ViewPager2
import com.aibrowser.app.R
import com.aibrowser.app.databinding.ActivityOnboardingBinding
import com.aibrowser.app.ui.HomeActivity

/**
 * Onboarding activity presenting a 4-slide introductory welcome flow to first-time app users.
 */
class OnboardingActivity : AppCompatActivity() {

    private lateinit var binding: ActivityOnboardingBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityOnboardingBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // If already completed, skip straight to HomeActivity
        val prefs = getSharedPreferences("app_launch_prefs", Context.MODE_PRIVATE)
        if (prefs.getBoolean("onboarding_completed", false)) {
            launchHome()
            return
        }

        setupViewPager()
        setupListeners()
    }

    private fun setupViewPager() {
        val adapter = OnboardingAdapter(
            onEnableNotifications = {
                Toast.makeText(this, "Notifications enabled successfully!", Toast.LENGTH_SHORT).show()
            }
        )
        binding.viewPager.adapter = adapter

        binding.viewPager.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                super.onPageSelected(position)
                binding.dotsIndicatorText.text = "${position + 1} / 4"
                if (position == 3) {
                    binding.btnNext.text = "Get Started"
                } else {
                    binding.btnNext.text = "Next"
                }
            }
        })
    }

    private fun setupListeners() {
        binding.btnNext.setOnClickListener {
            val current = binding.viewPager.currentItem
            if (current < 3) {
                binding.viewPager.currentItem = current + 1
            } else {
                completeOnboarding()
            }
        }

        binding.btnSkip.setOnClickListener {
            completeOnboarding()
        }
    }

    private fun completeOnboarding() {
        getSharedPreferences("app_launch_prefs", Context.MODE_PRIVATE)
            .edit()
            .putBoolean("onboarding_completed", true)
            .apply()
        launchHome()
    }

    private fun launchHome() {
        startActivity(Intent(this, HomeActivity::class.java))
        finish()
    }

    /**
     * Adapter for ViewPager2 loading our 4 beautiful custom onboarding slide views.
     */
    private class OnboardingAdapter(
        private val onEnableNotifications: () -> Unit
    ) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

        override fun getItemViewType(position: Int): Int = position

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
            val inflater = LayoutInflater.from(parent.context)
            val view = when (viewType) {
                0 -> inflater.inflate(R.layout.slide_welcome, parent, false)
                1 -> inflater.inflate(R.layout.slide_ai_features, parent, false)
                2 -> inflater.inflate(R.layout.slide_privacy, parent, false)
                else -> inflater.inflate(R.layout.slide_permissions, parent, false)
            }
            return object : RecyclerView.ViewHolder(view) {}
        }

        override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
            if (position == 3) {
                holder.itemView.findViewById<Button>(R.id.btnRequestNotification)?.setOnClickListener {
                    onEnableNotifications()
                }
            }
        }

        override fun getItemCount(): Int = 4
    }
}
