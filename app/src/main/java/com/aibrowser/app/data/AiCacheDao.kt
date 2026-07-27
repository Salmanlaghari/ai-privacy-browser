package com.aibrowser.app.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.aibrowser.app.domain.AiCache

/**
 * Data Access Object (DAO) for managing AI cache table entries.
 */
@Dao
interface AiCacheDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(cache: AiCache)

    @Query("SELECT * FROM ai_cache WHERE cacheKey = :key LIMIT 1")
    suspend fun getByCacheKey(key: String): AiCache?

    @Query("DELETE FROM ai_cache WHERE cacheKey = :key")
    suspend fun delete(key: String)

    @Query("DELETE FROM ai_cache WHERE :currentTimestamp - timestamp > :expiryTtl")
    suspend fun deleteExpired(currentTimestamp: Long, expiryTtl: Long)
}
