package com.aibrowser.app.data

import androidx.lifecycle.LiveData
import com.aibrowser.app.domain.HistoryEntry

/**
 * Repository to abstract data sources for browsing History.
 */
class HistoryRepository(private val historyDao: HistoryDao) {

    val allHistory: LiveData<List<HistoryEntry>> = historyDao.getAll()

    suspend fun addVisit(url: String, title: String) {
        val existing = historyDao.getByUrl(url)
        if (existing != null) {
            val updated = existing.copy(
                title = title,
                visitedAt = System.currentTimeMillis(),
                visitCount = existing.visitCount + 1
            )
            historyDao.insert(updated)
        } else {
            val newEntry = HistoryEntry(
                title = title,
                url = url,
                visitedAt = System.currentTimeMillis(),
                visitCount = 1
            )
            historyDao.insert(newEntry)
        }
    }

    suspend fun delete(entry: HistoryEntry) {
        historyDao.delete(entry)
    }

    fun getRecent(limit: Int): LiveData<List<HistoryEntry>> {
        return historyDao.getRecent(limit)
    }

    fun search(query: String): LiveData<List<HistoryEntry>> {
        return historyDao.search("%$query%")
    }

    suspend fun clearAll() {
        historyDao.clearAll()
    }
}
