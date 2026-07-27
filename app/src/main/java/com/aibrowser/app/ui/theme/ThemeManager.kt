package com.aibrowser.app.ui.theme

import android.app.Activity
import android.content.Context
import androidx.appcompat.app.AppCompatDelegate
import com.aibrowser.app.R
import java.util.Calendar

/**
 * Orchestrates application-wide theming, pure black AMOLED modes, accent color selections,
 * and scheduled dark modes (Sunset to Sunrise / Location placeholder).
 */
object ThemeManager {

    private const val PREFS_NAME = "browser_theme_prefs"
    private const val KEY_THEME_MODE = "key_theme_mode"
    private const val KEY_ACCENT_COLOR = "key_accent_color"

    // Theme Mode constants
    const val MODE_LIGHT = 0
    const val MODE_DARK = 1
    const val MODE_SYSTEM = 2
    const val MODE_SCHEDULED = 3

    // Accent Color constants
    const val ACCENT_BLUE = "blue"
    const val ACCENT_GREEN = "green"
    const val ACCENT_ORANGE = "orange"
    const val ACCENT_PURPLE = "purple"
    const val ACCENT_TEAL = "teal"
    const val ACCENT_PINK = "pink"
    const val ACCENT_MONOCHROME = "monochrome"
    const val ACCENT_AMOLED = "amoled"

    /**
     * Applies the saved theme mode (Light, Dark, System, Scheduled) to the application.
     */
    fun applyThemeMode(context: Context) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val mode = prefs.getInt(KEY_THEME_MODE, MODE_SYSTEM)

        when (mode) {
            MODE_LIGHT -> AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
            MODE_DARK -> AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
            MODE_SYSTEM -> AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM)
            MODE_SCHEDULED -> {
                if (isSunsetToSunrise()) {
                    AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
                } else {
                    AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
                }
            }
        }
    }

    /**
     * Applies the custom accent colors and lock-screen premium styles to the given [activity].
     */
    fun applyAccentTheme(activity: Activity) {
        val prefs = activity.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val accent = prefs.getString(KEY_ACCENT_COLOR, ACCENT_BLUE) ?: ACCENT_BLUE

        // Based on selection, set corresponding theme resources
        when (accent) {
            ACCENT_BLUE -> activity.setTheme(R.style.Theme_AIBrowser_Blue)
            ACCENT_GREEN -> activity.setTheme(R.style.Theme_AIBrowser_Green)
            ACCENT_ORANGE -> activity.setTheme(R.style.Theme_AIBrowser_Orange)
            ACCENT_PURPLE -> activity.setTheme(R.style.Theme_AIBrowser_Purple)
            ACCENT_TEAL -> activity.setTheme(R.style.Theme_AIBrowser_Teal)
            ACCENT_PINK -> activity.setTheme(R.style.Theme_AIBrowser_Pink)
            ACCENT_MONOCHROME -> activity.setTheme(R.style.Theme_AIBrowser_Monochrome)
            ACCENT_AMOLED -> activity.setTheme(R.style.Theme_AIBrowser_Amoled)
        }
    }

    /**
     * Saves the chosen [mode] (Light/Dark/System/Scheduled) and applies it.
     */
    fun setThemeMode(context: Context, mode: Int) {
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .edit()
            .putInt(KEY_THEME_MODE, mode)
            .apply()
        applyThemeMode(context)
    }

    /**
     * Saves the chosen [accent] and applies it.
     */
    fun setAccentColor(context: Context, accent: String) {
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .edit()
            .putString(KEY_ACCENT_COLOR, accent)
            .apply()
    }

    /**
     * Gets current theme mode value.
     */
    fun getThemeMode(context: Context): Int {
        return context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .getInt(KEY_THEME_MODE, MODE_SYSTEM)
    }

    /**
     * Gets current accent color key.
     */
    fun getAccentColor(context: Context): String {
        return context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .getString(KEY_ACCENT_COLOR, ACCENT_BLUE) ?: ACCENT_BLUE
    }

    private fun isSunsetToSunrise(): Boolean {
        val hour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)
        // Basic sunset/sunrise scheduling: dark mode between 6 PM (18) and 6 AM (6)
        return hour >= 18 || hour < 6
    }
}
