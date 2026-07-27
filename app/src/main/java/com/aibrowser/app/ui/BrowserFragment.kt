package com.aibrowser.app.ui

import android.app.DownloadManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.view.ContextMenu
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import android.webkit.*
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.widget.PopupMenu
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.aibrowser.app.R
import com.aibrowser.app.data.AppDatabase
import com.aibrowser.app.data.BookmarkRepository
import com.aibrowser.app.data.HistoryRepository
import com.aibrowser.app.data.TabManager
import com.aibrowser.app.databinding.FragmentBrowserBinding
import timber.log.Timber

/**
 * Core WebView browser component. Handles web loading, page navigation,
 * bookmarks, history integration, pull-to-refresh, downloads, and multi-tab rendering.
 */
class BrowserFragment : Fragment() {

    private var _binding: FragmentBrowserBinding? = null
    private val binding get() = _binding!!

    private lateinit var viewModel: BrowserViewModel

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentBrowserBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Initialize Manual DI & ViewModel
        val context = requireContext()
        val database = AppDatabase.getDatabase(context)
        val bookmarkRepo = BookmarkRepository(database.bookmarkDao())
        val historyRepo = HistoryRepository(database.historyDao())
        val factory = BrowserViewModel.Factory(bookmarkRepo, historyRepo)
        viewModel = ViewModelProvider(this, factory)[BrowserViewModel::class.java]

        setupWebView()
        setupBottomToolbar()
        setupTopBar()
        setupObservers()

        // Load default/active tab
        val activeTab = TabManager.getActiveTab()
        if (activeTab != null) {
            val urlToLoad = if (activeTab.url == "about:blank") {
                // If it is a blank tab, check user homepage settings, or load a fallback
                getHomepageUrl()
            } else {
                activeTab.url
            }
            binding.webView.loadUrl(urlToLoad)
            binding.urlEditText.setText(urlToLoad)
        }
    }

    private fun getHomepageUrl(): String {
        val prefs = requireContext().getSharedPreferences("browser_settings", Context.MODE_PRIVATE)
        return prefs.getString("homepage_url", "https://www.google.com") ?: "https://www.google.com"
    }

    private fun setupWebView() {
        val webView = binding.webView
        val settings = webView.settings

        // Enable settings requested by the specifications
        settings.javaScriptEnabled = true
        settings.domStorageEnabled = true
        settings.builtInZoomControls = true
        settings.displayZoomControls = false
        settings.cacheMode = WebSettings.LOAD_DEFAULT

        // Block mixed content (security requirement)
        settings.mixedContentMode = WebSettings.MIXED_CONTENT_NEVER_ALLOW

        // Smooth WebView rendering hardware acceleration
        webView.setLayerType(View.LAYER_TYPE_HARDWARE, null)

        // Cookies accept by default
        CookieManager.getInstance().setAcceptCookie(true)

        // Configure WebView Clients
        webView.webViewClient = object : WebViewClient() {
            override fun shouldOverrideUrlLoading(view: WebView?, request: WebResourceRequest?): Boolean {
                // Intercept navigation
                return false
            }

            override fun onPageStarted(view: WebView?, url: String?, favicon: android.graphics.Bitmap?) {
                super.onPageStarted(view, url, favicon)
                url?.let {
                    binding.urlEditText.setText(it)
                    viewModel.checkIsBookmarked(it)
                    // Update current active tab title/URL state
                    TabManager.getActiveTab()?.let { tab ->
                        TabManager.updateTab(tab.id, view?.title ?: "Loading...", it)
                    }
                }
                binding.progressBar.visibility = View.VISIBLE
            }

            override fun onPageFinished(view: WebView?, url: String?) {
                super.onPageFinished(view, url)
                url?.let {
                    viewModel.checkIsBookmarked(it)
                    val pageTitle = view?.title ?: "AI Browser"
                    viewModel.recordVisit(it, pageTitle)
                    TabManager.getActiveTab()?.let { tab ->
                        TabManager.updateTab(tab.id, pageTitle, it)
                    }
                }
                binding.progressBar.visibility = View.GONE
                binding.swipeRefreshLayout.isRefreshing = false
            }

            override fun onReceivedSslError(view: WebView?, handler: SslErrorHandler?, error: android.net.http.SslError?) {
                // Simple warning placeholder, cancel for safety or proceed for testing based on preference
                Toast.makeText(context, "SSL Connection Warning", Toast.LENGTH_SHORT).show()
                handler?.cancel() // Standard secure default
            }

            override fun shouldInterceptRequest(view: WebView?, request: WebResourceRequest?): WebResourceResponse? {
                // Ad blocker phase 4 placeholder
                return super.shouldInterceptRequest(view, request)
            }
        }

        webView.webChromeClient = object : WebChromeClient() {
            override fun onProgressChanged(view: WebView?, newProgress: Int) {
                super.onProgressChanged(view, newProgress)
                binding.progressBar.progress = newProgress
            }

            override fun onShowFileChooser(
                webView: WebView?,
                filePathCallback: ValueCallback<Array<Uri>>?,
                fileChooserParams: FileChooserParams?
            ): Boolean {
                // Placeholder/Return true to indicate we will handle it in a future update
                Toast.makeText(context, "File upload is a placeholder (Phase 2)", Toast.LENGTH_SHORT).show()
                return false
            }

            override fun onCreateWindow(
                view: WebView?,
                isDialog: Boolean,
                isUserGesture: Boolean,
                resultMsg: android.os.Message?
            ): Boolean {
                // Multi-window popup support
                return super.onCreateWindow(view, isDialog, isUserGesture, resultMsg)
            }
        }

        // Setup Pull-to-refresh on WebView
        binding.swipeRefreshLayout.setOnRefreshListener {
            webView.reload()
        }

        // Setup downloads using Android DownloadManager
        webView.setDownloadListener { url, userAgent, contentDisposition, mimetype, contentLength ->
            try {
                val request = DownloadManager.Request(Uri.parse(url)).apply {
                    setMimeType(mimetype)
                    addRequestHeader("User-Agent", userAgent)
                    setDescription("Downloading file...")
                    setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
                    setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, URLUtil.guessFileName(url, contentDisposition, mimetype))
                }
                val dm = requireContext().getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager
                dm.enqueue(request)
                Toast.makeText(context, "Download started...", Toast.LENGTH_SHORT).show()
            } catch (e: Exception) {
                Timber.e(e, "Failed to start download")
                Toast.makeText(context, "Download failed", Toast.LENGTH_SHORT).show()
            }
        }

        // Long press link context menus
        registerForContextMenu(webView)
    }

    private fun setupTopBar() {
        binding.btnGo.setOnClickListener {
            val input = binding.urlEditText.text.toString().trim()
            if (input.isNotEmpty()) {
                loadResolvedUrl(input)
            }
        }

        binding.urlEditText.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_GO) {
                val input = binding.urlEditText.text.toString().trim()
                if (input.isNotEmpty()) {
                    loadResolvedUrl(input)
                    true
                } else false
            } else false
        }

        // Bookmark Toggle click action
        binding.btnBookmark.setOnClickListener {
            val url = binding.webView.url ?: ""
            val title = binding.webView.title ?: "No Title"
            if (url.isNotEmpty() && url != "about:blank") {
                viewModel.toggleBookmark(title, url)
            } else {
                Toast.makeText(context, "Cannot bookmark a blank page", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun loadResolvedUrl(input: String) {
        val trimmed = input.trim()
        val isUrl = trimmed.contains(".") && (trimmed.contains("://") ||
                trimmed.matches(Regex("^(?i)(https?://)?([a-z0-9]+(-[a-z0-9]+)*\\.)+[a-z]{2,}(/.*)?$")))

        val resolvedUrl = if (isUrl) {
            if (!trimmed.startsWith("http://") && !trimmed.startsWith("https://")) {
                "https://$trimmed"
            } else {
                trimmed
            }
        } else {
            val prefs = requireContext().getSharedPreferences("browser_settings", Context.MODE_PRIVATE)
            val useDuckDuckGo = prefs.getBoolean("use_duckduckgo", false)
            val searchBase = if (useDuckDuckGo) "https://duckduckgo.com/?q=" else "https://www.google.com/search?q="
            searchBase + java.net.URLEncoder.encode(trimmed, "UTF-8")
        }

        binding.webView.loadUrl(resolvedUrl)
        binding.urlEditText.setText(resolvedUrl)
    }

    private fun setupBottomToolbar() {
        binding.btnBack.setOnClickListener {
            if (binding.webView.canGoBack()) {
                binding.webView.goBack()
            } else {
                // If can't go back, close or return to home
                activity?.finish()
            }
        }

        binding.btnForward.setOnClickListener {
            if (binding.webView.canGoForward()) {
                binding.webView.goForward()
            }
        }

        binding.btnReload.setOnClickListener {
            binding.webView.reload()
        }

        binding.btnShare.setOnClickListener {
            val url = binding.webView.url
            if (!url.isNullOrEmpty() && url != "about:blank") {
                val sendIntent = Intent().apply {
                    action = Intent.ACTION_SEND
                    putExtra(Intent.EXTRA_TEXT, url)
                    type = "text/plain"
                }
                startActivity(Intent.createChooser(sendIntent, "Share Page"))
            } else {
                Toast.makeText(context, "No shareable page loaded", Toast.LENGTH_SHORT).show()
            }
        }

        binding.btnTabs.setOnClickListener {
            val intent = Intent(requireContext(), TabsActivity::class.java)
            startActivity(intent)
        }

        binding.btnMenu.setOnClickListener { view ->
            showOverflowMenu(view)
        }
    }

    private fun showOverflowMenu(view: View) {
        val popup = PopupMenu(requireContext(), view)
        popup.menuInflater.inflate(R.menu.menu_browser, popup.menu)
        popup.setOnMenuItemClickListener { menuItem ->
            when (menuItem.itemId) {
                R.id.action_add_bookmark -> {
                    val url = binding.webView.url ?: ""
                    val title = binding.webView.title ?: "No Title"
                    if (url.isNotEmpty() && url != "about:blank") {
                        viewModel.toggleBookmark(title, url)
                    }
                    true
                }
                R.id.action_bookmarks -> {
                    startActivity(Intent(requireContext(), BookmarksActivity::class.java))
                    true
                }
                R.id.action_history -> {
                    startActivity(Intent(requireContext(), HistoryActivity::class.java))
                    true
                }
                R.id.action_settings -> {
                    startActivity(Intent(requireContext(), SettingsActivity::class.java))
                    true
                }
                else -> false
            }
        }
        popup.show()
    }

    private fun setupObservers() {
        viewModel.isCurrentPageBookmarked.observe(viewLifecycleOwner) { isBookmarked ->
            if (isBookmarked) {
                binding.btnBookmark.setImageResource(R.drawable.ic_bookmark)
                binding.btnBookmark.setColorFilter(resources.getColor(R.color.primary, null))
            } else {
                binding.btnBookmark.setImageResource(R.drawable.ic_bookmark)
                binding.btnBookmark.clearColorFilter()
            }
        }
    }

    override fun onCreateContextMenu(menu: ContextMenu, v: View, menuInfo: ContextMenu.ContextMenuInfo?) {
        super.onCreateContextMenu(menu, v, menuInfo)
        val webView = v as WebView
        val result = webView.hitTestResult

        if (result.type == WebView.HitTestResult.SRC_ANCHOR_TYPE ||
            result.type == WebView.HitTestResult.SRC_IMAGE_ANCHOR_TYPE) {
            val linkUrl = result.extra
            menu.setHeaderTitle(linkUrl ?: "Link Options")
            menu.add(0, 1, 0, "Open in new tab")
            menu.add(0, 2, 0, "Copy link address")
            menu.add(0, 3, 0, "Share link")
            menu.add(0, 4, 0, "Download link content")
        }
    }

    override fun onContextItemSelected(item: MenuItem): Boolean {
        val result = binding.webView.hitTestResult
        val linkUrl = result.extra ?: return super.onContextItemSelected(item)

        when (item.itemId) {
            1 -> {
                // Open in new tab
                TabManager.createTab("New Tab", linkUrl)
                binding.webView.loadUrl(linkUrl)
                binding.urlEditText.setText(linkUrl)
                Toast.makeText(context, "Opened in new tab", Toast.LENGTH_SHORT).show()
                return true
            }
            2 -> {
                // Copy link address
                val clipboard = requireContext().getSystemService(Context.CLIPBOARD_SERVICE) as android.content.ClipboardManager
                val clip = android.content.ClipData.newPlainText("Copied Link", linkUrl)
                clipboard.setPrimaryClip(clip)
                Toast.makeText(context, "Link copied to clipboard", Toast.LENGTH_SHORT).show()
                return true
            }
            3 -> {
                // Share link
                val sendIntent = Intent().apply {
                    action = Intent.ACTION_SEND
                    putExtra(Intent.EXTRA_TEXT, linkUrl)
                    type = "text/plain"
                }
                startActivity(Intent.createChooser(sendIntent, "Share Link"))
                return true
            }
            4 -> {
                // Download link content
                val request = DownloadManager.Request(Uri.parse(linkUrl)).apply {
                    setDescription("Downloading from link...")
                    setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
                    setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, URLUtil.guessFileName(linkUrl, null, null))
                }
                val dm = requireContext().getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager
                dm.enqueue(request)
                Toast.makeText(context, "Download started...", Toast.LENGTH_SHORT).show()
                return true
            }
        }
        return super.onContextItemSelected(item)
    }

    override fun onResume() {
        super.onResume()
        // Reload settings if homepage or default tab changes
        val activeTab = TabManager.getActiveTab()
        if (activeTab != null && activeTab.url != binding.webView.url && activeTab.url != "about:blank") {
            binding.webView.loadUrl(activeTab.url)
            binding.urlEditText.setText(activeTab.url)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
