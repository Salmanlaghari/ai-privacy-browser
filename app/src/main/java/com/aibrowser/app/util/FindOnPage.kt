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

    fun find(webView: WebView, query: String, resultCallback: (Int) -> Unit) {
        currentQuery = query
        currentMatch = 0

        val escapedQuery = query.replace("\\", "\\\\")
            .replace("'", "\\'")
            .replace("\n", "\\n")

        val js = "(function() {" +
                "var old = document.querySelectorAll('.ai-browser-highlight');" +
                "old.forEach(function(el) { el.outerHTML = el.innerHTML; });" +
                "var query = '$escapedQuery';" +
                "if (query === '') return 0;" +
                "var body = document.body.innerHTML;" +
                "var safeQuery = query.replace(/[.*+?^\${}()|[\\]\\\\]/g, '\\\\' + '\$&');" +
                "var regex = new RegExp('(' + safeQuery + ')', 'gi');" +
                "var count = (body.match(regex) || []).length;" +
                "document.body.innerHTML = body.replace(regex, '<mark class=\"ai-browser-highlight\" style=\"background:#FFD700;color:#000;padding:1px 2px;border-radius:2px;\">' + '\$1</mark>');" +
                "var first = document.querySelector('.ai-browser-highlight');" +
                "if (first) first.scrollIntoView({behavior:'smooth',block:'center'});" +
                "return count;" +
                "})();"

        webView.evaluateJavascript(js) { result ->
            matchCount = result?.replace("\"", "")?.toIntOrNull() ?: 0
            resultCallback(matchCount)
        }
    }

    fun next(webView: WebView) {
        if (matchCount == 0) return
        currentMatch = (currentMatch + 1) % matchCount
        scrollToMatch(webView, currentMatch)
    }

    fun previous(webView: WebView) {
        if (matchCount == 0) return
        currentMatch = if (currentMatch > 0) currentMatch - 1 else matchCount - 1
        scrollToMatch(webView, currentMatch)
    }

    private fun scrollToMatch(webView: WebView, index: Int) {
        val js = "(function() {" +
                "var matches = document.querySelectorAll('.ai-browser-highlight');" +
                "if (matches.length > $index) {" +
                "matches[$index].scrollIntoView({behavior:'smooth',block:'center'});" +
                "matches.forEach(function(m) { m.style.outline = ''; });" +
                "matches[$index].style.outline = '2px solid #FF6B35';" +
                "}" +
                "})();"
        webView.evaluateJavascript(js, null)
    }

    fun clear(webView: WebView) {
        currentQuery = ""
        matchCount = 0
        currentMatch = 0
        val js = "(function() {" +
                "var old = document.querySelectorAll('.ai-browser-highlight');" +
                "old.forEach(function(el) { el.outerHTML = el.innerHTML; });" +
                "})();"
        webView.evaluateJavascript(js, null)
    }

    fun getMatchInfo(): String = "$currentMatch / $matchCount"
    fun getMatchCount(): Int = matchCount
}
