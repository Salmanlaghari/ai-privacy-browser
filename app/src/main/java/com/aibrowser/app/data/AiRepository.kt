package com.aibrowser.app.data

import android.content.Context
import com.aibrowser.app.data.remote.GeminiClient
import com.aibrowser.app.domain.AiCache
import timber.log.Timber
import java.security.MessageDigest
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Repository to orchestrate AI features, daily limit tracking, and Room database caching.
 */
class AiRepository(
    private val context: Context,
    private val cacheDao: AiCacheDao,
    private val client: GeminiClient = GeminiClient()
) {

    companion object {
        const val FREE_LIMIT = 20
        const val CACHE_TTL_SEARCH = 24 * 60 * 60 * 1000L // 24 hours
        const val CACHE_TTL_GENERAL = 7 * 24 * 60 * 60 * 1000L // 7 days
    }

    private val prefs = context.getSharedPreferences("ai_usage_prefs", Context.MODE_PRIVATE)

    /**
     * Executes AI Search query. Checks cache first, and verifies daily user limit.
     */
    @Throws(Exception::class)
    suspend fun getAiSearch(query: String): String {
        val systemPrompt = "You are a helpful search assistant. Answer the user's question concisely and accurately. If relevant, cite sources as [1], [2] format. Keep answer under 200 words unless asked for detail."
        val prompt = "$systemPrompt\n\nQuestion: $query"
        return getAiResponseWithCache("search", query, prompt, CACHE_TTL_SEARCH)
    }

    /**
     * Executes Page Summary query. Checks cache first, and verifies daily user limit.
     */
    @Throws(Exception::class)
    suspend fun getPageSummary(articleText: String): String {
        val prompt = "Summarize this article in 5 bullet points. Article: $articleText"
        return getAiResponseWithCache("summary", articleText, prompt, CACHE_TTL_GENERAL)
    }

    /**
     * Executes Translation query. Checks cache first, and verifies daily user limit.
     */
    @Throws(Exception::class)
    suspend fun getPageTranslation(articleText: String, targetLang: String): String {
        val prompt = "Translate this webpage content to $targetLang. Preserve formatting. Content: $articleText"
        return getAiResponseWithCache("translate_$targetLang", articleText, prompt, CACHE_TTL_GENERAL)
    }

    /**
     * Executes Smart Compose text generation.
     */
    @Throws(Exception::class)
    suspend fun getSmartCompose(promptText: String): String {
        val prompt = "Generate a reply contextually fitting this prompt. Content: $promptText"
        return getAiResponseWithCache("compose", promptText, prompt, CACHE_TTL_GENERAL)
    }

    /**
     * Increments the user's daily usage tracker.
     */
    fun incrementDailyUsage() {
        val currentCount = getDailyUsageCount()
        prefs.edit().putInt(getDailyPrefKey(), currentCount + 1).apply()
        Timber.d("Daily usage incremented to %d", currentCount + 1)
    }

    /**
     * Increases count dynamically, for example when user watches an ad.
     */
    fun addBonusUsageCalls(bonus: Int) {
        val currentCount = getDailyUsageCount()
        // Reducing the current usage count effectively awards bonus calls
        val newCount = (currentCount - bonus).coerceAtLeast(0)
        prefs.edit().putInt(getDailyPrefKey(), newCount).apply()
        Timber.d("Awarded %d bonus calls. New count: %d", bonus, newCount)
    }

    /**
     * Returns true if user has remaining daily limit or is Premium.
     */
    fun canMakeAiCall(): Boolean {
        if (isPremiumUser()) return true
        return getDailyUsageCount() < FREE_LIMIT
    }

    /**
     * Returns the number of AI requests made today.
     */
    fun getDailyUsageCount(): Int {
        val key = getDailyPrefKey()
        return prefs.getInt(key, 0)
    }

    /**
     * Checks whether the user is a Premium subscriber.
     */
    fun isPremiumUser(): Boolean {
        val settingsPrefs = context.getSharedPreferences("browser_settings", Context.MODE_PRIVATE)
        return settingsPrefs.getBoolean("is_premium", false)
    }

    /**
     * Common method to handle cached response lookup and API client calls.
     */
    private suspend fun getAiResponseWithCache(
        feature: String,
        input: String,
        prompt: String,
        ttl: Long
    ): String {
        val key = generateCacheKey(feature, input)
        val cached = cacheDao.getByCacheKey(key)

        if (cached != null) {
            val age = System.currentTimeMillis() - cached.timestamp
            if (age < ttl) {
                Timber.d("Cache hit for feature: %s. Returning cached text.", feature)
                return cached.response
            } else {
                Timber.d("Cache expired for feature: %s. Deleting entry.", feature)
                cacheDao.delete(key)
            }
        }

        // Validate daily limit before invoking the network client
        if (!canMakeAiCall()) {
            throw Exception("Daily limit reached. Watch an ad for +10 calls, or upgrade to Premium.")
        }

        // Execute API call
        val result = client.generateContent(prompt)

        // Cache successful response
        cacheDao.insert(AiCache(cacheKey = key, response = result))
        incrementDailyUsage()

        return result
    }

    private fun generateCacheKey(feature: String, input: String): String {
        val rawString = feature + "_" + input.trim()
        val bytes = MessageDigest.getInstance("MD5").digest(rawString.toByteArray())
        return bytes.joinToString("") { "%02x".format(it) }
    }

    private fun getDailyPrefKey(): String {
        val dateString = SimpleDateFormat("yyyy-MM-dd", Locale.US).format(Date())
        return "usage_count_$dateString"
    }
}
