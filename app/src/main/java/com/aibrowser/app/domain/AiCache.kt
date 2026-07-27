package com.aibrowser.app.domain

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Room entity for caching AI response results locally to avoid excessive API usage.
 *
 * @property cacheKey The unique hash of (featureName + input_text). Used as Primary Key.
 * @property response The saved Gemini markdown text response.
 * @property timestamp Epoch timestamp in milliseconds when this entry was created.
 */
@Entity(tableName = "ai_cache")
data class AiCache(
    @PrimaryKey
    val cacheKey: String,
    val response: String,
    val timestamp: Long = System.currentTimeMillis()
)
