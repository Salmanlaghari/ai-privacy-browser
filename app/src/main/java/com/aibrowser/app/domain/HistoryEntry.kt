package com.aibrowser.app.domain

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Represents an entry in the user's browsing history.
 *
 * @property id The unique identifier of the history entry. Auto-generated.
 * @property title The title of the visited page.
 * @property url The URL of the visited page.
 * @property visitedAt Timestamp in milliseconds of the last visit.
 * @property visitCount Number of times the page was visited.
 */
@Entity(tableName = "history_entries")
data class HistoryEntry(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val title: String,
    val url: String,
    val visitedAt: Long = System.currentTimeMillis(),
    val visitCount: Int = 1
)
