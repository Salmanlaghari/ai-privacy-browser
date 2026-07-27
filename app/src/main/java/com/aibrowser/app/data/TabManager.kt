package com.aibrowser.app.data

import android.content.Context
import android.content.SharedPreferences
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import timber.log.Timber

/**
 * Represents a single web browser tab in the tab hierarchy.
 *
 * @property id Unique identifier for the tab.
 * @property title Document title of the loaded page.
 * @property url Current URL of the tab.
 * @property isIncognito Whether the tab is in incognito mode (no history/cache recorded).
 * @property lastAccessed Last access timestamp in milliseconds.
 */
data class Tab(
    val id: String,
    var title: String,
    var url: String,
    val isIncognito: Boolean = false,
    var lastAccessed: Long = System.currentTimeMillis()
)

/**
 * Singleton [TabManager] for orchestrating, persisting, and restoring active browser tabs.
 */
object TabManager {

    private const val PREFS_NAME = "tab_manager_prefs"
    private const val KEY_TABS = "key_tabs_list"
    private const val KEY_ACTIVE_TAB_ID = "key_active_tab_id"

    private val tabs = mutableListOf<Tab>()
    private var activeTabId: String? = null
    private val gson = Gson()

    /**
     * Re-initializes and restores tabs from SharedPreferences.
     */
    fun initialize(context: Context) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val tabsJson = prefs.getString(KEY_TABS, null)
        activeTabId = prefs.getString(KEY_ACTIVE_TAB_ID, null)

        tabs.clear()
        if (tabsJson != null) {
            try {
                val type = object : TypeToken<List<Tab>>() {}.type
                val restoredTabs: List<Tab> = gson.fromJson(tabsJson, type)
                tabs.addAll(restoredTabs)
                Timber.d("TabManager restored %d tabs.", tabs.size)
            } catch (e: Exception) {
                Timber.e(e, "Error deserializing tabs from SharedPreferences.")
            }
        }

        // If no tabs exist, create a default tab
        if (tabs.isEmpty()) {
            createTab("New Tab", "about:blank")
        }

        // Ensure activeTabId is valid
        if (activeTabId == null || tabs.none { it.id == activeTabId }) {
            activeTabId = tabs.firstOrNull()?.id
        }
    }

    /**
     * Persists all active tabs and active tab ID to SharedPreferences.
     */
    fun persist(context: Context) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val tabsJson = gson.toJson(tabs)
        prefs.edit()
            .putString(KEY_TABS, tabsJson)
            .putString(KEY_ACTIVE_TAB_ID, activeTabId)
            .apply()
        Timber.d("TabManager persisted %d tabs. Active Tab ID: %s", tabs.size, activeTabId)
    }

    /**
     * Creates a new browser tab and sets it as the active tab.
     */
    fun createTab(title: String, url: String, isIncognito: Boolean = false): Tab {
        val id = java.util.UUID.randomUUID().toString()
        val newTab = Tab(
            id = id,
            title = title,
            url = url,
            isIncognito = isIncognito,
            lastAccessed = System.currentTimeMillis()
        )
        tabs.add(newTab)
        activeTabId = id
        Timber.d("TabManager created tab. ID: %s, URL: %s", id, url)
        return newTab
    }

    /**
     * Closes an existing tab by its ID. Re-assigns active tab if necessary.
     */
    fun closeTab(tabId: String, context: Context? = null) {
        val index = tabs.indexOfFirst { it.id == tabId }
        if (index != -1) {
            tabs.removeAt(index)
            Timber.d("TabManager closed tab. ID: %s", tabId)
            if (activeTabId == tabId) {
                activeTabId = if (tabs.isNotEmpty()) {
                    val nextIndex = if (index < tabs.size) index else tabs.size - 1
                    tabs[nextIndex].id
                } else {
                    null
                }
            }
        }

        // If no tabs remain, create a fallback new tab
        if (tabs.isEmpty()) {
            createTab("New Tab", "about:blank")
        }

        context?.let { persist(it) }
    }

    /**
     * Switches the current active tab to [tabId].
     */
    fun switchTab(tabId: String) {
        if (tabs.any { it.id == tabId }) {
            activeTabId = tabId
            getActiveTab()?.lastAccessed = System.currentTimeMillis()
            Timber.d("TabManager switched active tab to ID: %s", tabId)
        }
    }

    /**
     * Returns the current active [Tab].
     */
    fun getActiveTab(): Tab? {
        return tabs.firstOrNull { it.id == activeTabId } ?: tabs.firstOrNull()
    }

    /**
     * Returns all open [Tab] items.
     */
    fun getAllTabs(): List<Tab> {
        return tabs
    }

    /**
     * Updates the details of a tab by ID.
     */
    fun updateTab(tabId: String, title: String, url: String) {
        val tab = tabs.find { it.id == tabId }
        tab?.let {
            it.title = title
            it.url = url
            it.lastAccessed = System.currentTimeMillis()
            Timber.d("TabManager updated tab ID: %s to Title: %s, URL: %s", tabId, title, url)
        }
    }

    /**
     * Clear all tabs (for setting clear data).
     */
    fun clearAllTabs() {
        tabs.clear()
        activeTabId = null
    }
}
