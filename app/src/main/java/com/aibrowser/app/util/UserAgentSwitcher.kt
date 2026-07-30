package com.aibrowser.app.util

import android.webkit.WebView

/**
 * User Agent switcher for desktop/mobile mode toggling.
 * Useful for accessing desktop-only websites on mobile.
 */
object UserAgentSwitcher {

    enum class AgentMode(val label: String, val agent: String) {
        MOBILE("Mobile", "Mozilla/5.0 (Linux; Android 14; Pixel 8) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Mobile Safari/537.36"),
        DESKTOP("Desktop", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Safari/537.36"),
        TABLET("Tablet", "Mozilla/5.0 (Linux; Android 14; SM-X900) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Safari/537.36"),
        CHROME("Chrome", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Safari/537.36"),
        FIREFOX("Firefox", "Mozilla/5.0 (Windows NT 10.0; Win64; x64; rv:121.0) Gecko/20100101 Firefox/121.0"),
        SAFARI("Safari", "Mozilla/5.0 (Macintosh; Intel Mac OS X 14_0) AppleWebKit/605.1.15 (KHTML, like Gecko) Version/17.0 Safari/605.1.15"),
        BOT("Bot/Crawler", "Mozilla/5.0 (compatible; Googlebot/2.1; +http://www.google.com/bot.html)")
    }

    private var currentMode = AgentMode.MOBILE

    fun getCurrentMode(): AgentMode = currentMode

    fun switchMode(webView: WebView, mode: AgentMode) {
        currentMode = mode
        webView.settings.userAgentString = mode.agent
        webView.reload()
    }

    fun toggleDesktop(webView: WebView) {
        if (currentMode == AgentMode.DESKTOP) {
            switchMode(webView, AgentMode.MOBILE)
        } else {
            switchMode(webView, AgentMode.DESKTOP)
        }
    }

    fun isDesktopMode(): Boolean = currentMode == AgentMode.DESKTOP

    fun getCurrentLabel(): String = currentMode.label
}
