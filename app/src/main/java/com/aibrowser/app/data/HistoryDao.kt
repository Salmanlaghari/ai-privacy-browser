package com.aibrowser.app.data

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.aibrowser.app.domain.HistoryEntry

/**
 * Data Access Object (DAO) for managing the history table.
 */
@Dao
interface HistoryDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(entry: HistoryEntry): Long

    @Delete
    suspend fun delete(entry: HistoryEntry)

    @Query("SELECT * FROM history_entries ORDER BY visitedAt DESC")
    fun getAll(): LiveData<List<HistoryEntry>>

    @Query("SELECT * FROM history_entries ORDER BY visitedAt DESC LIMIT :limit")
    fun getRecent(limit: Int): LiveData<List<HistoryEntry>>

    @Query("SELECT * FROM history_entries WHERE url = :url LIMIT 1")
    suspend fun getByUrl(url: String): HistoryEntry?

    @Query("SELECT * FROM history_entries WHERE title LIKE :query OR url LIKE :query ORDER BY visitedAt DESC")
    fun search(query: String): LiveData<List<HistoryEntry>>

    @Query("DELETE FROM history_entries")
    suspend fun clearAll()
}
