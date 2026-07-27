package com.aibrowser.app.ui.settings

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.aibrowser.app.databinding.FragmentSettingsAppearanceBinding
import com.aibrowser.app.ui.theme.ThemeManager

/**
 * Fragment containing visual customization appearance settings.
 */
class AppearanceSettingsFragment : Fragment() {

    private var _binding: FragmentSettingsAppearanceBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSettingsAppearanceBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        loadAppearanceSettings()
        setupListeners()
    }

    private fun loadAppearanceSettings() {
        val context = requireContext()
        val themeMode = ThemeManager.getThemeMode(context)
        val accent = ThemeManager.getAccentColor(context)

        // Select correct theme mode radio
        when (themeMode) {
            ThemeManager.MODE_LIGHT -> binding.radioThemeLight.isChecked = true
            ThemeManager.MODE_DARK -> binding.radioThemeDark.isChecked = true
            ThemeManager.MODE_SYSTEM -> binding.radioThemeSystem.isChecked = true
            ThemeManager.MODE_SCHEDULED -> binding.radioThemeScheduled.isChecked = true
        }

        // Select correct accent radio
        when (accent) {
            ThemeManager.ACCENT_BLUE -> binding.accentBlue.isChecked = true
            ThemeManager.ACCENT_GREEN -> binding.accentGreen.isChecked = true
            ThemeManager.ACCENT_ORANGE -> binding.accentOrange.isChecked = true
            ThemeManager.ACCENT_PURPLE -> binding.accentPurple.isChecked = true
            ThemeManager.ACCENT_TEAL -> binding.accentTeal.isChecked = true
            ThemeManager.ACCENT_PINK -> binding.accentPink.isChecked = true
            ThemeManager.ACCENT_MONOCHROME -> binding.accentMonochrome.isChecked = true
            ThemeManager.ACCENT_AMOLED -> binding.accentAmoled.isChecked = true
        }

        // Text scale
        val prefs = context.getSharedPreferences("browser_settings", Context.MODE_PRIVATE)
        val scale = prefs.getFloat("text_scale", 100.0f)
        binding.textSizeSlider.value = scale
        binding.textSizeLabel.text = "Scale: ${scale.toInt()}%"
    }

    private fun setupListeners() {
        binding.textSizeSlider.addOnChangeListener { _, value, _ ->
            binding.textSizeLabel.text = "Scale: ${value.toInt()}%"
        }
    }

    override fun onPause() {
        super.onPause()
        saveAppearanceSettings()
    }

    private fun saveAppearanceSettings() {
        val context = requireContext()

        // 1. Theme mode
        val selectedThemeMode = when (binding.themeModeRadioGroup.checkedRadioButtonId) {
            binding.radioThemeLight.id -> ThemeManager.MODE_LIGHT
            binding.radioThemeDark.id -> ThemeManager.MODE_DARK
            binding.radioThemeSystem.id -> ThemeManager.MODE_SYSTEM
            binding.radioThemeScheduled.id -> ThemeManager.MODE_SCHEDULED
            else -> ThemeManager.MODE_SYSTEM
        }
        ThemeManager.setThemeMode(context, selectedThemeMode)

        // 2. Accent color
        val selectedAccent = when (binding.accentRadioGroup.checkedRadioButtonId) {
            binding.accentBlue.id -> ThemeManager.ACCENT_BLUE
            binding.accentGreen.id -> ThemeManager.ACCENT_GREEN
            binding.accentOrange.id -> ThemeManager.ACCENT_ORANGE
            binding.accentPurple.id -> ThemeManager.ACCENT_PURPLE
            binding.accentTeal.id -> ThemeManager.ACCENT_TEAL
            binding.accentPink.id -> ThemeManager.ACCENT_PINK
            binding.accentMonochrome.id -> ThemeManager.ACCENT_MONOCHROME
            binding.accentAmoled.id -> ThemeManager.ACCENT_AMOLED
            else -> ThemeManager.ACCENT_BLUE
        }
        ThemeManager.setAccentColor(context, selectedAccent)

        // 3. Text scale
        val prefs = context.getSharedPreferences("browser_settings", Context.MODE_PRIVATE)
        val scaleValue = binding.textSizeSlider.value
        prefs.edit().putFloat("text_scale", scaleValue).apply()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
