package com.aibrowser.app.util

import android.text.Html
import android.text.Spanned

/**
 * Utility to convert markdown syntax to Android [Spanned] HTML text.
 */
object MarkdownRenderer {

    /**
     * Converts markdown syntax (such as bold, italics, bullets, headers) into [Spanned] HTML text.
     */
    fun renderMarkdown(markdown: String): Spanned {
        var html = markdown
            // Escape HTML entities to avoid broken tags
            .replace("&", "&amp;")
            .replace("<", "&lt;")
            .replace(">", "&gt;")

            // Bold (**text** or __text__)
            .replace(Regex("\\*\\*(.*?)\\*\\*"), "<b>$1</b>")
            .replace(Regex("__(.*?)__"), "<b>$1</b>")

            // Italic (*text* or _text_)
            .replace(Regex("\\*(.*?)\\*"), "<i>$1</i>")
            .replace(Regex("_(.*?)_"), "<i>$1</i>")

            // Bullets (* or -)
            .replace(Regex("(?m)^\\*\\s+(.*?)$"), "<li>$1</li>")
            .replace(Regex("(?m)^-\\s+(.*?)$"), "<li>$1</li>")

            // Headers
            .replace(Regex("(?m)^###\\s+(.*?)$"), "<h3>$1</h3>")
            .replace(Regex("(?m)^##\\s+(.*?)$"), "<h2>$1</h2>")
            .replace(Regex("(?m)^#\\s+(.*?)$"), "<h1>$1</h1>")

            // Newlines to line breaks
            .replace("\n", "<br/>")

        // Wrap list items in <ul>
        if (html.contains("<li>")) {
            html = "<ul>$html</ul>"
        }

        return Html.fromHtml(html, Html.FROM_HTML_MODE_LEGACY)
    }
}
