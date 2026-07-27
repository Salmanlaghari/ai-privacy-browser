package com.aibrowser.app

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.aibrowser.app.databinding.ActivityMainBinding
import timber.log.Timber

/**
 * The primary [MainActivity] of the AI Privacy Browser app.
 * Configured as the launcher activity. It sets the main content view
 * using View Binding to [ActivityMainBinding] and confirms initialization.
 */
class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Timber.d("MainActivity onCreate called.")

        // Use View Binding to inflate the layout and set content view
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Set the initialization text
        binding.initTextView.text = getString(R.string.initialization_text)
        Timber.d("MainActivity initialized successfully.")
    }
}
