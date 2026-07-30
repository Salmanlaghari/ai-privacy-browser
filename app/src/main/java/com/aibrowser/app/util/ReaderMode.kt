package com.aibrowser.app.util

import android.webkit.WebView

/**
 * Reader Mode strips ads, navigation, and clutter from web pages
 * to provide a clean, distraction-free reading experience.
 */
object ReaderMode {

    private var isActive = false

    fun isActive(): Boolean = isActive

    /**
     * Inject reader mode CSS and JS to strip clutter
     */
    fun activate(webView: WebView) {
        isActive = true

        val css = """
            (function() {
                var style = document.createElement('style');
                style.id = 'reader-mode-style';
                style.textContent = `
                    body {
                        font-family: Georgia, 'Times New Roman', serif !important;
                        font-size: 18px !important;
                        line-height: 1.8 !important;
                        max-width: 700px !important;
                        margin: 0 auto !important;
                        padding: 20px !important;
                        background: #fafafa !important;
                        color: #333 !important;
                    }
                    header, footer, nav, aside, .ad, .ads, .advertisement,
                    .sidebar, .menu, .header, .footer, .nav, .navigation,
                    .social-share, .comments, .related-posts, .popup,
                    [class*="ad-"], [id*="ad-"], [class*="banner"],
                    iframe, embed, object, .cookie-banner, .newsletter-signup {
                        display: none !important;
                    }
                    img {
                        max-width: 100% !important;
                        height: auto !important;
                        display: block !important;
                        margin: 16px auto !important;
                    }
                    h1, h2, h3, h4, h5, h6 {
                        font-family: Georgia, serif !important;
                        margin-top: 1.5em !important;
                        margin-bottom: 0.5em !important;
                        color: #111 !important;
                    }
                    h1 { font-size: 28px !important; }
                    h2 { font-size: 24px !important; }
                    p { margin-bottom: 1em !important; }
                    blockquote {
                        border-left: 3px solid #ccc !important;
                        padding-left: 16px !important;
                        margin: 16px 0 !important;
                        color: #666 !important;
                        font-style: italic !important;
                    }
                    code, pre {
                        background: #f4f4f4 !important;
                        padding: 2px 6px !important;
                        border-radius: 3px !important;
                        font-size: 14px !important;
                    }
                    a { color: #0066cc !important; text-decoration: underline !important; }
                `;
                document.head.appendChild(style);
                document.body.classList.add('reader-mode-active');
            })();
        """.trimIndent()

        webView.evaluateJavascript(css, null)
    }

    /**
     * Deactivate reader mode by removing injected styles
     */
    fun deactivate(webView: WebView) {
        isActive = false

        val js = """
            (function() {
                var style = document.getElementById('reader-mode-style');
                if (style) style.remove();
                document.body.classList.remove('reader-mode-active');
            })();
        """.trimIndent()

        webView.evaluateJavascript(js, null)
    }

    /**
     * Toggle reader mode
     */
    fun toggle(webView: WebView) {
        if (isActive) deactivate(webView) else activate(webView)
    }
}
