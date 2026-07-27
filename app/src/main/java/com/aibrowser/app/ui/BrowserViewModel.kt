package com.aibrowser.app.ui

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.aibrowser.app.data.BookmarkRepository
import com.aibrowser.app.data.HistoryRepository
import com.aibrowser.app.domain.Bookmark
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/**
 * [ViewModel] for the primary browsing screen ([BrowserFragment] and [BrowserActivity]).
 * Orchestrates bookmarking and recording page visits.
 */
class BrowserViewModel(
    private val bookmarkRepository: BookmarkRepository,
    private val historyRepository: HistoryRepository
) : ViewModel() {

    private val _isCurrentPageBookmarked = MutableLiveData<Boolean>(false)
    val isCurrentPageBookmarked: LiveData<Boolean> = _isCurrentPageBookmarked

    /**
     * Checks if a URL is currently bookmarked and updates [_isCurrentPageBookmarked].
     */
    fun checkIsBookmarked(url: String) {
        viewModelScope.launch {
            val bookmark = withContext(Dispatchers.IO) {
                bookmarkRepository.getByUrl(url)
            }
            _isCurrentPageBookmarked.postValue(bookmark != null)
        }
    }

    /**
     * Records a page visit in history.
     */
    fun recordVisit(url: String, title: String) {
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                historyRepository.addVisit(url, title)
            }
        }
    }

    /**
     * Toggles bookmark status for a URL.
     */
    fun toggleBookmark(title: String, url: String) {
        viewModelScope.launch {
            val existing = withContext(Dispatchers.IO) {
                bookmarkRepository.getByUrl(url)
            }
            withContext(Dispatchers.IO) {
                if (existing != null) {
                    bookmarkRepository.delete(existing)
                    _isCurrentPageBookmarked.postValue(false)
                } else {
                    bookmarkRepository.insert(Bookmark(title = title, url = url))
                    _isCurrentPageBookmarked.postValue(true)
                }
            }
        }
    }

    /**
     * Factory for creating [BrowserViewModel] instances.
     */
    class Factory(
        private val bookmarkRepository: BookmarkRepository,
        private val historyRepository: HistoryRepository
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(BrowserViewModel::class.java)) {
                return BrowserViewModel(bookmarkRepository, historyRepository) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class")
        }
    }
}
