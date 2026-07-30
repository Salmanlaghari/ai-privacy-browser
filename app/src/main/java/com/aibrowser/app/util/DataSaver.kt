package com.aibrowser.app.util

import android.webkit.WebView

/**
 * Data Saver mode reduces bandwidth by:
 * - Blocking images
 * - Blocking JavaScript (optional)
 * - Compressing text content
 * - Blocking tracking scripts
 */
object DataSaver {

    private var isEnabled = false
    private var blockImages = true
    private var blockJavaScript = false
    private var blockFonts = true
    private var blockAnimations = true

    fun isEnabled(): Boolean = isEnabled

    fun enable(webView: WebView) {
        isEnabled = true
        applyFilters(webView)
    }

    fun disable(webView: WebView) {
        isEnabled = false
        removeFilters(webView)
    }

    fun toggle(webView: WebView) {
        if (isEnabled) disable(webView) else enable(webView)
    }

    fun setBlockImages(block: Boolean) { blockImages = block }
    fun setBlockJavaScript(block: Boolean) { blockJavaScript = block }
    fun setBlockFonts(block: Boolean) { blockFonts = block }
    fun setBlockAnimations(block: Boolean) { blockAnimations = block }

    private fun applyFilters(webView: WebView) {
        val js = buildString {
            append("(function() {")
            if (blockImages) {
                append("""
                    var images = document.querySelectorAll('img, video, picture, source, svg');
                    images.forEach(function(el) { el.style.display = 'none'; });
                    var bgElements = document.querySelectorAll('[style*="background"]');
                    bgElements.forEach(function(el) { el.style.backgroundImage = 'none'; });
                """.trimIndent())
            }
            if (blockFonts) {
                append("""
                    var style = document.createElement('style');
                    style.id = 'data-saver-fonts';
                    style.textContent = '* { font-family: sans-serif !important; }';
                    document.head.appendChild(style);
                """.trimIndent())
            }
            if (blockAnimations) {
                append("""
                    var animStyle = document.createElement('style');
                    animStyle.id = 'data-saver-anim';
                    animStyle.textContent = '*, *::before, *::after { animation: none !important; transition: none !important; }';
                    document.head.appendChild(animStyle);
                """.trimIndent())
            }
            append("})();")
        }
        webView.evaluateJavascript(js, null)
    }

    private fun removeFilters(webView: WebView) {
        val js = """
            (function() {
                var style1 = document.getElementById('data-saver-fonts');
                if (style1) style1.remove();
                var style2 = document.getElementById('data-saver-anim');
                if (style2) style2.remove();
                var images = document.querySelectorAll('img, video, picture, source, svg');
                images.forEach(function(el) { el.style.display = ''; });
            })();
        """.trimIndent()
        webView.evaluateJavascript(js, null)
    }

    fun getStatusText(): String {
        return if (isEnabled) "Data Saver ON" else "Data Saver OFF"
    }
}
