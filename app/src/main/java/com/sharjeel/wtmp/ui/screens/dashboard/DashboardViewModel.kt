package com.sharjeel.wtmp.ui.screens.dashboard

import android.content.Context
import android.content.Intent
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sharjeel.wtmp.domain.repository.SecurityRepository
import com.sharjeel.wtmp.model.SecurityEvent
import com.sharjeel.wtmp.model.SecurityEventType
import com.sharjeel.wtmp.service.MonitoringService
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import javax.inject.Inject

enum class TimeInterval {
    LAST_24_HOURS, PAST_WEEK, PAST_MONTH, TODAY, ALL
}

enum class ReportType {
    SUCCESSFUL_UNLOCK, UNSUCCESSFUL_UNLOCK, APP_LAUNCHED
}

data class DashboardUiState(
    val isProtectionActive: Boolean = false,
    val events: List<SecurityEvent> = emptyList(),
    val todayEventsCount: Int = 0,
    val timeInterval: TimeInterval = TimeInterval.ALL,
    val reportTypes: Set<ReportType> = ReportType.entries.toSet(),
    val isLoading: Boolean = false,
    val currentDate: String = "" // Added to dynamically update date and year
)

@HiltViewModel
class DashboardViewModel @Inject constructor(
    private val repository: SecurityRepository,
    @ApplicationContext private val context: Context
) : ViewModel() {

    private val _timeInterval = MutableStateFlow(TimeInterval.ALL)
    private val _reportTypes = MutableStateFlow(ReportType.entries.toSet())

    // Generates formatted date string e.g., "SATURDAY, 18 JULY 2026"
    private fun getFormattedCurrentDate(): String {
        return SimpleDateFormat("EEEE, d MMMM yyyy", Locale.getDefault()).format(Date()).uppercase(Locale.getDefault())
    }

    val uiState: StateFlow<DashboardUiState> = combine(
        repository.isProtectionActive,
        repository.getAllEvents(),
        _timeInterval,
        _reportTypes
    ) { active, events, interval, types ->
        val todayCount = events.count { isToday(it.timestamp) }

        val filteredEvents = events.filter { event ->
            val matchesInterval = when (interval) {
                TimeInterval.LAST_24_HOURS -> isWithinHours(event.timestamp, 24)
                TimeInterval.TODAY -> isToday(event.timestamp)
                TimeInterval.PAST_WEEK -> isWithinDays(event.timestamp, 7)
                TimeInterval.PAST_MONTH -> isWithinDays(event.timestamp, 30)
                TimeInterval.ALL -> true
            }

            val matchesType = when (event.type) {
                SecurityEventType.DEVICE_UNLOCKED, SecurityEventType.UNEXPECTED_UNLOCK -> 
                    ReportType.SUCCESSFUL_UNLOCK in types
                SecurityEventType.FAILED_UNLOCK, SecurityEventType.FAILED_ATTEMPT -> 
                    ReportType.UNSUCCESSFUL_UNLOCK in types
                SecurityEventType.APP_OPENED -> 
                    ReportType.APP_LAUNCHED in types
                else -> true
            }

            matchesInterval && matchesType
        }

        DashboardUiState(
            isProtectionActive = active,
            events = filteredEvents,
            todayEventsCount = todayCount,
            timeInterval = interval,
            reportTypes = types,
            isLoading = false,
            currentDate = getFormattedCurrentDate()
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = DashboardUiState(isLoading = true, currentDate = getFormattedCurrentDate())
    )

    fun setTimeInterval(interval: TimeInterval) {
        _timeInterval.value = interval
    }

    fun toggleReportType(type: ReportType) {
        _reportTypes.update { current ->
            val mutableSet = current.toMutableSet()
            if (type in mutableSet) {
                if (mutableSet.size > 1) mutableSet.remove(type)
            } else {
                mutableSet.add(type)
            }
            mutableSet
        }
    }

    fun resetFilters() {
        _timeInterval.value = TimeInterval.ALL
        _reportTypes.value = ReportType.entries.toSet()
    }

    fun toggleProtection() {
        viewModelScope.launch {
            val newStatus = !uiState.value.isProtectionActive
            repository.setProtectionActive(newStatus)

            val intent = Intent(context, MonitoringService::class.java)
            if (newStatus) {
                context.startForegroundService(intent)
            } else {
                context.stopService(intent)
            }
        }
    }

    private fun isToday(timestamp: Long): Boolean {
        val calendar = Calendar.getInstance()
        val todayDay = calendar.get(Calendar.DAY_OF_YEAR)
        val todayYear = calendar.get(Calendar.YEAR)

        calendar.timeInMillis = timestamp
        return calendar.get(Calendar.DAY_OF_YEAR) == todayDay && calendar.get(Calendar.YEAR) == todayYear
    }

    private fun isWithinHours(timestamp: Long, hours: Int): Boolean {
        val now = System.currentTimeMillis()
        val diff = now - timestamp
        return diff >= 0 && diff <= hours.toLong() * 60 * 60 * 1000
    }

    private fun isWithinDays(timestamp: Long, days: Int): Boolean {
        val now = System.currentTimeMillis()
        val diff = now - timestamp
        return diff >= 0 && diff <= days.toLong() * 24 * 60 * 60 * 1000
    }
}