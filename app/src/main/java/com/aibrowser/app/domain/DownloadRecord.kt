package com.aibrowser.app.domain

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Represents a record of a file download initiated by the user.
 *
 * @property id Unique identifier. Auto-generated.
 * @property url Source URL of the downloaded file.
 * @property fileName Name of the downloaded file.
 * @property mimeType MIME type of the file.
 * @property contentLength Content length of the file in bytes.
 * @property timestamp Epoch timestamp in milliseconds when the download started.
 */
@Entity(tableName = "download_records")
data class DownloadRecord(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val url: String,
    val fileName: String,
    val mimeType: String?,
    val contentLength: Long,
    val timestamp: Long = System.currentTimeMillis()
)
