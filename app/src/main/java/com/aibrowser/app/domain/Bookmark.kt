package com.aibrowser.app.domain

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Represent a user bookmark entity in the database.
 *
 * @property id The unique identifier of the bookmark. Auto-generated.
 * @property title The user-facing title of the bookmarked page.
 * @property url The actual URL of the bookmarked page.
 * @property folder Optional category folder for grouping bookmarks.
 * @property createdAt Timestamp in milliseconds when the bookmark was created.
 * @property favicon Optional base64 or URL path to the site's favicon.
 */
@Entity(tableName = "bookmarks")
data class Bookmark(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val title: String,
    val url: String,
    val folder: String? = null,
    val createdAt: Long = System.currentTimeMillis(),
    val favicon: String? = null
)
