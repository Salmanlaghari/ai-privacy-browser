package com.aibrowser.app.data

import android.content.Context
import android.content.SharedPreferences
import org.json.JSONArray
import org.json.JSONObject

/**
 * Speed Dial manager for quick-access favorite websites on the home screen.
 * Users can add, remove, and reorder their favorite sites.
 */
data class SpeedDialItem(
    val id: String,
    val title: String,
    val url: String,
    val color: String = "#FF6B35", // Default orange
    val order: Int = 0
)

object SpeedDialManager {

    private const val PREFS_NAME = "speed_dial"
    private const val KEY_ITEMS = "items"

    // Default speed dial items
    private val defaultItems = listOf(
        SpeedDialItem("1", "Google", "https://www.google.com", "#4285F4", 0),
        SpeedDialItem("2", "YouTube", "https://www.youtube.com", "#FF0000", 1),
        SpeedDialItem("3", "Facebook", "https://www.facebook.com", "#1877F2", 2),
        SpeedDialItem("4", "Twitter", "https://twitter.com", "#1DA1F2", 3),
        SpeedDialItem("5", "Instagram", "https://www.instagram.com", "#E4405F", 4),
        SpeedDialItem("6", "Wikipedia", "https://www.wikipedia.org", "#636466", 5),
        SpeedDialItem("7", "Reddit", "https://www.reddit.com", "#FF4500", 6),
        SpeedDialItem("8", "GitHub", "https://github.com", "#333333", 7)
    )

    fun getItems(context: Context): List<SpeedDialItem> {
        val prefs = getPrefs(context)
        val json = prefs.getString(KEY_ITEMS, null)

        if (json == null) {
            // Save defaults and return them
            saveItems(context, defaultItems)
            return defaultItems
        }

        return try {
            val array = JSONArray(json)
            (0 until array.length()).map { i ->
                val obj = array.getJSONObject(i)
                SpeedDialItem(
                    id = obj.getString("id"),
                    title = obj.getString("title"),
                    url = obj.getString("url"),
                    color = obj.optString("color", "#FF6B35"),
                    order = obj.optInt("order", i)
                )
            }
        } catch (e: Exception) {
            defaultItems
        }
    }

    fun addItem(context: Context, title: String, url: String, color: String = "#FF6B35") {
        val items = getItems(context).toMutableList()
        val id = System.currentTimeMillis().toString()
        items.add(SpeedDialItem(id, title, url, color, items.size))
        saveItems(context, items)
    }

    fun removeItem(context: Context, id: String) {
        val items = getItems(context).filter { it.id != id }
        saveItems(context, items)
    }

    fun updateItem(context: Context, id: String, title: String, url: String, color: String) {
        val items = getItems(context).map {
            if (it.id == id) it.copy(title = title, url = url, color = color) else it
        }
        saveItems(context, items)
    }

    private fun saveItems(context: Context, items: List<SpeedDialItem>) {
        val array = JSONArray()
        items.forEach { item ->
            array.put(JSONObject().apply {
                put("id", item.id)
                put("title", item.title)
                put("url", item.url)
                put("color", item.color)
                put("order", item.order)
            })
        }
        getPrefs(context).edit().putString(KEY_ITEMS, array.toString()).apply()
    }

    private fun getPrefs(context: Context): SharedPreferences {
        return context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    }
}
