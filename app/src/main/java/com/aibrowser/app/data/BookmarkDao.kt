package com.aibrowser.app.data

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.aibrowser.app.domain.Bookmark

/**
 * Data Access Object (DAO) for managing the bookmarks table.
 */
@Dao
interface BookmarkDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(bookmark: Bookmark): Long

    @Delete
    suspend fun delete(bookmark: Bookmark)

    @Query("SELECT * FROM bookmarks ORDER BY createdAt DESC")
    fun getAll(): LiveData<List<Bookmark>>

    @Query("SELECT * FROM bookmarks WHERE url = :url LIMIT 1")
    suspend fun getByUrl(url: String): Bookmark?

    @Query("SELECT * FROM bookmarks WHERE title LIKE :query OR url LIKE :query ORDER BY createdAt DESC")
    fun search(query: String): LiveData<List<Bookmark>>
}
