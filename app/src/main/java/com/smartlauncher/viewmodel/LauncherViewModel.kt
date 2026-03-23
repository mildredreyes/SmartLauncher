package com.smartlauncher.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.smartlauncher.data.AppCategory
import com.smartlauncher.data.AppItem
import com.smartlauncher.data.AppRepository
import kotlinx.coroutines.launch

class LauncherViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = AppRepository(application)

    private val _allApps = MutableLiveData<List<AppItem>>()

    private val _filteredApps = MutableLiveData<List<AppItem>>()
    val filteredApps: LiveData<List<AppItem>> = _filteredApps

    private val _isLoading = MutableLiveData(true)
    val isLoading: LiveData<Boolean> = _isLoading

    private val _error = MutableLiveData<String?>()
    val error: LiveData<String?> = _error

    var activeFilter: AppCategory? = null
        private set

    var searchQuery: String = ""
        private set

    init { loadApps() }

    fun loadApps() {
        _isLoading.value = true
        viewModelScope.launch {
            try {
                val apps = repository.loadRankedApps()
                _allApps.value = apps
                applyFilters()
                _isLoading.value = false
            } catch (e: Exception) {
                _error.value = "Failed to load apps: ${e.message}"
                _isLoading.value = false
            }
        }
    }

    fun setFilter(category: AppCategory?) {
        activeFilter = category
        applyFilters()
    }

    fun setSearch(query: String) {
        searchQuery = query
        applyFilters()
    }

    private fun applyFilters() {
        val all = _allApps.value ?: return
        var result = all
        if (activeFilter != null) result = result.filter { it.category == activeFilter }
        if (searchQuery.isNotBlank()) {
            val q = searchQuery.lowercase()
            result = result.filter { it.label.lowercase().contains(q) }
        }
        _filteredApps.value = result
    }
}
