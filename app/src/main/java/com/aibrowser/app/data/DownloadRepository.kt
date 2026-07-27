package com.aibrowser.app.data

import androidx.lifecycle.LiveData
import com.aibrowser.app.domain.DownloadRecord

/**
 * Repository to abstract data sources for file downloads.
 */
class DownloadRepository(private val dao: DownloadRecordDao) {

    val allDownloads: LiveData<List<DownloadRecord>> = dao.getAll()

    suspend fun insert(record: DownloadRecord): Long {
        return dao.insert(record)
    }

    suspend fun delete(record: DownloadRecord) {
        dao.delete(record)
    }

    suspend fun clearAll() {
        dao.clearAll()
    }
}
