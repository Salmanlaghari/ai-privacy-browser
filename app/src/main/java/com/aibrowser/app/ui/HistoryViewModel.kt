package com.aibrowser.app.ui

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.switchMap
import androidx.lifecycle.viewModelScope
import com.aibrowser.app.data.HistoryRepository
import com.aibrowser.app.domain.HistoryEntry
import kotlinx.coroutines.launch

/**
 * [ViewModel] for managing user's browsing history list.
 */
class HistoryViewModel(private val repository: HistoryRepository) : ViewModel() {

    private val _searchQuery = MutableLiveData<String>("")

    val historyEntries: LiveData<List<HistoryEntry>> = _searchQuery.switchMap { query ->
        if (query.isNullOrEmpty()) {
            repository.allHistory
        } else {
            repository.search(query)
        }
    }

    fun setSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun addVisit(url: String, title: String) {
        viewModelScope.launch {
            repository.addVisit(url, title)
        }
    }

    fun deleteHistoryEntry(entry: HistoryEntry) {
        viewModelScope.launch {
            repository.delete(entry)
        }
    }

    fun clearAllHistory() {
        viewModelScope.launch {
            repository.clearAll()
        }
    }

    /**
     * Factory for creating [HistoryViewModel] instances.
     */
    class Factory(private val repository: HistoryRepository) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(HistoryViewModel::class.java)) {
                return HistoryViewModel(repository) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class")
        }
    }
}
