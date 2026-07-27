package com.aibrowser.app.ui

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.asFlow
import androidx.lifecycle.switchMap
import androidx.lifecycle.viewModelScope
import com.aibrowser.app.data.BookmarkRepository
import com.aibrowser.app.domain.Bookmark
import kotlinx.coroutines.launch

/**
 * [ViewModel] for managing bookmarks list, additions, and deletions.
 */
class BookmarksViewModel(private val repository: BookmarkRepository) : ViewModel() {

    private val _searchQuery = MutableLiveData<String>("")

    val bookmarks: LiveData<List<Bookmark>> = _searchQuery.switchMap { query ->
        if (query.isNullOrEmpty()) {
            repository.allBookmarks
        } else {
            repository.search(query)
        }
    }

    fun setSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun addBookmark(title: String, url: String, folder: String? = null, favicon: String? = null) {
        viewModelScope.launch {
            repository.insert(Bookmark(title = title, url = url, folder = folder, favicon = favicon))
        }
    }

    fun deleteBookmark(bookmark: Bookmark) {
        viewModelScope.launch {
            repository.delete(bookmark)
        }
    }

    /**
     * Factory for creating [BookmarksViewModel] instances with custom parameters.
     */
    class Factory(private val repository: BookmarkRepository) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(BookmarksViewModel::class.java)) {
                return BookmarksViewModel(repository) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class")
        }
    }
}
