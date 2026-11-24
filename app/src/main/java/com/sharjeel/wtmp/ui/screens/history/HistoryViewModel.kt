package com.sharjeel.wtmp.ui.screens.history

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sharjeel.wtmp.domain.repository.SecurityRepository
import com.sharjeel.wtmp.model.SecurityEvent
import com.sharjeel.wtmp.model.SecurityEventType
import com.sharjeel.wtmp.repository.UserPreferencesRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.util.Calendar
import javax.inject.Inject

enum class HistoryFilter {
    TODAY,
    WEEK,
    MONTH,
    ALL
}

data class HistoryUiState(
    val events: List<SecurityEvent> = emptyList(),
    val currentFilter: HistoryFilter = HistoryFilter.ALL,
    val searchQuery: String = "",
    val unreadActivityCount: Int = 0,
    val isLoading: Boolean = false
)

@HiltViewModel
class HistoryViewModel @Inject constructor(
    private val repository: SecurityRepository,
    private val userPreferencesRepository: UserPreferencesRepository
) : ViewModel() {

    private val _filter = MutableStateFlow(HistoryFilter.ALL)
    private val _searchQuery = MutableStateFlow("")

    val uiState: StateFlow<HistoryUiState> = combine(
        repository.getAllEvents(),
        _filter,
        _searchQuery,
        userPreferencesRepository.lastVisitedActivityTimestamp
    ) { events, filter, query, lastVisited ->
        val filteredEvents = events.filter { event ->
            val matchesFilter = when (filter) {
                HistoryFilter.TODAY -> isToday(event.timestamp)
                HistoryFilter.WEEK -> isThisWeek(event.timestamp)
                HistoryFilter.MONTH -> isThisMonth(event.timestamp)
                HistoryFilter.ALL -> true
            }

            val matchesQuery = if (query.isBlank()) {
                true
            } else {
                event.type.title.contains(query, ignoreCase = true) ||
                        event.deviceState.contains(query, ignoreCase = true)
            }

            matchesFilter && matchesQuery
        }

        val unreadCount = events.count { event ->
            (event.type == SecurityEventType.FAILED_UNLOCK || event.type == SecurityEventType.FAILED_ATTEMPT) &&
                    event.timestamp > lastVisited
        }

        HistoryUiState(
            events = filteredEvents,
            currentFilter = filter,
            searchQuery = query,
            unreadActivityCount = unreadCount,
            isLoading = false
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = HistoryUiState(isLoading = true)
    )

    fun markActivityAsVisited() {
        viewModelScope.launch {
            try {
                val events = repository.getAllEvents().first()
                val latestEventTimestamp = events.maxOfOrNull { it.timestamp } ?: System.currentTimeMillis()
                val markTimestamp = maxOf(latestEventTimestamp, System.currentTimeMillis()) + 1000L
                userPreferencesRepository.setLastVisitedActivityTimestamp(markTimestamp)
            } catch (_: Exception) {
                userPreferencesRepository.setLastVisitedActivityTimestamp(System.currentTimeMillis() + 1000L)
            }
        }
    }

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
