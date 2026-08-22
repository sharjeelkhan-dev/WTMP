package com.sharjeel.wtmp.ui.screens.history

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sharjeel.wtmp.domain.repository.SecurityRepository
import com.sharjeel.wtmp.model.SecurityEvent
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import java.util.*
import javax.inject.Inject

enum class HistoryFilter {
    TODAY, WEEK, MONTH, ALL
}

data class HistoryUiState(
    val events: List<SecurityEvent> = emptyList(),
    val currentFilter: HistoryFilter = HistoryFilter.ALL,
    val searchQuery: String = "",
    val isLoading: Boolean = false
)

@HiltViewModel
class HistoryViewModel @Inject constructor(
    private val repository: SecurityRepository
) : ViewModel() {

    private val _filter = MutableStateFlow(HistoryFilter.ALL)
    private val _searchQuery = MutableStateFlow("")

    val uiState: StateFlow<HistoryUiState> = combine(
        repository.getAllEvents(),
        _filter,
        _searchQuery
    ) { events, filter, query ->
        val filteredEvents = events.filter { event ->
            // Apply Date Filter
            val matchesFilter = when (filter) {
                HistoryFilter.TODAY -> isToday(event.timestamp)
                HistoryFilter.WEEK -> isThisWeek(event.timestamp)
                HistoryFilter.MONTH -> isThisMonth(event.timestamp)
                HistoryFilter.ALL -> true
            }

            // Apply Search Query
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

    fun setFilter(filter: HistoryFilter) {
        _filter.value = filter
    }

    fun searchEvents(query: String) {
        _searchQuery.value = query
    }

    private fun isToday(timestamp: Long): Boolean {
        val calendar = Calendar.getInstance()
        val today = calendar.get(Calendar.DAY_OF_YEAR)
        val year = calendar.get(Calendar.YEAR)

        calendar.timeInMillis = timestamp
        return calendar.get(Calendar.DAY_OF_YEAR) == today && calendar.get(Calendar.YEAR) == year
    }

    private fun isThisWeek(timestamp: Long): Boolean {
        val calendar = Calendar.getInstance()
        val week = calendar.get(Calendar.WEEK_OF_YEAR)
        val year = calendar.get(Calendar.YEAR)

        calendar.timeInMillis = timestamp
        return calendar.get(Calendar.WEEK_OF_YEAR) == week && calendar.get(Calendar.YEAR) == year
    }

    private fun isThisMonth(timestamp: Long): Boolean {
        val calendar = Calendar.getInstance()
        val month = calendar.get(Calendar.MONTH)
        val year = calendar.get(Calendar.YEAR)

        calendar.timeInMillis = timestamp
        return calendar.get(Calendar.MONTH) == month && calendar.get(Calendar.YEAR) == year
    }
}
