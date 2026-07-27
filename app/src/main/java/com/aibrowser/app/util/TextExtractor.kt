package com.aibrowser.app.util

import android.webkit.WebView

/**
 * Utility to extract clean, readable text from a [WebView] for AI processing (Summaries/Translations).
 */
object TextExtractor {

    private const val EXTRACTION_SCRIPT = """
        (function() {
            var text = '';
            var paras = document.getElementsByTagName('p');
            for (var i = 0; i < paras.length; i++) {
                text += paras[i].innerText + '\n';
            }
            // Fallback to body innerText if no paragraphs are found
            if (!text.trim()) {
                text = document.body.innerText;
            }
            // Truncate to safe character boundaries (e.g. 8000 characters)
            return text.substring(0, 8000);
        })()
    """

    /**
     * Extracts text from [webView] asynchronously and returns it via [callback].
     */
    fun extractText(webView: WebView, callback: (String) -> Unit) {
        webView.evaluateJavascript(EXTRACTION_SCRIPT) { result ->
            // JS evaluation returns string wrapped in quotes or escaped, unescape basic quotes
            var cleanText = result ?: ""
            if (cleanText.startsWith("\"") && cleanText.endsWith("\"") && cleanText.length > 1) {
                cleanText = cleanText.substring(1, cleanText.length - 1)
            }
            // Replace escaped JS elements
            cleanText = cleanText
                .replace("\\n", "\n")
                .replace("\\\"", "\"")
                .replace("\\t", " ")
                .trim()

            if (cleanText.isEmpty() || cleanText == "null") {
                cleanText = "No readable content extracted from webpage."
            }
            callback(cleanText)
        }
    }
}
