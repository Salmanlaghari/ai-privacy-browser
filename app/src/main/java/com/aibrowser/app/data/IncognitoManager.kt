package com.aibrowser.app.data

import android.content.Context
import android.webkit.CookieManager
import android.webkit.WebStorage

/**
 * Manages Incognito (Private Browsing) mode.
 * When active: no history saved, no cookies persisted, no cache stored.
 */
object IncognitoManager {

    private var isIncognito = false
    private val listeners = mutableListOf<(Boolean) -> Unit>()

    fun isActive(): Boolean = isIncognito

    fun toggle() {
        isIncognito = !isIncognito
        listeners.forEach { it(isIncognito) }
    }

    fun enable() {
        isIncognito = true
        listeners.forEach { it(true) }
    }

    fun disable() {
        isIncognito = false
        listeners.forEach { it(false) }
    }

    fun addListener(listener: (Boolean) -> Unit) {
        listeners.add(listener)
    }

    fun removeListener(listener: (Boolean) -> Unit) {
        listeners.remove(listener)
    }

    /**
     * Clear all browsing data (cookies, cache, local storage)
     */
    fun clearAllData(context: Context) {
        CookieManager.getInstance().removeAllCookies(null)
        WebStorage.getInstance().deleteAllData()
        context.cacheDir?.deleteRecursively()
        context.deleteDatabase("browser_history.db")
    }

    /**
     * Get status text for UI display
     */
    fun getStatusText(): String {
        return if (isIncognito) "Incognito ON" else "Incognito OFF"
    }
}
