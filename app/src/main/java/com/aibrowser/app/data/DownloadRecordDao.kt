package com.aibrowser.app.data

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.aibrowser.app.domain.DownloadRecord

/**
 * Data Access Object (DAO) for managing the download records table.
 */
@Dao
interface DownloadRecordDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(record: DownloadRecord): Long

    @Delete
    suspend fun delete(record: DownloadRecord)

    @Query("SELECT * FROM download_records ORDER BY timestamp DESC")
    fun getAll(): LiveData<List<DownloadRecord>>

    @Query("DELETE FROM download_records")
    suspend fun clearAll()
}
