package com.aibrowser.app.data

import android.content.Context
import android.net.Uri
import timber.log.Timber
import java.io.BufferedReader
import java.io.InputStreamReader
import java.util.Locale
import java.util.concurrent.ConcurrentHashMap

/**
 * Thread-safe ad and tracker blocking engine using host-based blacklists loaded from assets.
 */
class AdBlockEngine private constructor(context: Context) {

    companion object {
        @Volatile
        private var INSTANCE: AdBlockEngine? = null

        /**
         * Returns the singleton instance of the AdBlockEngine.
         */
        fun getInstance(context: Context): AdBlockEngine {
            return INSTANCE ?: synchronized(this) {
                val instance = AdBlockEngine(context.applicationContext)
                INSTANCE = instance
                instance
            }
        }
    }

    private val blockedDomains = ConcurrentHashMap.newKeySet<String>()

    init {
        loadBlocklists(context)
    }

    /**
     * Checks if a given [url] is blocked by checking its host or any parent domains against the blocklist.
     */
    fun isBlocked(url: String): Boolean {
        val uri = try {
            Uri.parse(url)
        } catch (e: Exception) {
            return false
        }
        val host = uri.host?.lowercase(Locale.US) ?: return false

        // Check the direct host first
        if (blockedDomains.contains(host)) {
            Timber.d("AdBlockEngine: BLOCKED direct host: %s", url)
            return true
        }

        // Check parent domains (e.g., ad.doubleclick.net -> check doubleclick.net)
        var tempHost = host
        while (tempHost.contains(".")) {
            val nextDotIndex = tempHost.indexOf('.')
            if (nextDotIndex != -1 && nextDotIndex < tempHost.length - 1) {
                tempHost = tempHost.substring(nextDotIndex + 1)
                if (blockedDomains.contains(tempHost)) {
                    Timber.d("AdBlockEngine: BLOCKED parent host: %s (via rule: %s)", url, tempHost)
                    return true
                }
            } else {
                break
            }
        }

        return false
    }

    private fun loadBlocklists(context: Context) {
        val assetManager = context.assets
        val files = arrayOf(
            "blocklists/easylist.txt",
            "blocklists/easyprivacy.txt",
            "blocklists/stevenblack.txt",
            "blocklists/malwaredomains.txt"
        )

        var count = 0
        for (filePath in files) {
            try {
                assetManager.open(filePath).use { inputStream ->
                    BufferedReader(InputStreamReader(inputStream)).use { reader ->
                        var line = reader.readLine()
                        while (line != null) {
                            val trimmedLine = line.trim()
                            if (trimmedLine.isNotEmpty() && !trimmedLine.startsWith("#")) {
                                var domain = trimmedLine
                                // If it is in hosts file format: "0.0.0.0 doubleclick.net" or "127.0.0.1 doubleclick.net"
                                if (domain.startsWith("0.0.0.0") || domain.startsWith("127.0.0.1")) {
                                    val parts = domain.split(Regex("\\s+"))
                                    if (parts.size >= 2) {
                                        domain = parts[1]
                                    }
                                }
                                blockedDomains.add(domain.lowercase(Locale.US))
                                count++
                            }
                            line = reader.readLine()
                        }
                    }
                }
                Timber.d("AdBlockEngine: Loaded %s successfully.", filePath)
            } catch (e: Exception) {
                Timber.e(e, "AdBlockEngine: Failed to read blocklist asset: %s", filePath)
            }
        }
        Timber.d("AdBlockEngine successfully loaded %d unique block rules in-memory.", count)
    }
}
