package com.aibrowser.app.data

import androidx.lifecycle.LiveData
import com.aibrowser.app.domain.Bookmark

/**
 * Repository to abstract data sources for Bookmarks.
 */
class BookmarkRepository(private val bookmarkDao: BookmarkDao) {

    val allBookmarks: LiveData<List<Bookmark>> = bookmarkDao.getAll()

    suspend fun insert(bookmark: Bookmark): Long {
        return bookmarkDao.insert(bookmark)
    }

    suspend fun delete(bookmark: Bookmark) {
        bookmarkDao.delete(bookmark)
    }

    suspend fun getByUrl(url: String): Bookmark? {
        return bookmarkDao.getByUrl(url)
    }

    fun search(query: String): LiveData<List<Bookmark>> {
        return bookmarkDao.search("%$query%")
    }
}
