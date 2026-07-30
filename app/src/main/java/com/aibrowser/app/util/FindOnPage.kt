package com.aibrowser.app.util

import android.webkit.WebView

/**
 * Find on Page utility for searching text within a loaded web page.
 * Highlights all matches and allows navigation between them.
 */
object FindOnPage {

    private var currentQuery = ""
    private var matchCount = 0
    private var currentMatch = 0

    /**
     * Find and highlight all occurrences of the query in the page
     */
    fun find(webView: WebView, query: String, resultCallback: (Int) -> Unit) {
        currentQuery = query
        currentMatch = 0

        val js = """
            (function() {
                // Remove previous highlights
                var old = document.querySelectorAll('.ai-browser-highlight');
                old.forEach(function(el) {
                    el.outerHTML = el.innerHTML;
                });

                if ('$query' === '') return 0;

                var body = document.body.innerHTML;
                var regex = new RegExp('(' + '$query'.replace(/[.*+?^${}()|[\]\\]/g, '\\$&') + ')', 'gi');
                var count = (body.match(regex) || []).length;

                // Highlight matches
                document.body.innerHTML = body.replace(regex, '<mark class="ai-browser-highlight" style="background: #FFD700; color: #000; padding: 1px 2px; border-radius: 2px;">$1</mark>');

                // Scroll to first match
                var first = document.querySelector('.ai-browser-highlight');
                if (first) first.scrollIntoView({ behavior: 'smooth', block: 'center' });

                return count;
            })();
        """.trimIndent()

        webView.evaluateJavascript(js) { result ->
            matchCount = result?.replace("\"", "")?.toIntOrNull() ?: 0
            resultCallback(matchCount)
        }
    }

    /**
     * Navigate to the next match
     */
    fun next(webView: WebView) {
        if (matchCount == 0) return
        currentMatch = (currentMatch + 1) % matchCount

        val js = """
            (function() {
                var matches = document.querySelectorAll('.ai-browser-highlight');
                if (matches.length > 0) {
                    matches[$currentMatch].scrollIntoView({ behavior: 'smooth', block: 'center' });
                    matches.forEach(function(m) { m.style.outline = ''; });
                    matches[$currentMatch].style.outline = '2px solid #FF6B35';
                }
            })();
        """.trimIndent()

        webView.evaluateJavascript(js, null)
    }

    /**
     * Navigate to the previous match
     */
    fun previous(webView: WebView) {
        if (matchCount == 0) return
        currentMatch = if (currentMatch > 0) currentMatch - 1 else matchCount - 1

        val js = """
            (function() {
                var matches = document.querySelectorAll('.ai-browser-highlight');
                if (matches.length > 0) {
                    matches[$currentMatch].scrollIntoView({ behavior: 'smooth', block: 'center' });
                    matches.forEach(function(m) { m.style.outline = ''; });
                    matches[$currentMatch].style.outline = '2px solid #FF6B35';
                }
            })();
        """.trimIndent()

        webView.evaluateJavascript(js, null)
    }

    /**
     * Clear all highlights
     */
    fun clear(webView: WebView) {
        currentQuery = ""
        matchCount = 0
        currentMatch = 0

        val js = """
            (function() {
                var old = document.querySelectorAll('.ai-browser-highlight');
                old.forEach(function(el) { el.outerHTML = el.innerHTML; });
            })();
        """.trimIndent()

        webView.evaluateJavascript(js, null)
    }

    fun getMatchInfo(): String = "$currentMatch / $matchCount"
    fun getMatchCount(): Int = matchCount
}
