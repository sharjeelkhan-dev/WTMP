package com.sharjeel.wtmp.ui.screens.history

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sharjeel.wtmp.domain.repository.SecurityRepository
import com.sharjeel.wtmp.model.SecurityEvent
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import java.util.Calendar
import javax.inject.Inject

// =================================================================
// 1. ENUMS & UI STATE DATA MODELS
// =================================================================

/**
 * Categorical filters for refining security event logs chronologically.
 */
enum class HistoryFilter {
    TODAY,
    WEEK,
    MONTH,
    ALL
}

/**
 * Immutable UI State for HistoryScreen reflecting event lists, active filter,
 * real-time search query, and progress indication.
 */
data class HistoryUiState(
    val events: List<SecurityEvent> = emptyList(),
    val currentFilter: HistoryFilter = HistoryFilter.ALL,
    val searchQuery: String = "",
    val isLoading: Boolean = false
)

// =================================================================
// 2. HISTORY VIEWMODEL
// =================================================================

/**
 * HistoryViewModel processes local database event streams and applies reactive
 * filtering (by date enum and text query) using Kotlin Coroutines Flow operators.
 */
@HiltViewModel
class HistoryViewModel @Inject constructor(
    private val repository: SecurityRepository
) : ViewModel() {

    // Internal reactive state sources for filter criteria
    private val _filter = MutableStateFlow(HistoryFilter.ALL)
    private val _searchQuery = MutableStateFlow("")

    /**
     * Combined state stream merging repository Flow with active filters and search terms.
     */
    val uiState: StateFlow<HistoryUiState> = combine(
        repository.getAllEvents(),
        _filter,
        _searchQuery
    ) { events, filter, query ->
        val filteredEvents = events.filter { event ->
            // 1. Evaluate Date Criteria
            val matchesFilter = when (filter) {
                HistoryFilter.TODAY -> isToday(event.timestamp)
                HistoryFilter.WEEK -> isThisWeek(event.timestamp)
                HistoryFilter.MONTH -> isThisMonth(event.timestamp)
                HistoryFilter.ALL -> true
            }

            // 2. Evaluate Search Query Criteria
            val matchesQuery = if (query.isBlank()) {
                true
            } else {
                event.type.title.contains(query, ignoreCase = true) ||
                        event.deviceState.contains(query, ignoreCase = true)
            }

            matchesFilter && matchesQuery
        }

        HistoryUiState(
            events = filteredEvents,
            currentFilter = filter,
            searchQuery = query,
            isLoading = false
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = HistoryUiState(isLoading = true)
    )

    // =================================================================
    // 3. USER INTENT / EVENT HANDLERS
    // =================================================================

    /**
     * Updates active time filter chip option.
     */
    fun setFilter(filter: HistoryFilter) {
        _filter.value = filter
    }

    /**
     * Updates real-time search query.
     */
    fun searchEvents(query: String) {
        _searchQuery.value = query
    }

    // =================================================================
    // 4. PRIVATE HELPER DATE CALCULATORS
    // =================================================================

    /**
     * Checks if given timestamp belongs to current day.
     */
    private fun isToday(timestamp: Long): Boolean {
        val calendar = Calendar.getInstance()
        val today = calendar.get(Calendar.DAY_OF_YEAR)
        val year = calendar.get(Calendar.YEAR)

        calendar.timeInMillis = timestamp
        return calendar.get(Calendar.DAY_OF_YEAR) == today && calendar.get(Calendar.YEAR) == year
    }

    /**
     * Checks if given timestamp belongs to current calendar week.
     */
    private fun isThisWeek(timestamp: Long): Boolean {
        val calendar = Calendar.getInstance()
        val week = calendar.get(Calendar.WEEK_OF_YEAR)
        val year = calendar.get(Calendar.YEAR)

        calendar.timeInMillis = timestamp
        return calendar.get(Calendar.WEEK_OF_YEAR) == week && calendar.get(Calendar.YEAR) == year
    }

    /**
     * Checks if given timestamp belongs to current month.
     */
    private fun isThisMonth(timestamp: Long): Boolean {
        val calendar = Calendar.getInstance()
        val month = calendar.get(Calendar.MONTH)
        val year = calendar.get(Calendar.YEAR)

        calendar.timeInMillis = timestamp
        return calendar.get(Calendar.MONTH) == month && calendar.get(Calendar.YEAR) == year
    }
}